# Penny Control Backend

A Spring Boot backend application for financial control and management.

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **Spring Web**
- **Lombok**
- **Gradle** (build tool)

## Prerequisites

- Java 21 or higher
- Gradle (or use the included Gradle wrapper)

## Getting Started

### Build the Project

```bash
./gradlew build
```

### Run the Application

```bash
./gradlew bootRun
```

### Run Tests

```bash
./gradlew test
```

## Project Structure

```
penny-control-backend/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/pennycontrol/pennycontrolbackend/
│   └── test/
│       └── java/
│           └── com/pennycontrol/pennycontrolbackend/
├── build.gradle
└── settings.gradle
```

## Development

This project uses:

- Spring Boot for the application framework
- JPA for database interactions
- Lombok to reduce boilerplate code

## License

All rights reserved.