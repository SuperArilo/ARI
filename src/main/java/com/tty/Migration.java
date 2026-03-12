package com.tty;

import com.tty.api.enumType.SQLType;

import java.sql.*;

@SuppressWarnings("SqlSourceToSinkFlow")
public interface Migration {

    int getVersion();
    String getDescription();
    void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException;

    /**
     * 检查表是否存在
     */
    default boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * 检查列是否存在（仅用于 SQLite）
     */
    default boolean columnExistsSQLite(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String existingColumn = rs.getString("name");
                if (existingColumn.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 增加列，如果存在
     * @param conn 连接对象
     * @param tableName 表名
     * @param columnName 列名
     * @param columnDefinition 列属性
     * @param sqlType 数据库类型
     * @throws SQLException 抛出 sql 错误
     */
    default void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnDefinition, SQLType sqlType) throws SQLException {
        if (sqlType == SQLType.SQLITE && this.columnExistsSQLite(conn, tableName, columnName)) {
            return;
        }
        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

}

