# Changelog

Wszystkie istotne zmiany w projekcie **cwupSchedule** będą dokumentowane w tym pliku.

Format bazuje na [Keep a Changelog](https://keepachangelog.com/pl/1.0.0/), a projekt stosuje [Semantic Versioning](https://semver.org/lang/pl/).

---

## [1.0.0] - 2026-10-03

### Dodano (Added)
- **Moduł parsera (`:parser`):**
  - Czysty moduł JVM do parsowania stron HTML z planu Collegium Witelona w Legnicy (`Jsoup`).
  - Parsowanie wydziałów (`DepartmentParser`), kierunków i grup (`CourseGroupParser`).
  - Pełne parsowanie planów tygodniowych i semestralnych z obsługą wariantów ("Różne", Legenda1, Legenda2).
  - Ekstrakcja wykładowców (`TeacherParser`) oraz sal dydaktycznych (`RoomParser`).
  - Zestaw 11 zautomatyzowanych testów jednostkowych na rzeczywistych fixture HTML.
- **Warstwa danych i offline (`:app`):**
  - Baza danych SQLite z biblioteką Room (`AppDatabase`, `LessonDao`, `StructureDao`, `MetadataDao`).
  - `UserPreferencesRepository` z DataStore Preferences do przechowywania ustawień i filtrów.
  - Klient sieciowy OkHttp z wymuszeniem kodowania `ISO-8859-2` i polityką ponawiania prób.
  - Wykrywacz zmian w planie (`ChangeDetector`).
  - Okresowa synchronizacja w tle przez `WorkManager` (`ScheduleSyncWorker`).
- **Powiadomienia Exact Alarm:**
  - Silnik harmonogramowania `NotificationSchedulerEngine` oparty o `AlarmManager.setExactAndAllowWhileIdle`.
  - Stałe powiadomienie na 60 min przed zajęciami z dynamicznym odliczaniem.
  - Cicha aktualizacja w miejscu bez drenowania baterii procesem Foreground Service.
- **Interfejs użytkownika (Jetpack Compose):**
  - Customowy motyw ciemny z dedykowanymi gradientami dla każdego typu zajęć akademickich.
  - Tryb "Głęboka czerń" (AMOLED).
  - Ekran powitalny (Onboarding) z kaskadowym wyborem grupy i dialogiem uprawnień w Androidzie 13+.
  - Ekran główny "Plan" z widokiem dualnym ("Najbliższe" ze sticky headers i odliczaniem + "Dzień po dniu").
  - Ekran "Kalendarz" z oznaczeniem dni z zajęciami i podglądem wybranego dnia.
  - Ekran "Szukaj" z wyszukiwarką grup, wykładowców, sal i funkcją "Wolne sale teraz".
  - Ekran "Ustawienia" z przełącznikami i instrukcją konfiguracji optymalizacji baterii dla Xiaomi, Samsung, Huawei.
- **Widget Pulpitu (Jetpack Glance):**
  - Trzy responsywne rozmiary (2x2, 4x2, 4x4) z automatyczną aktualizacją po zmianach w bazie.
- **Infrastruktura CI/CD:**
  - GitHub Actions CI workflow (`ci.yml`) testujący parser i aplikację.
  - GitHub Actions Release workflow (`release.yml`) budujący i publikujący oficjalne pliki APK i AAB.
  - Kompletna dokumentacja w języku polskim w katalogu `docs/`.
