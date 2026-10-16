package br.cefetmg.ocva.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minha Primeira API Documentada")
                        .version("1.0.0")
                        .description("Documentação interativa gerada com Springdoc OpenAPI e Java 21")
                        .contact(new Contact().name("Seu Nome").email("seu-email@dominio.com")));
    }
}