# Банковское приложение (Bank Application)

**Bank Application** — это приложение для управления банковскими счетами, позволяющее пользователям выполнять операции с балансом, просматривать информацию о счетах и обрабатывать транзакции. Приложение предоставляет REST API для взаимодействия с банковскими данными.

## Основные функции

1. **Управление счетами**:

    - Создание, редактирование и удаление банковских счетов.
    - Просмотр информации о счетах и их балансе.

2. **Транзакции**:

    - Проведение переводов между счетами.
    - Просмотр истории транзакций.
    - Фильтрация транзакций по дате и типу.

3. **Аналитика**:

    - Статистика по поступлениям и расходам.
    - Визуализация финансовых данных.

4. **REST API**:

    - Интеграция с другими сервисами через REST API.

---

## Технологии

- **Язык программирования**: Java 17
- **Фреймворк**: Spring Boot 3.4.2
- **База данных**: PostgreSQL
- **Сборка**: Maven
- **Документация API**: Swagger
- **Контейнеризация**: Docker

## Установка и запуск

### Требования

- Установленная Java 17 или выше.
- Установленный Maven.
- Установленная PostgreSQL.
- Установленный Docker (опционально, для контейнерного запуска).

### SonarCloud

[Sonar](https://sonarcloud.io/project/overview?id=LINKERUU_Bank)

### Шаги для запуска

Перед запуском рекомендуется выполнить анализ кода с помощью Sonar:

```sh
mvn clean verify sonar:sonar
```

```sh
git clone https://github.com/ваш-username/bank-application.git
cd bank-application
mvn clean install
mvn spring-boot:run
```



