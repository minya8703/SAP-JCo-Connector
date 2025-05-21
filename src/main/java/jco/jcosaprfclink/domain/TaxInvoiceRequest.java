package jco.jcosaprfclink.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class TaxInvoiceRequest {
    private String invoiceNumber;
    private String companyCode;
    private Date requestDate;
} 