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

@SuppressWarnings("SqlSourceToSinkFlow")
public class SQLInstance {

    @Getter
    private SQLType sqlType;

    @Getter
    private SqlSessionFactory factory;

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
                case MYSQL -> createMysql();
                case SQLITE -> createSQLite();
                default -> {
                    Ari.instance.getLog().error("unsupported SQL type: {}", sqlType);
                    return;
                }
            }
        } catch (Exception e) {
            Ari.instance.getLog().error(e, "failed to initialize database connection factory");
            return;
        }
        Ari.instance.getLog().debug("initializing database schema");

        this.ensureSchemaVersionTable();

        try {
            MigrationManager manager = null;
            if (factory != null) {
                manager = new MigrationManager(this.sqlType, this.getTablePrefix(), factory.getConfiguration().getEnvironment().getDataSource());
            }
            if (manager != null) {
                manager.migrate();
            }
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
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(sqlType.getDriver());
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + config.getString("data.address") + ":" + config.getString("data.port") +  "/" + config.getString("data.database") + "?useUnicode=true&character_set_server=utf8mb4");
        hikariDataSource.setUsername(config.getString("data.username"));
        hikariDataSource.setPassword(config.getString("data.password"));
        hikariDataSource.setMaximumPoolSize(config.getInt("data.maximum-pool-size"));
        hikariDataSource.setMinimumIdle(config.getInt("data.minimum-idle"));
        hikariDataSource.setMaxLifetime(config.getInt("data.connection-timeout"));
        hikariDataSource.setKeepaliveTime(config.getLong("data.keepalive-time"));
        this.setLiteFactory(hikariDataSource);
    }

    private void createSQLite() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(sqlType.getDriver());
        hikariDataSource.setJdbcUrl("jdbc:sqlite:" + Ari.instance.getDataFolder().getAbsolutePath() + "/" + "AriDB.db");
        this.setLiteFactory(hikariDataSource);
    }

    @SneakyThrows
    private void setLiteFactory(HikariDataSource dataSource) {

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment(Ari.instance.isDebug() ? "dev":"prod", new JdbcTransactionFactory(), dataSource));

        configuration.addMapper(PlayersMapper.class);
        configuration.addMapper(WarpMapper.class);
        configuration.addMapper(HomeMapper.class);
        configuration.addMapper(WhitelistMapper.class);
        configuration.addMapper(BanPlayerMapper.class);

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor innerInterceptor = new DynamicTableNameInnerInterceptor((sql, original) -> Ari.instance.getConfig().getString("data.table-prefix", "ari_") + original);
        interceptor.addInnerInterceptor(innerInterceptor);

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        configuration.addInterceptor(interceptor);

        factory = new SqlSessionFactoryBuilder().build(configuration);
    }

    private String getTablePrefix() {
        return Ari.instance.getConfig().getString("data.table-prefix", "ari");
    }

    private void ensureSchemaVersionTable() {
        String sql = SqlTable.SCHEMA_VERSION.getSql(this.getTablePrefix(), sqlType);
        try (Connection conn = factory.getConfiguration().getEnvironment().getDataSource().getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Ari.instance.getLog().error(e, "Failed to create schema_version table");
        }
    }

    public void close() {
        if (this.factory != null) {
            this.factory = null;
            Ari.instance.getLog().debug("Connection closed successfully");
        }
        this.factory= null;
    }

}
