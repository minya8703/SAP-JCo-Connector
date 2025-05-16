package jco.jcosaprfclink.config.saprfc.connection;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerFactory;
import jco.jcosaprfclink.config.saprfc.connection.handler.SapRfcServerHandler;
import jco.jcosaprfclink.config.saprfc.properties.SapRfcConnectionProperties;
import jco.jcosaprfclink.config.saprfc.properties.SapRfcProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static jco.jcosaprfclink.type.ErrorCode.NOT_CREATE_FILE_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class JCoSapRfcConnection {
    private static final String DESTINATION_NAME = "ABAP_AS_WITHOUT_POOL";

    private final SapRfcProperties sapRfcProperties;
    private final SapRfcConnectionProperties connectionProperties;
    private final ApplicationContext applicationContext;
    private final Map<String, SapRfcServerHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void initialize() {
        try {
            log.info("SAP 연결 프로퍼티: host={}, sysnr={}, client={}, user={}, passwd={}, lang={}, gatewayHost={}, gatewayService={}, programId={}, repositoryDestination={}, serverName={}",
                connectionProperties.getHost(),
                connectionProperties.getSysnr(),
                connectionProperties.getClient(),
                connectionProperties.getUser(),
                connectionProperties.getPasswd(),
                connectionProperties.getLang(),
                connectionProperties.getGatewayHost(),
                connectionProperties.getGatewayService(),
                connectionProperties.getProgramId(),
                connectionProperties.getRepositoryDestination(),
                connectionProperties.getServerName()
            );
            log.info("SAP RFC 연결 초기화 시작...");
            createDestinationProperties();
            createServerProperties();
            registerHandlers();
            startRfcServer();
            log.info("SAP RFC 연결 초기화 완료");
        } catch (Exception e) {
            log.error("SAP RFC 연결 초기화 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createDestinationProperties() {
        Properties properties = new Properties();
        properties.setProperty(DestinationDataProvider.JCO_ASHOST, connectionProperties.getHost());
        properties.setProperty(DestinationDataProvider.JCO_SYSNR, connectionProperties.getSysnr());
        properties.setProperty(DestinationDataProvider.JCO_CLIENT, connectionProperties.getClient());
        properties.setProperty(DestinationDataProvider.JCO_USER, connectionProperties.getUser());
        properties.setProperty(DestinationDataProvider.JCO_PASSWD, connectionProperties.getPasswd());
        properties.setProperty(DestinationDataProvider.JCO_LANG, connectionProperties.getLang());
        properties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, connectionProperties.getPoolCapacity());
        properties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, connectionProperties.getPeakLimit());
        createDataFile(DESTINATION_NAME, "jcoDestination", properties);
    }

    private void createServerProperties() {
        Properties properties = new Properties();
        properties.setProperty(ServerDataProvider.JCO_GWHOST, connectionProperties.getGatewayHost());
        properties.setProperty(ServerDataProvider.JCO_GWSERV, connectionProperties.getGatewayService());
        properties.setProperty(ServerDataProvider.JCO_PROGID, connectionProperties.getProgramId());
        properties.setProperty(ServerDataProvider.JCO_REP_DEST, connectionProperties.getRepositoryDestination());
        properties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, String.valueOf(connectionProperties.getConnectionCount()));
        properties.setProperty(ServerDataProvider.JCO_WORKER_THREAD_COUNT, String.valueOf(connectionProperties.getThreadCount()));
        createDataFile(connectionProperties.getServerName(), "jcoServer", properties);
    }

    private void createDataFile(String name, String suffix, Properties properties) {
        File cfg = new File(name + "." + suffix);
        try (FileOutputStream fos = new FileOutputStream(cfg, false)) {
            properties.store(fos, "SAP JCo Configuration");
        } catch (Exception e) {
            throw new RuntimeException(NOT_CREATE_FILE_ERROR + cfg.getName(), e);
        }
    }

    private void registerHandlers() {
        log.info("RFC 핸들러 등록 시작...");
        for (var func : sapRfcProperties.getFunctions()) {
            try {
                SapRfcServerHandler handler = (SapRfcServerHandler) applicationContext.getBean(func.getHandler());
                handlerMap.put(func.getName(), handler);
                log.info("핸들러 등록 완료: function={}, handler={}", func.getName(), func.getHandler());
            } catch (Exception e) {
                log.error("핸들러 등록 실패: function={}, handler={}, error={}", 
                    func.getName(), func.getHandler(), e.getMessage());
                throw e;
            }
        }
        log.info("RFC 핸들러 등록 완료: 총 {}개", handlerMap.size());
    }

    public void startRfcServer() {
        log.info("SAP RFC 서버 시작 시도...");
        log.info("JCoServer 설정: programId={}, gatewayHost={}, gatewayService={}, repositoryDestination={}, serverName={}",
            connectionProperties.getProgramId(),
            connectionProperties.getGatewayHost(),
            connectionProperties.getGatewayService(),
            connectionProperties.getRepositoryDestination(),
            connectionProperties.getServerName()
        );
        
        if (handlerMap.isEmpty()) {
            log.error("등록된 핸들러가 없습니다. 서버를 시작할 수 없습니다.");
            return;
        }

        int retryCount = 3;
        while (retryCount > 0) {
            try {
                JCoServer server = JCoServerFactory.getServer(connectionProperties.getServerName());
                DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
                
                for (Map.Entry<String, SapRfcServerHandler> entry : handlerMap.entrySet()) {
                    factory.registerHandler(entry.getKey(), entry.getValue());
                    log.info("핸들러 등록: function={}", entry.getKey());
                }
                
                server.setCallHandlerFactory(factory);
                server.start();
                log.info("SAP RFC 서버가 정상적으로 시작되었습니다. (programId={}, serverName={})", 
                    connectionProperties.getProgramId(), connectionProperties.getServerName());
                return;
            } catch (JCoException ex) {
                log.error("SAP RFC 서버 시작 실패 (남은 재시도: {}): {}", retryCount - 1, ex.getMessage(), ex);
                retryCount--;
                if (retryCount > 0) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        log.error("SAP RFC 서버 시작을 3회 시도했지만 실패하였습니다.");
    }

    // 필요시 파라미터 변환 메서드 등 추가
}