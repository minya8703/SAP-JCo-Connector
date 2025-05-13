package jco.jcosaprfclink.dto;

import jco.jcosaprfclink.domain.StateTaxinvoice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Component
public class StateInvoiceSapImpl implements StateInvoiceSap {
    List<StateTaxinvoice> taxinvocieCancelList;

    @Override
    public List<StateTaxinvoice> toEntityList(List<Map<String, Object>> stateInvoiceList) {
        for (Map<String, Object> map : stateInvoiceList) {
            taxinvocieCancelList.add(StateTaxinvoice.builder()
                    .channel("NARINER")
                    .corpBizNo(map.get("CORP_BIZ_NO").toString())
                    .userId(map.get("USER_ID").toString())
                    .mgrdocNo(map.get("MGR_DOC_NO").toString())
                    .build());
        }
        return taxinvocieCancelList;
    }
}
