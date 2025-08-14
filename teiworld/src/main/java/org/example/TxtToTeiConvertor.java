package org.example;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TxtToTeiConvertor implements ConvertorInterface {

    String inputFilePath;
    String outputFilePath;
    String urlForTxtConversion = "https://teigarage.tei-c.org/ege-webservice/Conversions/txt%3Atext%3Aplain/odt%3Aapplication%3Avnd.oasis.opendocument.text/TEI%3Atext%3Axml";
    Path tempOutputFilePath; // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp

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
            // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp
            tempOutputFilePath = Files.createTempFile(tmpOutputFileNameNoExtension, ".tmp.xml");
        } catch (IOException e) {
            System.err.println("Error creating secure temporary file: " + e.getMessage());
        }

        // curl -o outputfile.tei_garage.xml -F upload=@file.txt https://teigarage.tei-c.org/ege-webservice/Conversions/txt%3Atext%3Aplain/odt%3Aapplication%3Avnd.oasis.opendocument.text/TEI%3Atext%3Axml
        String cmd = "curl -o " + tempOutputFilePath + " -F upload=@" + this.inputFilePath + " " + this.urlForTxtConversion;

        try {
            process = Runtime.getRuntime().exec(cmd);
            Thread.sleep(2000);
            while(process.isAlive()){
                Thread.sleep(100);
            }
            prettyPrintXML(tempOutputFilePath);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();  // Ensure cleanup in all Java versions
            }
        }
    }


    /**
     * Pretty print the XML file after its conversion to TEI (TEIgarage prints output in a single line)
     * @param tempXmlOutputFilePath the Path of the temporary file to be pretty printed
     */
    private void prettyPrintXML(Path tempXmlOutputFilePath) {
        System.out.println("Going to pretty print this: " + tempXmlOutputFilePath);

        try {
            File tempXmlOutputFile = new File(tempXmlOutputFilePath.toString());
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempXmlOutputFile);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            Writer out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));

            System.out.println("pretty printed string: ");
            System.out.println(out.toString());

            FileWriter fw = new FileWriter(this.outputFilePath);
            fw.write(out.toString());
            fw.close();
        } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
            System.err.println("Error: " + e.getMessage());
        }

        try {
            Files.delete(tempXmlOutputFilePath);
            System.out.println("Temporary file deleted.");
        } catch (IOException e) {
            System.err.println("Error managing temporary file: " + e.getMessage());
        }
    }
}
