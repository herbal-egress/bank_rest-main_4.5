
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// добавленный код: Аннотация @Suite определяет тестовую сюиту для JUnit 5
@Suite
// добавленный код: @SelectPackages указывает пакет, в котором JUnit будет искать все тесты
@SelectPackages("com.example.bankcards")
// добавленный код: @SpringBootTest загружает контекст Spring для тестов
@SpringBootTest
// добавленный код: @Testcontainers включает поддержку Testcontainers для управления контейнерами
@Testcontainers
public class AllTestsSuite {

    // добавленный код: Создаем контейнер PostgreSQL для тестовой базы данных
    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    // добавленный код: Динамически регистрируем настройки базы данных для Spring
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        // добавленный код: Указываем Liquibase использовать тестовую базу данных
        registry.add("spring.liquibase.url", postgresContainer::getJdbcUrl);
        registry.add("spring.liquibase.user", postgresContainer::getUsername);
        registry.add("spring.liquibase.password", postgresContainer::getPassword);
        // добавленный код: Указываем тестовый changelog для миграций
        registry.add("spring.liquibase.change-log", () -> "classpath:db.changelog-test.xml");
    }
}