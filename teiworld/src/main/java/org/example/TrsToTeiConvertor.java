package org.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;

public class TrsToTeiConvertor implements ConvertorInterface {

    String inputFilePath;
    String outputFilePath;
    String jarPath;  // where to store JAR file of TEICorpo? Inside the java project structure?
    // JARs must be stored in two places:
    // TEIWorLD\teiworld\target\classes and
    // TEIWorLD\teiworld\src\main\resources

    public TrsToTeiConvertor(String jar, String inPath, String outPath){
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
        // see https://www.baeldung.com/java-execute-jar-file
        Process process = null;

        try {
            String jarFile = new File(Objects.requireNonNull(getClass().getClassLoader()
                            .getResource(this.jarPath))
                    .toURI()).getAbsolutePath();

            // JAR must be stored in two places:
            // TEIWorLD\teiworld\target\classes and
            // TEIWorLD\teiworld\src\main\resources
            String jarFileCommonsIO = new File(Objects.requireNonNull(getClass().getClassLoader()
                            .getResource("commons-io-2.19.0.jar"))
                    .toURI()).getAbsolutePath();

            // Command: java -cp teicorpo.jar;commons-io-2.19.0.jar fr.ortolang.teicorpo.TranscriberToTei  "inputfile.trs" -o "path/output/directory/"
            // command needs to be passed to process as a String array:
            // { "java",
            //   "-cp",
            //   "C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes\teicorpo.jar;C:\Users\Schwarz\Documents\Git\TEIWorLD\teiworld\target\classes\commons-io-2.19.0.jar",
            //   "fr.ortolang.teicorpo.TranscriberToTei",
            //   "C:\Users\Schwarz\Documents\Git\omniconverter\in\spokendata\file.trs",
            //   "-o",
            //   "C:\Users\Schwarz\Documents\Git\TEIWorLD\tmpoutput\" };
            String[] command = { "java", "-cp", jarFile.toString() + ";" + jarFileCommonsIO.toString(),
                    "fr.ortolang.teicorpo.TranscriberToTei", this.inputFilePath, "-o", this.outputFilePath };

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

    }
}
