package org.example;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

public class TeiWorld {

    // Class variables holding accepted file format endings to check for the right file format of the files to be processed
    public static final String[] SPOKENFORMATS = {"eaf", "cha", "trs", "TextGrid", "qdpx"};
    public static final String[] WRITTENFORMATS = {"docx", "txt"};

    // Class variables holding accepted file format and their corresponding mime type - not possible to use for
    // checking input files as java will return null with most of the file formats (trs, cha...)
    private static final Map<String, String> FORMATMIMETYPES;
    static { //static initializer: static blocks can only use static variables and ALWAYS EXECUTE FIRST before the main() method (without having to create an object instance)
             // (see: https://www.baeldung.com/java-static-instance-initializer-blocks)
        Map<String, String> tmpMap = new HashMap<String, String>();
        tmpMap.put("eaf", "text/xml");
        tmpMap.put("cha", "text/plain");
        tmpMap.put("trs", "text/xml");
        tmpMap.put("TextGrid", "application/octet-stream");
        tmpMap.put("qdpx", "application/zip");
        tmpMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        tmpMap.put("txt", "text/plain");
        FORMATMIMETYPES = Collections.unmodifiableMap(tmpMap);
    }

    private String mode;
    private String inputDir;
    private String outputDir;

    // Constructor
    public TeiWorld(String mode, String in, String out) {
        this.mode = mode;
        this.inputDir = in;
        this.outputDir = out;
    }

    public String getMode(){
        return mode;
    }

    public String getInputDir(){
        return inputDir;
    }

    public String getOutputDir(){
        return outputDir;
    }

    /**
     * Checks if the command line arguments are complete and correct.
     * The first argument hast to be either "spoken" or "written",
     * the second and third arguments are the input and output directory (check for existence)
     *
     * @param CliArguments String array with the command line arguments
     * @return true if the arguments are all correct, existing, and in the right order, false otherwise
     */
    public static boolean checkArgs (String[] CliArguments) {
        boolean mode = false;
        boolean inputExists = false;
        boolean outputExists = false;

        if (CliArguments.length != 3){
            return false;
        }

        if (CliArguments[0].equals("spoken") || CliArguments[0].equals("written")) {
            mode = true;
        }

        File inputDir = new File(CliArguments[1]);
        if (inputDir.exists() && inputDir.isDirectory()) {
            inputExists = true;
        }

        File outputDir = new File(CliArguments[2]);
        if (outputDir.exists() && outputDir.isDirectory()) {
            outputExists = true;
        }

        return mode && inputExists && outputExists;
    }

    /**
     * getFilesToConvert gets all relevant files for a conversion process filtered by mode
     * @param directory directory containing files to convert
     * @param mode the mode to filter the files for (spoken or written)
     * @return an array of file names with all files to be converted or null if the specified directory is empty
     *          or does not contain files of the specified mode
     */
    public File[] getFilesToConvert(String directory, String mode) {
        File folder = new File(inputDir);

        // file filter to sort File array for only spoken formats
        FileFilter spokenFilefilter = new FileFilter() {
            public boolean accept(File file) {
                for (int i = 0; i < SPOKENFORMATS.length; i++){
                    if (file.getName().endsWith(SPOKENFORMATS[i])) {
                        return true;
                    }
                }
                return false;
            }
        };

        // file filter to sort File array for only written formats
        FileFilter writtenFilefilter = new FileFilter() {
            public boolean accept(File file) {
                for (int i = 0; i < WRITTENFORMATS.length; i++){
                    if (file.getName().endsWith(WRITTENFORMATS[i])) {
                        return true;
                    }
                }
                return false;
            }
        };

        // if mode = spoken -> get all spoken files, if mode = written -> get all written files
        if (mode.equals("spoken")){
            File[] files = folder.listFiles(spokenFilefilter);
            if (files.length == 0){
                return null;
            }
            return files;

        } else if (mode.equals("written")) {
            File[] files = folder.listFiles(writtenFilefilter);
            if (files.length == 0){
                return null;
            }
            return files;
        }

        return null;
    }


