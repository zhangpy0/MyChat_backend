package top.zhangpy.mychat.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("SpringTest API")
                        .description("SpringTest application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                        .externalDocs(new ExternalDocumentation()
                        .description("MyChat Wiki Documentation"));
    }
}
