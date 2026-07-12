package com.tty.ari;

import com.tty.api.enumType.SQLType;

import java.sql.*;

public interface Migration {

    int getVersion();
    String getDescription();
    void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException;

    private static void validateIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }

    default boolean tableExists(Connection conn, String tableName) throws SQLException {
        validateIdentifier(tableName);
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    default boolean columnExistsSQLite(Connection conn, String tableName, String columnName) throws SQLException {
        validateIdentifier(tableName);
        validateIdentifier(columnName);
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    default boolean columnExistsMySQL(Connection conn, String tableName, String columnName) throws SQLException {
        validateIdentifier(tableName);
        validateIdentifier(columnName);
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS " + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }

    }

    default void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnDefinition, SQLType sqlType) throws SQLException {
        validateIdentifier(tableName);
        validateIdentifier(columnName);
        if (sqlType == null) {
            throw new IllegalArgumentException("sqlType must not be null");
        }
        switch (sqlType) {
            case SQLITE:
                if (columnExistsSQLite(conn, tableName, columnName)) return;
                break;
            case MYSQL:
                if (columnExistsMySQL(conn, tableName, columnName)) return;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported SQL type: " + sqlType);
        }

        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

    }
}