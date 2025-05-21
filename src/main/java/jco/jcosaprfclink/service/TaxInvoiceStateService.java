package jco.jcosaprfclink.service;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import jco.jcosaprfclink.config.aop.TimeTrace;
import jco.jcosaprfclink.domain.RfcDataRepository;
import jco.jcosaprfclink.domain.StateTaxinvoice;
import jco.jcosaprfclink.dto.StateInvoiceSap;
import jco.jcosaprfclink.exception.BusinessExceptionHandler;
import jco.jcosaprfclink.repository.TaxinvoiceStateRepository;
import jco.jcosaprfclink.type.ErrorCode;
import jco.jcosaprfclink.type.HttpMethod;
import jco.jcosaprfclink.utils.HttpUtil;
import jco.jcosaprfclink.utils.JsonPaserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxInvoiceStateService {
    private final StateInvoiceSap stateInvoiceSap;
    private final TaxinvoiceStateRepository taxinvoiceStateRepository;
    private final HttpUtil httpUtil;
    private final RfcDataRepository rfcDataRepository;

    @Value("${api.dev_url}")
    private String apiUrl;

    @TimeTrace
    public void taxinvoiceState(List<Map<String, Object>> taxinvoiceStateList, JCoFunction function) {
        try {
            // 1. 요청 데이터 변환 및 저장
            JSONArray requestData = convertAndSaveRequestData(taxinvoiceStateList);
            
            // 2. API 호출
            String response = httpUtil.sendHttpRequest(
                apiUrl + "/resultTaxInvoice",
                HttpMethod.POST,
                JsonPaserUtil.getStringFromJsonArray(requestData),
                null
            );
            log.info("API 응답: {}", response);

            // 3. 응답 데이터 처리 및 SAP 테이블 매핑
            processResponseAndMapToSap(response, function);

        } catch (Exception e) {
            log.error("세금계산서 상태 조회 중 오류 발생: {}", e.getMessage(), e);
            handleErrorResponse(taxinvoiceStateList, function);
            throw new BusinessExceptionHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleErrorResponse(List<Map<String, Object>> taxinvoiceStateList, JCoFunction function) {
        JCoTable outputTable = function.getTableParameterList().getTable("T_IF_ZTAXT020");
        outputTable.clear();
        
        for (Map<String, Object> request : taxinvoiceStateList) {
            outputTable.appendRow();
            outputTable.setValue("CORP_BIZ_NO", request.get("CORP_BIZ_NO"));
            outputTable.setValue("USER_ID", request.get("USER_ID"));
            outputTable.setValue("MGR_DOC_NO", request.get("MGR_DOC_NO"));
            outputTable.setValue("RESULT", "E");
            outputTable.setValue("ERR_CODE", "KH_COM_9999");
            outputTable.setValue("ERR_MSG", "API 서버 연결 실패");
        }
    }

    private JSONArray convertAndSaveRequestData(List<Map<String, Object>> taxinvocieStateList) {
        log.debug("요청 데이터 변환 시작");
        
        // JSON 변환
        JSONArray jsonArray = JsonPaserUtil.getJsonArrayFromMapList(taxinvocieStateList);
        log.info("요청 데이터: {}", JsonPaserUtil.getStringFromJsonArray(jsonArray));
        
        // DB에 요청 데이터 저장
        taxinvoiceStateRepository.saveAllAndFlush(stateInvoiceSap.toEntityList(taxinvocieStateList));
        
        return jsonArray;
    }

    private void processResponseAndMapToSap(String responseData, JCoFunction function) {
        log.debug("응답 데이터 처리 시작");
        
        JSONArray jsonArrayResult = JsonPaserUtil.getJsonArrayFromString(responseData);
        JCoTable outputTable = function.getTableParameterList().getTable("T_IF_ZTAXT020");
        outputTable.clear();
        List<StateTaxinvoice> taxinvocieStateResultList = new ArrayList<>();
        
        for (Object item : jsonArrayResult) {
            JSONObject jsonObject = (JSONObject) item;
            outputTable.appendRow();
            
            if (isSuccessResponse(jsonObject)) {
                mapSuccessResponseToTable(jsonObject, outputTable);
            } else {
                mapErrorResponseToTable(jsonObject, outputTable);
            }
            
            taxinvocieStateResultList.add(mapResponseToEntity(jsonObject));
        }
        
        taxinvoiceStateRepository.saveAllAndFlush(taxinvocieStateResultList);
        log.info("응답 데이터 처리 완료: {}", outputTable.toString());
    }

    private boolean isSuccessResponse(JSONObject jsonObject) {
        return "KH_COM_0000".equals(jsonObject.get("errCode"));
    }

    private void mapErrorResponseToTable(JSONObject jsonObject, JCoTable outputTable) {
        log.error("에러 발생: code={}, message={}", 
            jsonObject.get("errCode"), 
            jsonObject.get("errMsg")
        );
        
        outputTable.setValue("RESULT", "E");
        outputTable.setValue("ERR_CODE", jsonObject.get("errCode"));
        outputTable.setValue("ERR_MSG", jsonObject.get("errMsg"));
    }

    private void mapSuccessResponseToTable(JSONObject jsonObject, JCoTable outputTable) {
        outputTable.setValue("RESULT", "S");
        outputTable.setValue("SEND_DD", jsonObject.get("issueDd"));
        outputTable.setValue("APPR_NO", jsonObject.get("apprNo"));
        outputTable.setValue("ERR_CODE", jsonObject.get("errCode"));
        outputTable.setValue("ERR_MSG", "세금계산서 조회 완료");
        outputTable.setValue("DOC_STATE", jsonObject.get("docState"));
        outputTable.setValue("DOC_STATE_NM", jsonObject.get("docStateNm"));
    }

    private StateTaxinvoice mapResponseToEntity(JSONObject jsonObject) {
        return StateTaxinvoice.builder()
                .channel(jsonObject.get("channel").toString())
                .corpBizNo(jsonObject.get("corpBizNo").toString())
                .mgrdocNo(jsonObject.get("mgrdocNo").toString())
                .userId(jsonObject.get("userId").toString())
                .result(jsonObject.get("result").toString())
                .errCode(jsonObject.get("errCode").toString())
                .errMsg(jsonObject.get("errMsg").toString())
                .build();
    }
}
