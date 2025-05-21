package jco.jcosaprfclink.config.saprfc.connection;

import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.ext.ServerDataEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyServerDataProvider implements ServerDataProvider {
    private final Map<String, Properties> propertiesMap = new HashMap<>();

    public void setServerProperties(String serverName, Properties properties) {
        propertiesMap.put(serverName, properties);
    }

    @Override
    public Properties getServerProperties(String serverName) {
        return propertiesMap.get(serverName);
    }

    @Override
    public void setServerDataEventListener(ServerDataEventListener listener) {
        // 이벤트 리스너 설정이 필요한 경우 구현
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }
} 