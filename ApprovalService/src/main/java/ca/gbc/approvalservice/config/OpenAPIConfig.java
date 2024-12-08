package ca.gbc.approvalservice.config;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${ApprovalService.version}")
    private String version;

    @Bean
    public OpenAPI ApprovalServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Approval Service API")
                        .description("This is the REST API for Approval Service")
                        .version(version)
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Approval Service Wiki Documentation")
                        .url("https://mycompany.ca/Approval-service/docs"));
    }
}