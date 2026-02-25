package com.tty.tool;

import com.tty.Migration;
import com.tty.api.enumType.SQLType;
import com.tty.sql_version.V1Tables;
import com.tty.sql_version.V2UpdatePreSlot;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.List;

public record MigrationManager(SQLType sqlType, String tablePrefix, DataSource dataSource) {

    public int getCurrentVersion() throws SQLException {
        String tableName = tablePrefix + "schema_version";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 检查表是否存在
            if (!this.tableExists(conn, tableName)) {
                return 0;
            }
            ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM " + tableName);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    public void migrate() throws SQLException {
        int currentVersion = this.getCurrentVersion();
        List<Migration> migrations = getMigrations();
        for (Migration migration : migrations) {
            if (migration.getVersion() > currentVersion) {
                this.applyMigration(migration);
            }
        }
    }

    private void applyMigration(Migration migration) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (sqlType == SQLType.MYSQL) {
                conn.setAutoCommit(false);
            }
            try {
                migration.migrate(conn, sqlType, tablePrefix);
                this.recordMigration(conn, migration);
                if (sqlType == SQLType.MYSQL) {
                    conn.commit();
                }
            } catch (SQLException e) {
                if (sqlType == SQLType.MYSQL) {
                    conn.rollback();
                }
                throw e;
            }
        }
    }

    private void recordMigration(Connection conn, Migration migration) throws SQLException {
        String sql = "INSERT INTO " + tablePrefix + "schema_version (version, applied_at, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, migration.getVersion());
            ps.setLong(2, System.currentTimeMillis() / 1000);
            ps.setString(3, migration.getDescription());
            ps.executeUpdate();
        }
    }

    private List<Migration> getMigrations() {
        return Arrays.asList(new V1Tables(), new V2UpdatePreSlot());
    }

}
