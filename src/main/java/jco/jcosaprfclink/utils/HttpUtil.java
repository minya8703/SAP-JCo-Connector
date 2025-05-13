package jco.jcosaprfclink.utils;

import lombok.extern.slf4j.Slf4j;
import jco.jcosaprfclink.exception.BusinessExceptionHandler;
import jco.jcosaprfclink.type.HttpMethod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static jco.jcosaprfclink.type.ErrorCode.*;

/**
 * HTTP 요청 유틸리티 클래스
 */
@Slf4j
public class HttpUtil {

    /**
     * 범용 HTTP 요청 메서드
     *
     * @param apiUrl 요청할 URL
     * @param method HTTP 메서드 (GET, POST, PUT, DELETE)
     * @param body 요청 본문 (POST, PUT에서 사용, GET에서는 무시됨)
     * @param token 인증 토큰 (필요 없는 경우 null 또는 빈 문자열)
     * @return 응답 본문
     */
    public static String sendHttpRequest(String apiUrl, HttpMethod method, String body, String token) {
        StringBuilder response = new StringBuilder();

        try {
            // 1. URL 및 HttpURLConnection 설정
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.name());
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            if (token != null && !token.isEmpty()) {
                connection.setRequestProperty("Authorization", "Token " + token);
            }
            connection.setDoOutput(method == HttpMethod.POST || method == HttpMethod.PUT);

            // 2. 요청 본문 전송 (POST, PUT 요청일 경우)
            if (connection.getDoOutput() && body != null && !body.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(body);
                }
            }

            // 3. 응답 코드 확인
            int responseCode = connection.getResponseCode();
            handleResponseCode(responseCode);

            // 4. 응답 데이터 읽기
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

        } catch (IOException e) {
            log.warn("❌ HTTP 요청 실패: {}", e.getLocalizedMessage());
            throw new BusinessExceptionHandler(IO_ERROR);
        }

        return response.toString();
    }

    /**
     * HTTP 응답 코드 처리
     */
    private static void handleResponseCode(int responseCode) {
        switch (responseCode) {
            case 200, 201 -> log.info("✅ 성공: 응답 코드 {}", responseCode);
            case 400 -> {
                log.warn("❌ 400 Bad Request: 요청 오류");
                throw new BusinessExceptionHandler(INVALID_REQUEST);
            }
            case 500 -> {
                log.warn("❌ 500 Internal Server Error: 서버 오류");
                throw new BusinessExceptionHandler(INTERNAL_SERVER_ERROR);
            }
            default -> log.info("응답 코드: {}", responseCode);
        }
    }
}