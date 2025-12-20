# Getting Started

## Prerequisites

- **Java 21 (JDK 21)**
- Docker

## Build

### macOS / Linux

```bash
./gradlew clean build
```

### Windows

```powershell
.\gradlew.bat clean build
```

## Database

Start db container

```bash
docker compose up db
```

## Run

### macOS / Linux

```bash
./gradlew bootRun
```

### Windows

```powershell
.\gradlew.bat bootRun
```

