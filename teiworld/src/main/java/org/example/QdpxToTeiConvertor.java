package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Objects;
import java.net.URISyntaxException;


public class QdpxToTeiConvertor implements ConvertorInterface {

    String inputFilePath;
    String outputFilePath;
    String jarPath;  // where to store JAR file of TEICorpo? Inside the java project structure?
    Path tempOutputFilePath; // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp, in Linux and macOS: /tmp
    // JARs must be stored in two places:
    // TEIWorLD\teiworld\target\classes and
    // TEIWorLD\teiworld\src\main\resources

    public QdpxToTeiConvertor(String jar, String inPath, String outPath) {
        this.jarPath = jar;
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

    public String getExecutableFilePath() {
        return this.jarPath;
    }

    @Override
    public void convert() {
        // process from Shell script:
        // append ".ZIP" to file: mv 3wCno_4b_In.qdpx 3wCno_4b_In.qdpx.ZIP to be able to unzip it in the following step
        // then unzip into temporary dir: unzip 3wCno_4b_In.qdpx.ZIP -d ./tmp
        // get the txt-file's name in a variable which is in the dir "sources" that exists after unzipping: ./sources/*.txt
        // check if the file is a txt file
        // delete empty lines in file and save as temporary file: grep . ./tmp/sources/*.txt > ./tmp/sources/filename.txt
        // execute java command for conversion: java -cp "teicorpo.jar" fr.ortolang.teicorpo.TeiCorpo -from text ./tmp/sources/filename.txt -o "$OUTPUTFILE_final"
        // delete the appended ".ZIP" from file: mv 3wCno_4b_In.qdpx.ZIP 3wCno_4b_In.qdpx


        // Actual process:

        // copy file to be converted to temporary directory and append .zip
        Path source = new File(this.inputFilePath).toPath();
        String tmpDir = System.getProperty("java.io.tmpdir");
        File newTmpDirectory = new File(tmpDir, source.getFileName().toString());
        if (!newTmpDirectory.exists()){
            newTmpDirectory.mkdir();
        } else {
            System.out.println("Error: Creating temporary directory not possible. '" + newTmpDirectory + "' already exists");
        }
        Path target = new File(newTmpDirectory.toString() + "\\" + source.getFileName() + ".zip").toPath();



        System.out.println("source: " + source.getFileName());
        System.out.println("target: " + target.getFileName());
        System.out.println("target path: " + target);
        System.out.println("target path without file: " + newTmpDirectory);



        try {
            // In Windows the directory for temporary files is by default: C:\Users\User\AppData\Local\Temp, in Linux and macOS: /tmp
            tempOutputFilePath = Files.copy(source, target);
        } catch (IOException e) {
            System.err.println("Error copying file to temp directory: " + e.getMessage());
        }

        // unzip zip file into temporary directory
        try {
            unzip(target.toString(), newTmpDirectory.toString());
        } catch (IOException e) {
            System.err.println("Error unzipping file in temp directory: " + e.getMessage());
        }

        // get the name of the txt-file(s) to be converted which is in the dir "sources" that exists after unzipping: ./sources/*.txt
        File directory = new File(newTmpDirectory.toString() + "\\sources");
        File[] allTxtFiles = directory.listFiles( new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if ( name.toUpperCase().endsWith(".TXT") ) {
                    return new File(dir,name).isFile(); // Make sure we don't accept sub-directories ending in .txt
                }
                return false;
            }
        });



        System.out.println("ALL TXT FILES in QDPX FOLDER:");
        for (int i = 0; i < allTxtFiles.length; i++) {
            System.out.println("File: " + allTxtFiles[i].getAbsoluteFile());
        }


        // delete empty lines in file
        String txtFileNoEmptyLines = allTxtFiles[0].toString().replace(".txt", "_workingFile.txt");
        deleteEmptyLines(allTxtFiles[0].toString(), txtFileNoEmptyLines);


        // TO DO: keep the original name (without _workingFile), do something like:
        //
        //File file1 = new File("src/data.txt");
        //File file2 = new File("src/data2.txt");
        //
        //file1.delete();
        //file2.renameTo(file1);


        System.out.println("workingFile: " + txtFileNoEmptyLines);



        // see https://www.baeldung.com/java-execute-jar-file
        Process process = null;

        try {
            String jarFile = new File(Objects.requireNonNull(getClass().getClassLoader()
                            .getResource(this.jarPath))
                    .toURI()).getAbsolutePath();

            // JAR must be stored in two places:
            // C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes and
            // C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\src\main\resources
            String jarFileCommonsIO = new File(Objects.requireNonNull(getClass().getClassLoader()
                            .getResource("commons-io-2.19.0.jar"))
                    .toURI()).getAbsolutePath();

            // Command: java -cp teicorpo.jar;commons-io-2.19.0.jar fr.ortolang.teicorpo.TeiCorpo -from text inputfile.txt -o "path/output/directory/"
            // command needs to be passed to process as a String array:
            // { "java",
            //   "-cp",
            //   "C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes\teicorpo.jar;C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes\commons-io-2.19.0.jar",
            //   "fr.ortolang.teicorpo.TeiCorpo",
            //   "-from",
            //   "text",
            //   "C:\Users\Schwarz\AppData\Local\Temp\3wCno_4b_In.qdpx\sources\9F718AB7-6D95-411D-8A76-8B22FAC42672_workingFile.txt",
            //   "-o",
            //   "C:\Users\Schwarz\Documents\Git\TEIWorLD\tmpoutput\" };
            String[] command = { "java", "-cp", jarFile.toString() + ";" + jarFileCommonsIO.toString(),
                    "fr.ortolang.teicorpo.TeiCorpo", "-from", "text", txtFileNoEmptyLines, "-o", this.outputFilePath };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();  // Start process

            try (InputStream inputStream = process.getInputStream()) {
                byte[] output = inputStream.readAllBytes();
                System.out.println("Output: " + new String(output));
            } catch (IOException e) {
                System.err.println("Error handling process: " + e.getMessage());
            }

        } catch (URISyntaxException e) {
            System.err.println("Error with URI syntax of jar files: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error starting process: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();  // Ensure cleanup in all Java versions
            }
        }


        // TO DO: delete temporary directory (variable newTmpDirectory) recursively (C:\Users\Schwarz\AppData\Local\Temp\3wCno_4b_In.qdpx)


        // TO DO: instead of working on allTxtFiles[0] -> create for loop



    }


    private static void unzip(String zippedFile, String targetDir) throws IOException{
        File destDir = new File(targetDir);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zippedFile));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
    }


    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        // protect against zip slip
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


    private static void deleteEmptyLines(String in, String out) {
        Scanner file;
        PrintWriter writer;

        try {
            file = new Scanner(new File(in));
            writer = new PrintWriter(out);

            while (file.hasNext()) {
                String line = file.nextLine();
                if (!line.isEmpty()) {
                    writer.write(line);
                    if (file.hasNextLine()){
                        writer.write("\n");
                    }
                }
            }
            file.close();
            writer.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error deleting empty lines from file: " + e.getMessage());
        }
    }
}
