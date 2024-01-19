package be;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ThumbnailGenerator {
    public void createThumbnail(String videoPath, String outputPath, String timestamp) {
        try {
            ProcessBuilder pBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-ss", timestamp,
                    "-vframes", "1",
                    outputPath
            );

            pBuilder.redirectErrorStream(true);
            final Process process = pBuilder.start();

            final InputStream is = process.getInputStream();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line = null;
                    while ((line = reader.readLine()) != null) {}
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }).start();

            Thread waitForThread = new Thread(() -> {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            });
            waitForThread.start();

            long timeOut = 250;
            waitForThread.join(timeOut);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}