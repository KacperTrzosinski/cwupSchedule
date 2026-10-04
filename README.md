# Plan Zajęć — Collegium Witelona (PWSZ Legnica)

[![CI](https://github.com/mixbux/cwupSchedule/actions/workflows/ci.yml/badge.svg)](https://github.com/mixbux/cwupSchedule/actions/workflows/ci.yml)
[![Release](https://github.com/mixbux/cwupSchedule/actions/workflows/release.yml/badge.svg)](https://github.com/mixbux/cwupSchedule/actions/workflows/release.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg)](https://developer.android.com)

Nowoczesna, natywna aplikacja na system Android do przeglądania planu zajęć studentów **Collegium Witelona Uczelnia Państwowa w Legnicy** (dawniej PWSZ im. Witelona w Legnicy: `http://www.plan.pwsz.legnica.edu.pl/`).

Zaprojektowana z naciskiem na szybkość działania, pełną pracę **offline**, brak reklam i śledzenia oraz minimalne zużycie energii.

---

## Główne Funkcjonalności

- **Praca 100% Offline (Offline-First):** Plan zapisywany w lokalnej bazie SQLite (Room). Po jednorazowym pobraniu dostępny w piwnicach i salach bez zasięgu.
- **Dwa Widoki Planu:**
  - *Najbliższe zajęcia:* chronologiczna lista z dynamicznym licznikiem czasu do zajęć ("za 32 min", "trwa od 15 min") i wskaźnikiem postępu.
  - *Dzień po dniu:* paginowany przegląd całego tygodnia z wyborem daty.
- **Powiadomienia Exact Alarm:**
  - Stałe powiadomienie z odliczaniem na 60 minut przed pierwszymi zajęciami danego dnia.
  - Aktualizacja w miejscu (ten sam ID powiadomienia, `setOnlyAlertOnce(true)`) w trakcie trwania bloku i przejścia do następnego.
  - Automatyczne wygaszenie po zakończeniu ostatniej lekcji.
  - Zero drenowania baterii — brak działającego w tle serwisu (brak Foreground Service).
- **Interaktywny Kalendarz:** Widok miesiąca z kropkami oznaczającymi dni z zajęciami i natychmiastowym podglądem planu po kliknięciu w dany dzień.
- **Wyszukiwarka Globalna:**
  - Wyszukiwanie planów wszystkich grup, wykładowców i sal.
  - Szybki podgląd planu prowadzącego lub sali bez zmiany własnej grupy domyślnej.
  - Funkcja *Wolne sale teraz* — ułatwia znalezienie cichego miejsca do nauki w okienku.
- **Widget Pulpitu (Jetpack Glance):**
  - Trzy responsywne rozmiary (2x2, 4x2, 4x4) w dopasowanym ciemnym stylu.
  - Bezpośredni deep link do aplikacji po kliknięciu.
- **Filtrowanie i Personalizacja:**
  - Wybór podgrupy laboratoryjnej/ćwiczeniowej (L1, L2, C1, C2).
  - Scalanie kolejnych bloków tego samego przedmiotu.
  - Tryb Głęboka czerń (AMOLED) z oszczędzaniem energii na ekranach OLED.

---

## Architektura i Moduły

Projekt wykorzystuje architekturę warstwową zgodną z zaleceniami Google Modern Android Development (MAD):

```
cwupSchedule/
├── parser/            # Czysty moduł JVM (Kotlin, Jsoup, JUnit 5)
│                      # Niezależny od Android SDK, łatwy do testowania w CI
└── app/               # Moduł Android (Jetpack Compose, Hilt, Room, Glance)
    ├── data/          # Baza Room, DataStore, Network Client, Sync Worker
    ├── notification/  # AlarmManager scheduler engine i NotificationPublisher
    ├── ui/            # Ekrany Compose (Plan, Calendar, Search, Settings, Onboarding)
    │   └── theme/     # Customowy motyw ciemny (Dark Surface + Lesson Gradients)
    └── widget/        # Jetpack Glance AppWidget
```

Szczegółowy opis architektury znajduje się w dokumencie [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Wymagania i Kompilacja

### Wymagania systemowe
- **JDK:** Java 21 LTS (np. Eclipse Temurin 21)
- **Android SDK:** Compile SDK 35, Min SDK 26 (Android 8.0 Oreo), Target SDK 35 (Android 15)
- **Gradle:** 8.11.1 (wbudowany Gradle Wrapper)

### Budowanie z linii poleceń

```bash
# Uruchomienie testów modułu parsera i aplikacji:
./gradlew test

# Budowanie wersji deweloperskiej (Debug APK):
./gradlew :app:assembleDebug

# Budowanie wersji produkcyjnej (Release APK):
./gradlew :app:assembleRelease
```

Wyjściowy plik instalacyjny znajduje się w:
`app/build/outputs/apk/release/app-release.apk`

---

## Prywatność i Bezpieczeństwo

Aplikacja:
- **Nie zbiera ani nie przetwarza** żadnych danych osobowych.
- **Nie posiada** modułów analitycznych ani reklamowych (brak Firebase Analytics, AdMob itp.).
- Łączy się **wyłącznie** bezpośrednio z oficjalnym serwerem planu uczelni (`plan.pwsz.legnica.edu.pl`).
- Kod źródłowy jest w 100% jawny i otwarty na licencji [MIT z wymogiem atrybucji autora](LICENSE) (Autor: Kacper Trzosiński).

---

## Zrzeczenie Odpowiedzialności (Disclaimer)

*Projekt jest niezależną, studencką inicjatywą open source i nie jest oficjalnie wspierany ani sponsorowany przez Collegium Witelona Uczelnia Państwowa w Legnicy. Znak towarowy i prawa do planów zajęć należą do uczelni.*
