package community.theprojects.fairy.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class JsonSerializer {
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private static SerializationConfig config = new SerializationConfig();

    public static class SerializationConfig {
        private boolean includeNullValues = true;
        private boolean includeTransientFields = false;
        private boolean useCustomDateFormat = true;
        private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        private final Set<String> excludedFields = new HashSet<>();
        private final Set<Class<?>> excludedTypes = new HashSet<>();
        private boolean prettyPrint = false;
        private int indentFactor = 2;

        public boolean isIncludeNullValues() {
            return includeNullValues;
        }

        public void setIncludeNullValues(boolean includeNullValues) {
            this.includeNullValues = includeNullValues;
        }

        public boolean isIncludeTransientFields() {
            return includeTransientFields;
        }

        public void setIncludeTransientFields(boolean includeTransientFields) {
            this.includeTransientFields = includeTransientFields;
        }

        public boolean isUseCustomDateFormat() {
            return useCustomDateFormat;
        }

        public void setUseCustomDateFormat(boolean useCustomDateFormat) {
            this.useCustomDateFormat = useCustomDateFormat;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public Set<String> getExcludedFields() {
            return excludedFields;
        }

        public Set<Class<?>> getExcludedTypes() {
            return excludedTypes;
        }

        public boolean isPrettyPrint() {
            return prettyPrint;
        }

        public void setPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
        }

        public int getIndentFactor() {
            return indentFactor;
        }

        public void setIndentFactor(int indentFactor) {
            this.indentFactor = indentFactor;
        }

        public void excludeField(String fieldName) {
            excludedFields.add(fieldName);
        }

        public void excludeType(Class<?> type) {
            excludedTypes.add(type);
        }
    }

    public static void setConfig(SerializationConfig newConfig) {
        config = newConfig;
    }

    public static SerializationConfig getConfig() {
        return config;
    }

    public static String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }
        Object jsonValue = serializeToJsonValue(obj);
        if (jsonValue instanceof JSONObject jsonObject) {
            return config.isPrettyPrint() ? jsonObject.toString(config.getIndentFactor()) : jsonObject.toString();
        }
        if (jsonValue instanceof JSONArray jsonArray) {
            return config.isPrettyPrint() ? jsonArray.toString(config.getIndentFactor()) : jsonArray.toString();
        }
        return jsonValue.toString();
    }

    public static JSONObject serializeToJSONObject(Object obj) {
        if (obj == null) {
            return null;
        }
        Object result = serializeToJsonValue(obj);
        if (result instanceof JSONObject) {
            return (JSONObject) result;
        }
        JSONObject wrapper = new JSONObject();
        wrapper.put("value", result);
        return wrapper;
    }

    public static JSONArray serializeToJSONArray(Collection<?> collection) {
        if (collection == null) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        for (Object item : collection) {
            jsonArray.put(serializeToJsonValue(item));
        }
        return jsonArray;
    }

    public static JSONArray serializeArrayToJSONArray(Object array) {
        if (array == null || !array.getClass().isArray()) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            jsonArray.put(serializeToJsonValue(element));
        }
        return jsonArray;
    }

    static Object serializeToJsonValue(Object obj) {
        if (obj == null) {
            return JSONObject.NULL;
        }
        Class<?> clazz = obj.getClass();
        if (obj instanceof JSONObject || obj instanceof JSONArray) {
            return obj;
        }
        if (isPrimitiveOrWrapper(clazz) || obj instanceof String) {
            return obj;
        }
        if (config.getExcludedTypes().contains(clazz)) {
            return JSONObject.NULL;
        }
        if (clazz.isArray()) {
            return serializeArrayToJsonValue(obj);
        }
        return switch (obj) {
            case Collection collection -> serializeCollectionToJsonValue(collection);
            case Map map -> serializeMapToJsonValue(map);
            case Enum anEnum -> anEnum.name();
            case Date date -> serializeDateValue(date);
            case LocalDateTime localDateTime -> serializeLocalDateTimeValue(localDateTime);
            case LocalDate ignored -> obj.toString();
            case LocalTime ignored -> obj.toString();
            case Instant instant -> instant.toString();
            default -> serializeObjectToJsonObject(obj);
        };
    }

    private static JSONObject serializeObjectToJsonObject(Object obj) {
        JSONObject jsonObject = new JSONObject();
        Class<?> clazz = obj.getClass();
        try {
            Field[] fields = getFields(clazz);
            for (Field field : fields) {
                if (shouldSkipField(field)) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(obj);
                String fieldName = getFieldName(field);
                if (value == null && !config.isIncludeNullValues()) {
                    continue;
                }
                Object serializedValue = serializeToJsonValue(value);
                jsonObject.put(fieldName, serializedValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed for class: " + clazz.getName(), e);
        }
        return jsonObject;
    }

    private static JSONArray serializeArrayToJsonValue(Object array) {
        JSONArray jsonArray = new JSONArray();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            jsonArray.put(serializeToJsonValue(element));
        }
        return jsonArray;
    }

    private static JSONArray serializeCollectionToJsonValue(Collection<?> collection) {
        JSONArray jsonArray = new JSONArray();
        for (Object element : collection) {
            jsonArray.put(serializeToJsonValue(element));
        }
        return jsonArray;
    }

    private static JSONObject serializeMapToJsonValue(Map<?, ?> map) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = serializeToJsonValue(entry.getValue());
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    private static Object serializeDateValue(Date date) {
        if (config.isUseCustomDateFormat()) {
            return DateTimeFormatter.ofPattern(config.getDateFormat())
                    .format(date.toInstant().atZone(ZoneOffset.UTC));
        }
        return date.getTime();
    }

    private static Object serializeLocalDateTimeValue(LocalDateTime dateTime) {
        if (config.isUseCustomDateFormat()) {
            return DateTimeFormatter.ofPattern(config.getDateFormat())
                    .format(dateTime.atZone(ZoneOffset.UTC));
        }
        return dateTime.toString();
    }

    public static <T> T deserialize(String jsonString, Class<T> targetClass) {
        if (jsonString == null || "null".equals(jsonString)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return deserializeFromJSONObject(jsonObject, targetClass);
        } catch (Exception e) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                return deserializeFromJSONArray(jsonArray, targetClass);
            } catch (Exception e2) {
                // Only try to deserialize as primitive if it's actually a primitive type
                if (isPrimitiveOrWrapper(targetClass) || targetClass == String.class) {
                    return deserializePrimitiveValue(jsonString, targetClass);
                } else {
                    // For complex objects, throw the original JSON parsing exception
                    throw new RuntimeException("Failed to parse JSON for class: " + targetClass.getName() +
                            ". JSON content: " + jsonString, e);
                }
            }
        }
    }

    public static <T> List<T> deserializeList(String jsonString, Class<T> elementClass) {
        if (jsonString == null || "null".equals(jsonString)) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(jsonString);
        List<T> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.get(i);
            T deserializedElement;
            if (element instanceof JSONObject) {
                deserializedElement = deserializeFromJSONObject((JSONObject) element, elementClass);
            } else {
                deserializedElement = deserializeValue(element, elementClass, elementClass);
            }
            result.add(deserializedElement);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromJSONObject(JSONObject jsonObject, Class<T> targetClass) {
        if (jsonObject == null) {
            return null;
        }
        try {
            if (targetClass == JSONObject.class) {
                return (T) jsonObject;
            }
            if (Map.class.isAssignableFrom(targetClass)) {
                return (T) deserializeToMap(jsonObject, targetClass);
            }
            T instance = createInstance(targetClass);
            Field[] fields = getFields(targetClass);
            for (Field field : fields) {
                if (shouldSkipField(field)) {
                    continue;
                }
                field.setAccessible(true);
                String fieldName = getFieldName(field);
                if (jsonObject.has(fieldName)) {
                    Object jsonValue = jsonObject.get(fieldName);
                    Object deserializedValue = deserializeValue(jsonValue, field.getType(), field.getGenericType());
                    field.set(instance, deserializedValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed for class: " + targetClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromJSONArray(JSONArray jsonArray, Class<T> targetClass) {
        if (jsonArray == null) {
            return null;
        }
        try {
            if (targetClass == JSONArray.class) {
                return (T) jsonArray;
            }
            if (targetClass.isArray()) {
                return (T) deserializeToArray(jsonArray, targetClass.getComponentType());
            }
            if (Collection.class.isAssignableFrom(targetClass)) {
                return (T) deserializeToCollection(jsonArray, targetClass);
            }
            throw new RuntimeException("Cannot deserialize JSONArray to " + targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed for class: " + targetClass.getName(), e);
        }
    }

    public static <T> List<T> deserializeJSONArrayToList(JSONArray jsonArray, Class<T> elementClass) {
        if (jsonArray == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.get(i);
            T deserializedElement = deserializeValue(element, elementClass, elementClass);
            result.add(deserializedElement);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeValue(Object jsonValue, Class<T> targetClass, Type genericType) {
        if (jsonValue == null || jsonValue == JSONObject.NULL) {
            return null;
        }
        if (targetClass.isInstance(jsonValue)) {
            return (T) jsonValue;
        }
        if (jsonValue instanceof JSONObject) {
            if (targetClass == JSONObject.class) {
                return (T) jsonValue;
            }
            if (Map.class.isAssignableFrom(targetClass)) {
                return (T) deserializeToMap((JSONObject) jsonValue, targetClass);
            }
            return deserializeFromJSONObject((JSONObject) jsonValue, targetClass);
        }
        if (jsonValue instanceof JSONArray) {
            if (targetClass == JSONArray.class) {
                return (T) jsonValue;
            }
            if (targetClass.isArray()) {
                return (T) deserializeToArray((JSONArray) jsonValue, targetClass.getComponentType());
            }
            if (Collection.class.isAssignableFrom(targetClass)) {
                return (T) deserializeToCollection((JSONArray) jsonValue, targetClass, genericType);
            }
        }
        if (isPrimitiveOrWrapper(targetClass) || targetClass == String.class) {
            return convertPrimitive(jsonValue, targetClass);
        }
        if (targetClass.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>) targetClass, jsonValue.toString());
        }
        if (targetClass == Date.class) {
            return (T) deserializeDateValue(jsonValue);
        }
        if (targetClass == LocalDateTime.class) {
            return (T) deserializeLocalDateTimeValue(jsonValue);
        }
        if (targetClass == LocalDate.class) {
            return (T) LocalDate.parse(jsonValue.toString());
        }
        if (targetClass == LocalTime.class) {
            return (T) LocalTime.parse(jsonValue.toString());
        }
        if (targetClass == Instant.class) {
            return (T) Instant.parse(jsonValue.toString());
        }
        throw new RuntimeException("Cannot deserialize value: " + jsonValue + " to type: " + targetClass);
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> deserializeToMap(JSONObject jsonObject, Class<?> mapClass) {
        Map<Object, Object> map;
        if (mapClass.isInterface()) {
            map = new HashMap<>();
        } else {
            try {
                map = (Map<Object, Object>) createInstance(mapClass);
            } catch (Exception e) {
                map = new HashMap<>();
            }
        }
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            map.put(key, value);
        }
        return map;
    }

    private static Object deserializeToArray(JSONArray jsonArray, Class<?> componentType) {
        int length = jsonArray.length();
        Object array = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Object element = jsonArray.get(i);
            Object deserializedElement = deserializeValue(element, componentType, componentType);
            Array.set(array, i, deserializedElement);
        }
        return array;
    }

    private static Collection<?> deserializeToCollection(JSONArray jsonArray, Class<?> collectionClass, Type genericType) {
        Collection<Object> collection;
        if (collectionClass.isInterface()) {
            if (collectionClass.getSimpleName().equals("Set")) {
                collection = new HashSet<>();
            } else {
                collection = new ArrayList<>();
            }
        } else {
            collection = createCollectionInstance(collectionClass);
        }
        Class<?> elementType = Object.class;
        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                elementType = (Class<?>) typeArgs[0];
            }
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.get(i);
            Object deserializedElement = deserializeValue(element, elementType, elementType);
            collection.add(deserializedElement);
        }
        return collection;
    }

    private static Collection<Object> createCollectionInstance(Class<?> collectionClass) {
        try {
            Object instance = createInstance(collectionClass);
            if (instance instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) instance;
                return collection;
            }
            throw new IllegalArgumentException("Class " + collectionClass.getName() + " is not a Collection");
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static Collection<?> deserializeToCollection(JSONArray jsonArray, Class<?> collectionClass) {
        return deserializeToCollection(jsonArray, collectionClass, collectionClass);
    }

    private static Date deserializeDateValue(Object jsonValue) {
        if (jsonValue instanceof Number) {
            return new Date(((Number) jsonValue).longValue());
        } else if (jsonValue instanceof String) {
            try {
                if (config.isUseCustomDateFormat()) {
                    LocalDateTime dateTime = LocalDateTime.parse((String) jsonValue,
                            DateTimeFormatter.ofPattern(config.getDateFormat()));
                    return Date.from(dateTime.atZone(ZoneOffset.UTC).toInstant());
                }
                return new Date(Long.parseLong((String) jsonValue));
            } catch (Exception e) {
                throw new RuntimeException("Cannot parse date: " + jsonValue, e);
            }
        }
        throw new RuntimeException("Cannot deserialize date from: " + jsonValue);
    }

    private static LocalDateTime deserializeLocalDateTimeValue(Object jsonValue) {
        if (jsonValue instanceof String) {
            try {
                if (config.isUseCustomDateFormat()) {
                    return LocalDateTime.parse((String) jsonValue,
                            DateTimeFormatter.ofPattern(config.getDateFormat()));
                }
                return LocalDateTime.parse((String) jsonValue);
            } catch (Exception e) {
                throw new RuntimeException("Cannot parse LocalDateTime: " + jsonValue, e);
            }
        }
        throw new RuntimeException("Cannot deserialize LocalDateTime from: " + jsonValue);
    }

    private static <T> T deserializePrimitiveValue(String jsonString, Class<T> targetClass) {
        jsonString = jsonString.trim();
        if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
            jsonString = jsonString.substring(1, jsonString.length() - 1);
        }
        return convertPrimitive(jsonString, targetClass);
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertPrimitive(Object value, Class<T> targetType) {
        // Add a safety check to ensure we only convert primitive types
        if (!isPrimitiveOrWrapper(targetType) && targetType != String.class) {
            throw new RuntimeException("Cannot convert value: " + value + " to non-primitive type: " + targetType);
        }

        // Handle direct primitive conversions for Numbers
        if (value instanceof Number number) {
            if (targetType == boolean.class || targetType == Boolean.class) {
                return (T) Boolean.valueOf(number.intValue() != 0);
            }
            if (targetType == byte.class || targetType == Byte.class) {
                return (T) Byte.valueOf(number.byteValue());
            }
            if (targetType == short.class || targetType == Short.class) {
                return (T) Short.valueOf(number.shortValue());
            }
            if (targetType == int.class || targetType == Integer.class) {
                return (T) Integer.valueOf(number.intValue());
            }
            if (targetType == long.class || targetType == Long.class) {
                return (T) Long.valueOf(number.longValue());
            }
            if (targetType == float.class || targetType == Float.class) {
                return (T) Float.valueOf(number.floatValue());
            }
            if (targetType == double.class || targetType == Double.class) {
                return (T) Double.valueOf(number.doubleValue());
            }
            if (targetType == char.class || targetType == Character.class) {
                return (T) Character.valueOf((char) number.intValue());
            }
            if (targetType == String.class) {
                return (T) value.toString();
            }
        }

        // Handle Boolean direct conversion
        if (value instanceof Boolean && (targetType == boolean.class || targetType == Boolean.class)) {
            return (T) value;
        }

        // Handle String conversions
        String stringValue = value.toString();
        if (targetType == boolean.class || targetType == Boolean.class) {
            return (T) Boolean.valueOf(stringValue);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return (T) Byte.valueOf(stringValue);
        }
        if (targetType == short.class || targetType == Short.class) {
            return (T) Short.valueOf(stringValue);
        }
        if (targetType == int.class || targetType == Integer.class) {
            return (T) Integer.valueOf(stringValue);
        }
        if (targetType == long.class || targetType == Long.class) {
            return (T) Long.valueOf(stringValue);
        }
        if (targetType == float.class || targetType == Float.class) {
            return (T) Float.valueOf(stringValue);
        }
        if (targetType == double.class || targetType == Double.class) {
            return (T) Double.valueOf(stringValue);
        }
        if (targetType == char.class || targetType == Character.class) {
            return (T) Character.valueOf(stringValue.isEmpty() ? '\0' : stringValue.charAt(0));
        }
        if (targetType == String.class) {
            return (T) stringValue;
        }

        // This should never be reached due to the safety check above
        throw new RuntimeException("Unsupported primitive type: " + targetType);
    }

    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class || clazz == Byte.class || clazz == Short.class ||
                clazz == Integer.class || clazz == Long.class || clazz == Float.class ||
                clazz == Double.class || clazz == Character.class;
    }

    private static boolean shouldSkipField(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            return true;
        }
        if (Modifier.isTransient(modifiers) && !config.isIncludeTransientFields()) {
            return true;
        }
        if (config.getExcludedFields().contains(field.getName())) {
            return true;
        }
        return field.isAnnotationPresent(JsonIgnore.class);
    }

    private static String getFieldName(Field field) {
        if (field.isAnnotationPresent(JsonProperty.class)) {
            return field.getAnnotation(JsonProperty.class).value();
        }
        return field.getName();
    }

    private static <T> T createInstance(Class<T> clazz) throws Exception {
        Constructor<?> constructor = CONSTRUCTOR_CACHE.computeIfAbsent(clazz, k -> {
            try {
                Constructor<?> ctor = k.getDeclaredConstructor();
                ctor.setAccessible(true);
                return ctor;
            } catch (Exception e) {
                throw new RuntimeException("No default constructor found for class: " + k.getName(), e);
            }
        });
        return clazz.cast(constructor.newInstance());
    }

    private static Field[] getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, k -> {
            List<Field> allFields = new ArrayList<>();
            Class<?> currentClass = k;
            while (currentClass != null && currentClass != Object.class) {
                Field[] fields = currentClass.getDeclaredFields();
                Collections.addAll(allFields, fields);
                currentClass = currentClass.getSuperclass();
            }
            return allFields.toArray(new Field[0]);
        });
    }

    public static void clearCache() {
        FIELD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
    }

    public static JSONObject mergeJSONObjects(JSONObject base, JSONObject overlay) {
        if (base == null) return overlay;
        if (overlay == null) return base;
        JSONObject result = new JSONObject(base.toString());
        for (String key : overlay.keySet()) {
            Object value = overlay.get(key);
            if (result.has(key) && value instanceof JSONObject && result.get(key) instanceof JSONObject) {
                JSONObject mergedValue = mergeJSONObjects((JSONObject) result.get(key), (JSONObject) value);
                result.put(key, mergedValue);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    public static JSONArray mergeJSONArrays(JSONArray base, JSONArray overlay) {
        if (base == null) return overlay;
        if (overlay == null) return base;
        JSONArray result = new JSONArray();
        for (int i = 0; i < base.length(); i++) {
            result.put(base.get(i));
        }
        for (int i = 0; i < overlay.length(); i++) {
            result.put(overlay.get(i));
        }
        return result;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonIgnore {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonProperty {
        String value();
    }
}
