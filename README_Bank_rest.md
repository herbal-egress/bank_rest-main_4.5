# Bank REST API

REST API для управления банковскими картами, транзакциями и пользователями. Приложение реализовано с использованием Java 17, Spring Boot, Spring Security, PostgreSQL, Liquibase и Docker. Поддерживает роли `ADMIN` и `USER`, JWT-аутентификацию, маскирование номеров карт и документирование API через Swagger UI.

## Требования

- Java 17
- Maven 3.8+
- Docker и Docker Compose
- IntelliJ IDEA (для запуска через IDE)
- Git

## Запуск приложения через Maven

1. **Подготовка окружения**
   - Убедитесь, что Java 17 и Maven установлены: `java -version`, `mvn -version`.
   - Установите Docker Desktop (Windows/Mac) или Docker (Linux): https://www.docker.com/get-started.
   - Проверьте, что Docker запущен: `docker --version`.

2. **Настройка PostgreSQL через Docker**
   - Убедитесь, что файл `docker-compose.yml` находится в корне проекта.
   - Выполните команду для запуска PostgreSQL:
     ```bash
     docker-compose up -d
     ```
   - Проверьте, что контейнер `bank_postgres` работает: `docker ps`.

3. **Генерация кода через OpenAPI Generator**
   - Выполните команду в корне проекта (где находится `pom.xml`):
     ```bash
     mvn clean generate-sources
     ```
   - Сгенерированный код появится в `target/generated-sources`.

4. **Сборка и запуск приложения**
   - Убедитесь, что кодировка консоли поддерживает UTF-8:
     ```bash
     chcp 65001
     ```
   - Скомпилируйте проект: `mvn compile`.
   - Запустите приложение:
     ```bash
     mvn spring-boot:run
     ```

5. **Проверка**
   - Откройте Swagger UI по адресу: `http://localhost:8080/swagger-ui.html`.
   - Проверьте корректность отображения русского текста в логах.

## Запуск приложения через IntelliJ IDEA

1. **Подготовка окружения**
   - Убедитесь, что Java 17 и IntelliJ IDEA установлены.
   - Установите Docker Desktop (Windows/Mac) или Docker (Linux): https://www.docker.com/get-started.
   - Проверьте, что Docker запущен: `docker --version`.

2. **Настройка PostgreSQL через Docker**
   - Откройте терминал в IntelliJ IDEA или PowerShell.
   - Убедитесь, что `docker-compose.yml` находится в корне проекта.
   - Выполните:
     ```bash
     docker-compose up -d
     ```
   - Проверьте, что контейнер `bank_postgres` работает: `docker ps`.

3. **Импорт проекта**
   - Откройте IntelliJ IDEA.
   - Выберите `File > Open` и укажите корневую папку проекта (`pom.xml`).
   - Дождитесь завершения индексации и загрузки зависимостей Maven.

4. **Настройка конфигурации запуска**
   - Нажмите `Add Configuration` в верхнем правом углу.
   - Выберите `+ > Spring Boot`.
   - Укажите:
     - Main class: `com.example.bankcards.BankRestApplication`
     - Active profiles: `dev`
     - VM options: `-Dfile.encoding=UTF-8`
   - Сохраните конфигурацию.

5. **Генерация кода через Maven**
   - Откройте вкладку `Maven` в IntelliJ IDEA (справа).
   - Выполните: `bank-rest > Lifecycle > clean`, затем `generate-sources`.

6. **Запуск и проверка**
   - Запустите приложение, нажав `Run` (зеленый треугольник) для созданной конфигурации.
   - Откройте Swagger UI: `http://localhost:8080/swagger-ui.html`.
   - Проверьте логи в консоли IDEA — русский текст должен отображаться корректно.

## Полная инструкция по запуску после скачивания из репозитория

1. **Клонирование репозитория**
   - Убедитесь, что Git установлен: `git --version`.
   - Склонируйте репозиторий:
     ```bash
     git clone https://github.com/herbal-egress/bank_rest-main_4.5.git
     ```
   - Перейдите в папку проекта:
     ```bash
     cd bank_rest-main_4.5
     ```

2. **Подготовка окружения**
   - Установите Java 17: `java -version`.
   - Установите Maven: `mvn -version`.
   - Установите Docker Desktop (Windows/Mac) или Docker (Linux): https://www.docker.com/get-started.
   - Проверьте, что Docker запущен: `docker --version`.

3. **Настройка PostgreSQL через Docker**
   - Убедитесь, что файл `docker-compose.yml` находится в корне проекта.
   - Выполните:
     ```bash
     docker-compose up -d
     ```
   - Проверьте, что контейнер `bank_postgres` работает: `docker ps`.

4. **Сборка проекта**
   - Загрузите зависимости Maven:
     ```bash
     mvn dependency:resolve
     ```
   - Сгенерируйте код OpenAPI:
     ```bash
     mvn clean generate-sources
     ```
   - Скомпилируйте проект:
     ```bash
     mvn compile
     ```

5. **Запуск приложения**
   - Настройте кодировку консоли для поддержки русского языка:
     ```bash
     chcp 65001
     ```
   - Запустите приложение:
     ```bash
     mvn spring-boot:run
     ```

6. **Проверка**
   - Откройте Swagger UI: `http://localhost:8080/swagger-ui.html`.
   - Проверьте логи в консоли — русский текст (например, «Приложение успешно запущено») должен отображаться корректно.
   - Для тестирования API используйте эндпоинты, описанные в `docs/openapi.yaml`.

## Потенциальные проблемы и решения

- **Порт 5432 занят**: Если порт занят локальным PostgreSQL, измените порт в `docker-compose.yml` (например, `5433:5432`) и обновите `application.yml`/`application-test.yml` (`jdbc:postgresql://localhost:5433/bankdb`).
- **Кодировка логов**: Если русский текст отображается некорректно, проверьте, что в `application.yml` и `application-test.yml` установлены `spring.main.banner-mode=off` и `logging.charset=UTF-8`, а в `pom.xml` — `project.build.sourceEncoding=UTF-8` и JVM параметр `-Dfile.encoding=UTF-8`.
- **Конфликты зависимостей**: Выполните `mvn dependency:tree` для проверки конфликтов.

## Тестирование

- Для запуска тестов выполните:
  ```bash
  mvn test
  ```
- Testcontainers автоматически запустит PostgreSQL в Docker для тестового окружения.