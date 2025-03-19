import java.io.File;
import java.util.HashMap;

public class TeiWorld {
    /**
     * Checks if the command line arguments are complete and correct.
     * The first argument hast to be either "spoken" or "written",
     * the second and third arguments are the input and output directory (check for existence)
     *
     * @param CliArguments String array with the command line arguments
     * @return true if the arguments are all correct, existing, and in the right order, false otherwise
     */
    public static boolean checkArgs (String[] CliArguments) {
        for (String arg : CliArguments) {
            System.out.println(arg);
        }

        boolean mode = false;
        boolean inputExists = false;
        boolean outputExists = false;

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

    // For running main class with CLI args: right click on TeiWorld.java > More Run/Debug > Modify Run Configuration
    public static void main(String[] args) {

        // TO DO: add CLI help menu for: java -jar TeiWorld --help / -h

        // check if CLI args are correct and complete
        boolean correctArgs = checkArgs(args);
        System.out.println("Are args correct? " + correctArgs);
        if (!correctArgs) {
            System.out.println("Error with arguments, restart the converter with: java -jar TeiWorld.jar [\"spoken\" or \"written\"] [existing input directory path] [existing output directory path]");
            System.exit(0);
        }

        // TO DO (write method returning boolean): for each mode (spoken and written) check if there are files to convert input directory and if files have one of the required formats
        // if not, return false and display error similar to Line 48 and exit program
        // spoken: .eaf .cha .trs .TextGrid .qdpx
        // written: .docx .txt
        String[] spokenFormats = {"eaf", "cha", "trs", "TextGrid", "qdpx"};
        String[] writtenFormats = {"docx", "txt"};
        HashMap<String, String> formatMimeTypes = new HashMap<String, String>();
        formatMimeTypes.put("eaf", "text/xml");
        formatMimeTypes.put("cha", "text/plain");
        formatMimeTypes.put("trs", "text/xml");
        formatMimeTypes.put("TextGrid", "application/octet-stream");
        formatMimeTypes.put("qdpx", "application/zip");
        formatMimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        formatMimeTypes.put("txt", "text/plain");

        // TO DO: check which mode the user wants and collect all relevant files in input folder

        // TO DO: Do the conversions for each file: Write separate class for each Format Conversion:
        // maybe write super class Convertor which all Convertor classes extend/overwrite

        // TO DO: Execute Conversion for written: call respective class and save each converted file into output directory
        // Then call the code p52i5 to create a single written I5 file (IDS corpus) from the individually converted files

        // TO DO: Execute Conversion for spoken: call respective class and save each converted file into output directory

    }
}