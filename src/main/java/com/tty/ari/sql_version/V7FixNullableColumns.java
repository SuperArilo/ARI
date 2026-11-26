package com.tty.ari.sql_version;

import com.tty.api.enumType.SQLType;
import com.tty.ari.Migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V7FixNullableColumns implements Migration {

    @Override
    public int getVersion() {
        return 7;
    }

    @Override
    public String getDescription() {
        return "normalize nullable add_time and cost";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        String whitelist = tablePrefix + "whitelist";
        String warps = tablePrefix + "warps";
        boolean hasWhitelist = this.tableExists(conn, whitelist);
        boolean hasWarps = this.tableExists(conn, warps);
        if (!hasWhitelist && !hasWarps) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            if (hasWhitelist) {
                stmt.executeUpdate("UPDATE " + whitelist + " SET add_time = 0 WHERE add_time IS NULL");
                if (sqlType == SQLType.MYSQL) {
                    stmt.execute("ALTER TABLE " + whitelist + " MODIFY add_time BIGINT NOT NULL DEFAULT 0");
                }
            }
            if (hasWarps) {
                stmt.executeUpdate("UPDATE " + warps + " SET cost = 0 WHERE cost IS NULL");
                if (sqlType == SQLType.MYSQL) {
                    stmt.execute("ALTER TABLE " + warps + " MODIFY cost DOUBLE NOT NULL DEFAULT 0");
                }
            }
        }
    }
}
