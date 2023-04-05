package ak.batik.svgconverter.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ImageConfig.class)
public class ApplicationConfig {

    private final ImageConfig imageConfig;

    @Autowired
    public ApplicationConfig(ImageConfig imageConfig) {
        this.imageConfig = imageConfig;
    }

    @Bean
    public String command(){
        return imageConfig.getCommand();
    }
}
