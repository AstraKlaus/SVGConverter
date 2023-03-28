package ak.batik.svgconverter.controllers;

import ak.batik.svgconverter.services.ImageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.Objects;


@RestController
@RequestMapping("/endpoint")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping("/put")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) throws InterruptedException, IOException {
        String convertedImage = imageService.convertFile(file);
        return ResponseEntity.ok().body(convertedImage);
    }

    @PostMapping("/converting")
    public byte[] convertImages(@RequestParam("image") MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        byte[] pngBytes = imageService.imageToBytes(image);

        // Конвертация PNG в PNM с помощью ImageMagick
        ProcessBuilder magickProcessBuilder = new ProcessBuilder("magick","convert", "-", "pnm:-");
        magickProcessBuilder.redirectErrorStream(true);
        Process magickProcess = magickProcessBuilder.start();

        OutputStream stdin = magickProcess.getOutputStream();
        stdin.write(pngBytes);


        InputStream pnmStream = magickProcess.getInputStream();

        // Конвертация PNM в SVG с помощью potrace
        ProcessBuilder potraceProcessBuilder = new ProcessBuilder("potrace", "--svg", "-");
        potraceProcessBuilder.redirectErrorStream(true);
        Process potraceProcess = potraceProcessBuilder.start();

        stdin = potraceProcess.getOutputStream();
        IOUtils.copy(pnmStream, stdin);

        InputStream svgStream = potraceProcess.getInputStream();

        // Чтение SVG из процесса и запись его в байтовый массив
        ByteArrayOutputStream svgOutStream = new ByteArrayOutputStream();
        IOUtils.copy(svgStream, svgOutStream);
        byte[] svgBytes = svgOutStream.toByteArray();

        // Закрытие потоков ввода-вывода
        pnmStream.close();
        svgStream.close();
        svgOutStream.close();

        // Возврат SVG в виде байтового массива
        return svgBytes;
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertImage(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).replace(".png", "");
        File pngFile = File.createTempFile(fileName, ".png");
        file.transferTo(pngFile);

        // Конвертация файла из PNG в PNM с помощью ImageMagick
        File pnmFile = File.createTempFile(fileName, ".pnm");
        System.out.println(pnmFile.createNewFile() + " " + file.getName() + " " + pnmFile.getAbsolutePath());
        System.out.println("convert " + file.getOriginalFilename() + pnmFile.getAbsolutePath());

        ProcessBuilder pb1 = new ProcessBuilder("E:/Басня/ImageMagick/convert.exe", pngFile.getAbsolutePath(), pnmFile.getAbsolutePath());
        pb1.redirectErrorStream(true);
        Process process1 = pb1.start();

        // Запись данных изображения в поток ввода для ImageMagick
        try (InputStream input = pnmFile.toURL().openStream();
             OutputStream output = process1.getOutputStream()) {
            IOUtils.copy(input, output);
        }

        File svgFile = File.createTempFile(fileName, ".svg");
        // Чтение результата конвертации изображения из потока вывода ImageMagick
        try (InputStream input = process1.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copy(input, output);

            // Конвертация файла из PNM в SVG с помощью potrace
            System.out.println(pnmFile.getAbsolutePath() + " " + svgFile.getAbsolutePath());
            ProcessBuilder pb2 = new ProcessBuilder("E:/Басня/potrace-1.16.win64/potrace.exe", "--svg",
                    pnmFile.getAbsolutePath(), "-o" ,svgFile.getAbsolutePath());
            pb2.redirectErrorStream(true);
            Process process2 = pb2.start();

            // Запись данных PNM-изображения в поток ввода для potrace
            try (InputStream pnmInput = new ByteArrayInputStream(output.toByteArray());
                 OutputStream pnmOutput = process2.getOutputStream()) {
                IOUtils.copy(pnmInput, pnmOutput);
            }

            // Чтение результата конвертации PNM-изображения в SVG из потока вывода potrace
            try (InputStream svgInput = process2.getInputStream()) {
                byte[] svgBytes = IOUtils.toByteArray(svgInput);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
                return new ResponseEntity<>(svgBytes, headers, HttpStatus.OK);
            }
        }
    }


}
