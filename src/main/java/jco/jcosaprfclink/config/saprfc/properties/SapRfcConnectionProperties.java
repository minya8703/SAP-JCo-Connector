package jco.jcosaprfclink.config.saprfc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sap.connect")
public class SapRfcConnectionProperties {
    // 기본 연결 설정
    private String host;
    private String sysnr;
    private String client;
    private String user;
    private String passwd;
    private String lang;
    private String poolCapacity;
    private String peakLimit;
    
    // 서버 설정
    private String serverName;
    private String gatewayHost;
    private String gatewayService;
    private String programId;
    
    // Repository 설정
    private String repositoryDestination;
    private String repositoryUser;
    private String repositoryPassword;

    private Integer connectionCount = 5;  // 기본값 5로 설정
    private Integer threadCount = 3;      // 기본값 3으로 설정
} 