package com.tty.ari.sql_version;

import com.tty.api.enumType.SQLType;
import com.tty.ari.Migration;
import com.tty.ari.enumType.SqlTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V4UpdateWarpAndHomeLocation implements Migration {

    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public String getDescription() {
        return "update warp and home table location type";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        if (tablePrefix == null) {
            tablePrefix = "";
        }
        String homeTable = tablePrefix + SqlTable.PLAYER_HOME.name().toLowerCase();
        String warpTable = tablePrefix + SqlTable.WARPS.name().toLowerCase();

        switch (sqlType) {
            case MYSQL:
                String alterHome = "ALTER TABLE " + homeTable + " MODIFY COLUMN location TEXT";
                String alterWarp = "ALTER TABLE " + warpTable + " MODIFY COLUMN location TEXT";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterHome);
                    stmt.execute(alterWarp);
                }
                break;
            case SQLITE:
                break;
            default: throw new UnsupportedOperationException("unsupported SQL type: " + sqlType);
        }
    }

}