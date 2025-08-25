package community.theprojects.fairy.node.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database implements AutoCloseable {
    private final Connection connection;

    public Database(Class<?> annotatedClass) throws SQLException {
        if (annotatedClass == null) {
            throw new IllegalArgumentException("annotatedClass cannot be null");
        }
        SQLConnection cfg = annotatedClass.getAnnotation(SQLConnection.class);
        if (cfg == null) {
            throw new IllegalArgumentException("Class must be annotated with @SQLConnection");
        }
        String url = String.format("jdbc:mysql://%s:%d/%s", cfg.host(), cfg.port(), cfg.database());
        this.connection = DriverManager.getConnection(url, cfg.username(), cfg.password());
    }

    public PreparedStatement prepareStatement(String sql, Object... objects) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < objects.length; i++) {
            statement.setObject(i + 1, objects[i]);
        }
        return statement;
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public Connection raw() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
