package de.ids;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Path;
import java.util.Locale;

public class ConvertorUtils {

    /**
     * Pretty print the XML file after its conversion to TEI (TEIgarage prints output in a single line)
     * @param tempXmlOutputFilePath the Path of the temporary file to be pretty printed
     * @return the pretty printed and well indented XML String
     */
    public static String prettyPrintXML(Path tempXmlOutputFilePath) throws IOException, ParserConfigurationException, TransformerException, SAXException {

        File tempXmlOutputFile = new File(tempXmlOutputFilePath.toString());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document document = documentBuilderFactory.newDocumentBuilder().parse(tempXmlOutputFile);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        Writer prettyPrinted = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(prettyPrinted));

        return prettyPrinted.toString();
    }


    /**
     * Generates the ISO 639-1 language code for a given language name written in English or German.
     * @param languageName the name of the language to look up, in either English or German
     * @return the ISO 639-1 language code of the matching locale, or "de" if no match is found
     */
    public static String getLanguageCode(String languageName) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayLanguage(Locale.ENGLISH).equalsIgnoreCase(languageName) ||
                    locale.getDisplayLanguage(Locale.GERMAN).equalsIgnoreCase(languageName)) {
                return locale.getLanguage();
            }
        }
        return "de"; // Default German
    }
}
