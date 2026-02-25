package com.tty.sql_version;

import com.tty.Migration;
import com.tty.api.enumType.SQLType;
import com.tty.enumType.SqlTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("SqlSourceToSinkFlow")
public class V1Tables implements Migration {
    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Initial tables";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        for (SqlTable table : SqlTable.values()) {
            String sql = table.getSql(tablePrefix, sqlType);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

}
