package jco.jcosaprfclink.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "RFC_DATA")
@Getter
@Setter
@NoArgsConstructor
public class RfcData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "FUNCTION_NAME", nullable = false)
    private String functionName;
    
    @Column(name = "DIRECTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;
    
    @Column(name = "DATA", columnDefinition = "TEXT")
    private String data;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum Direction {
        REQUEST, RESPONSE
    }
    
    public static RfcData ofRequest(String functionName, String data) {
        RfcData rfcData = new RfcData();
        rfcData.setFunctionName(functionName);
        rfcData.setDirection(Direction.REQUEST);
        rfcData.setData(data);
        return rfcData;
    }
    
    public static RfcData ofResponse(String functionName, String data) {
        RfcData rfcData = new RfcData();
        rfcData.setFunctionName(functionName);
        rfcData.setDirection(Direction.RESPONSE);
        rfcData.setData(data);
        return rfcData;
    }
} 