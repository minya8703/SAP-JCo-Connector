package jco.jcosaprfclink.config.saprfc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sap.rfc")
public class SapRfcProperties {
    private List<SapRfcFunctionProperties> functions;
} 