package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.cj.conf.ConnectionUrl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class JdbcPool {

	private static String dbName;
	private static String userName;
	private static String password;

	private static String jdbcUrl;
	private static String jdbcDriver;

	private static HikariConfig config;
	private static HikariDataSource dataSource;

	private static final Logger LOGGER = LogManager.getLogger(JdbcPool.class);

	static {
		LOGGER.info("JdbcPool initializing...");
		setDbProperties();
		try {
			boolean autoCommit = true;
			int connectionTimeout = 30 * 1000; // Default: 30000 (30 seconds)

			int maximumPoolSize = 10; // Default: 10
			int maxLifetime = 1800 * 1000; // The minimum allowed value is 30000ms (30 seconds). Default: 1800000 (30 minutes)

			int minimumIdle = 10; //  Default: same as maximumPoolSize
			int idleTimeout = 600 * 1000; // The minimum allowed value is 10000ms (10 seconds). Default: 600000 (10 minutes)

			config = new HikariConfig();
			config.setPoolName("mysql hikari pool");

			config.setAutoCommit(autoCommit);
			config.setConnectionTimeout(connectionTimeout);

			config.setMaximumPoolSize(maximumPoolSize);
			config.setMaxLifetime(maxLifetime);

			config.setMinimumIdle(minimumIdle);
			config.setIdleTimeout(idleTimeout);

			config.setLeakDetectionThreshold(30000);
			config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
			config.addDataSourceProperty("useSSL", "false");
			config.addDataSourceProperty("characterEncoding", "UTF-8");
			config.addDataSourceProperty("useUnicode", "true");
			config.addDataSourceProperty("useJDBCCompliantTimezoneShift", "true");
			config.addDataSourceProperty("useLegacyDatetimeCode", "false");
			config.addDataSourceProperty("serverTimezone", "UTC");

			config.setUsername(userName);
			config.setPassword(password);
			config.setSchema(dbName);
			config.setJdbcUrl(jdbcUrl);
			config.setDriverClassName(jdbcDriver);

			dataSource = new HikariDataSource(config);

			LOGGER.info("JdbcPool initialized");
		} catch (Exception e) {
			LOGGER.fatal("Couldn't able to intialize JdbcPool, JdbcUrl: {}", jdbcUrl, e);
		}
	}

	private static void setDbProperties() {
		Properties configs = MessageProperty.getConfigs();

		if (configs == null) {
			configs = new Properties();
			LOGGER.error("No database configs found, iniatilizing JdbcPool with default database properties");
		}

		String hostName = configs.getProperty("host_name", ConnectionUrl.DEFAULT_HOST);
		int port = Integer.parseInt(configs.getProperty("port", "" + ConnectionUrl.DEFAULT_PORT));

		userName = configs.getProperty("user_name", "root");
		password = configs.getProperty("password", "root");
		dbName = configs.getProperty("db_name", "test");

		jdbcDriver = "com.mysql.cj.jdbc.Driver";
		jdbcUrl = "jdbc:mysql://" + hostName + ":" + port + "/" + dbName;
	}

	public static Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			LOGGER.fatal("Exception while getConnection from JdbcPool", e);
			return null;
		}
	}

	private JdbcPool() {}
}
