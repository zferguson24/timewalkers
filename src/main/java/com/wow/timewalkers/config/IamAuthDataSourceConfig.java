package com.wow.timewalkers.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsUtilities;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

// zachs-db (the shared Aurora Serverless v2 cluster) is IAM-auth-only — there is no
// static password. Every new physical connection needs a fresh, short-lived auth token
// (valid 15 minutes), generated locally via SigV4 signing against whatever credentials
// are available (the Lambda execution role's, at runtime). HikariCP's maxLifetime is
// kept well under that 15-minute expiry so pooled connections are recycled — and
// re-authenticated with a fresh token — before their existing token could go stale.
@Profile("!local")
@Configuration
public class IamAuthDataSourceConfig {

    @Value("${PGHOST}")
    private String host;

    @Value("${PGPORT:5432}")
    private int port;

    @Value("${PGDATABASE}")
    private String database;

    @Value("${PGUSER}")
    private String user;

    @Value("${AWS_REGION:us-east-2}")
    private String region;

    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(new IamTokenPostgresDataSource(host, port, database, user, region));
        hikariConfig.setMaximumPoolSize(2);
        hikariConfig.setMaxLifetime(Duration.ofMinutes(10).toMillis());
        return new HikariDataSource(hikariConfig);
    }

    // Plain javax.sql.DataSource implementation, deliberately avoiding any compile-time
    // dependency on org.postgresql classes — the JDBC driver is only a runtime-scoped
    // dependency in pom.xml, loaded via the standard JDBC 4 ServiceLoader mechanism.
    private static final class IamTokenPostgresDataSource implements DataSource {

        private final String jdbcUrl;
        private final String hostname;
        private final int port;
        private final String user;
        private final RdsUtilities rdsUtilities;

        IamTokenPostgresDataSource(String host, int port, String database, String user, String region) {
            this.jdbcUrl = "jdbc:postgresql://%s:%d/%s".formatted(host, port, database);
            this.hostname = host;
            this.port = port;
            this.user = user;
            this.rdsUtilities = RdsUtilities.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        }

        @Override
        public Connection getConnection() throws SQLException {
            String token = rdsUtilities.generateAuthenticationToken(builder -> builder
                .hostname(hostname)
                .port(port)
                .username(user));
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", token);
            props.setProperty("sslmode", "require");
            return DriverManager.getConnection(jdbcUrl, props);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }
}
