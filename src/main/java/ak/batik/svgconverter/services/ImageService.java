package ak.batik.svgconverter.services;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageService {


    public String convertFile(MultipartFile file) throws IOException, InterruptedException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).replace(".png", ".pnm");

// Конвертация файла из PNG в PNM с помощью ImageMagick
        File pnmFile = new File(fileName);
        System.out.println(pnmFile.createNewFile()+file.getName() + pnmFile.getAbsolutePath());
        ProcessBuilder pb1 = new ProcessBuilder("convert ", "C:/Users/Xaser/Documents/SVGConverter/arc.png", "C:/Users/Xaser/Documents/SVGConverter/arc.pnm");
        //ProcessBuilder pb1 = new ProcessBuilder("convert", file.getResource().getFile().toPath().toString(), pnmFile.getAbsolutePath());
        pb1.redirectErrorStream(true);
        Process process1 = pb1.start();
        process1.waitFor();

// Конвертация файла из PNM в SVG с помощью potrace
        ProcessBuilder pb2 = new ProcessBuilder("potrace", pnmFile.getAbsolutePath(), "-s", "-o", "-");
        pb2.redirectErrorStream(true);
        Process process2 = pb2.start();

        String svgString;
        try (InputStream inputStream = process2.getInputStream()) {
            svgString = IOUtils.toString(inputStream);
        }

        return svgString;
    }

    public byte[] imageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
