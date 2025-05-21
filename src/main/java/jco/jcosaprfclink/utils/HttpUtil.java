package jco.jcosaprfclink.utils;

import lombok.extern.slf4j.Slf4j;
import jco.jcosaprfclink.exception.BusinessExceptionHandler;
import jco.jcosaprfclink.type.ErrorCode;
import jco.jcosaprfclink.type.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 요청 유틸리티 클래스
 */
@Slf4j
@Component
public class HttpUtil {
    @Value("${http.connection.timeout:5000}")
    private int connectionTimeout;

    @Value("${http.read.timeout:5000}")
    private int readTimeout;

    @Value("${http.max.retries:3}")
    private int maxRetries;

    @Value("${http.retry.delay:1000}")
    private int retryDelay;

    /**
     * 범용 HTTP 요청 메서드
     *
     * @param apiUrl 요청할 URL
     * @param method HTTP 메서드 (GET, POST, PUT, DELETE)
     * @param body 요청 본문 (POST, PUT에서 사용, GET에서는 무시됨)
     * @param token 인증 토큰 (필요 없는 경우 null 또는 빈 문자열)
     * @return 응답 본문
     */
    public String sendHttpRequest(String apiUrl, HttpMethod method, String body, String token) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                return executeHttpRequest(apiUrl, method, body, token);
            } catch (IOException e) {
                lastException = e;
                retryCount++;
                
                if (retryCount < maxRetries) {
                    log.warn("HTTP 요청 실패 (재시도 {}/{}): {}", 
                        retryCount, maxRetries, e.getLocalizedMessage());
                    sleep(retryDelay);
                }
            }
        }

        log.error("HTTP 요청 최종 실패: {}", lastException.getLocalizedMessage());
        throw new BusinessExceptionHandler(ErrorCode.IO_ERROR);
    }

    private String executeHttpRequest(String apiUrl, HttpMethod method, String body, String token) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = createConnection(apiUrl, method, token);
            sendRequestBody(connection, body);
            return readResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection createConnection(String apiUrl, HttpMethod method, String token) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // 기본 설정
        connection.setRequestMethod(method.name());
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setDoOutput(method == HttpMethod.POST || method == HttpMethod.PUT);
        
        // 토큰 설정
        if (token != null && !token.isEmpty()) {
            connection.setRequestProperty("Authorization", "Token " + token);
        }
        
        return connection;
    }

    private void sendRequestBody(HttpURLConnection connection, String body) throws IOException {
        if (connection.getDoOutput() && body != null && !body.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(body);
                writer.flush();
            }
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        handleResponseCode(responseCode);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream(),
                    StandardCharsets.UTF_8))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * HTTP 응답 코드 처리
     */
    private void handleResponseCode(int responseCode) {
        switch (responseCode) {
            case 200, 201 -> log.info("✅ 성공: 응답 코드 {}", responseCode);
            case 400 -> {
                log.warn("❌ 400 Bad Request: 요청 오류");
                throw new BusinessExceptionHandler(ErrorCode.BAD_REQUEST_ERROR);
            }
            case 401 -> {
                log.warn("❌ 401 Unauthorized: 인증 실패");
                throw new BusinessExceptionHandler(ErrorCode.FORBIDDEN_ERROR);
            }
            case 403 -> {
                log.warn("❌ 403 Forbidden: 접근 권한 없음");
                throw new BusinessExceptionHandler(ErrorCode.FORBIDDEN_ERROR);
            }
            case 404 -> {
                log.warn("❌ 404 Not Found: 리소스를 찾을 수 없음");
                throw new BusinessExceptionHandler(ErrorCode.NOT_FOUND_ERROR);
            }
            case 500 -> {
                log.warn("❌ 500 Internal Server Error: 서버 오류");
                throw new BusinessExceptionHandler(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            default -> log.info("응답 코드: {}", responseCode);
        }
    }

    private void sleep(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessExceptionHandler(ErrorCode.IO_ERROR);
        }
    }
}