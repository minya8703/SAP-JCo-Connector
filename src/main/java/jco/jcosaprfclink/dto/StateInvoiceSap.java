package jco.jcosaprfclink.dto;

import jco.jcosaprfclink.domain.StateTaxinvoice;

import java.util.List;
import java.util.Map;

public interface StateInvoiceSap {
    List<StateTaxinvoice> toEntityList(List<Map<String, Object>> stateInvoiceData);
}
