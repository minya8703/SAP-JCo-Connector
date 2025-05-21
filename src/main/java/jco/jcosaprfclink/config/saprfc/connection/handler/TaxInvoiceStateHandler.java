package jco.jcosaprfclink.config.saprfc.connection.handler;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import jco.jcosaprfclink.domain.RfcDataRepository;
import jco.jcosaprfclink.service.TaxInvoiceStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TaxInvoiceStateHandler implements JCoServerFunctionHandler, SapRfcServerHandler {
    private final TaxInvoiceStateService taxInvoiceStateService;
    private final RfcDataRepository rfcDataRepository;
    
    public TaxInvoiceStateHandler(
            @Qualifier("rfcDataRepository") RfcDataRepository rfcDataRepository,
            TaxInvoiceStateService taxInvoiceStateService) {
        this.rfcDataRepository = rfcDataRepository;
        this.taxInvoiceStateService = taxInvoiceStateService;
    }
    
    @Override
    public String getFunctionName() {
        return "ZFI_TAXINV_STATUS_TO_WEB";
    }
    
    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function) {
        String handlerName = this.getClass().getSimpleName();
        String functionName = function.getName();
        log.info("[{}] handleRequest 시작 - function: {}", handlerName, functionName);
        
        try {
            // 1. SAP 테이블에서 데이터 추출
            JCoTable inputTable = function.getTableParameterList().getTable("T_IF_ZTAXT020");
            List<Map<String, Object>> taxinvoiceStateList = extractRequests(inputTable);
            log.debug("[{}] 추출된 요청 데이터: {}", handlerName, taxinvoiceStateList);
            
            // 2. 서비스 호출
            taxInvoiceStateService.taxinvoiceState(taxinvoiceStateList, function);
            log.info("[{}] handleRequest 정상 완료 - function: {}", handlerName, functionName);
        } catch (Exception e) {
            log.error("[{}] handleRequest 예외 발생 - function: {}", handlerName, functionName, e);
            throw new RuntimeException("SAP RFC 처리 중 예외 발생: " + e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> extractRequests(JCoTable inputTable) {
        List<Map<String, Object>> requests = new ArrayList<>();
        inputTable.firstRow();
        do {
            Map<String, Object> request = new HashMap<>();
            request.put("CORP_BIZ_NO", inputTable.getString("CORP_BIZ_NO"));
            request.put("USER_ID", inputTable.getString("USER_ID"));
            request.put("MGR_DOC_NO", inputTable.getString("MGR_DOC_NO"));
            requests.add(request);
        } while (inputTable.nextRow());
        return requests;
    }
} 