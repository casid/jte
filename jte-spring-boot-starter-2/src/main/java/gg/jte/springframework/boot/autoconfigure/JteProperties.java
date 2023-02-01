package gg.jte.springframework.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.util.Arrays;

@ConfigurationProperties(prefix = "gg.jte")
public class JteProperties {

    private String productionProfileName = "prod";
    private String templateLocation = "src/main/jte";
    private String templateSuffix = ".jte";

    public String getTemplateSuffix() {
        return templateSuffix;
    }

    public void setTemplateSuffix(final String templateSuffix) {
        this.templateSuffix = templateSuffix;
    }

    public String getProductionProfileName() {
        return productionProfileName;
    }

    public void setProductionProfileName(String productionProfileName) {
        this.productionProfileName = productionProfileName;
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    public boolean isProductionEnabled(@NonNull Environment environment) {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch(it -> it.equals(this.getProductionProfileName()));
    }
}
