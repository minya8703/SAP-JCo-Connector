package jco.jcosaprfclink.service;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import jco.jcosaprfclink.config.aop.TimeTrace;
import jco.jcosaprfclink.domain.StateTaxinvoice;
import jco.jcosaprfclink.dto.StateInvoiceSap;
import jco.jcosaprfclink.repository.TaxinvoiceStateRepository;
import jco.jcosaprfclink.type.HttpMethod;
import jco.jcosaprfclink.utils.HttpUtil;
import jco.jcosaprfclink.utils.JsonPaserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxInvoiceStateService {
    @Autowired
    StateInvoiceSap stateInvoiceSap;

    @Autowired
    TaxinvoiceStateRepository taxinvoiceStateRepository;

    @Value("${api.dev_url}")
    private String apiUrl;

    @TimeTrace
    public void taxinvoiceState(List<Map<String, Object>> taxinvocieStateList, JCoFunction jCofunction) {
        log.info("Service handleRequest, 'taxinvoiceState'");
        JSONObject info;
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> taxinvoiceStateMap : taxinvocieStateList) {
            info = JsonPaserUtil.getJsonObjectFromMap(taxinvoiceStateMap);
            jsonArray.add(info);
        }
        System.out.println("jsonArray result : " + jsonArray.toJSONString());
        taxinvoiceStateRepository.saveAllAndFlush(stateInvoiceSap.toEntityList(taxinvocieStateList));
//        String result = HttpUtil.callConversionApi(jsonArray.toJSONString(), apiUrl + "/resultTaxInvoice");
        String result = HttpUtil.sendHttpRequest(apiUrl + "/resultTaxInvoice", HttpMethod.POST, jsonArray.toJSONString(), null );
        JSONArray jsonArrayResult = JsonPaserUtil.getJsonArrayFromString(result);
        JCoTable jCoTable = jCofunction.getTableParameterList().getTable("T_IF_ZTAXT020");
        List<StateTaxinvoice> taxinvocieStateResultList = new ArrayList<>();
        for (int i = 0; i < jsonArrayResult.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArrayResult.get(i);
            jCoTable.setRow(i);
            if (!jsonObject.get("errCode").equals("KH_COM_0000")) { // 호출 실패
                log.error("에러 오류 코드 : " + jsonObject.get("errCode"));
                jCoTable.setValue("RESULT", jsonObject.get("result"));
                jCoTable.setValue("ERR_CODE", jsonObject.get("errCode"));
                jCoTable.setValue("ERR_MSG", jsonObject.get("errMsg"));
            } else {
                jCoTable.setValue("RESULT", jsonObject.get("result"));
                jCoTable.setValue("SEND_DD", jsonObject.get("issueDd"));
                jCoTable.setValue("APPR_NO", jsonObject.get("apprNo"));
                jCoTable.setValue("ERR_CODE", jsonObject.get("errCode"));
                jCoTable.setValue("ERR_MSG", "세금계산서 조회 완료");

                jCoTable.setValue("DOC_STATE", jsonObject.get("docState"));
                jCoTable.setValue("DOC_STATE_NM", jsonObject.get("docStateNm"));
            }
            taxinvocieStateResultList.add(StateTaxinvoice.builder()
                    .channel(jsonObject.get("channel").toString())
                    .corpBizNo(jsonObject.get("corpBizNo").toString())
                    .mgrdocNo(jsonObject.get("mgrdocNo").toString())
                    .userId(jsonObject.get("userId").toString())
                    .result(jsonObject.get("result").toString())
                    .errCode(jsonObject.get("errCode").toString())
                    .errMsg(jsonObject.get("errMsg").toString())
                    .build());
            taxinvoiceStateRepository.saveAllAndFlush(taxinvocieStateResultList);
        }
        log.info(jCoTable.toString());
        log.info("Service handleRequest, 'taxinvoiceState'");
    }
}
