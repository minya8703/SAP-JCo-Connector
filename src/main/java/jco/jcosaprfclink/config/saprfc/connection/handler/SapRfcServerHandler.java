package jco.jcosaprfclink.config.saprfc.connection.handler;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.JCoFunction;

public interface SapRfcServerHandler {
    String getFunctionName();
    void handleRequest(JCoServerContext serverCtx, JCoFunction function) throws Exception;
} 