    // For running main class with CLI args: right click on org.example.TeiWorld.java > More Run/Debug > Modify Run Configuration
    public static void main(String[] args) {

        // TO DO: add CLI help menu for: java -jar org.example.TeiWorld --help / -h

        // check if CLI args are correct and complete - if not: exit program
        boolean correctArgs = checkArgs(args);
        System.out.println("Are args correct? " + correctArgs);
        if (!correctArgs) {
            System.out.println("Error with arguments, restart the converter with: java -jar org.example.TeiWorld.jar [\"spoken\" or \"written\"] [existing input directory path] [existing output directory path]");
            System.exit(0);
        }

        TeiWorld teiwrld = new TeiWorld(args[0], args[1], args[2]);

        System.out.println("teiwrld mode: " + teiwrld.getMode());
        System.out.println("teiwrld input dir: " + teiwrld.getInputDir());
        System.out.println("teiwrld output dir: " + teiwrld.getOutputDir());
        System.out.println("------");
        System.out.println("Allowed file formats for spoken: " + Arrays.toString(teiwrld.SPOKENFORMATS));
        System.out.println("Allowed file formats for written: " + Arrays.toString(teiwrld.WRITTENFORMATS));
        System.out.println("Mimetype mapping:");
        teiwrld.FORMATMIMETYPES.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("------");

        // TO DO (write method returning boolean): for each mode (spoken and written) check if there are files to convert input directory and if files have one of the required formats
        // if not, return false and display error similar to Line 48 and exit program
        // spoken: .eaf .cha .trs .TextGrid .qdpx
        // written: .docx .txt

        // test getFilesToConvert method
        File[] fileList = teiwrld.getFilesToConvert(teiwrld.inputDir, teiwrld.mode);
        if (fileList != null){
            System.out.println("List of files after sorting out filtering with mode " + teiwrld.mode + ":");
            for (int i = 0; i < fileList.length; i++) {
                System.out.println("File: " + fileList[i].getAbsoluteFile());
            }
        } else {
            System.out.println("No files found in input folder with the required file format. The following formats are possible:" +
                    "\nAllowed file formats for spoken: " + Arrays.toString(teiwrld.SPOKENFORMATS) + "\nAllowed formats for written: " +
                    Arrays.toString(teiwrld.WRITTENFORMATS));
        }


        System.out.println("------");


        // DONE: check which mode the user wants and collect all relevant files in input folder -> method getFilesToConvert

        // TO DO: Do the conversions for each file: Write separate class for each Format Conversion:
        // -> write interface Convertor which the individual Convertor classes implement:
        // SPOKEN: ElanToTeiConvertor, ClanToTeiConvertor, PraatToTeiConvertor, TranscriberToTeiConvertor, QdpxToTeiConvertor
        // WRITTEN: DocxToTeiConvertor, TxtToTeiConvertor
        // interface has: attribute inputFilePath, attribute outputFilePath, method convert, method getInputFilePath, method getOutputFilePath
        // see https://www.baeldung.com/java-execute-jar-file

        // [1-3] All conversions with TeiCorpo: ElanToTeiConvertor, ClanToTeiConvertor, TranscriberToTei:
        // Test the method converting with TeiCorpo - jar file , JAR files must be stored in two places:
        // C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes and
        // C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\src\main\resources
        String jarTeicorpo = "teicorpo.jar";
        String out= "C:\\Users\\Schwarz\\Documents\\Git\\TEIWorLD\\tmpoutput\\";


        for (int i = 0; i < fileList.length; i++){

            // ------------- SPOKEN FORMATS: ----------------------
            // [1] Conversion of .eaf files with teicorpo
            if (fileList[i].getName().endsWith(".eaf")){
                String in = fileList[i].getAbsoluteFile().toString();
                ElanToTeiConvertor elanToTeiConv = new ElanToTeiConvertor(jarTeicorpo, in, out);
                elanToTeiConv.convert();
            }
            // [2] Conversion of .cha files with teicorpo
            if (fileList[i].getName().endsWith(".cha")){
                String in = fileList[i].getAbsoluteFile().toString();
                ClanToTeiConvertor clanToTeiConv = new ClanToTeiConvertor(jarTeicorpo, in, out);
                clanToTeiConv.convert();
            }
            // [3] Conversion of .trs files with teicorpo
            if (fileList[i].getName().endsWith(".trs")){
                String in = fileList[i].getAbsoluteFile().toString();
                TrsToTeiConvertor trsToTeiConv = new TrsToTeiConvertor(jarTeicorpo, in, out);
                trsToTeiConv.convert();
            }
            // [4] Conversion of .TextGrid praat files with teicorpo
            if (fileList[i].getName().endsWith(".TextGrid")){
                String in = fileList[i].getAbsoluteFile().toString();
                TextGridToTeiConvertor textGridToTeiConv = new TextGridToTeiConvertor(jarTeicorpo, in, out);
                textGridToTeiConv.convert();
            }

            // ------------- WRITTEN FORMATS: ----------------------
            // [5] Conversion of .txt files with TEIgarage
            if (fileList[i].getName().endsWith(".txt")){
                String in = fileList[i].getAbsoluteFile().toString();
                TxtToTeiConvertor txtToTeiConvertor = new TxtToTeiConvertor(in, out);
                txtToTeiConvertor.convert();
            }


        }

        // TO DO: Execute Conversion for written: call respective class and save each converted file into output directory
        // Then call the code p52i5 to create a single written I5 file (IDS corpus) from the individually converted files


    }
}