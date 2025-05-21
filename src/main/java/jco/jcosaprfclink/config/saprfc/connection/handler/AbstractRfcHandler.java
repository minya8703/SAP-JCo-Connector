package jco.jcosaprfclink.config.saprfc.connection.handler;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import jco.jcosaprfclink.config.saprfc.util.DateUtils;
import jco.jcosaprfclink.domain.RfcData;
import jco.jcosaprfclink.domain.RfcDataRepository;
import jco.jcosaprfclink.exception.BusinessExceptionHandler;
import jco.jcosaprfclink.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRfcHandler implements SapRfcServerHandler, JCoServerFunctionHandler {
    
    protected final RfcDataRepository rfcDataRepository;
    
    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function) {
        String handlerName = this.getClass().getSimpleName();
        String functionName = function.getName();
        log.info("[{}] handleRequest 시작 - function: {}", handlerName, functionName);
        
        try {
            // 1. RFC 요청 데이터 DB 저장
            saveRequestData(function);
            
            // 2. 실제 처리
            log.info("[{}] doHandleRequest 시작 - function: {}", handlerName, functionName);
            doHandleRequest(function);
            log.info("[{}] doHandleRequest 완료 - function: {}", handlerName, functionName);
            
            // 3. RFC 응답 데이터 DB 저장
            if (function.getExportParameterList() != null || function.getTableParameterList() != null) {
                saveResponseData(function);
            } else {
                log.warn("[{}] 응답 데이터가 없습니다 - function: {}", handlerName, functionName);
            }
            
            log.info("[{}] handleRequest 정상 완료 - function: {}", handlerName, functionName);
        } catch (Exception e) {
            log.error("[{}] handleRequest 예외 발생 - function: {}, error: {}", 
                handlerName, functionName, e.getMessage(), e);
            throw new BusinessExceptionHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    protected abstract void doHandleRequest(JCoFunction function) throws Exception;
    
    protected void saveRequestData(JCoFunction function) {
        try {
            String functionName = getFunctionName();
            String requestData = function.toXML();
            
            RfcData rfcData = RfcData.ofRequest(functionName, requestData);
            rfcDataRepository.save(rfcData);
            
            log.debug("[{}] RFC 요청 데이터 저장 완료 - function: {}", 
                this.getClass().getSimpleName(), functionName);
        } catch (Exception e) {
            log.error("[{}] RFC 요청 데이터 저장 실패 - function: {}, error: {}", 
                this.getClass().getSimpleName(), getFunctionName(), e.getMessage(), e);
        }
    }
    
    protected void saveResponseData(JCoFunction function) {
        try {
            String functionName = getFunctionName();
            String responseData = function.toXML();
            
            RfcData rfcData = RfcData.ofResponse(functionName, responseData);
            rfcDataRepository.save(rfcData);
            
            log.debug("[{}] RFC 응답 데이터 저장 완료 - function: {}", 
                this.getClass().getSimpleName(), functionName);
        } catch (Exception e) {
            log.error("[{}] RFC 응답 데이터 저장 실패 - function: {}, error: {}", 
                this.getClass().getSimpleName(), getFunctionName(), e.getMessage(), e);
        }
    }
    
    protected Date convertSapDate(String sapDate) {
        return DateUtils.parseSapDate(sapDate);
    }
    
    protected String formatSapDate(Date date) {
        return DateUtils.formatSapDate(date);
    }
} 