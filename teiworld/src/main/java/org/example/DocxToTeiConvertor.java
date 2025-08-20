package org.example;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocxToTeiConvertor implements ConvertorInterface{
    String inputFilePath;
    String outputFilePath;
    String urlForTxtConversion = "https://teigarage.tei-c.org/ege-webservice/Conversions/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/TEI%3Atext%3Axml/";
    Path tempOutputFilePath; // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp

    public DocxToTeiConvertor(String inPath, String outPath){
        this.inputFilePath = inPath;
        File f = new File(inPath);
        String newOutputFileName = f.getName().toString().replace(".docx", ".tei_garage.xml");
        this.outputFilePath = outPath + newOutputFileName;
    }

    @Override
    public String getInputFilePath() {
        return this.inputFilePath;
    }

    @Override
    public String getOutputFilePath() {
        return this.outputFilePath;
    }

    public String getUrlForTxtConversion() {
        return this.urlForTxtConversion;
    }

    @Override
    public void convert() {
        Process process = null;

        // Create temporary file for saving the non-pretty-printed XML output returned from Teigarage Webservice
        File f = new File(this.outputFilePath);
        String tmpOutputFileNameNoExtension = f.getName().toString().replace(".xml", "");

        try {
            // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp, in Linux and macOS: /tmp
            tempOutputFilePath = Files.createTempFile(tmpOutputFileNameNoExtension, ".tmp.xml");
        } catch (IOException e) {
            System.err.println("Error creating secure temporary file: " + e.getMessage());
        }

        // curl -o outputfile.tei_garage.xml -F upload=@file.docx https://teigarage.tei-c.org/ege-webservice/Conversions/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/TEI%3Atext%3Axml/
        String cmd = "curl -o " + tempOutputFilePath + " -F upload=@" + this.inputFilePath + " " + this.urlForTxtConversion;
        try {
            process = Runtime.getRuntime().exec(cmd);
            Thread.sleep(2000);
            while(process.isAlive()){
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command (" + cmd + "): " + e.getMessage());
        }

        try {
            String prettyPrintedXml = ConvertorUtils.prettyPrintXML(tempOutputFilePath);
            FileWriter fw = new FileWriter(this.outputFilePath);
            fw.write(prettyPrintedXml);
            fw.close();
        } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
            System.err.println("Error pretty printing the XML result: " + e.getMessage());
        }

        //delete the temporary file as it is no longer needed
        try {
            ConvertorUtils.deleteFile(tempOutputFilePath);
        } catch (IOException e) {
            System.err.println("Error managing temporary file: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();  // Ensure cleanup in all Java versions
            }
        }
    }
}
