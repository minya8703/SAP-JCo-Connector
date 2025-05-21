package jco.jcosaprfclink.config.saprfc.properties;

import lombok.Data;
import java.util.List;

@Data
public class SapRfcFunctionProperties {
    private String name;
    private String handler;
    private List<String> tables;
} 