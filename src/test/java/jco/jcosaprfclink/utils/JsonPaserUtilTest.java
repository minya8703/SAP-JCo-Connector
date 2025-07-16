package jco.jcosaprfclink.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonPaserUtil 테스트")
class JsonPaserUtilTest {

    @Test
    @DisplayName("Map 리스트를 JSON 배열로 변환 테스트")
    void testGetJsonArrayFromMapList() {
        // Given
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "John");
        item1.put("age", 30);
        mapList.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "Jane");
        item2.put("age", 25);
        mapList.add(item2);

        // When
        JSONArray result = JsonPaserUtil.getJsonArrayFromMapList(mapList);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        JSONObject firstItem = (JSONObject) result.get(0);
        assertEquals("John", firstItem.get("name"));
        assertEquals(30, firstItem.get("age"));
        
        JSONObject secondItem = (JSONObject) result.get(1);
        assertEquals("Jane", secondItem.get("name"));
        assertEquals(25, secondItem.get("age"));
    }

    @Test
    @DisplayName("빈 리스트를 JSON 배열로 변환 테스트")
    void testGetJsonArrayFromMapList_EmptyList() {
        // Given
        List<Map<String, Object>> emptyList = new ArrayList<>();

        // When
        JSONArray result = JsonPaserUtil.getJsonArrayFromMapList(emptyList);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("JSON 배열을 문자열로 변환 테스트")
    void testGetStringFromJsonArray() {
        // Given
        JSONArray jsonArray = new JSONArray();
        JSONObject item1 = new JSONObject();
        item1.put("id", 1);
        item1.put("name", "Test");
        jsonArray.add(item1);

        // When
        String result = JsonPaserUtil.getStringFromJsonArray(jsonArray);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("id"));
        assertTrue(result.contains("name"));
        assertTrue(result.contains("Test"));
    }

    @Test
    @DisplayName("빈 JSON 배열을 문자열로 변환 테스트")
    void testGetStringFromJsonArray_EmptyArray() {
        // Given
        JSONArray emptyArray = new JSONArray();

        // When
        String result = JsonPaserUtil.getStringFromJsonArray(emptyArray);

        // Then
        assertEquals("[]", result);
    }

    @Test
    @DisplayName("문자열을 JSON 배열로 변환 테스트")
    void testGetJsonArrayFromString() {
        // Given
        String jsonString = "[{\"id\":1,\"name\":\"Test\"},{\"id\":2,\"name\":\"Test2\"}]";

        // When
        JSONArray result = JsonPaserUtil.getJsonArrayFromString(jsonString);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        JSONObject firstItem = (JSONObject) result.get(0);
        assertEquals(1, firstItem.get("id"));
        assertEquals("Test", firstItem.get("name"));
        
        JSONObject secondItem = (JSONObject) result.get(1);
        assertEquals(2, secondItem.get("id"));
        assertEquals("Test2", secondItem.get("name"));
    }

    @Test
    @DisplayName("잘못된 JSON 문자열 변환 시 예외 발생 테스트")
    void testGetJsonArrayFromString_InvalidJson() {
        // Given
        String invalidJson = "invalid json string";

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                JsonPaserUtil.getJsonArrayFromString(invalidJson));
    }

    @Test
    @DisplayName("null 값이 포함된 Map 변환 테스트")
    void testGetJsonArrayFromMapList_WithNullValues() {
        // Given
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", "John");
        item.put("age", null);
        item.put("email", "john@example.com");
        mapList.add(item);

        // When
        JSONArray result = JsonPaserUtil.getJsonArrayFromMapList(mapList);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        JSONObject jsonItem = (JSONObject) result.get(0);
        assertEquals("John", jsonItem.get("name"));
        assertNull(jsonItem.get("age"));
        assertEquals("john@example.com", jsonItem.get("email"));
    }

    @Test
    @DisplayName("복잡한 중첩 구조 변환 테스트")
    void testGetJsonArrayFromMapList_ComplexStructure() {
        // Given
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("name", "John");
        
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "New York");
        item.put("address", address);
        
        List<String> hobbies = new ArrayList<>();
        hobbies.add("reading");
        hobbies.add("swimming");
        item.put("hobbies", hobbies);
        
        mapList.add(item);

        // When
        JSONArray result = JsonPaserUtil.getJsonArrayFromMapList(mapList);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        JSONObject jsonItem = (JSONObject) result.get(0);
        assertEquals(1, jsonItem.get("id"));
        assertEquals("John", jsonItem.get("name"));
        
        // 중첩된 객체 확인
        JSONObject addressObj = (JSONObject) jsonItem.get("address");
        assertEquals("123 Main St", addressObj.get("street"));
        assertEquals("New York", addressObj.get("city"));
        
        // 배열 확인
        JSONArray hobbiesArray = (JSONArray) jsonItem.get("hobbies");
        assertEquals(2, hobbiesArray.size());
        assertEquals("reading", hobbiesArray.get(0));
        assertEquals("swimming", hobbiesArray.get(1));
    }
} 