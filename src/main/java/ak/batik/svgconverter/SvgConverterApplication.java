package ak.batik.svgconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class SvgConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SvgConverterApplication.class, args);
    }

}
