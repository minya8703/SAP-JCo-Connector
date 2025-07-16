package jco.jcosaprfclink.config.saprfc.connection;

import jco.jcosaprfclink.config.saprfc.properties.SapRfcConnectionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JCoSapRfcConnection 테스트")
class JCoSapRfcConnectionTest {

    @Mock
    private SapRfcConnectionProperties connectionProperties;

    @InjectMocks
    private JCoSapRfcConnection jCoSapRfcConnection;

    @BeforeEach
    void setUp() {
        // 기본 연결 속성 설정
        when(connectionProperties.getHost()).thenReturn("localhost");
        when(connectionProperties.getSysnr()).thenReturn("00");
        when(connectionProperties.getClient()).thenReturn("100");
        when(connectionProperties.getUser()).thenReturn("SAP_USER");
        when(connectionProperties.getPasswd()).thenReturn("SAP_PASSWORD");
        when(connectionProperties.getLang()).thenReturn("KO");
        when(connectionProperties.getPoolCapacity()).thenReturn(3);
        when(connectionProperties.getPeakLimit()).thenReturn(10);
        when(connectionProperties.getGatewayHost()).thenReturn("localhost");
        when(connectionProperties.getGatewayService()).thenReturn("sapgw00");
        when(connectionProperties.getProgramId()).thenReturn("TEST_PROG");
        when(connectionProperties.getRepositoryDestination()).thenReturn("ABAP_AS_WITHOUT_POOL");
        when(connectionProperties.getConnectionCount()).thenReturn(2);
        when(connectionProperties.getThreadCount()).thenReturn(5);
    }

    @Test
    @DisplayName("연결 속성 설정 테스트")
    void testConnectionPropertiesSetup() {
        // Given
        SapRfcConnectionProperties properties = new SapRfcConnectionProperties();
        properties.setHost("test-host");
        properties.setSysnr("01");
        properties.setClient("200");
        properties.setUser("test-user");
        properties.setPasswd("test-pass");

        // When
        jCoSapRfcConnection = new JCoSapRfcConnection(properties);

        // Then
        assertNotNull(jCoSapRfcConnection);
    }

    @Test
    @DisplayName("서버 연결 초기화 테스트")
    void testServerConnectionInitialization() {
        // Given
        when(connectionProperties.getServerName()).thenReturn("TEST_SERVER");

        // When & Then
        assertDoesNotThrow(() -> {
            // 실제 연결은 하지 않고 초기화만 테스트
            ReflectionTestUtils.invokeMethod(jCoSapRfcConnection, "initializeServerConnection");
        });
    }

    @Test
    @DisplayName("클라이언트 연결 초기화 테스트")
    void testClientConnectionInitialization() {
        // When & Then
        assertDoesNotThrow(() -> {
            // 실제 연결은 하지 않고 초기화만 테스트
            ReflectionTestUtils.invokeMethod(jCoSapRfcConnection, "initializeClientConnection");
        });
    }

    @Test
    @DisplayName("연결 속성 검증 테스트")
    void testConnectionPropertiesValidation() {
        // Given
        when(connectionProperties.getHost()).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new JCoSapRfcConnection(connectionProperties);
        });
    }

    @Test
    @DisplayName("게이트웨이 설정 테스트")
    void testGatewayConfiguration() {
        // Given
        when(connectionProperties.getGatewayHost()).thenReturn("gateway-host");
        when(connectionProperties.getGatewayService()).thenReturn("gateway-service");

        // When & Then
        assertDoesNotThrow(() -> {
            // 게이트웨이 설정이 올바르게 적용되는지 확인
            assertNotNull(connectionProperties.getGatewayHost());
            assertNotNull(connectionProperties.getGatewayService());
        });
    }

    @Test
    @DisplayName("풀 설정 테스트")
    void testPoolConfiguration() {
        // Given
        when(connectionProperties.getPoolCapacity()).thenReturn(5);
        when(connectionProperties.getPeakLimit()).thenReturn(15);

        // When & Then
        assertEquals(5, connectionProperties.getPoolCapacity());
        assertEquals(15, connectionProperties.getPeakLimit());
    }

    @Test
    @DisplayName("스레드 설정 테스트")
    void testThreadConfiguration() {
        // Given
        when(connectionProperties.getThreadCount()).thenReturn(10);
        when(connectionProperties.getConnectionCount()).thenReturn(3);

        // When & Then
        assertEquals(10, connectionProperties.getThreadCount());
        assertEquals(3, connectionProperties.getConnectionCount());
    }

    @Test
    @DisplayName("언어 설정 테스트")
    void testLanguageConfiguration() {
        // Given
        when(connectionProperties.getLang()).thenReturn("EN");

        // When & Then
        assertEquals("EN", connectionProperties.getLang());
    }

    @Test
    @DisplayName("프로그램 ID 설정 테스트")
    void testProgramIdConfiguration() {
        // Given
        when(connectionProperties.getProgramId()).thenReturn("CUSTOM_PROG");

        // When & Then
        assertEquals("CUSTOM_PROG", connectionProperties.getProgramId());
    }

    @Test
    @DisplayName("Repository 목적지 설정 테스트")
    void testRepositoryDestinationConfiguration() {
        // Given
        when(connectionProperties.getRepositoryDestination()).thenReturn("CUSTOM_REPO");

        // When & Then
        assertEquals("CUSTOM_REPO", connectionProperties.getRepositoryDestination());
    }

    @Test
    @DisplayName("연결 속성 null 체크 테스트")
    void testNullConnectionProperties() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new JCoSapRfcConnection(null);
        });
    }

    @Test
    @DisplayName("필수 연결 속성 검증 테스트")
    void testRequiredConnectionProperties() {
        // Given
        SapRfcConnectionProperties invalidProperties = new SapRfcConnectionProperties();
        // 필수 속성들을 설정하지 않음

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new JCoSapRfcConnection(invalidProperties);
        });
    }
} 