package backend.academy.scrapper;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.SearchPathResourceAccessor;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

// isolated from the "bot" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    private String changeLogFile = "/master.xml";
    private String changeLogDir = "../migrations";

    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() throws SQLException {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17.4")
            .withExposedPorts(5432)
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("postgres");
        container.start();
        migrate(container);
        return container;
    }

    public void migrate(PostgreSQLContainer<?> container) throws SQLException {
        String username = container.getUsername();
        String password = container.getPassword();
        String jdbcUrl = container.getJdbcUrl();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (Database database = new PostgresDatabase()) {
                database.setConnection(new JdbcConnection(conn));
                Map<String, Object> scopeObjects = new HashMap<>();
                scopeObjects.put(liquibase.Scope.Attr.database.name(), database);
                SearchPathResourceAccessor resourceAccessor = new SearchPathResourceAccessor(
                    new DirectoryResourceAccessor(Paths.get(changeLogDir).toFile()));
                scopeObjects.put(liquibase.Scope.Attr.resourceAccessor.name(), resourceAccessor);
                Scope.child(scopeObjects, () -> {
                    liquibase.command.CommandScope updateCommand = new liquibase.command.CommandScope(liquibase.command.core.UpdateCommandStep.COMMAND_NAME);
                    updateCommand.addArgumentValue(liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
                    updateCommand.addArgumentValue(liquibase.command.core.UpdateToTagCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
                    updateCommand.execute();
                });
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @RestartScope
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
    }

}
