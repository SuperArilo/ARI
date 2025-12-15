package com.tty.enumType;

import com.tty.lib.enum_type.SQLType;
import com.tty.tool.SQLInstance;

public enum SqlTable {

    PLAYER("""
                CREATE TABLE IF NOT EXISTS %splayers (
                id INTEGER PRIMARY KEY %s,
                player_name varchar(64) NOT NULL,
                player_uuid varchar(128) NOT NULL,
                first_login_time INTEGER NULL DEFAULT 0,
                last_login_off_time INTEGER NULL DEFAULT 0,
                total_online_time INTEGER NULL DEFAULT 0,
                name_prefix varchar(128) DEFAULT NULL,
                name_suffix varchar(128) DEFAULT NULL);
            """),
    HOME("""
                CREATE TABLE IF NOT EXISTS %splayer_home (
                id INTEGER PRIMARY KEY %s,
                home_id VARCHAR(128) NOT NULL,
                home_name VARCHAR(128) NOT NULL,
                player_uuid VARCHAR(128) NOT NULL,
                location VARCHAR(128) NOT NULL,
                show_material VARCHAR(128) NOT NULL,
                top_slot boolean NOT NULL DEFAULT 0);
            """),
    WARP("""
                CREATE TABLE IF NOT EXISTS %swarps (
                id INTEGER PRIMARY KEY %s,
                warp_id VARCHAR(128) NOT NULL,
                warp_name VARCHAR(128) NOT NULL,
                create_by VARCHAR(128) NOT NULL,
                location VARCHAR(128) NOT NULL,
                show_material VARCHAR(128) NOT NULL,
                permission VARCHAR(128) default NULL,
                cost INTEGER default 0,
                top_slot boolean NOT NULL DEFAULT 0);
             """),
    WHITELIST("""
                CREATE TABLE IF NOT EXISTS %swhitelist (
                id INTEGER PRIMARY KEY %s,
                player_uuid VARCHAR(128) NOT NULL,
                add_time INTEGER NULL DEFAULT 0,
                operator VARCHAR(128) NOT NULL);
            """),
    BAN_LIST("""
            CREATE TABLE IF NOT EXISTS %sbad_list (
            id INTEGER PRIMARY KEY %s,
            player_uuid varchar(128) NOT NULL,
            operator varchar(128) NOT NULL,
            start_time INTEGER NULL DEFAULT 0,
            end_time INTEGER NULL DEFAULT 0,
            reason varchar(128) NOT NULL);
            """);
    private final String sql;

    SqlTable(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql.formatted(
                SQLInstance.getTablePrefix(),
                SQLInstance.sqlType.equals(SQLType.MYSQL) ? "AUTO_INCREMENT":"AUTOINCREMENT");
    }

}
