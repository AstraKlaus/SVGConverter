package ak.batik.svgconverter.services;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

@Service
public class ImageService {

    private final String command;

    @Autowired
    public ImageService(String command) {
        this.command = command;
    }


    public byte[] convertToSvg(MultipartFile file) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            String fileName = Objects.requireNonNull(file.getOriginalFilename()).replace(".png", "");

            File startFile = File.createTempFile(fileName, Objects.requireNonNull(file.getContentType())
                    .substring(file.getContentType().indexOf("/")).replace("/", "."));
            file.transferTo(startFile);
            startFile.deleteOnExit();

            File svgFile = File.createTempFile(fileName, ".svg");
            svgFile.deleteOnExit();

            ProcessBuilder builder = new ProcessBuilder(String.format(command, svgFile.getAbsolutePath(), startFile.getAbsolutePath()).split(" "));
            builder.redirectInput(ProcessBuilder.Redirect.PIPE);
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);

            Process process = builder.start();
            process.waitFor();

            try (FileInputStream fis = new FileInputStream(svgFile)){
                fis.transferTo(outputStream);
            }

            return outputStream.toByteArray();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    public byte[] imageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
