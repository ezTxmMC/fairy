package community.theprojects.fairy.util.json;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class JsonFileHandler {

    public static <T> void writeToFile(T object, String filePath) throws IOException {
        writeToFile(object, filePath, false);
    }

    public static <T> void writeToFile(T object, String filePath, boolean prettyPrint) throws IOException {
        String json;
        if (prettyPrint) {
            JsonSerializer.SerializationConfig config = JsonSerializer.getConfig();
            boolean originalPrettyPrint = config.isPrettyPrint();
            config.setPrettyPrint(true);
            json = JsonSerializer.serialize(object);
            config.setPrettyPrint(originalPrettyPrint);
        } else {
            json = JsonSerializer.serialize(object);
        }

        writeStringToFile(json, filePath);
    }

    public static <T> void writeListToFile(List<T> objects, String filePath) throws IOException {
        writeListToFile(objects, filePath, false);
    }

    public static <T> void writeListToFile(List<T> objects, String filePath, boolean prettyPrint) throws IOException {
        JSONArray jsonArray = JsonSerializer.serializeToJSONArray(objects);
        writeJSONArrayToFile(jsonArray, filePath, prettyPrint);
    }

    public static void writeJSONObjectToFile(JSONObject jsonObject, String filePath, boolean prettyPrint) throws IOException {
        String json = prettyPrint ? jsonObject.toString(2) : jsonObject.toString();
        writeStringToFile(json, filePath);
    }

    public static void writeJSONArrayToFile(JSONArray jsonArray, String filePath, boolean prettyPrint) throws IOException {
        String json = prettyPrint ? jsonArray.toString(2) : jsonArray.toString();
        writeStringToFile(json, filePath);
    }

    public static <T> T readFromFile(String filePath, Class<T> targetClass) throws IOException {
        String json = readStringFromFile(filePath);
        return JsonSerializer.deserialize(json, targetClass);
    }

    public static <T> List<T> readListFromFile(String filePath, Class<T> elementClass) throws IOException {
        String json = readStringFromFile(filePath);
        return JsonSerializer.deserializeList(json, elementClass);
    }

    public static JSONObject readJSONObjectFromFile(String filePath) throws IOException {
        String json = readStringFromFile(filePath);
        return new JSONObject(json);
    }

    public static JSONArray readJSONArrayFromFile(String filePath) throws IOException {
        String json = readStringFromFile(filePath);
        return new JSONArray(json);
    }

    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static void createBackup(String filePath) throws IOException {
        Path source = Paths.get(filePath);
        if (Files.exists(source)) {
            String backupPath = filePath + ".backup." + System.currentTimeMillis();
            Files.copy(source, Paths.get(backupPath));
        }
    }

    public static <T> void appendToArrayFile(T object, String filePath) throws IOException {
        JSONArray jsonArray;
        if (fileExists(filePath)) {
            jsonArray = readJSONArrayFromFile(filePath);
        } else {
            jsonArray = new JSONArray();
        }
        jsonArray.put(JsonSerializer.serializeToJsonValue(object));
        writeJSONArrayToFile(jsonArray, filePath, true);
    }

    public static void updateFieldInFile(String filePath, String fieldName, Object value) throws IOException {
        JSONObject jsonObject;
        if (fileExists(filePath)) {
            jsonObject = readJSONObjectFromFile(filePath);
        } else {
            jsonObject = new JSONObject();
        }
        jsonObject.put(fieldName, JsonSerializer.serializeToJsonValue(value));
        writeJSONObjectToFile(jsonObject, filePath, true);
    }

    private static void writeStringToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    private static String readStringFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return content.toString().trim();
        }
    }
}