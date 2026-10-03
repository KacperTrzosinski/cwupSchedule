# Przewodnik Dewelopera — cwupSchedule

Dokument opisuje konfigurację środowiska programistycznego oraz standardy pracy nad projektem **cwupSchedule**.

---

## 1. Wymagania Środowiskowe

- **IDE:** Android Studio (rekomendowane: Ladybug / Koala Feature Drop) lub IntelliJ IDEA Ultimate.
- **Java Development Kit (JDK):** Java 21 LTS (Oracle JDK 21 lub Eclipse Temurin 21).
- **Android SDK:**
  - Build Tools: 35.0.0
  - Compile SDK: 35
  - Min SDK: 26 (Android 8.0 Oreo)
  - Target SDK: 35 (Android 15)

---

## 2. Pierwsze Uruchomienie

1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/mixbux/cwupSchedule.git
   cd cwupSchedule
   ```
2. Ustaw zmienną `JAVA_HOME` na ścieżkę instalacji JDK 21:
   - **Linux/macOS:**
     ```bash
     export JAVA_HOME=/path/to/jdk-21
     ```
   - **Windows (PowerShell):**
     ```powershell
     $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
     ```
3. Zbuduj projekt i uruchom testy jednostkowe:
   ```bash
   ./gradlew test assembleDebug
   ```

---

## 3. Struktura Katalogów

```
cwupSchedule/
├── gradle/
│   └── libs.versions.toml   # Centralny katalog wersji (Version Catalog)
├── parser/                  # Czysty moduł JVM
│   ├── src/main/java/       # Klasy parserów (ScheduleParser, DepartmentParser itd.)
│   └── src/test/            # Testy jednostkowe z prawdziwymi fixture HTML
├── app/                     # Moduł aplikacji Android
│   ├── src/main/java/
│   │   └── pl/legnica/planzajec/
│   │       ├── data/        # Room, DataStore, Network Client, WorkManager
│   │       ├── di/          # Moduły Hilt (DatabaseModule, NetworkModule)
│   │       ├── notification/# Silnik AlarmManager, powiadomienia
│   │       ├── ui/          # Compose UI (theme, components, navigation)
│   │       └── widget/      # Jetpack Glance AppWidget
│   └── src/main/res/        # Zasoby (ikony, układy Glance, konfiguracja sieci)
└── docs/                    # Dokumentacja techniczna i architektoniczna
```

---

## 4. Standardy Kodowania i Konwencje

- **Wcięcia:** 4 spacje (brak tabulatorów).
- **Formatowanie kodu:** Zgodne z oficjalnym stylem Kotlina (`ktlint` / `detekt`).
- **Język w kodzie:**
  - Kod techniczny, nazwy klas, funkcji, zmiennych oraz komentarze deweloperskie: **Język angielski**.
  - Teksty wyświetlane użytkownikowi w interfejsie (UI) oraz dokumentacja w `docs/`: **Język polski**.
- **Komunikacja z serwerem uczelni:**
  - Kodowanie: Zawsze jawne `ISO-8859-2`.
  - Protokół: Zwykłe `http://` (serwer uczelni nie posiada prawidłowego certyfikatu SSL dla subdomeny planu).
- **Bezpieczeństwo:**
  - Bezwzględny zakaz logowania lub commitowania kluczy, haseł i wrażliwych danych.
