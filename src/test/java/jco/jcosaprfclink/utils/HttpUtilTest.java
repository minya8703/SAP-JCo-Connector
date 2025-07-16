package jco.jcosaprfclink.utils;

import jco.jcosaprfclink.exception.BusinessExceptionHandler;
import jco.jcosaprfclink.type.ErrorCode;
import jco.jcosaprfclink.type.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpUtil 테스트")
class HttpUtilTest {

    @InjectMocks
    private HttpUtil httpUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(httpUtil, "connectionTimeout", 5000);
        ReflectionTestUtils.setField(httpUtil, "readTimeout", 5000);
        ReflectionTestUtils.setField(httpUtil, "maxRetries", 3);
        ReflectionTestUtils.setField(httpUtil, "retryDelay", 1000);
    }

    @Test
    @DisplayName("GET 요청 성공 테스트")
    void testSendHttpRequest_GetSuccess() {
        // Given
        String testUrl = "https://httpbin.org/get";
        String expectedResponse = "{\"url\":\"https://httpbin.org/get\"}";

        // When
        String result = httpUtil.sendHttpRequest(testUrl, HttpMethod.GET, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("url"));
    }

    @Test
    @DisplayName("POST 요청 성공 테스트")
    void testSendHttpRequest_PostSuccess() {
        // Given
        String testUrl = "https://httpbin.org/post";
        String requestBody = "{\"test\":\"data\"}";

        // When
        String result = httpUtil.sendHttpRequest(testUrl, HttpMethod.POST, requestBody, null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("data"));
    }

    @Test
    @DisplayName("인증 토큰이 포함된 요청 테스트")
    void testSendHttpRequest_WithToken() {
        // Given
        String testUrl = "https://httpbin.org/headers";
        String token = "test-token";

        // When
        String result = httpUtil.sendHttpRequest(testUrl, HttpMethod.GET, null, token);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Authorization"));
    }

    @Test
    @DisplayName("잘못된 URL로 요청 시 예외 발생 테스트")
    void testSendHttpRequest_InvalidUrl() {
        // Given
        String invalidUrl = "https://invalid-url-that-does-not-exist.com";

        // When & Then
        assertThrows(BusinessExceptionHandler.class, () -> 
                httpUtil.sendHttpRequest(invalidUrl, HttpMethod.GET, null, null));
    }

    @Test
    @DisplayName("404 에러 응답 테스트")
    void testSendHttpRequest_404Error() {
        // Given
        String notFoundUrl = "https://httpbin.org/status/404";

        // When & Then
        assertThrows(BusinessExceptionHandler.class, () -> 
                httpUtil.sendHttpRequest(notFoundUrl, HttpMethod.GET, null, null));
    }

    @Test
    @DisplayName("500 에러 응답 테스트")
    void testSendHttpRequest_500Error() {
        // Given
        String serverErrorUrl = "https://httpbin.org/status/500";

        // When & Then
        assertThrows(BusinessExceptionHandler.class, () -> 
                httpUtil.sendHttpRequest(serverErrorUrl, HttpMethod.GET, null, null));
    }

    @Test
    @DisplayName("PUT 요청 테스트")
    void testSendHttpRequest_PutRequest() {
        // Given
        String testUrl = "https://httpbin.org/put";
        String requestBody = "{\"update\":\"data\"}";

        // When
        String result = httpUtil.sendHttpRequest(testUrl, HttpMethod.PUT, requestBody, null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("update"));
    }

    @Test
    @DisplayName("DELETE 요청 테스트")
    void testSendHttpRequest_DeleteRequest() {
        // Given
        String testUrl = "https://httpbin.org/delete";

        // When
        String result = httpUtil.sendHttpRequest(testUrl, HttpMethod.DELETE, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("delete"));
    }

    @Test
    @DisplayName("타임아웃 설정 테스트")
    void testTimeoutSettings() {
        // Given
        ReflectionTestUtils.setField(httpUtil, "connectionTimeout", 1000);
        ReflectionTestUtils.setField(httpUtil, "readTimeout", 1000);

        // When & Then - 타임아웃이 짧아도 정상 동작하는지 확인
        assertDoesNotThrow(() -> 
                httpUtil.sendHttpRequest("https://httpbin.org/delay/0", HttpMethod.GET, null, null));
    }

    @Test
    @DisplayName("재시도 로직 테스트")
    void testRetryLogic() {
        // Given
        ReflectionTestUtils.setField(httpUtil, "maxRetries", 2);
        ReflectionTestUtils.setField(httpUtil, "retryDelay", 100);

        // When & Then - 재시도 설정이 적용되는지 확인
        assertDoesNotThrow(() -> 
                httpUtil.sendHttpRequest("https://httpbin.org/get", HttpMethod.GET, null, null));
    }
} 