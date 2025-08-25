package community.theprojects.fairy.node.database;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatabaseProcessor {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private Connection connection;
    private final Map<String, FieldType> fieldTypes;
    private final Class<?> entityClass;

    public DatabaseProcessor(Class<?> entityClass) {
        SQLConnection sqlConfig = entityClass.getAnnotation(SQLConnection.class);
        if (sqlConfig == null) {
            throw new IllegalArgumentException("Entity class must be annotated with @SQLConnection");
        }

        this.host = sqlConfig.host();
        this.port = sqlConfig.port();
        this.database = sqlConfig.database();
        this.username = sqlConfig.username();
        this.password = sqlConfig.password();
        this.fieldTypes = new HashMap<>();
        this.entityClass = entityClass;
        processValueTypes(entityClass);
    }

    public void processValueTypes(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            ValueType valueType = field.getAnnotation(ValueType.class);
            if (valueType != null) {
                fieldTypes.put(field.getName(), valueType.type());
            }
        }
    }

    public Connection connect() throws SQLException {
        boolean wasNull = (connection == null || connection.isClosed());
        if (wasNull) {
            String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            connection = DriverManager.getConnection(url, username, password);
            // Beim ersten Verbindungsaufbau Schema sicherstellen
            ensureTableAndColumns(connection, entityClass);
        }
        return connection;
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Map<String, FieldType> getFieldTypes() {
        return new HashMap<>(fieldTypes);
    }

    public void handleDatabaseOperation(Object entity, Method method, Object[] args) throws SQLException {
        String methodName = method.getName();
        if (methodName.startsWith("set")) {
            String fieldName = methodName.substring(3);
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            saveToDatabase(entity, fieldName, args[0]);
        } else if (methodName.startsWith("get")) {
            String fieldName = methodName.substring(3);
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            loadFromDatabase(entity, fieldName);
        }
    }

    private void saveToDatabase(Object entity, String fieldName, Object value) throws SQLException {
        Connection conn = connect();
        // Stelle sicher, dass Spalte existiert (falls sich die Entity zur Laufzeit geändert hat)
        ensureTableAndColumns(conn, entity.getClass());

        String tableName = entity.getClass().getSimpleName().toLowerCase();
        // UPSERT: Insert id + Feld, bei Konflikt auf id -> Update
        String sql = "INSERT INTO " + tableName + " (id, " + fieldName + ") VALUES (?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET " + fieldName + " = EXCLUDED." + fieldName;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, getEntityId(entity));
            Object dbVal = toDbValue(getDeclaredField(entity.getClass(), fieldName), value);
            stmt.setObject(2, dbVal);
            stmt.executeUpdate();
        }
    }

    private void loadFromDatabase(Object entity, String fieldName) throws SQLException {
        Connection conn = connect();
        // Stelle sicher, dass Spalte existiert
        ensureTableAndColumns(conn, entity.getClass());

        String tableName = entity.getClass().getSimpleName().toLowerCase();
        String sql = "SELECT " + fieldName + " FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, getEntityId(entity));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Field f = getDeclaredField(entity.getClass(), fieldName);
                if (f != null) {
                    Object value = fromDbValue(f, rs, fieldName);
                    // final-Felder oder nicht unterstützte Typen nicht setzen
                    if (value != null) {
                        setEntityField(entity, f, value);
                    }
                }
            }
        }
    }

    private Object getEntityId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Entity must have an 'id' field", e);
        }
    }

    private Field getDeclaredField(Class<?> cls, String name) {
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private void setEntityField(Object entity, Field field, Object value) {
        try {
            if (Modifier.isFinal(field.getModifiers())) {
                // nicht setzbar -> überspringen
                return;
            }
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set field value", e);
        }
    }

    // ---------- Schema-Verwaltung ----------

    private void ensureTableAndColumns(Connection conn, Class<?> cls) throws SQLException {
        String table = cls.getSimpleName().toLowerCase();
        if (!tableExists(conn, table)) {
            createTable(conn, cls, table);
        } else {
            ensureColumnsExist(conn, cls, table);
        }
    }

    private boolean tableExists(Connection conn, String table) throws SQLException {
        String sql = "SELECT EXISTS (" +
                "SELECT 1 FROM information_schema.tables " +
                "WHERE table_schema = current_schema() AND table_name = ?" +
                ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private void createTable(Connection conn, Class<?> cls, String table) throws SQLException {
        // id-Spalte bestimmen
        Field idField = findIdField(cls);
        if (idField == null) {
            throw new IllegalStateException("Entity must declare an 'id' field");
        }
        String idType = resolveSqlTypeForField(idField);

        StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(table)
                .append(" (id ").append(idType).append(" PRIMARY KEY");

        for (Field f : cls.getDeclaredFields()) {
            if (isSkippableField(f) || f.getName().equals("id")) continue;
            String col = f.getName().toLowerCase();
            String type = resolveSqlTypeForField(f);
            ddl.append(", ").append(col).append(" ").append(type);
        }
        ddl.append(")");

        try (Statement st = conn.createStatement()) {
            st.execute(ddl.toString());
        }
    }

    private void ensureColumnsExist(Connection conn, Class<?> cls, String table) throws SQLException {
        Set<String> existing = readExistingColumns(conn, table);
        for (Field f : cls.getDeclaredFields()) {
            if (isSkippableField(f) || f.getName().equals("id")) continue;
            String col = f.getName().toLowerCase();
            if (!existing.contains(col)) {
                String type = resolveSqlTypeForField(f);
                String ddl = "ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + col + " " + type;
                try (Statement st = conn.createStatement()) {
                    st.execute(ddl);
                }
            }
        }
    }

    private Set<String> readExistingColumns(Connection conn, String table) throws SQLException {
        String sql = "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema = current_schema() AND table_name = ?";
        Set<String> cols = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cols.add(rs.getString(1).toLowerCase());
            }
        }
        return cols;
    }

    private Field findIdField(Class<?> cls) {
        try {
            Field f = cls.getDeclaredField("id");
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private boolean isSkippableField(Field f) {
        int m = f.getModifiers();
        if (Modifier.isStatic(m) || Modifier.isTransient(m) || f.isSynthetic()) return true;
        // Nicht persistierbare Typen auslassen (z. B. Process)
        if (f.getType() == java.lang.Process.class) return true;
        return false;
    }

    private String resolveSqlTypeForField(Field f) {
        FieldType ft = fieldTypes.get(f.getName());
        if (ft != null) {
            return mapFieldTypeToSql(ft);
        }
        return inferSqlType(f.getType());
    }

    private String mapFieldTypeToSql(FieldType ft) {
        return switch (ft) {
            case INTEGER, INT, MEDIUMINT, SMALLINT, TINYINT -> "integer";
            case BIGINT -> "bigint";
            case BOOLEAN, BIT -> "boolean";
            case FLOAT, REAL -> "real";
            case DOUBLE -> "double precision";
            case TIME -> "time";
            case DATE -> "date";
            case TIMESTAMP, YEAR -> "timestamp";
            case DECIMAL, NUMERIC, MONEY -> "numeric";
            case JSON -> "jsonb";
            case BINARY, VARBINARY, BLOB, TINYBLOB, MEDIUMBLOB, LONGBLOB -> "bytea";
            case UUID -> "uuid";
            case ARRAY -> "text[]";
            case GEOMETRY -> "geometry";
            default -> "text";
        };
    }

    private String inferSqlType(Class<?> type) {
        if (type == String.class) return "text";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "bigint";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        if (type == Double.class || type == double.class) return "double precision";
        if (type == Float.class || type == float.class) return "real";
        if (type == java.util.UUID.class) return "uuid";
        if (type == java.math.BigDecimal.class) return "numeric";
        if (type == byte[].class) return "bytea";
        if (type == java.time.Instant.class || type == java.time.LocalDateTime.class) return "timestamp";
        if (type == java.time.LocalDate.class) return "date";
        if (type == java.nio.file.Path.class) return "text"; // Pfade als Text
        if (type.isEnum()) return "text"; // Enums als Text (NAME)
        if (type == java.lang.Process.class) return "bigint"; // PID
        // Fallback
        return "text";
    }

    // ---------- (De-)Serialisierung ----------

    private Object toDbValue(Field f, Object value) {
        if (f == null) return value;
        if (value == null) return null;
        Class<?> t = f.getType();
        if (t == java.nio.file.Path.class) {
            return value.toString();
        }
        if (t.isEnum()) {
            return ((Enum<?>) value).name();
        }
        if (t == java.lang.Process.class) {
            try {
                return ((Process) value).pid();
            } catch (Exception ignored) {
                return null;
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private Object fromDbValue(Field f, ResultSet rs, String column) throws SQLException {
        Class<?> t = f.getType();
        if (t == java.nio.file.Path.class) {
            String s = rs.getString(column);
            return (s == null) ? null : java.nio.file.Paths.get(s);
        }
        if (t.isEnum()) {
            String name = rs.getString(column);
            if (name == null) return null;
            return Enum.valueOf((Class<Enum>) t, name);
        }
        if (t == java.lang.Process.class) {
            // Kann nicht aus der DB rekonstruiert werden -> ignorieren
            return null;
        }
        // Standard: JDBC-Mapping
        return rs.getObject(column);
    }
}