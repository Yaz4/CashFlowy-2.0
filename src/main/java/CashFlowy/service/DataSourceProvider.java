package CashFlowy.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DataSourceProvider implements the Singleton pattern and acts as a simple Factory
 * for creating and providing a configured {@link HikariDataSource} instance.
 * This decouples clients from the concrete configuration details and centralizes
 * resource management.
 */
public final class DataSourceProvider {

    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    // Keeping default values as in Main.java to preserve behavior
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/jdbc_schema?user=user&password=secret&ssl=false";

    private static volatile DataSourceProvider instance;
    private final HikariDataSource dataSource;

    private DataSourceProvider() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(JDBC_DRIVER);
        config.setJdbcUrl(JDBC_URL);
        config.setLeakDetectionThreshold(2000);
        this.dataSource = new HikariDataSource(config);
    }

    public static DataSourceProvider getInstance() {
        if (instance == null) {
            synchronized (DataSourceProvider.class) {
                if (instance == null) {
                    instance = new DataSourceProvider();
                }
            }
        }
        return instance;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
