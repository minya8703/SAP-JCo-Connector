package jco.jcosaprfclink.config.saprfc.connection;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.*;
import jco.jcosaprfclink.config.saprfc.connection.handler.SapRfcServerHandler;
import jco.jcosaprfclink.config.saprfc.connection.handler.TaxInvoiceStateHandler;
import jco.jcosaprfclink.config.saprfc.properties.SapRfcConnectionProperties;
import jco.jcosaprfclink.config.saprfc.properties.SapRfcFunctionProperties;
import jco.jcosaprfclink.config.saprfc.properties.SapRfcProperties;
import jco.jcosaprfclink.exception.BusinessExceptionHandler;
import jco.jcosaprfclink.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class JCoSapRfcConnection implements JCoServerTIDHandler, JCoServerFunctionHandler {
    private static final String DESTINATION_NAME = "ABAP_AS_WITHOUT_POOL";
    private static final String SERVER_NAME = "SAP_RFC_SERVER";

    private final SapRfcProperties sapRfcProperties;
    private final SapRfcConnectionProperties connectionProperties;
    private final ApplicationContext applicationContext;
    private JCoServer server;
    private final Map<String, SapRfcServerHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void initialize() {
        try {
            log.info("SAP RFC 서버 초기화 시작");
            
            // SAP 연결 설정
            Properties connectProperties = new Properties();
            connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, connectionProperties.getHost());
            connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, connectionProperties.getSysnr());
            connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, connectionProperties.getClient());
            connectProperties.setProperty(DestinationDataProvider.JCO_USER, connectionProperties.getUser());
            connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, connectionProperties.getPasswd());
            connectProperties.setProperty(DestinationDataProvider.JCO_LANG, connectionProperties.getLang());
            connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, connectionProperties.getPoolCapacity());
            connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, connectionProperties.getPeakLimit());

            // 서버 설정
            Properties serverProperties = new Properties();
            serverProperties.setProperty(ServerDataProvider.JCO_GWHOST, connectionProperties.getGatewayHost());
            serverProperties.setProperty(ServerDataProvider.JCO_GWSERV, connectionProperties.getGatewayService());
            serverProperties.setProperty(ServerDataProvider.JCO_PROGID, connectionProperties.getProgramId());
            serverProperties.setProperty(ServerDataProvider.JCO_REP_DEST, connectionProperties.getRepositoryDestination());
            serverProperties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, 
                String.valueOf(connectionProperties.getConnectionCount()));
            serverProperties.setProperty(ServerDataProvider.JCO_WORKER_THREAD_COUNT, 
                String.valueOf(connectionProperties.getThreadCount()));

            // 데이터 프로바이더 설정
            MyDestinationDataProvider provider = new MyDestinationDataProvider();
            provider.addDestinationProperties(DESTINATION_NAME, connectProperties);
            provider.addDestinationProperties(SERVER_NAME, serverProperties);
            
            // 서버 데이터 프로바이더 등록
            MyServerDataProvider serverProvider = new MyServerDataProvider();
            serverProvider.setServerProperties(SERVER_NAME, serverProperties);
            Environment.registerServerDataProvider(serverProvider);
            
            // 목적지 데이터 프로바이더 등록
            Environment.registerDestinationDataProvider(provider);

            // 서버 생성 및 시작
            server = JCoServerFactory.getServer(SERVER_NAME);
            server.setTIDHandler(this);
            
            // 핸들러 팩토리 설정
            DefaultServerHandlerFactory.FunctionHandlerFactory factory = 
                new DefaultServerHandlerFactory.FunctionHandlerFactory();
            
            // application.yml에서 설정된 핸들러 등록
            registerHandlers(factory);
            
            // 팩토리 설정
            server.setCallHandlerFactory(factory);
            
            // 서버 시작
            server.start();
            log.info("SAP RFC 서버가 성공적으로 시작되었습니다. Program ID: {}", 
                connectionProperties.getProgramId());

        } catch (Exception e) {
            log.error("SAP RFC 서버 초기화 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessExceptionHandler(ErrorCode.NOT_CREATE_SAP_SERVER_ERROR);
        }
    }

    private void registerHandlers(DefaultServerHandlerFactory.FunctionHandlerFactory factory) {
        log.info("RFC 핸들러 등록 시작");
        log.info("설정된 함수 목록: {}", sapRfcProperties.getFunctions());
        
        for (SapRfcFunctionProperties function : sapRfcProperties.getFunctions()) {
            try {
                String handlerName = function.getHandler();
                log.info("핸들러 등록 시도: function={}, handler={}", function.getName(), handlerName);
                
                // Bean 존재 여부 확인
                if (!applicationContext.containsBean(handlerName)) {
                    log.error("핸들러 Bean이 존재하지 않음: {}", handlerName);
                    log.error("등록된 Bean 목록: {}", 
                        String.join(", ", applicationContext.getBeanDefinitionNames()));
                    continue;
                }
                
                // Bean 타입 확인
                Class<?> handlerType = applicationContext.getType(handlerName);
                log.info("핸들러 타입: {}", handlerType.getName());
                
                // Bean 인스턴스 가져오기
                SapRfcServerHandler handler = applicationContext.getBean(handlerName, SapRfcServerHandler.class);
                log.info("핸들러 인스턴스 생성 성공: {}", handler.getClass().getName());
                
                // 핸들러 맵에 저장
                handlerMap.put(function.getName(), handler);
                log.info("핸들러 맵에 저장 완료: function={}, handler={}", 
                    function.getName(), handler.getClass().getSimpleName());
                
                // JCoServerFunctionHandler로 캐스팅하여 등록
                if (handler instanceof JCoServerFunctionHandler) {
                    factory.registerHandler(function.getName(), (JCoServerFunctionHandler) handler);
                    log.info("핸들러 등록 완료: function={}, handler={}, type={}", 
                        function.getName(), handlerName, handler.getClass().getName());
                } else {
                    log.error("핸들러가 JCoServerFunctionHandler를 구현하지 않음: {}, type={}", 
                        handlerName, handler.getClass().getName());
                }
            } catch (Exception e) {
                log.error("핸들러 등록 실패: function={}, handler={}, error={}", 
                    function.getName(), function.getHandler(), e.getMessage(), e);
            }
        }
        
        // 등록된 핸들러 목록 출력
        log.info("등록된 핸들러 목록:");
        handlerMap.forEach((functionName, handler) -> 
            log.info("- function: {}, handler: {}, type: {}", 
                functionName, handler.getClass().getSimpleName(), handler.getClass().getName()));
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function) {
        String functionName = function.getName();
        log.info("RFC 요청 수신: function={}, handlerMap={}", functionName, handlerMap.keySet());

        SapRfcServerHandler handler = handlerMap.get(functionName);
        if (handler == null) {
            log.error("핸들러를 찾을 수 없음: function={}, 등록된 핸들러={}", 
                functionName, handlerMap.keySet());
            throw new BusinessExceptionHandler(ErrorCode.NOT_FOUND_ERROR);
        }

        try {
            log.info("핸들러 호출 시작: function={}, handler={}", 
                functionName, handler.getClass().getName());
            handler.handleRequest(serverCtx, function);
            log.info("핸들러 호출 완료: function={}", functionName);
        } catch (Exception e) {
            log.error("핸들러 호출 중 오류 발생: function={}, handler={}, error={}", 
                functionName, handler.getClass().getName(), e.getMessage(), e);
            throw new BusinessExceptionHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean checkTID(JCoServerContext serverCtx, String tid) {
        log.info("TID 확인: {}", tid);
        return true;
    }

    @Override
    public void commit(JCoServerContext serverCtx, String tid) {
        log.info("TID 커밋: {}", tid);
    }

    @Override
    public void rollback(JCoServerContext serverCtx, String tid) {
        log.error("TID 롤백: {}", tid);
    }

    @Override
    public void confirmTID(JCoServerContext serverCtx, String tid) {
        log.info("TID 확인: {}", tid);
    }
}