package com.tty.ari.tool;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.tty.ari.Ari;
import com.tty.ari.enumType.SqlTable;
import com.tty.api.enumType.SQLType;
import com.tty.ari.mapper.*;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLInstance {

    @Getter
    private SQLType sqlType;

    @Getter
    private SqlSessionFactory factory;

    private HikariDataSource dataSource;

    public SQLInstance() {
        this.start();
    }

    private void start() {
        Ari.instance.getLog().debug("initializing database connection");

        String storageType = Ari.instance.getConfig().getString("data.storage-type");

        if (storageType == null) {
            Ari.instance.getLog().warn("data.storage-type not configured; defaulting to SQLITE");
            sqlType = SQLType.SQLITE;
        } else {
            try {
                sqlType = SQLType.valueOf(storageType.toUpperCase());
            } catch (IllegalArgumentException e) {
                Ari.instance.getLog().error(e, "invalid data.storage-type '{}'; supported values: MYSQL, SQLITE", storageType);
                return;
            }
        }

        Ari.instance.getLog().debug("using database type: {}", sqlType.getType());

        try {
            switch (sqlType) {
                case MYSQL -> this.createMysql();
                case SQLITE -> this.createSQLite();
                default -> {
                    Ari.instance.getLog().error("unsupported SQL type: {}", sqlType);
                    return;
                }
            }
        } catch (Exception e) {
            Ari.instance.getLog().error(e, "failed to initialize database connection factory");
            return;
        }

        if (factory == null) {
            Ari.instance.getLog().error("SqlSessionFactory is null, aborting schema initialization");
            return;
        }

        Ari.instance.getLog().debug("initializing database schema");
        this.ensureSchemaVersionTable();

        try {
            new MigrationManager(this.sqlType, this.getTablePrefix(), this.dataSource).migrate();
        } catch (SQLException e) {
            Ari.instance.getLog().error(e, "Failed to apply database migrations");
        }
    }

    public void reconnect() {
        Ari.instance.getLog().debug("connection is closing...");
        this.close();
        this.start();
    }

    private void createMysql() {
        FileConfiguration config = Ari.instance.getConfig();

        String address = config.getString("data.address");
        String port = config.getString("data.port");
        String database = config.getString("data.database");
        String username = config.getString("data.username");
        String password = config.getString("data.password");

        if (address == null || port == null || database == null || username == null) {
            Ari.instance.getLog().error("MySQL connection configuration incomplete (address, port, database, username are required)");
            return;
        }

        HikariDataSource source = new HikariDataSource();
        source.setDriverClassName(sqlType.getDriver());
        source.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&connectionTimeZone=UTC&useSSL=false");
        source.setUsername(username);
        source.setPassword(password);
        source.setMaximumPoolSize(config.getInt("data.maximum-pool-size", 10));
        source.setMinimumIdle(config.getInt("data.minimum-idle", 2));
        source.setMaxLifetime(config.getInt("data.maximum-lifetime", 1800000));
        source.setConnectionTimeout(config.getInt("data.connection-timeout", 30000));
        source.setKeepaliveTime(config.getLong("data.keepalive-time", 0));
        this.setLiteFactory(source);
    }

    private void createSQLite() {
        HikariDataSource source = new HikariDataSource();
        source.setDriverClassName(sqlType.getDriver());
        source.setJdbcUrl("jdbc:sqlite:" + Ari.instance.getDataFolder().getAbsolutePath() + "/AriDB.db");
        source.setMaximumPoolSize(1);
        this.setLiteFactory(source);
    }

    @SneakyThrows
    private void setLiteFactory(HikariDataSource dataSource) {
        this.dataSource = dataSource;

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment(Ari.instance.isDebug() ? "dev":"prod", new JdbcTransactionFactory(), dataSource));

        configuration.addMapper(PlayersMapper.class);
        configuration.addMapper(WarpMapper.class);
        configuration.addMapper(HomeMapper.class);
        configuration.addMapper(WhitelistMapper.class);
        configuration.addMapper(BanPlayerMapper.class);

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor innerInterceptor = new DynamicTableNameInnerInterceptor((sql, original) -> this.getTablePrefix() + original);
        interceptor.addInnerInterceptor(innerInterceptor);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        configuration.addInterceptor(interceptor);

        factory = new SqlSessionFactoryBuilder().build(configuration);
    }

    private String getTablePrefix() {
        return Ari.instance.getConfig().getString("data.table-prefix", "ari_");
    }

    private void ensureSchemaVersionTable() {
        String sql = SqlTable.SCHEMA_VERSION.getSql(this.getTablePrefix(), sqlType);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Ari.instance.getLog().error(e, "Failed to create schema_version table");
        }
    }

    public void close() {
        if (this.dataSource != null) {
            this.dataSource.close();
            this.dataSource = null;
            Ari.instance.getLog().debug("Connection pool closed successfully");
        }
        this.factory = null;
    }

}