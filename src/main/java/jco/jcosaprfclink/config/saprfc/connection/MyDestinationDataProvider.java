package jco.jcosaprfclink.config.saprfc.connection;

import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyDestinationDataProvider implements DestinationDataProvider {
    private final Map<String, Properties> propertiesMap = new HashMap<>();

    public void addDestinationProperties(String destinationName, Properties properties) {
        propertiesMap.put(destinationName, properties);
    }

    @Override
    public Properties getDestinationProperties(String destinationName) {
        return propertiesMap.get(destinationName);
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener listener) {
        // 이벤트 리스너 설정이 필요한 경우 구현
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }
} 