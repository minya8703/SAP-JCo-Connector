package jco.jcosaprfclink.service;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import jco.jcosaprfclink.domain.RfcDataRepository;
import jco.jcosaprfclink.domain.StateTaxinvoice;
import jco.jcosaprfclink.dto.StateInvoiceSap;
import jco.jcosaprfclink.repository.TaxinvoiceStateRepository;
import jco.jcosaprfclink.utils.HttpUtil;
import jco.jcosaprfclink.utils.JsonPaserUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaxInvoiceStateService 테스트")
class TaxInvoiceStateServiceTest {

    @Mock
    private StateInvoiceSap stateInvoiceSap;

    @Mock
    private TaxinvoiceStateRepository taxinvoiceStateRepository;

    @Mock
    private HttpUtil httpUtil;

    @Mock
    private RfcDataRepository rfcDataRepository;

    @Mock
    private JCoFunction jCoFunction;

    @Mock
    private JCoTable jCoTable;

    @InjectMocks
    private TaxInvoiceStateService taxInvoiceStateService;

    private List<Map<String, Object>> testRequestData;
    private String testApiUrl = "http://localhost:8088";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taxInvoiceStateService, "apiUrl", testApiUrl);
        
        // 테스트 데이터 설정
        testRequestData = new ArrayList<>();
        Map<String, Object> requestItem = new HashMap<>();
        requestItem.put("CORP_BIZ_NO", "1234567890");
        requestItem.put("USER_ID", "testuser");
        requestItem.put("MGR_DOC_NO", "DOC001");
        testRequestData.add(requestItem);
    }

    @Test
    @DisplayName("성공적인 세금계산서 상태 조회 테스트")
    void testTaxinvoiceState_Success() {
        // Given
        String successResponse = createSuccessResponse();
        when(jCoFunction.getTableParameterList()).thenReturn(mock(JCoFunction.TableParameterList.class));
        when(jCoFunction.getTableParameterList().getTable("T_IF_ZTAXT020")).thenReturn(jCoTable);
        when(httpUtil.sendHttpRequest(anyString(), any(), anyString(), isNull()))
                .thenReturn(successResponse);
        when(stateInvoiceSap.toEntityList(any())).thenReturn(new ArrayList<>());

        // When
        assertDoesNotThrow(() -> taxInvoiceStateService.taxinvoiceState(testRequestData, jCoFunction));

        // Then
        verify(httpUtil).sendHttpRequest(
                eq(testApiUrl + "/resultTaxInvoice"),
                any(),
                anyString(),
                isNull()
        );
        verify(jCoTable, times(1)).clear();
        verify(jCoTable, times(1)).appendRow();
    }

    @Test
    @DisplayName("API 호출 실패 시 에러 처리 테스트")
    void testTaxinvoiceState_ApiFailure() {
        // Given
        when(jCoFunction.getTableParameterList()).thenReturn(mock(JCoFunction.TableParameterList.class));
        when(jCoFunction.getTableParameterList().getTable("T_IF_ZTAXT020")).thenReturn(jCoTable);
        when(httpUtil.sendHttpRequest(anyString(), any(), anyString(), isNull()))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                taxInvoiceStateService.taxinvoiceState(testRequestData, jCoFunction));

        verify(jCoTable, times(1)).clear();
        verify(jCoTable, times(1)).appendRow();
        verify(jCoTable).setValue("RESULT", "E");
        verify(jCoTable).setValue("ERR_CODE", "KH_COM_9999");
        verify(jCoTable).setValue("ERR_MSG", "API 서버 연결 실패");
    }

    @Test
    @DisplayName("성공 응답 데이터 매핑 테스트")
    void testMapSuccessResponseToTable() {
        // Given
        JSONObject successJson = new JSONObject();
        successJson.put("errCode", "KH_COM_0000");
        successJson.put("issueDd", "20241201");
        successJson.put("apprNo", "APPR001");
        successJson.put("docState", "APPROVED");
        successJson.put("docStateNm", "승인완료");

        // When
        taxInvoiceStateService.taxinvoiceState(testRequestData, jCoFunction);

        // Then - 실제로는 private 메서드이므로 간접적으로 검증
        verify(jCoTable, atLeastOnce()).setValue(anyString(), any());
    }

    @Test
    @DisplayName("에러 응답 데이터 매핑 테스트")
    void testMapErrorResponseToTable() {
        // Given
        String errorResponse = createErrorResponse();
        when(jCoFunction.getTableParameterList()).thenReturn(mock(JCoFunction.TableParameterList.class));
        when(jCoFunction.getTableParameterList().getTable("T_IF_ZTAXT020")).thenReturn(jCoTable);
        when(httpUtil.sendHttpRequest(anyString(), any(), anyString(), isNull()))
                .thenReturn(errorResponse);
        when(stateInvoiceSap.toEntityList(any())).thenReturn(new ArrayList<>());

        // When
        taxInvoiceStateService.taxinvoiceState(testRequestData, jCoFunction);

        // Then
        verify(jCoTable, atLeastOnce()).setValue("RESULT", "E");
        verify(jCoTable, atLeastOnce()).setValue(eq("ERR_CODE"), anyString());
        verify(jCoTable, atLeastOnce()).setValue(eq("ERR_MSG"), anyString());
    }

    private String createSuccessResponse() {
        JSONArray responseArray = new JSONArray();
        JSONObject successItem = new JSONObject();
        successItem.put("channel", "WEB");
        successItem.put("corpBizNo", "1234567890");
        successItem.put("mgrdocNo", "DOC001");
        successItem.put("userId", "testuser");
        successItem.put("result", "S");
        successItem.put("errCode", "KH_COM_0000");
        successItem.put("errMsg", "성공");
        successItem.put("issueDd", "20241201");
        successItem.put("apprNo", "APPR001");
        successItem.put("docState", "APPROVED");
        successItem.put("docStateNm", "승인완료");
        responseArray.add(successItem);
        
        return responseArray.toJSONString();
    }

    private String createErrorResponse() {
        JSONArray responseArray = new JSONArray();
        JSONObject errorItem = new JSONObject();
        errorItem.put("channel", "WEB");
        errorItem.put("corpBizNo", "1234567890");
        errorItem.put("mgrdocNo", "DOC001");
        errorItem.put("userId", "testuser");
        errorItem.put("result", "E");
        errorItem.put("errCode", "KH_COM_9999");
        errorItem.put("errMsg", "조회 실패");
        responseArray.add(errorItem);
        
        return responseArray.toJSONString();
    }
} 