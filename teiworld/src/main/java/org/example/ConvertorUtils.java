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

public class ConvertorUtils {

    /**
     * Pretty print the XML file after its conversion to TEI (TEIgarage prints output in a single line)
     * @param tempXmlOutputFilePath the Path of the temporary file to be pretty printed
     */
    public static String prettyPrintXML(Path tempXmlOutputFilePath) throws IOException, ParserConfigurationException, TransformerException, SAXException {

        System.out.println("Going to pretty print this: " + tempXmlOutputFilePath);

        File tempXmlOutputFile = new File(tempXmlOutputFilePath.toString());
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempXmlOutputFile);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        Writer prettyPrinted = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(prettyPrinted));

        System.out.println("pretty printed string: ");
        System.out.println(prettyPrinted);

        return prettyPrinted.toString();
    }


    public static void deleteFile(Path file) throws IOException {
        Files.delete(file);
        System.out.println("Temporary file deleted.");

    }
}
