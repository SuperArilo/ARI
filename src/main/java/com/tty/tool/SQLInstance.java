package com.tty.tool;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.tty.Ari;
import com.tty.enumType.Mapper;
import com.tty.enumType.SqlTable;
import com.tty.lib.Log;
import com.tty.lib.enum_type.SQLType;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Statement;

public class SQLInstance {

    public static SQLType sqlType;
    public static SqlSessionFactory SESSION_FACTORY;

    public void start() {
        Log.debug("Start connecting");
        try {
            sqlType = SQLType.valueOf(Ari.instance.getConfig().getString("data.storage-type", "null").toUpperCase());
        } catch (Exception e) {
            Log.warn("storage-type is null, Running sqlite mode");
            sqlType = SQLType.SQLITE;
        }
        Log.debug("The database type is %s", sqlType.getType());
        switch (sqlType) {
            case MYSQL -> this.createMysql();
            case SQLITE -> this.createSQLite();
        }

        try (SqlSession connection = SESSION_FACTORY.openSession()) {
            for (SqlTable value : SqlTable.values()) {
                try (Statement statement = connection.getConnection().createStatement()) {
                    statement.execute(value.getSql());
                }
            }
        } catch (Exception e) {
            Log.error(e, "sql error");
        }

    }
    public void reconnect() {
        Log.debug("Connection is closing...");
        close();
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

    protected void setLiteFactory(HikariDataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("dev", transactionFactory, dataSource);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(environment);

        // 注册 Mapper
        for (Mapper value : Mapper.values()) {
            configuration.addMapper(value.getMapperClass());
        }

        // 动态表名前缀
        TableNameHandler handler = (sql, original) -> {
            String prefix = Ari.instance.getConfig().getString("data.table-prefix", "ari_");
            return prefix + original;
        };
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor dtni = new DynamicTableNameInnerInterceptor(handler);
        interceptor.addInnerInterceptor(dtni);

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        configuration.addInterceptor(interceptor);

        // 构建 SqlSessionFactory
        SESSION_FACTORY = new SqlSessionFactoryBuilder().build(configuration);
    }


    public static String getTablePrefix() {
        return Ari.instance.getConfig().getString("data.table-prefix", "ari");
    }

    public static void close() {
        if (SQLInstance.SESSION_FACTORY != null) {
            SQLInstance.SESSION_FACTORY = null;
            Log.debug("Connection closed successfully");
        }
        SQLInstance.SESSION_FACTORY = null;
    }

}
