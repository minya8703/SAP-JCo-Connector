package jco.jcosaprfclink.exception;

import jco.jcosaprfclink.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessExceptionHandler 테스트")
class BusinessExceptionHandlerTest {

    @Test
    @DisplayName("ErrorCode로 예외 생성 테스트")
    void testConstructorWithErrorCode() {
        // Given
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 메시지로 예외 생성 테스트")
    void testConstructorWithErrorCodeAndMessage() {
        // Given
        ErrorCode errorCode = ErrorCode.BAD_REQUEST_ERROR;
        String customMessage = "사용자 정의 오류 메시지";

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode, customMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 원인 예외로 예외 생성 테스트")
    void testConstructorWithErrorCodeAndCause() {
        // Given
        ErrorCode errorCode = ErrorCode.IO_ERROR;
        Throwable cause = new RuntimeException("원인 예외");

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode, cause);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("ErrorCode, 메시지, 원인 예외로 예외 생성 테스트")
    void testConstructorWithErrorCodeMessageAndCause() {
        // Given
        ErrorCode errorCode = ErrorCode.NOT_FOUND_ERROR;
        String customMessage = "리소스를 찾을 수 없습니다";
        Throwable cause = new RuntimeException("원인 예외");

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode, customMessage, cause);

        // Then
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(customMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("다양한 ErrorCode 테스트")
    void testVariousErrorCodes() {
        // Given & When & Then
        ErrorCode[] errorCodes = {
            ErrorCode.BAD_REQUEST_ERROR,
            ErrorCode.FORBIDDEN_ERROR,
            ErrorCode.NOT_FOUND_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR,
            ErrorCode.IO_ERROR
        };

        for (ErrorCode errorCode : errorCodes) {
            BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode);
            assertEquals(errorCode, exception.getErrorCode());
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("예외 메시지 검증 테스트")
    void testExceptionMessage() {
        // Given
        ErrorCode errorCode = ErrorCode.BAD_REQUEST_ERROR;
        String customMessage = "잘못된 요청입니다";

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode, customMessage);

        // Then
        assertEquals(customMessage, exception.getMessage());
        assertNotEquals(errorCode.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예외 원인 검증 테스트")
    void testExceptionCause() {
        // Given
        ErrorCode errorCode = ErrorCode.IO_ERROR;
        RuntimeException cause = new RuntimeException("테스트 원인");

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode, cause);

        // Then
        assertEquals(cause, exception.getCause());
        assertEquals("테스트 원인", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("예외 스택 트레이스 테스트")
    void testExceptionStackTrace() {
        // Given
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("예외 상속 관계 테스트")
    void testExceptionInheritance() {
        // Given
        ErrorCode errorCode = ErrorCode.BAD_REQUEST_ERROR;

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode);

        // Then
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("null ErrorCode 처리 테스트")
    void testNullErrorCode() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new BusinessExceptionHandler(null);
        });
    }

    @Test
    @DisplayName("예외 정보 출력 테스트")
    void testExceptionToString() {
        // Given
        ErrorCode errorCode = ErrorCode.NOT_FOUND_ERROR;
        String customMessage = "사용자 정의 메시지";

        // When
        BusinessExceptionHandler exception = new BusinessExceptionHandler(errorCode, customMessage);
        String exceptionString = exception.toString();

        // Then
        assertNotNull(exceptionString);
        assertTrue(exceptionString.contains(customMessage));
        assertTrue(exceptionString.contains(errorCode.name()));
    }
} 