package com.tty.tool;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.tty.Ari;
import com.tty.enumType.SqlTable;
import com.tty.api.enumType.SQLType;
import com.tty.mapper.*;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Statement;

@SuppressWarnings("SqlSourceToSinkFlow")
public class SQLInstance {

    @Getter
    public SQLType sqlType;
    public static SqlSessionFactory SESSION_FACTORY;

    public SQLInstance() {
        this.start();
    }

    public void start() {
        Ari.LOG.debug("initializing database connection");

        if (Ari.instance == null) {
            Ari.LOG.error("plugin instance not initialized; database startup aborted");
            return;
        }

        String storageType = Ari.instance.getConfig().getString("data.storage-type");

        if (storageType == null) {
            Ari.LOG.warn("data.storage-type not configured; defaulting to SQLITE");
            sqlType = SQLType.SQLITE;
        } else {
            try {
                sqlType = SQLType.valueOf(storageType.toUpperCase());
            } catch (IllegalArgumentException e) {
                Ari.LOG.error(e, "invalid data.storage-type '{}'; supported values: MYSQL, SQLITE", storageType);
                return;
            }
        }

        Ari.LOG.info("using database type: {}", sqlType.getType());

        try {
            switch (sqlType) {
                case MYSQL -> createMysql();
                case SQLITE -> createSQLite();
                default -> {
                    Ari.LOG.error("unsupported SQL type: {}", sqlType);
                    return;
                }
            }
        } catch (Exception e) {
            Ari.LOG.error(e, "failed to initialize database connection factory");
            return;
        }
        Ari.LOG.debug("initializing database schema");

        try (SqlSession session = SESSION_FACTORY.openSession()) {
            for (SqlTable table : SqlTable.values()) {
                Ari.LOG.debug("creating or updating table: {}", table.name());
                try (Statement statement = session.getConnection().createStatement()) {
                    statement.execute(table.getSql(this.getTablePrefix(), this.getSqlType()));
                }
            }
            session.commit();
            Ari.LOG.info("database schema initialized successfully");
        } catch (Exception e) {
            Ari.LOG.error(e, "failed to initialize database schema");
        }
    }

    public void reconnect() {
        Ari.LOG.debug("connection is closing...");
        this.close();
        this.start();
    }

    protected void createMysql() {
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

    protected void createSQLite() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(sqlType.getDriver());
        hikariDataSource.setJdbcUrl("jdbc:sqlite:" + Ari.instance.getDataFolder().getAbsolutePath() + "/" + "AriDB.db");
        this.setLiteFactory(hikariDataSource);
    }

    @SneakyThrows
    protected void setLiteFactory(HikariDataSource dataSource) {

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment(Ari.DEBUG ? "dev":"prod", new JdbcTransactionFactory(), dataSource));

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

        SESSION_FACTORY = new SqlSessionFactoryBuilder().build(configuration);
    }

    public String getTablePrefix() {
        return Ari.instance.getConfig().getString("data.table-prefix", "ari");
    }

    public void close() {
        if (SQLInstance.SESSION_FACTORY != null) {
            SQLInstance.SESSION_FACTORY = null;
            Ari.LOG.debug("Connection closed successfully");
        }
        SQLInstance.SESSION_FACTORY = null;
    }

}
