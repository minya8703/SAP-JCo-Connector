package jco.jcosaprfclink.config.saprfc;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

import jco.jcosaprfclink.controller.TaxInvoiceStateController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static jco.jcosaprfclink.type.ErrorCode.NOT_CREATE_FILE_ERROR;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JCoConnectionManager {
    private static final String INVOICE_ISSUE_STATUS_FUN = "ZFI_TAXINV_STATUS_TO_WEB";

    static String SERVER_NAME1 = "SERVER";
    static String DESTINATION_NAME1 = "ABAP_AS_WITHOUT_POOL";

    @Value("${sap.connect.host}")
    private String ashost;
    @Value("${sap.connect.sysnr}")
    private String sysnr;
    @Value("${sap.connect.client}")
    private String client;
    @Value("${sap.connect.user}")
    private String user;
    @Value("${sap.connect.passwd}")
    private String passwd;
    @Value("${sap.connect.lang}")
    private String lang;

    @Value("${jco.server.connection_count}")
    private String conneCount;
    @Value("${jco.server.thread_count}")
    private String threadCount;
    @Value("${jco.server.progid}")
    private String progid;
    @Value("${jco.server.repository_destination}")
    private String repoDestination;
    @Value("${jco.server.gwserv}")
    private String gwserv;
    @Value("${jco.server.gwhost}")
    private String gwhost;

    @PostConstruct
    public void init() {
        Properties connectProperties = new Properties();
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, ashost); // SAP 호스트 정보
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, sysnr); // 인스턴스번호
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, client); // SAP 클라이언트
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, user); // SAP유저명
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, passwd); // SAP 패스워드
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, lang); // 언어
        createDataFile(DESTINATION_NAME1, "jcoDestination", connectProperties);

        Properties serverProperties = new Properties();
        serverProperties.setProperty(ServerDataProvider.JCO_GWHOST, gwhost);
        serverProperties.setProperty(ServerDataProvider.JCO_GWSERV, gwserv);
        serverProperties.setProperty(ServerDataProvider.JCO_PROGID, progid);
        serverProperties.setProperty(ServerDataProvider.JCO_REP_DEST, repoDestination);
        serverProperties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, conneCount);
        serverProperties.setProperty(ServerDataProvider.JCO_WORKER_THREAD_COUNT, threadCount);
        createDataFile(SERVER_NAME1, "jcoServer", serverProperties);
    }

    /**
     * 프로퍼티 파일 생성
     * 
     * @param name
     * @param suffix
     * @param properties
     */
    static void createDataFile(String name, String suffix, Properties properties) {
        File cfg = new File(name + "." + suffix);
        try (FileOutputStream fos = new FileOutputStream(cfg, false)) { // 항상 덮어쓰기
            properties.store(fos, "SAP JCo Configuration");
        } catch (Exception e) {
            throw new RuntimeException(NOT_CREATE_FILE_ERROR + cfg.getName(), e);
        }
    }

    public JCoDestination getConnectionSAP() {
        try {
            return JCoDestinationManager.getDestination(DESTINATION_NAME1);
        } catch (JCoException e) {
            log.error("SAP 연결 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SAP 연결에 실패했습니다. 설정을 확인하세요.", e);
        }
    }

    /**
     * RFC TCP/IP 연결
     */
    public void stepRfcServer() {
        log.info("SAP RFC 서버 시작 시도...");
        int retryCount = 3; // 최대 3번 재시도
        while (retryCount > 0) {
            try {
                JCoServer server = JCoServerFactory.getServer(SERVER_NAME1);
                JCoServerFunctionHandler stfcIssueStateConnectionHandler = taxInvoiceStateController();
                DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
                factory.registerHandler(INVOICE_ISSUE_STATUS_FUN, stfcIssueStateConnectionHandler);
                // factory.registerHandler(funName, handler);
                server.setCallHandlerFactory(factory);
                server.start();
                log.info("SAP RFC 서버가 정상적으로 시작되었습니다.");
                return; // 성공 시 종료
            } catch (JCoException ex) {
                log.error("SAP RFC 서버 시작 실패 (남은 재시도: {}): {}", retryCount - 1, ex.getMessage(), ex);
                retryCount--;
                try {
                    Thread.sleep(5000); // 5초 대기 후 재시도
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.error("SAP RFC 서버 시작을 3회 시도했지만 실패하였습니다.");
    }

    /**
     * import 파라미터 세팅
     *
     * @param inputMapList
     * @param inputTabName 입력 테이블명
     * @param jCoFunction  연결 jCoFunction
     * @throws ConversionException
     */
    public void setRFCImport(List<Map<String, Object>> inputMapList, String inputTabName, JCoFunction jCoFunction)
            throws ConversionException {
        JCoParameterList jCoParameterList = jCoFunction.getTableParameterList();
        JCoParameterFieldIterator jCoParameterFieldIterator = jCoParameterList.getParameterFieldIterator();

        JCoParameterField jCoParameterField;
        while (jCoParameterFieldIterator.hasNextField()) {
            jCoParameterField = jCoParameterFieldIterator.nextParameterField();
            if (jCoParameterField.isTable() && jCoParameterField.getName().equals(inputTabName)) {
                JCoTable jCoTable = jCoParameterField.getTable();
                JCoRecordFieldIterator fieldIter = jCoTable.getRecordFieldIterator();
                JCoRecordField field = null;

                for (Map<String, Object> inputMap : inputMapList) {
                    jCoTable.appendRow();
                    while (fieldIter.hasNextField()) {
                        field = fieldIter.nextRecordField();
                        field.setValue(inputMap.get(field.getName()));
                    }
                    fieldIter.reset();
                }

            }
        }
    }

    /**
     * export 파라미터 세팅
     *
     * @param outputTabName 출력 테이이블명
     * @param jCoFunction   연결 jCoFunction
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> setRFCExport(String outputTabName, JCoFunction jCoFunction) {
        List<Map<String, Object>> outMapList = new ArrayList<>();
        JCoTable jCoTable = jCoFunction.getTableParameterList().getTable(outputTabName);

        // jCoTable 데이터 loop
        JCoRecordMetaData metaData = jCoFunction.getTableParameterList().getTable(outputTabName).getRecordMetaData();
        for (int i = 0; i < jCoTable.getNumRows(); i++) {
            jCoTable.setRow(i);
            Map<String, Object> outMap = new LinkedHashMap<String, Object>();
            for (int j = 0; j < metaData.getFieldCount(); j++) {
                if (metaData.getType(j) == 2) {
                    outMap.put(metaData.getName(j), jCoTable.getFloat(metaData.getName(j)));
                } else {
                    outMap.put(metaData.getName(j), jCoTable.getString(metaData.getName(j)));
                }
            }
            outMapList.add(outMap);
        }
        return outMapList;
    }

    @Bean
    public TaxInvoiceStateController taxInvoiceStateController() {
        return new TaxInvoiceStateController();
    }
}