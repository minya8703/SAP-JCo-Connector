package jco.jcosaprfclink.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class TaxInvoiceResponse {
    private String invoiceNumber;
    private String status;
    private String message;
    private Date processDate;
} 