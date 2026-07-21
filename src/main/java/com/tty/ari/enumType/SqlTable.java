package com.tty.ari.enumType;

import com.tty.api.enumType.SQLType;

public enum SqlTable {
    SCHEMA_VERSION("""
            CREATE TABLE IF NOT EXISTS ${tablePrefix}schema_version (
                version INTEGER PRIMARY KEY,
                applied_at INTEGER NOT NULL,
                description VARCHAR(255)
            )
        """),
    PLAYERS("""
                CREATE TABLE IF NOT EXISTS ${tablePrefix}players (
                id INTEGER PRIMARY KEY ${autoIncrement},
                player_name varchar(64) NOT NULL,
                player_uuid varchar(128) NOT NULL,
                first_login_time BIGINT NULL DEFAULT 0,
                last_login_off_time BIGINT NULL DEFAULT 0,
                total_online_time BIGINT NULL DEFAULT 0,
                name_prefix varchar(128) DEFAULT NULL,
                name_suffix varchar(128) DEFAULT NULL)
            """),
    PLAYER_HOME("""
                CREATE TABLE IF NOT EXISTS ${tablePrefix}player_home (
                id INTEGER PRIMARY KEY ${autoIncrement},
                home_id VARCHAR(128) NOT NULL,
                home_name VARCHAR(128) NOT NULL,
                player_uuid VARCHAR(128) NOT NULL,
                location VARCHAR(128) NOT NULL,
                show_material VARCHAR(128) NOT NULL,
                top_slot boolean NOT NULL DEFAULT 0)
            """),
    WARPS("""
                CREATE TABLE IF NOT EXISTS ${tablePrefix}warps (
                id INTEGER PRIMARY KEY ${autoIncrement},
                warp_id VARCHAR(128) NOT NULL,
                warp_name VARCHAR(128) NOT NULL,
                create_by VARCHAR(128) NOT NULL,
                location VARCHAR(128) NOT NULL,
                show_material VARCHAR(128) NOT NULL,
                permission VARCHAR(128) default NULL,
                cost INTEGER default 0,
                top_slot boolean NOT NULL DEFAULT 0)
             """),
    WHITELIST("""
                CREATE TABLE IF NOT EXISTS ${tablePrefix}whitelist (
                id INTEGER PRIMARY KEY ${autoIncrement},
                player_uuid VARCHAR(128) NOT NULL,
                add_time BIGINT NULL DEFAULT 0,
                operator VARCHAR(128) NOT NULL,
                remark TEXT)
            """),
    BAN_LIST("""
            CREATE TABLE IF NOT EXISTS ${tablePrefix}bad_list (
            id INTEGER PRIMARY KEY ${autoIncrement},
            player_uuid varchar(128) NOT NULL,
            operator varchar(128) NOT NULL,
            start_time BIGINT NULL DEFAULT 0,
            end_time BIGINT NULL DEFAULT 0,
            reason TEXT)
            """);

    private final String sql;

    SqlTable(String sql) {
        this.sql = sql;
    }

    public String getSql(String tablePrefix, SQLType sqlType) {
        if (tablePrefix == null || sqlType == null) {
            throw new IllegalArgumentException("tablePrefix and sqlType must not be null");
        }

        String autoIncrement = sqlType == SQLType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT";
        return sql.replace("${tablePrefix}", tablePrefix).replace("${autoIncrement}", autoIncrement);
    }
}