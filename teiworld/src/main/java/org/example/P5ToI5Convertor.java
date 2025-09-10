package org.example;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class P5ToI5Convertor implements ConvertorInterface {

    String inputFilePath;
    String outputFilePath;

    public P5ToI5Convertor(String inPath, String outPath){
        this.inputFilePath = inPath;
        this.outputFilePath = outPath;
    }


    @Override
    public String getInputFilePath() {
        return this.inputFilePath;
    }

    @Override
    public String getOutputFilePath() {
        return this.outputFilePath;
    }


    private static File[] getP5FilesToConvert(String dirPath) {
        // filter to get all .tei_garage.xml files from inputFilePath
        FileFilter relevantFilefilter = file -> {
            return file.getName().endsWith(".tei_garage.xml");
        };
        File folder = new File(dirPath);

        return folder.listFiles(relevantFilefilter);
    }


    @Override
    public void convert() {
        // Create a TransformerFactory instance
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformerCorpHeader = null;
        Transformer transformerDocHeader = null;
        Transformer transformerText = null;
        JSONObject jsonObject = null;

        try {
            // Create transformers for XSL files
            transformerCorpHeader = factory.newTransformer(new StreamSource("teiworld/src/main/resources/idsCorpusHeader.xsl"));
            transformerDocHeader = factory.newTransformer(new StreamSource("teiworld/src/main/resources/idsDocHeader.xsl"));
            transformerText = factory.newTransformer(new StreamSource("teiworld/src/main/resources/idsText.xsl"));


        } catch (TransformerConfigurationException e) {
            System.err.println("Error creating XSL Transformers: " + e.getMessage());
        }

        try {
            // Open and Parse the JSON file and convert to JSONObject
            FileReader reader = new FileReader(this.inputFilePath + "\\metadata.json");
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(reader);
            jsonObject = (JSONObject) obj;
        } catch (FileNotFoundException e) {
            System.err.println("Error finding JSON metadata file: " + e.getMessage());
        } catch (IOException | ParseException e) {
            System.err.println("Error parsing JSON file: " + e.getMessage());
        }

        // Set transformer parameters with metadata from json file
        transformerCorpHeader.setParameter("Creator", jsonObject.get("Creator"));
        transformerCorpHeader.setParameter("Title", jsonObject.get("Title"));
        transformerCorpHeader.setParameter("Publisher", jsonObject.get("Publisher"));
        transformerCorpHeader.setParameter("PublisherYear", jsonObject.get("PublisherYear"));
        transformerCorpHeader.setParameter("ResourceType", jsonObject.get("ResourceType"));
        transformerCorpHeader.setParameter("Subject", jsonObject.get("Subject"));
        transformerCorpHeader.setParameter("Contributor", jsonObject.get("Contributor"));
        transformerCorpHeader.setParameter("Date", jsonObject.get("Date"));
        transformerCorpHeader.setParameter("Language", jsonObject.get("Language"));
        transformerCorpHeader.setParameter("LanguageCode", ConvertorUtils.getLanguageCode((String) jsonObject.get("Language")));
        transformerCorpHeader.setParameter("Size", jsonObject.get("Size"));
        transformerCorpHeader.setParameter("Rights", jsonObject.get("Rights"));
        // rights is the same for Corpus, Document and Text transformation:
        transformerDocHeader.setParameter("Rights", jsonObject.get("Rights"));
        transformerText.setParameter("Rights", jsonObject.get("Rights"));
        transformerCorpHeader.setParameter("Description", jsonObject.get("Description"));
        transformerCorpHeader.setParameter("Geolocation", jsonObject.get("Geolocation"));
        transformerCorpHeader.setParameter("FundingsReference", jsonObject.get("FundingsReference"));
        // Set parameter that is not in json file
        transformerCorpHeader.setParameter("Version", "1.0");  // default 1.0
        // indentifier for corpus and document
        String corpusSigle = jsonObject.get("CorpusSigle").toString();
        String docSigle = jsonObject.get("DocumentSigle").toString();

        if (corpusSigle.trim().length() == 0 || docSigle.trim().length() == 0) {
            System.out.println("Error: CorpusSigle and DocumentSigle must not be empty in file metadata.json. Stopping conversion.");
            System.exit(0);
        }

        // set corpus identifier as parameter
        transformerCorpHeader.setParameter("korpusSigle", corpusSigle);
        transformerDocHeader.setParameter("korpusSigle", corpusSigle);
        transformerText.setParameter("korpusSigle", corpusSigle);
        // set document identifier as parameter
        transformerDocHeader.setParameter("docSigle", docSigle);
        transformerText.setParameter("docSigle", docSigle);

        // the output file path is the location of the files already converted to p5 (with teigarage)
        File[] p5files = getP5FilesToConvert(this.outputFilePath);
        // store the transformed output in StringBuilder object
        StringBuilder outputTransformerText = new StringBuilder();
        int counter = 1; // necessary for text sigle generation

        // Transform all files with transformerText
        for (File p5file : p5files) {

            System.out.println("Done transforming file: " + p5file.getAbsoluteFile());

            Source xmlSource = new StreamSource(p5file);
            StringWriter writer = new StringWriter();
            // Set the "textSigle" parameter for transformer3
            String textSigleValue = String.format("%05d", counter);
            transformerText.setParameter("textSigle", textSigleValue);

            try {
                // execute the transformation
                transformerText.transform(xmlSource, new StreamResult(writer));
            } catch (TransformerException e) {
                System.err.println("Error executing the XSL transformation for file " + p5file.getAbsoluteFile() + " : " + e.getMessage());
            }

            outputTransformerText.append(writer);
            counter++;
        }

        // Transform first xml with transformerCorpHeader and transformerDocHeader
        File firstFile = p5files[0];
        Source mainXmlSource = new StreamSource(firstFile);
        StringWriter writer1 = new StringWriter();
        StringWriter writer2 = new StringWriter();
        try {
            transformerCorpHeader.transform(mainXmlSource, new StreamResult(writer1));
            transformerDocHeader.transform(mainXmlSource, new StreamResult(writer2));
        } catch (TransformerException e) {
            System.err.println("Error executing the XSL transformation for corpusHeader or DocHeader: " + e.getMessage());
        }

        // Concatenate the results
        String finalOutput = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE idsCorpus\n" +
                "  PUBLIC \"-//IDS//DTD IDS-I5 1.0//EN\" \"http://corpora.ids-mannheim.de/I5/DTD/i5.dtd\">\n" +
                "<idsCorpus version=\"1.0\">\n" +
                writer1 +
                "<idsDoc type=\"text\" version=\"1.0\">\n" +
                writer2 +
                outputTransformerText +
                "</idsDoc>\n" +
                "</idsCorpus>";

        // Write the concatenated output to a file
        File outputFile = new File( this.outputFilePath + "\\" + corpusSigle + ".i5.xml");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(finalOutput.getBytes());
            System.out.println("Transformation completed: " + outputFile.getAbsoluteFile());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        // delete the intermediary .tei_garage.xml files as they are no longer needed
        try {
            for (File p5file : p5files) {
                Files.delete(p5file.getAbsoluteFile().toPath());
            }
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }
}
