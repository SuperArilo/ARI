package com.tty.sql_version;

import com.tty.Migration;
import com.tty.api.enumType.SQLType;

import java.sql.Connection;
import java.sql.SQLException;

public class V2UpdatePreSlot implements Migration {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public String getDescription() {
        return "update table pre_slot to warp and home";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        String playerHome = tablePrefix + "player_home";
        String warp = tablePrefix + "warps";

        if (this.tableExists(conn, playerHome)) {
            this.addColumnIfNotExists(conn, playerHome, "pre_slot", "INTEGER NOT NULL DEFAULT 2147483647", sqlType);
        }

        if (this.tableExists(conn, warp)) {
            this.addColumnIfNotExists(conn, warp, "pre_slot", "INTEGER NOT NULL DEFAULT 2147483647", sqlType);
        }
    }

}