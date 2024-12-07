package ca.gbc.userservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${UserService.version}")
    private String version;

    @Bean
    public OpenAPI UserServiceAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8054"))
                .info(new Info()
                        .title("User Service API")
                        .description("This is the REST API for User Service")
                        .version(version)
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("User Service Wiki Documentation")
                        .url("https://mycompany.ca/user-service/docs"));
    }
}