package jco.jcosaprfclink.config.saprfc.connection.handler;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("anotherHandler")
public class AnotherHandler implements SapRfcServerHandler {
    @Override
    public String getFunctionName() {
        return "ZFI_ANOTHER_FUNC";
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function) {
        log.info("AnotherHandler.handleRequest() 호출됨");
        // TODO: 실제 비즈니스 로직 구현
    }
} 