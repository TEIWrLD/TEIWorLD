package de.ids;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class P5ToI5HierarchicalConvertor implements ConvertorInterface {

    String inputFilePath;
    String outputFilePath;
    File [] fileList;

    public P5ToI5HierarchicalConvertor(String inPath, String outPath, File [] list){
        this.inputFilePath = inPath;
        this.outputFilePath = outPath;
        this.fileList = list;
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

    /**
     * Creates a Linked Map of DokumentSigles that each hold a list of Files running under the respective dokumentSigle.
     * e.g. { Directory01=[path\file01.tei_garage.xml, path\file02.tei_garage.xml],
     *        Directory02=[path\fileXY.tei_garage.xml],
     *        Directory03=[path\fileA.tei_garage.xml, path\fileB.tei_garage.xml, path\fileC.tei_garage.xml] }
     * @param originalFilesFromInputDir the List of the original, not yet converted files as they are in the input directory
     *                                  (part of the file path of the input directory is necessesary extract the document sigle name)
     * @param p5fileList the List of the already to P5 converted files necessary to back check which original files have actually been converted
     * @return a Map containing the document sigles (that correspond to the directory names) as value and a list of files beloning to the document as a key
     */
    private static LinkedHashMap<String, List<File>> getHierarchicicalMap(File [] originalFilesFromInputDir, File [] p5fileList){
        LinkedHashMap<String, List<File>> data = new LinkedHashMap<String, List<File>>();

        for (File original : originalFilesFromInputDir) {
            String docName = original.getParentFile().getName();
            String fileName = original.getName().substring(0, original.getName().lastIndexOf('.'));

            File p5fileCurrent = null;
            for (File p5f : p5fileList){
                if (p5f.getName().contains(fileName)){
                    p5fileCurrent = p5f;
                    break;
                }
            }

            // add DocName and/or File to map
            if (p5fileCurrent != null && data.containsKey(docName)){
                // add File to existing DocName key in map
                data.get(docName).add(p5fileCurrent);
            } else if (p5fileCurrent != null) {
                // first add DocName as key in map and then the File as value
                List<File> p5files = new ArrayList<>(Arrays.asList(p5fileCurrent));
                data.put(docName, p5files);
            }
        }
        return data;
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
            FileReader reader = new FileReader(this.inputFilePath + File.separator + "metadata.json");
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

        if (corpusSigle.trim().length() == 0) {
            System.out.println("Error: CorpusSigle must not be empty in file metadata.json. Stopping conversion.");
            System.exit(0);
        }

        // the output file path is the location of the files already converted to p5 (with teigarage)
        File[] p5files = getP5FilesToConvert(this.outputFilePath);
        Map<String, List<File>> docFileMap = getHierarchicicalMap(fileList, p5files);

        // set corpus identifier as parameter
        transformerCorpHeader.setParameter("korpusSigle", corpusSigle);
        transformerDocHeader.setParameter("korpusSigle", corpusSigle);
        transformerText.setParameter("korpusSigle", corpusSigle);

        StringWriter writerCorpHeader = new StringWriter();
        File firstFile = null;
        String outputAllDocs = "";

        for (String doc : docFileMap.keySet()){
            StringWriter writerDocHeader = new StringWriter();

            transformerDocHeader.setParameter("docSigle", doc);
            transformerText.setParameter("docSigle", doc);

            int counter = 1; // necessary for text sigle generation

            // create StringBuilder object to store the transformed output
            StringBuilder outputTransformerText = new StringBuilder();

            // Transform all files with transformerText
            for (File p5file : docFileMap.get(doc)) {
                System.out.println("Done transforming file: " + p5file.getAbsoluteFile());
                firstFile = docFileMap.get(doc).getFirst();

                Source xmlSource = new StreamSource(p5file);
                StringWriter writer = new StringWriter();
                // Set the "textSigle" parameter for transformerText
                String textSigleValue = String.format("%05d", counter);
                transformerText.setParameter("textSigle", textSigleValue);
                transformerText.setParameter("textTitle", StringUtils.remove(p5file.getName(), ".tei_garage.xml"));

                try {
                    // execute the transformation
                    transformerText.transform(xmlSource, new StreamResult(writer));
                } catch (TransformerException e) {
                    System.err.println("Error executing the XSL transformation for file " + p5file.getAbsoluteFile() + " : " + e.getMessage());
                }

                outputTransformerText.append(writer);
                counter++;
            }

            // Transform first xml with transformerDocHeader
            Source mainXmlSource = new StreamSource(firstFile);

            try {
                transformerDocHeader.transform(mainXmlSource, new StreamResult(writerDocHeader));
            } catch (TransformerException e) {
                System.err.println("Error executing the XSL transformation for DocHeader: " + e.getMessage());
            }

            String outputCurrentDoc = "<idsDoc type=\"text\" version=\"1.0\">\n" +
                    writerDocHeader +
                    outputTransformerText +
                    "</idsDoc>\n";

            outputAllDocs += outputCurrentDoc;
        }

        // Transform first xml with transformerCorpHeader
        Source mainXmlSource = new StreamSource(firstFile);

        try {
            transformerCorpHeader.transform(mainXmlSource, new StreamResult(writerCorpHeader));
        } catch (TransformerException e) {
            System.err.println("Error executing the XSL transformation for corpusHeader: " + e.getMessage());
        }

        // Concatenate the results
        String finalOutput = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE idsCorpus\n" +
                "  PUBLIC \"-//IDS//DTD IDS-I5 1.0//EN\" \"http://corpora.ids-mannheim.de/I5/DTD/i5.dtd\">\n" +
                "<idsCorpus version=\"1.0\">\n" +
                writerCorpHeader +
                outputAllDocs +
                "</idsCorpus>";

        // Write the concatenated output to a file
        File outputFile = new File( this.outputFilePath + File.separator + corpusSigle + ".i5.xml");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(finalOutput.getBytes());
            System.out.println("... Transformation completed: " + outputFile.getAbsoluteFile());
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
