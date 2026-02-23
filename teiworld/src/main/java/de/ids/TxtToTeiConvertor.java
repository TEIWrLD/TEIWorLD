package de.ids;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TxtToTeiConvertor implements ConvertorInterface {

    String inputFilePath;
    String outputFilePath;
    String urlForTxtConversion = "https://teigarage.tei-c.org/ege-webservice/Conversions/txt%3Atext%3Aplain/odt%3Aapplication%3Avnd.oasis.opendocument.text/TEI%3Atext%3Axml";
    Path tempOutputFilePath; // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp, in Linux and macOS: /tmp

    public TxtToTeiConvertor(String inPath, String outPath) {
        this.inputFilePath = inPath;
        File f = new File(inPath);
        String newOutputFileName = f.getName().toString().replace(".txt", ".tei_garage.xml");
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

        // curl -o outputfile.tei_garage.xml -F upload=@file.txt https://teigarage.tei-c.org/ege-webservice/Conversions/txt%3Atext%3Aplain/odt%3Aapplication%3Avnd.oasis.opendocument.text/TEI%3Atext%3Axml
        ProcessBuilder pb = new ProcessBuilder(
                "curl",
                "-o", tempOutputFilePath.toString(),
                "-F", "upload=@" + this.inputFilePath,
                this.urlForTxtConversion
        );

        try {
            process = pb.start();
            int exitCode = process.waitFor(); // Wait until curl completely finishes writing the file
            //System.out.println("curl finished with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command calling TEIGarage: " + e.getMessage());
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
            Files.delete(tempOutputFilePath);
        } catch (IOException e) {
            System.err.println("Error managing temporary file: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();  // Ensure cleanup in all Java versions
            }
        }
    }
}
