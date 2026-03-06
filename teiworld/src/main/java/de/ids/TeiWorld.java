package de.ids;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TeiWorld {

    // Class variables holding accepted file format endings to check for the right file format of the files to be processed
    public static final String[] SPOKENFORMATS = {"eaf", "cha", "trs", "TextGrid", "qdpx"};
    public static final String[] WRITTENFORMATS = {"docx", "txt"};

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
        return inputDir + File.separator;
    }

    public String getOutputDir(){
        return outputDir + File.separator;
    }

    /**
     * Checks if the command line arguments are complete and correct.
     * The first argument hast to be either "spoken" or "written" or "writtenP5",
     * the second and third arguments are the input and output directory (check for existence)
     *
     * @param CliArguments String array with the command line arguments
     * @return true if the arguments are all correct, existing, and in the right order, false otherwise
     */
    public static boolean checkArgs (String[] CliArguments) {
        boolean mode = false;
        boolean inputExists = false;
        boolean outputExists = false;

        if (CliArguments[0].equalsIgnoreCase("h") || CliArguments[0].equalsIgnoreCase("-h")
                || CliArguments[0].equalsIgnoreCase("--h") || CliArguments[0].equalsIgnoreCase("help")
                || CliArguments[0].equalsIgnoreCase("-help") || CliArguments[0].equalsIgnoreCase("--help")) {
            System.out.println("usage: java -jar de.ids.TeiWorld.jar [<mode>] [<path to input directory>] [<path to output directory>]");
            System.out.println("   mode     'spoken' - converts to TEIspoken and keeps files separate if there is more than one in the input directory");
            System.out.println("            'written' - converts to TEI I5 and combines files to a single corpus in case there is more than one in the input directory. The file metadata.json needs to be in the same directory, containing the keys \"CorpusSigle\" and \"DocumentSigle\", both with non-empty values");
            System.out.println("            'writtenP5' - converts to TEI P5 and keeps files separate if there is more than one in the input directory");
            System.out.println("            'writtenHierarchical' - converts to TEI I5 and constructs the hierarchical document and text structure of a written corpus. The directory needs to contain the file metadata.json (with the key \"CorpusSigle\" and its non-empty value) and one or more subdirectories (= idsDoc) that contain the individual texts (= idsText).");
            System.exit(0);
        }

        // too many/few arguments
        if (CliArguments.length != 3){
            return false;
        }

        if (CliArguments[0].equals("spoken") ||
                CliArguments[0].equals("written") ||
                CliArguments[0].equalsIgnoreCase("writtenP5") ||
                CliArguments[0].equalsIgnoreCase("writtenHierarchical")) {
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
     * @param mode the mode to filter the files for (spoken or written)
     * @return an array of file names with all files to be converted or null if the specified directory is empty
     *          or does not contain files of the specified mode
     */
    public File[] getFilesToConvert(String mode) throws IOException {
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

        // if mode = spoken -> get all spoken files
        if (mode.equals("spoken")){
            File[] files = folder.listFiles(spokenFilefilter);
            if (files.length == 0){
                return null;
            }
            return files;

        // if mode = written -> get all written files
        } else if (mode.equals("written") || mode.equalsIgnoreCase("writtenP5")) {
            File[] files = folder.listFiles(writtenFilefilter);
            if (files.length == 0){
                return null;
            }
            return files;

        // if mode = writtenHierarchical -> get all written files in subdirectories (only go one level down)
        } else if (mode.equals("writtenHierarchical")) {
            List<File> fileArrayList = new ArrayList<>();
            // get all files that are contained in any subdirectories of depth = 2
            int minDepth = 2;
            int maxDepth = 2;
            Path rootPath = Paths.get(inputDir);
            int rootPathDepth = rootPath.getNameCount();
            Files.walk(rootPath, maxDepth)
                    .filter(e -> e.getNameCount() - rootPathDepth >= minDepth)
                    .filter(e -> e.toFile().isFile())
                    .forEach(e -> fileArrayList.add(e.toFile()));

            // remove any file that does not have one of the specified written formats
            for (int i = 0; i < fileArrayList.size(); i++) {
                boolean hasWrittenFormat = false;
                for (String fileEnding : WRITTENFORMATS){
                    if (fileArrayList.get(i).getName().endsWith(fileEnding)){
                        hasWrittenFormat = true;
                    }
                }
                if (hasWrittenFormat == false){
                    fileArrayList.remove(i);
                }
            }
            // convert to File [] array
            File[] files = (File[]) fileArrayList.toArray(new File[fileArrayList.size()]);
            if (files.length == 0){
                return null;
            }
            return files;
        }

        return null;
    }


    // For running main class with CLI args: right click on de.ids.TeiWorld.java > More Run/Debug > Modify Run Configuration
    public static void main(String[] args) {

        // check if CLI args are correct and complete - if not: exit program
        boolean correctArgs = checkArgs(args);
        if (!correctArgs) {
            System.out.print("Error with command line arguments! You entered: java -jar de.ids.TeiWorld.jar ");
            for (int i = 0; i < args.length; i++){
                System.out.print(args[i] + " ");
            }
            System.out.println("\nRestart the tool with the correct args: java -jar de.ids.TeiWorld.jar [\"spoken\" or \"written\" or \"writtenP5\" or \"writtenHierarchical\"] " +
                    "[existing input directory path] [existing output directory path] \nFind help with: java -jar de.ids.TeiWorld.jar -h for help.");
            System.exit(0);
        }

        TeiWorld teiwrld = new TeiWorld(args[0], args[1], args[2]);

        /*
        System.out.println("teiwrld mode: " + teiwrld.getMode());
        System.out.println("teiwrld input dir: " + teiwrld.getInputDir());
        System.out.println("teiwrld output dir: " + teiwrld.getOutputDir());
        System.out.println("------");
        System.out.println("Allowed file formats for spoken: " + Arrays.toString(teiwrld.SPOKENFORMATS));
        System.out.println("Allowed file formats for written: " + Arrays.toString(teiwrld.WRITTENFORMATS));
        System.out.println("Mimetype mapping:");
        teiwrld.FORMATMIMETYPES.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("------");
        */

        // Check if a file "metadata.json" is present in the input file directory
        if (teiwrld.mode.equalsIgnoreCase("written") || teiwrld.mode.equalsIgnoreCase("writtenHierarchical")) {
            File metadataFile = new File(teiwrld.inputDir + File.separator + "metadata.json");
            if (!metadataFile.exists()) {
                System.out.println("No file metadata.json found. When using the command 'written' or 'writtenHierarchical' the file " +
                        "metadata.json is mandatory and needs to be present in the input directory path.\n" +
                        "See also the help menu java -jar de.ids.TeiWorld.jar -h");
                System.exit(0);
            } else {
                try {
                    // Open and Parse the JSON file and convert to JSONObject
                    FileReader reader = new FileReader(metadataFile);
                    JSONParser jsonParser = new JSONParser();
                    Object obj = jsonParser.parse(reader);
                    JSONObject jsonObject = (JSONObject) obj;

                    // metadata.json exists but no CorpusSigle: for mode writtenHierarchical there needs to be a non-empty "CorpusSigle"
                    if (teiwrld.mode.equalsIgnoreCase("writtenHierarchical")){
                        if (!jsonObject.containsKey("CorpusSigle") || jsonObject.get("CorpusSigle").toString().length() == 0){
                            System.out.println("For the mode writtenHierarchical the file metadata.json needs to contain the key CorpusSigle with a non-empty value.\n" +
                                    "See also the help menu java -jar de.ids.TeiWorld.jar -h");
                            System.exit(0);
                        }
                    }

                    // metadata.json exists but no CorpusSigle or DocumentSigle for mode written there needs to be a non-empty "CorpusSigle" and a non-empty "DocumentSigle"
                    if (teiwrld.mode.equalsIgnoreCase("written")){
                        if ((!jsonObject.containsKey("CorpusSigle") || jsonObject.get("CorpusSigle").toString().length() == 0) ||
                                (!jsonObject.containsKey("DocumentSigle") || jsonObject.get("DocumentSigle").toString().length() == 0)){
                            System.out.println("For the mode written the file metadata.json needs to contain the keys CorpusSigle and DocumentSigle both with non-empty values.\n" +
                                    "See also the help menu java -jar de.ids.TeiWorld.jar -h");
                            System.exit(0);
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("Error finding JSON metadata file metadata.json: " + e.getMessage());
                    System.exit(0);
                } catch (IOException | ParseException e) {
                    System.err.println("Error parsing JSON file metadata.json: " + e.getMessage());
                    System.exit(0);
                }
            }
        }


        // Informing the user about the files that will be converted
        try{
            File[] fileList = teiwrld.getFilesToConvert(teiwrld.mode);
            if (fileList != null){
                System.out.println("Converting the following files to TEI:");
                for (int i = 0; i < fileList.length; i++) {
                    System.out.println("File: " + fileList[i].getAbsoluteFile());
                }
            } else {
                System.out.println("No files found in input folder with the required file format. The following formats are possible:" +
                        "\nAllowed file formats for spoken: " + Arrays.toString(teiwrld.SPOKENFORMATS) + "\nAllowed formats for written: " +
                        Arrays.toString(teiwrld.WRITTENFORMATS));
                System.exit(0);
            }
            System.out.println("... processing ...");


            // [S1-S5] All conversions with TeiCorpo: ElanToTeiConvertor, ClanToTeiConvertor, TranscriberToTeiConvertor, TextGridToTeiConvertor, QdpxToTeiConvertor:
            // Test the method converting with TeiCorpo - jar file , JAR files must be stored in two places:
            // C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes and
            // C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\src\main\resources
            String jarTeicorpo = "teicorpo.jar";

            for (int i = 0; i < fileList.length; i++){

                // ------------- S: SPOKEN FORMATS: ----------------------
                // [S1] Conversion of .eaf files with teicorpo
                if (fileList[i].getName().endsWith(".eaf")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    ElanToTeiConvertor elanToTeiConv = new ElanToTeiConvertor(jarTeicorpo, in, teiwrld.getOutputDir());
                    elanToTeiConv.convert();
                }
                // [S2] Conversion of .cha files with teicorpo
                if (fileList[i].getName().endsWith(".cha")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    ClanToTeiConvertor clanToTeiConv = new ClanToTeiConvertor(jarTeicorpo, in, teiwrld.getOutputDir());
                    clanToTeiConv.convert();
                }
                // [S3] Conversion of .trs files with teicorpo
                if (fileList[i].getName().endsWith(".trs")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    TrsToTeiConvertor trsToTeiConv = new TrsToTeiConvertor(jarTeicorpo, in, teiwrld.getOutputDir());
                    trsToTeiConv.convert();
                }
                // [S4] Conversion of .TextGrid praat files with teicorpo
                if (fileList[i].getName().endsWith(".TextGrid")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    TextGridToTeiConvertor textGridToTeiConv = new TextGridToTeiConvertor(jarTeicorpo, in, teiwrld.getOutputDir());
                    textGridToTeiConv.convert();
                }
                // [S5] Conversion of .qdpx files with teicorpo -----------
                if (fileList[i].getName().endsWith(".qdpx")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    QdpxToTeiConvertor qdpxToTeiConvertor = new QdpxToTeiConvertor(jarTeicorpo, in, teiwrld.getOutputDir());
                    qdpxToTeiConvertor.convert();
                }

                // ------------- W: WRITTEN FORMATS: ----------------------
                // [W1] Conversion of .txt files with TEIgarage
                if (fileList[i].getName().endsWith(".txt")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    TxtToTeiConvertor txtToTeiConvertor = new TxtToTeiConvertor(in, teiwrld.getOutputDir());
                    txtToTeiConvertor.convert();
                }
                // [W2] Conversion of .docx files with TEIgarage
                if (fileList[i].getName().endsWith(".docx")){
                    String in = fileList[i].getAbsoluteFile().toString();
                    DocxToTeiConvertor docxToTeiConvertor = new DocxToTeiConvertor(in, teiwrld.getOutputDir());
                    docxToTeiConvertor.convert();
                }
            }

            // if the mode is written call the code p52i5 to create a single written I5 file (IDS corpus) from the individually converted files
            if (teiwrld.mode.equalsIgnoreCase("written")) {
                System.out.println("Transforming the above mentioned files to TEI I5 and compiling a single corpus (TEI I5)...");
                P5ToI5Convertor p5ToI5Convertor =  new P5ToI5Convertor(teiwrld.inputDir, teiwrld.outputDir);
                p5ToI5Convertor.convert();
            }

            // if the mode is writtenHierarchical call the code p52i5 to create a single written I5 file (IDS corpus) from the individually converted files
            // where the sigles in idsDoc and idsText contain the folder structure
            if (teiwrld.mode.equalsIgnoreCase("writtenHierarchical")) {
                System.out.println("Transforming the above mentioned files to TEI I5 and compiling a hierarchical single corpus (TEI I5)...");
                P5ToI5HierarchicalConvertor p5ToI5HierarchicalConvertor =  new P5ToI5HierarchicalConvertor(teiwrld.inputDir, teiwrld.outputDir, fileList);
                p5ToI5HierarchicalConvertor.convert();
            }

        } catch (IOException e) {
            System.out.println("IO Exception when traversing directory structure in mode 'writtenHierarchical': " + e.getMessage());
        }
    }
}