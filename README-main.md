# Bank Cards REST API

Это REST API для управления банковскими картами и транзакциями. Приложение построено на Spring Boot и предоставляет
функционал для просмотра карт, блокировки карт, проверки баланса и выполнения переводов между картами.

## Требования

- **Java**: 17
- **Maven**: 3.8.0 или выше
- **PostgreSQL**: 13 или выше
- **Liquibase**: Для миграций базы данных (опционально, включен в продакшен)
- **Git**: Для клонирования репозитория

## Зависимости

Gроверьте `pom.xml` для точного списка:
- `spring-boot-starter-web` — для REST API
- `spring-boot-starter-data-jpa` — для работы с базой данных
- `spring-boot-starter-security` — для JWT-авторизации
- `postgresql` — драйвер PostgreSQL
- `liquibase-core` — для миграций базы данных
- `springdoc-openapi-starter-webmvc-ui` — для Swagger UI
- `spring-boot-starter-test` — для тестирования

## Установка и настройка

### 1. Клонирование репозитория

```bash
git clone https://github.com/herbal-egress/bank_rest-main_3.2.git
cd bank_rest-main_3.2
```

### 2. Настройка PostgreSQL

1. Убедитесь, что PostgreSQL установлен и запущен.
2. Создайте базу данных `bankdb`:

```sql
CREATE DATABASE bankdb;
```

3. Настройте учетные данные в `src/main/resources/application.yml`. Пример:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankdb?currentSchema=public
    username: postgres
    password: 123
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
```

### 3. Настройка окружения

- Проверьте, что Java 17 и Maven установлены:

```bash
java -version
mvn -version
```

- (Опционально) Настройте переменные окружения для `JWT_SECRET` и `JWT_EXPIRATION`, если они не указаны в `application.yml`.

### 4. Сборка и запуск

1. Соберите проект:

```bash
mvn clean install
```

2. Запустите приложение:

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу `http://localhost:8080`.

### 5. Настройка тестов

Тесты используют схему `test` в PostgreSQL или H2 (в зависимости от конфигурации). Настройте `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankdb?currentSchema=test
    username: postgres
    password: 123
  jpa:
    properties:
      hibernate:
        default_schema: test
  liquibase:
    enabled: false
```

Запустите тесты:

```bash
mvn test
```

### 6. Доступ к Swagger UI

После запуска приложения откройте Swagger UI для просмотра API-документации:

```
http://localhost:8080/swagger-ui.html
```

Спецификация OpenAPI доступна в `docs/openapi.yaml`.

## Использование API

1. **Аутентификация**:
   - Получите JWT-токен через эндпоинт аутентификации (например, `/api/auth/login`, если реализован).
   - Добавьте токен в заголовок `Authorization: Bearer <токен>` для всех запросов.

2. **Основные эндпоинты** (требуется роль `USER`):
   - `GET /api/user/cards?page=0&size=10&sort=balance` — Получить список карт пользователя.
   - `POST /api/user/cards/{id}/block` — Заблокировать карту.
   - `GET /api/user/cards/{id}/balance` — Проверить баланс карты.
   - `POST /api/user/transactions/transfer` — Выполнить перевод между картами.

Пример запроса для перевода:

```bash
curl -X POST http://localhost:8080/api/user/transactions/transfer \
-H "Authorization: Bearer <токен>" \
-H "Content-Type: application/json" \
-d '{"fromCardId": 1, "toCardId": 2, "amount": 100.0}'
```

## Миграции базы данных

- В продакшене Liquibase применяет миграции из `src/main/resources/db/changelog/db.changelog-master.xml`.
- В тестах миграции отключены, используются SQL-скрипты:
  - `001-initial-schema-test.sql` — создание схемы `test` и таблиц.
  - `002-initial-data-test.sql` — вставка тестовых данных (5 карт).

## Тестирование

- Тесты находятся в `src/test/java/com/example/bankcards/controller`.
- Используется `@SpringBootTest` и `MockMvc` для интеграционного тестирования.
- Тестовая база данных инициализируется скриптами из `src/test/resources/db/changelog/changes`.

## Возможные проблемы и решения

1. **Ошибка подключения к PostgreSQL**:
   - Убедитесь, что PostgreSQL запущен и учетные данные в `application.yml` верны.
2. **Тесты не находят данные**:
   - Проверьте, что `application-test.yml` содержит `currentSchema=test`.
3. **Liquibase конфликты**:
   - Убедитесь, что Liquibase включен только в продакшене (`spring.liquibase.enabled=true`).
```