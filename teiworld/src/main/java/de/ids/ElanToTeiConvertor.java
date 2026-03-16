package de.ids;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ElanToTeiConvertor implements ConvertorInterface{

    String inputFilePath;
    String outputFilePath;
    String jarPath;

    public ElanToTeiConvertor(String jar, String inPath, String outPath) {
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
            File appDir = new File(System.getProperty("user.dir"));
            File libDir = new File(appDir, "lib");

            File teicorpoJar = new File(libDir, "teicorpo.jar");
            File commonsIoJar = new File(libDir, "commons-io-2.19.0.jar");

            String classpath = teicorpoJar.getAbsolutePath()
                    + File.pathSeparator
                    + commonsIoJar.getAbsolutePath();

            String[] command = {
                    "java",
                    "-cp",
                    classpath,
                    "fr.ortolang.teicorpo.ElanToTei",
                    this.inputFilePath,
                    "-o",
                    this.outputFilePath
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();  // Start process

            // Output the executed conversion to command line for informing the user (could be omitted)
            try (InputStream inputStream = process.getInputStream()) {
                byte[] output = inputStream.readAllBytes();
                System.out.println("Output: " + new String(output));
            } catch (IOException e) {
                System.err.println("Error handling process: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error starting process: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();  // Ensure cleanup in all Java versions
            }
        }

    }
}
