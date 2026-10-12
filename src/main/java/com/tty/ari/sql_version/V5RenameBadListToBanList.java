package com.tty.ari.sql_version;

import com.tty.api.enumType.SQLType;
import com.tty.ari.Ari;
import com.tty.ari.Migration;
import com.tty.ari.enumType.SqlTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V5RenameBadListToBanList implements Migration {

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "rename bad_list to ban_list";
    }

    @Override
    public void migrate(Connection conn, SQLType sqlType, String tablePrefix) throws SQLException {
        if (tablePrefix == null) {
            tablePrefix = Ari.instance.getName() + "_";
        }
        String oldTable = tablePrefix + "bad_list";
        String newTable = tablePrefix + "ban_list";

        boolean oldExists = this.tableExists(conn, oldTable);
        boolean newExists = this.tableExists(conn, newTable);

        if (oldExists && !newExists) {
            try (Statement stmt = conn.createStatement()) {
                String createNew = SqlTable.BAN_LIST.getSql(tablePrefix, sqlType);
                stmt.execute(createNew);

                String insertCopy = "INSERT INTO " + newTable + " (id, player_uuid, operator, start_time, end_time, reason) "
                        + "SELECT id, player_uuid, operator, start_time, end_time, reason FROM " + oldTable;
                stmt.execute(insertCopy);

                String dropOld = "DROP TABLE IF EXISTS " + oldTable;
                stmt.execute(dropOld);
            }
        } else if (!oldExists && !newExists) {
            try (Statement stmt = conn.createStatement()) {
                String createNew = SqlTable.BAN_LIST.getSql(tablePrefix, sqlType);
                stmt.execute(createNew);
            }
        }
    }
}
