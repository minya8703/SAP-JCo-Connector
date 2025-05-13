package jco.jcosaprfclink.controller;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import jco.jcosaprfclink.config.aop.TimeTrace;
import jco.jcosaprfclink.config.saprfc.JCoConnectionManager;
import jco.jcosaprfclink.service.TaxInvoiceStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TaxInvoiceStateController implements JCoServerFunctionHandler {
    @Autowired
    private TaxInvoiceStateService taxInvoiceStateService;
    @Autowired
    private JCoConnectionManager jCoConnManager;
    @Override
    @TimeTrace
    public void handleRequest(JCoServerContext jCoServerContext, JCoFunction function) {
        List<Map<String, Object>> taxinvocieStateList = jCoConnManager.setRFCExport("T_IF_ZTAXT020", function);
        taxInvoiceStateService.taxinvoiceState(taxinvocieStateList, function);
    }
}
