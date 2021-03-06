package com.ecm.controlfile;

import com.ecm.controlfile.exception.UtilException;
import com.ecm.controlfile.model.BatchProperties;
import com.ecm.controlfile.model.OpCo;
import com.ecm.controlfile.util.Email;
import com.ecm.dto.ImportSchema;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Service
public class ControlFileGeneration {
    static final Logger logger = Logger.getLogger(ControlFileGeneration.class);
    private static Properties confighandle = new Properties();
    private static Properties loghandle = new Properties();
    private BatchProperties batchProperties;
    /**
     * map of map
     * where base map is the key of regions
     * and child map is the key with site-key
     * example: Alberta => [ 181 => 181-Calgary,
     * 257 => 257-Edmonton,
     * 823 => 823-Calgary RDC
     * ]
     */
    private Map<String, Map<String, String>> opCos = new HashMap<>();
    private String timeFolderName = null;

    public void main() throws UtilException, IOException, ClassNotFoundException, SQLException {
        //load data from config.yaml file to BatchProperties reference variable
        getConfigYml();

        //load data from OpCos list of batchProperties object to opCos map
        for (OpCo opco : batchProperties.getOpCos()) {
            String region = opco.getRegion();
            String[] siteIds = opco.getSiteIds().split(",");
            String[] siteNames = opco.getSiteNames().split(",");
            Map<String, String> mapper = new HashMap<>();
            for (int i = 0; i < siteIds.length; i++) {
                mapper.put(siteIds[i].trim(), siteNames[i].trim());
            }
            opCos.put(region.trim(), mapper);
        }

        try {
            confighandle.load(new FileInputStream("config.properties"));
            loghandle.load(new FileInputStream("log4j.properties"));
            PropertyConfigurator.configure(confighandle);
            PropertyConfigurator.configure(loghandle);
        } catch (IOException e) {
            throw new UtilException(
                    "Property file not found");
        }

        File[] localFiles = getLocalFiles();
        if (localFiles != null) {
            if (localFiles.length > 0) {
                //processImportFiles(localFiles);
                processImportFilesIC(localFiles, batchProperties, opCos);
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
        ImportSchema imp = new ImportSchema();
        ImportSchema.Node node = new ImportSchema.Node();
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
                        node = new ImportSchema.Node();
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
                        ImportSchema.Node.Category category = new ImportSchema.Node.Category();
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
                            throw new UtilException(file);
                        } catch (UtilException adobeError) {
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

    public void processImportFilesIC(File[] files, BatchProperties batchProperties, Map<String, Map<String, String>> opCos) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
        logger.debug("Start of processImportFiles Method");
        Map<String, String> opCo = null;
        String[] fileNameExtract = null;
        String[] cimplSt = null;
        String cimplInitial = null;
        String fileName = null;
        String file = null;
        String extn = null;
        String jobStatus = null;
        boolean jobFailed = false;
        int successCounter = 0;
        int failCounter = 0;
        String region = null;
        ImportSchema imp = new ImportSchema();
        ImportSchema.Node node = new ImportSchema.Node();
        try {
            for (int i = 0; i < files.length; i++) {
                boolean isOkay = false;
                if (files[i].isFile())
                    if (files[i].getName().length() > 0L) {
                        fileName = files[i].getName();
                        System.out.println("fileName " + fileName);

                        HashMap<String, String> fileNameSegements = new HashMap<String, String>();
                        fileNameExtract = fileName.trim().split("\\.");
                        System.out.println("File Name Extract: " + fileNameExtract);
                        if (fileNameExtract.length > 0) {
                            file = fileNameExtract[0];
                            logger.debug("File : " + file);
                            if (fileNameExtract.length >= 2)
                                extn = fileNameExtract[fileNameExtract.length - 1];

                            fileNameSegements.put("EXT", extn);
                            cimplSt = file.split("_");
                            cimplInitial = cimplSt[0];

                            int count = 0;

                            /**
                             * FISCAL Year logic start
                             */
                            String ts;
                            int year = 0;

                            if (cimplSt[cimplSt.length - 1].contains(batchProperties.getPart())) {
                                ts = cimplSt[cimplSt.length - 2];
                            } else {
                                ts = cimplSt[cimplSt.length - 1];
                            }

                            if (isNumeric(ts)) {
                                char[] dateArr = new char[ts.length()];
                                for (int j = 0; j < ts.length(); j++) {
                                    dateArr[j] = ts.charAt(j);
                                }
                                String month = new String(dateArr, 4, 2);
                                String yearStr = new String(dateArr, 0, 4);
                                if (Integer.parseInt(month) > 6) {
                                    year = Integer.parseInt(yearStr) + 1;
                                } else {
                                    year = Integer.parseInt(yearStr);
                                }
                            }
                            /**
                             * FISCAL Year logic end here
                             */

                            if (year != 0) {
                                if (cimplInitial.equalsIgnoreCase("CA")) {
                                    StringBuffer path = new StringBuffer();
                                    if (cimplSt[1].equals(batchProperties.getBase())) {
                                        if (batchProperties.getConsolidated().contains(cimplSt[2])) {
                                            path.append(String.valueOf(getPropertyValue("Enterprise")) +
                                                    ':' +
                                                    getPropertyValue("CanadaInterCompany") + ":" + batchProperties.getConsolidated());
                                            isOkay = true;
                                        } else {
                                            List<String> regions = Arrays.asList(batchProperties.getRegion().split(","));
                                            opCo = null;
                                            for (String key : opCos.keySet()) {
                                                for (String siteId : opCos.get(key).keySet()) {
                                                    if (siteId.trim().equalsIgnoreCase(cimplSt[2])) {
                                                        region = key;
                                                        opCo = opCos.get(key);
                                                        path.append(String.valueOf(getPropertyValue("Enterprise")) +
                                                                ':' +
                                                                getPropertyValue("CanadaInterCompany") + ":" + batchProperties.getRegion() + ":" + region + ":" + opCo.get(cimplSt[2]));
                                                        isOkay = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (opCo == null)
                                                isOkay = false;
                                        }
                                    } else if (batchProperties.getCorporate().contains(cimplSt[1])) {
                                        path.append(String.valueOf(getPropertyValue("Enterprise")) +
                                                ':' +
                                                getPropertyValue("CanadaInterCompany") + ":" + batchProperties.getCorporate());
                                        isOkay = true;
                                    } else if (batchProperties.getBalance().contains(cimplSt[1]) || cimplSt[1].contains("Ending")) {
                                        path.append(String.valueOf(getPropertyValue("Enterprise")) +
                                                ':' +
                                                getPropertyValue("CanadaInterCompany") + ":" + batchProperties.getBalance());
                                        isOkay = true;
                                    }

                                    path.append(":" + Integer.toString(year));
                                    fileNameSegements.put("otcsLocation", path.toString());
                                }
                                if (isOkay) {
                                    node = new ImportSchema.Node();
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
                                    isOkay = false;
                                }
                            } else {
                                isOkay = false;
                            }
                        } else {
                            isOkay = false;
                        }
                    } else {
                        isOkay = false;
                    }
                if (!isOkay) {
                    moveFileIntoDestinationFolder(
                            files[i],
                            String.valueOf(getPropertyValue("FAILEDFOLDER")) + "/");
                    logger.debug("File name corrupt");
                    Email.SendEmail();
                    try {
                        throw new UtilException(file);
                    } catch (UtilException adobeError) {
                        logger.error(adobeError.getMessage());
                        failCounter++;
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

    private void convertToXML(ImportSchema xmlInput) {
        logger.debug("Start of convertToXML Method");
        if (timeFolderName == null)
            timeFolderName = getCurrentTimeStamp();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{ImportSchema.class});
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

    private ImportSchema.Node.Category.Attribute createAttribute(String columnName, String value) {
        logger.debug("Start of createAttribute Method");
        ImportSchema.Node.Category.Attribute attribute = new ImportSchema.Node.Category.Attribute();
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

    public void getConfigYml() {
        // Loading the YAML file from the /resources folder
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // Mapping the OpCos from the YAML file to the OpCos class
        batchProperties = new BatchProperties();
        try {
            FileInputStream fis = new FileInputStream("config.yaml");
            batchProperties = om.readValue(fis, BatchProperties.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(batchProperties.toString());
    }
}
