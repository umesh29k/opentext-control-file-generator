# opentext-control-file-generator
This utility is used to generate a control file, which is a XML file to import documents defined as an document descriptor.

# required folder locations:
* input: a folder, where all the documents needs to be placed before initiating control-file-generator utility
* archive: this folder is used to move all the successfully processed file using this utility 
* ctl: all the control file [ ex. control-file-<timestamp>.xml ]
* failedFiles: this folder is used to move all the unsuccessful files using this utility 
* logs: this folder hold all the log from the utility. CA_IC_ControlFileGeneration.log file is created into this folder

