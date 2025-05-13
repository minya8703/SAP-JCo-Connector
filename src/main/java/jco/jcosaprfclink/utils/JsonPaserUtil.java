package jco.jcosaprfclink.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JsonPaserUtil {

    /**
     * Map을 JSONString으로 변환
     *
     * @param map
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static String getJsonStringFromMap(Map<String, Object> map) {

        JSONObject json = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }

        return json.toJSONString();
    }

    /**
     * Map을 JSONObject으로 변환
     *
     * @param map
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getJsonObjectFromMap(Map<String, Object> map) {

        JSONObject json = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }

        return json;
    }

    /**
     * List<Map>을 JSONString으로 변환
     *
     * @param list
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static String getJsonStringFromList(List<Map<String, Object>> list) {

        JSONArray jsonArray = getJsonArrayFromList(list);

        return jsonArray.toJSONString();
    }

    /**
     * String을 JSONString으로 변환
     *
     * @param str
     * @return JSONArray
     */
    public static JSONArray getJsonArrayFromString(String str) {

        JSONArray jsonArray = new JSONArray();

        try {

            JSONParser parser = new JSONParser();
            jsonArray = (JSONArray)parser.parse(str);

        } catch (ParseException e) {
            log.error("error : ", e);
        }
        return jsonArray;
    }

    /**
     * List<Map>을 JSONString으로 변환
     *
     * @param list
     * @return String
     */
    public static JSONArray getJsonArrayFromList(List<Map<String, Object>> list) {

        JSONArray jsonArray = new JSONArray();

        for (Map<String, Object> map : list) {
            jsonArray.add(getJsonStringFromMap(map));
        }

        return jsonArray;
    }

    /**
     * String을 JSONObject를 변환
     *
     * @param jsonStr
     * @return jsonObject
     */
    public static JSONObject getJsonObjectFromString(String jsonStr) {

        JSONObject jsonObject = new JSONObject();

        JSONParser jsonParser = new JSONParser();

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonStr);

        } catch (ParseException e) {
            log.error("error : ", e);
        }

        return jsonObject;
    }

    /**
     * JSONObject를 Map<String, String>으로 변환
     *
     * @param jsonObject
     * @return map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObject) {

        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonObject.toJSONString(), Map.class);
        } catch (IOException e) {
            log.error("error : ", e);
        }
        return map;
    }

    /**
     * JSONArray를 List<Map<String, String>>으로 변환
     *
     * @param jsonArray
     * @return list
     */
    public static List<Map<String, Object>> getListMapFromJsonArray(JSONArray jsonArray) {

        List<Map<String, Object>> list = new ArrayList<>();

        if (jsonArray != null) {

            int jsonSize = jsonArray.size();

            for (int i = 0; i < jsonSize; i++) {

                Map<String, Object> map = getMapFromJsonObject((JSONObject) jsonArray.get(i));
                list.add(map);
            }
        }

        return list;
    }

    /**
     * JSONObject를 Map<String, String>으로 변환
     *
     * @param str
     * @return map
     */
    public static Map<String, String> getMapFromString(String str) {
        return Arrays.stream(str.replace("{","").replace("}","").split(","))
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(entry -> entry[0].trim(), entry -> entry[1].trim()));
    }
}
