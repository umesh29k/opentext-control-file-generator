package com.ecm.batchprocess;

import com.ecm.batchprocess.exception.CimplErrorMessage;
import com.ecm.batchprocess.model.BatchProperties;
import com.ecm.batchprocess.model.OpCo;
import com.ecm.batchprocess.util.Email;
import com.ecm.xmlgen.Import;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Service
public class ControlFileGeneration {
    static final Logger logger = Logger.getLogger(ControlFileGeneration.class);
    private static Properties confighandle = new Properties();
    private static Properties loghandle = new Properties();
    private Map<String, Map<String, String>> opCos = new HashMap<>();

    public void main() throws CimplErrorMessage, IOException, ClassNotFoundException, SQLException {
        ControlFileGeneration obj = new ControlFileGeneration();
        List<OpCo> opCosList = getConfigYml();
        for (OpCo opco : opCosList) {
            String region = opco.getRegion();
            String[] siteIds = opco.getSiteIds().split(",");
            String[] siteNames = opco.getSiteNames().split(",");
            Map<String, String> mapper = new HashMap<>();
            for (int i = 0; i < siteIds.length; i++) {
                mapper.put(siteIds[i], siteNames[i]);
            }
            opCos.put(region, mapper);
        }
        try {
            confighandle.load(new FileInputStream("config.properties"));
            loghandle.load(new FileInputStream("log4j.properties"));
            PropertyConfigurator.configure(confighandle);
            PropertyConfigurator.configure(loghandle);
        } catch (IOException e) {
            throw new CimplErrorMessage(
                    "Property file not found");
        }
        File[] localFiles = obj.getLocalFiles();
        if (localFiles != null) {
            if (localFiles.length > 0) {
                //obj.processImportFiles(localFiles);
                obj.processImportFilesIC(localFiles);
            } else {
                logger.debug("No files in the input folder");
            }
        } else {
            logger.debug("No files in the input folder");
        }
    }

