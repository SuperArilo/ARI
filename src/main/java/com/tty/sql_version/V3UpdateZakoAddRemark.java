package com.tty.sql_version;

import com.tty.Migration;
import com.tty.api.enumType.SQLType;

import java.sql.Connection;
import java.sql.SQLException;

public class V3UpdateZakoAddRemark implements Migration {

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getDescription() {
        return "update table remark to whitelist";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        String tableName = tablePrefix + "whitelist";

        if (this.tableExists(conn, tableName)) {
            this.addColumnIfNotExists(conn, tableName, "remark", "NOT NULL DEFAULT ''", sqlType);
        }
    }

}
