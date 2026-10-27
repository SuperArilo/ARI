package com.tty.ari.sql_version;

import com.tty.api.enumType.SQLType;
import com.tty.ari.Migration;

import java.sql.Connection;
import java.sql.SQLException;

public class V6AddBanCount implements Migration {

    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public String getDescription() {
        return "add ban_count to players";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        String prefix = tablePrefix == null ? "" : tablePrefix;
        String playersTable = prefix + "players";

        if (!this.tableExists(conn, playersTable)) {
            return;
        }

        this.addColumnIfNotExists(conn, playersTable, "ban_count", "INTEGER NOT NULL DEFAULT 0", sqlType);
    }
}