    private File[] getLocalFiles() {
        File folder = new File(getPropertyValue("Input"));
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null)
            logger.debug("Number of files: " + listOfFiles.length);
        return listOfFiles;
    }

    public void processImportFiles(File[] files) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
        logger.debug("Start of processImportFiles Method");
        StringTokenizer fileNameExtract = null;
        StringTokenizer st = null;
        StringTokenizer cimplSt = null;
        String cimplInitial = null;
        String cimplSt2 = null;
        String fileName = null;
        String VendorName = null;
        String AccountNumber = null;
        String InvoiceNumber = null;
        String InvoiceDate = null;
        String file = null;
        String extn = null;
        String jobStatus = null;
        boolean jobFailed = false;
        int successCounter = 0;
        int failCounter = 0;
        Import imp = new Import();
        Import.Node node = new Import.Node();
        try {
            for (int i = 0; i < files.length; i++) {
                VendorName = "VendorName";
                AccountNumber = "AccountNumber";
                InvoiceNumber = "InvoiceNumber";
                InvoiceDate = "InvoiceDate";
                if (files[i].isFile() && files[i].length() > 0L) {
                    fileName = files[i].getName();
                    logger.debug("fileName " + fileName);
                    HashMap<String, String> fileNameSegements = new HashMap<String, String>();
                    fileNameExtract = new StringTokenizer(fileName, ".");
                    file = fileNameExtract.nextToken();
                    extn = fileNameExtract.nextToken();
                    fileNameSegements.put("EXT", extn);
                    cimplSt = new StringTokenizer(file, "_");
                    cimplInitial = cimplSt.nextToken();
                    cimplSt2 = cimplSt.nextToken();
                    st = new StringTokenizer(cimplSt2, "-");
                    int count = 0;
                    if (st.countTokens() == 4) {
                        while (st.hasMoreTokens()) {
                            count++;
                            switch (count) {
                                case 1:
                                    fileNameSegements.put(
                                            "VendorName",
                                            st.nextToken());
                                    VendorName = fileNameSegements.get("VendorName");
                                case 2:
                                    fileNameSegements.put(
                                            "AccountNumber",
                                            st.nextToken());
                                    AccountNumber = fileNameSegements.get("AccountNumber");
                                case 3:
                                    fileNameSegements.put(
                                            "InvoiceNumber",
                                            st.nextToken());
                                    InvoiceNumber = fileNameSegements.get("InvoiceNumber");
                                case 4:
                                    fileNameSegements.put(
                                            "InvoiceDate",
                                            st.nextToken());
                                    InvoiceDate = fileNameSegements.get("InvoiceDate");
                            }
                        }
                        node = new Import.Node();
                        node.setType(getPropertyValue("TYPE"));
                        node.setAction(getPropertyValue("ACTION"));
                        node.setFile(String.valueOf(getPropertyValue("FilePath")) +
                                fileName);
                        node.setLocation(
                                String.valueOf(getPropertyValue("Enterprise")) +
                                        ':' +
                                        getPropertyValue("SyscoBusinessServices") +
                                        ':' +
                                        getPropertyValue("Telecommunications") +
                                        ':' +
                                        getPropertyValue("CIMPL") +
                                        ':' +
                                        (String) fileNameSegements
                                                .get("VendorName"));
                        Import.Node.Category category = new Import.Node.Category();
                        category.setName(getPropertyValue("Category"));
                        category.getAttribute()
                                .add(createAttribute(
                                        getPropertyValue("VendorName"),
                                        fileNameSegements
                                                .get("VendorName")));
                        category.getAttribute()
                                .add(createAttribute(
                                        getPropertyValue("AccountNumber"),
                                        fileNameSegements
                                                .get("AccountNumber")));
                        category.getAttribute()
                                .add(createAttribute(
                                        getPropertyValue("InvoiceNumber"),
                                        fileNameSegements
                                                .get("InvoiceNumber")));
                        category.getAttribute()
                                .add(createAttribute(
                                        getPropertyValue("InvoiceDate"),
                                        fileNameSegements
                                                .get("InvoiceDate")));
                        node.setCategory(category);
                    }
                    if (VendorName != null && AccountNumber != null && InvoiceNumber != null && InvoiceDate != null) {
                        moveFileIntoDestinationFolder(
                                files[i],
                                String.valueOf(getPropertyValue("ARCHIVEFOLDER")) + "/");
                        successCounter++;
                        imp.getNode().add(node);
                        if (successCounter > 0) {
                            convertToXML(imp);
                        } else {
                            logger.debug("***************No files in Input Folder**************");
                        }
                    } else {
                        moveFileIntoDestinationFolder(
                                files[i],
                                String.valueOf(getPropertyValue("FAILEDFOLDER")) + "/");
                        logger.debug("File name corrupt");
                        Email.SendEmail();
                        try {
                            throw new CimplErrorMessage(file);
                        } catch (CimplErrorMessage adobeError) {
                            logger.error(adobeError.getMessage());
                            failCounter++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            jobFailed = true;
            logger.error(e);
        } finally {
            if (jobFailed) {
                jobStatus = "Incomplete";
            } else {
                jobStatus = "Complete";
            }
        }
    }

    public void processImportFilesIC(File[] files) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
        logger.debug("Start of processImportFiles Method");
        Map<String, String> opCo = null;
        String[] fileNameExtract = null;
        String[] cimplSt = null;
        String cimplInitial = null;
        String cimplSt2 = null;
        String fileName = null;
        String file = null;
        String extn = null;
        String jobStatus = null;
        boolean jobFailed = false;
        int successCounter = 0;
        int failCounter = 0;
        String region = null;
        Import imp = new Import();
        Import.Node node = new Import.Node();
        try {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile())
                    if (files[i].length() > 0L) {
                        fileName = files[i].getName();
                        logger.debug("fileName " + fileName);

                        HashMap<String, String> fileNameSegements = new HashMap<String, String>();
                        fileNameExtract = fileName.split(".");
                        file = fileNameExtract[0];

                        if (fileNameExtract.length >= 2)
                            extn = fileNameExtract[fileNameExtract.length - 1];

                        fileNameSegements.put("EXT", extn);
                        cimplSt = file.split("_");
                        cimplInitial = cimplSt[0];

                        int count = 0;
                        Date docDate = null;
                        String ts;
                        int year = 0;
                        if (cimplSt[cimplSt.length - 1].equalsIgnoreCase(getPropertyValue("part"))) {
                            ts = cimplSt[cimplSt.length - 2];
                        } else {
                            ts = cimplSt[cimplSt.length - 1];
                        }

                        if (isNumeric(ts)) {
                            Timestamp timestamp = new Timestamp(Long.parseLong(ts));
                            docDate = new Date(timestamp.getTime());
                        }

                        LocalDate localDate = docDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                        if (localDate.getMonthValue() > 6)
                            year = localDate.getYear() + 1;

                        if (year != 0) {
                            if (cimplInitial.equalsIgnoreCase("CA")) {
                                String path = String.valueOf(getPropertyValue("Enterprise")) +
                                        ':' +
                                        getPropertyValue("CanadaInterCompany") +
                                        ':' + region;
                                if (cimplSt[1].equals(getPropertyValue("base"))) {
                                    if (cimplSt[2].equals("consolidated")) {
                                        path += ":" + getPropertyValue("consolidated");
                                    } else {
                                        List<String> regions = Arrays.asList(getPropertyValue("Region").split(","));
                                        for (String key : opCos.keySet()) {
                                            if (opCos.get(key).keySet().contains(2)) {
                                                region = key;
                                                opCo = opCos.get(key);
                                            }
                                        }
                                        if (opCo != null)
                                            path += ":" + getPropertyValue("region") + ":" + opCo.get(cimplSt[2]);
                                        else {
                                            region = "";
                                            path += ":" + getPropertyValue(cimplSt[2]);
                                        }
                                    }
                                } else if (cimplSt[1].equals(getPropertyValue("corporate"))) {
                                    path += ":" + getPropertyValue("corporate");
                                } else if (cimplSt[1].equals(getPropertyValue("balance"))) {
                                    path += ":" + getPropertyValue("balance");
                                }

                                path += ":" + Integer.toString(year);

                                //String filter = cimplSt[2];
                                //path += ":" + getPropertyValue(region).substring(getPropertyValue(region).indexOf(filter)).split(",")[0];

                                fileNameSegements.put("otcsLocation", path);
                            }
                            if (region != null) {
                                node = new Import.Node();
                                node.setType(getPropertyValue("TYPE"));
                                node.setAction(getPropertyValue("ACTION"));
                                node.setFile(String.valueOf(getPropertyValue("FilePath")) + fileName);
                                node.setLocation(fileNameSegements.get("otcsLocation"));
                                moveFileIntoDestinationFolder(
                                        files[i],
                                        String.valueOf(getPropertyValue("ARCHIVEFOLDER")) + "/");
                                successCounter++;
                                imp.getNode().add(node);
                                if (successCounter > 0) {
                                    convertToXML(imp);
                                } else {
                                    logger.debug("***************No files in Input Folder**************");
                                }
                            } else {
                                moveFileIntoDestinationFolder(
                                        files[i],
                                        String.valueOf(getPropertyValue("FAILEDFOLDER")) + "/");
                                logger.debug("File name corrupt");
                                Email.SendEmail();
                                try {
                                    throw new CimplErrorMessage(file);
                                } catch (CimplErrorMessage adobeError) {
                                    logger.error(adobeError.getMessage());
                                    failCounter++;
                                }
                            }
                        }
                    }
            }
        } catch (
                Exception e) {
            jobFailed = true;
            logger.error(e);
        } finally {
            if (jobFailed) {
                jobStatus = "Incomplete";
            } else {
                jobStatus = "Complete";
            }
        }
    }

    private void moveFileIntoDestinationFolder(File file, String folderPath) {
        logger.debug("Start of moveFileIntoDestinationFolder Method");
        try {
            file.renameTo(new File(String.valueOf(folderPath) + "/" + file.getName()));
        } catch (Exception e) {
            logger.error(e);
        }
        logger.debug("End of moveFileIntoDestinationFolder Method");
    }

    private void convertToXML(Import xmlInput) {
        logger.debug("Start of convertToXML Method");
        String timeFolderName = getCurrentTimeStamp();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{Import.class});
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
            jaxbMarshaller.marshal(xmlInput, System.out);
            File xmlFile = new File(
                    String.valueOf(getPropertyValue("XMLFILESERVER")) + getPropertyValue("XMLNAME") + timeFolderName + getPropertyValue("XMLFILEEXTN"));
            jaxbMarshaller.marshal(xmlInput, xmlFile);
            logger.debug("XML generated successfully ");
        } catch (JAXBException e) {
            logger.error(e);
        }
        logger.debug("End of convertToXML Method");
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private Import.Node.Category.Attribute createAttribute(String columnName, String value) {
        logger.debug("Start of createAttribute Method");
        Import.Node.Category.Attribute attribute = new Import.Node.Category.Attribute();
        attribute.setName(columnName);
        attribute.setValue(value);
        logger.debug("End of createAttribute Method");
        return attribute;
    }

    public String getPropertyValue(String key) {
        String value = confighandle.getProperty(key);
        if (value != null) {
            return value;
        }
        return null;
    }

    public List<OpCo> getConfigYml() {
        // Loading the YAML file from the /resources folder
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // Mapping the OpCos from the YAML file to the OpCos class
        BatchProperties batchProperties = new BatchProperties();
        try {
            FileInputStream fis = new FileInputStream("config.yaml");
            batchProperties = om.readValue(fis, BatchProperties.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(batchProperties.toString());
        return batchProperties.getOpCos();
    }
}
