package jco.jcosaprfclink.config.saprfc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class SapRFCConnection {
    @Autowired
    private JCoConnectionManager connManager;

    @PostConstruct
    public void init() {
        log.info("RFC Server Start");
        connManager.stepRfcServer();
    }
}