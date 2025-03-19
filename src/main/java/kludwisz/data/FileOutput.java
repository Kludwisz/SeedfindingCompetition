package kludwisz.data;

import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("unused")
public class FileOutput {
    public static synchronized void appendToFile(String filename, String text) {
        try {
            FileWriter fileWriter = new FileWriter(filename, true);
            fileWriter.append(text).append("\n");
            fileWriter.close();
        }
        catch (IOException e) {
            System.err.println("Couldn't append to file: " + text);
        }
    }
}
