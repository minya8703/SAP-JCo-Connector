package jco.jcosaprfclink.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "s_state_taxinvoice")
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class StateTaxinvoice implements Serializable {

    @Id
    @NotNull
    private String mgrdocNo; // 문서관리번호

    @Column
    @NotNull
    private String channel; // 채널
    @NotNull
    private String corpBizNo; // 사업자번호
    @NotNull
    private String userId; // 아이디
    @Column(length = 24)
    private String apprNo;

    @Column(nullable = false, length = 8)
    private String  sendDd;
    @Column(length = 20)
    private String errCode;
    @Column(length = 200)
    private String errMsg;
    @Column(length = 10)
    private String result;
    @Column(nullable = false, length = 2)
    private String docType; // 문서구분
    @Column(length = 10)
    private String docTypeNm; // 문서구분명

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;


}
