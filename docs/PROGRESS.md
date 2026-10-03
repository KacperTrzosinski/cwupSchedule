# Postęp prac nad projektem Plan Zajęć PWSZ Legnica

Statusy zadań:
- `[ ]` – do zrobienia
- `[~]` – w toku
- `[x]` – zrobione i zweryfikowane
- `[!]` – zablokowane (z uzasadnieniem)

---

## Lista kontrolna etapów

### Przygotowanie i Rekonesans (Etap 0)
- [x] Utworzenie `docs/PROGRESS.md` oraz `docs/DECISIONS.md` (2026-10-03)
- [x] Pobranie rzeczywistych stron HTML z planu (wydziały 1, 2, 7, 10, 11, grupy stacjonarne/niestacjonarne, sale, nauczyciele) (2026-10-03)
- [x] Zapisanie stron jako fixtures w `parser/src/test/resources/fixtures/` z zachowaniem kodowania ISO-8859-2 (2026-10-03)
- [x] Sporządzenie dokumentu `docs/DATA_SOURCE.md` z opisem struktury, adresów i wyjątków (2026-10-03)

### Etap 1: Szkielet projektu i CI
- [x] Inicjalizacja struktury Gradle (root, `:app`, `:parser`) z Version Catalog (`libs.versions.toml`) (2026-10-03)
- [x] Konfiguracja Gradle Wrapper (Gradle 8.11.1, Java 21) i wtyczek (Kotlin 2.0, Compose, Hilt, Room KSP) (2026-10-03)
- [x] Konfiguracja `.gitignore` i licencji MIT (2026-10-03)
- [x] Utworzenie bazowego workflow `.github/workflows/ci.yml` (2026-10-03)
- [x] Weryfikacja buildu bazowego projektu (`:parser:test`, `:app:assembleDebug` - green) (2026-10-03)

### Etap 2: Parser i testy jednostkowe (`:parser`)
- [x] Model danych w module `:parser` (wydziały, kierunki, grupy, lekcje, sale, nauczyciele, warianty) (2026-10-03)
- [x] Implementacja parsera listy wydziałów, kierunków i grup (2026-10-03)
- [x] Implementacja parsera planu zajęć (podgrupy, bloki 45 min, parzystość tygodni, legenda skrótów, zajęcia online, wpisy „Różne”) (2026-10-03)
- [x] Implementacja parsera widoków nauczyciela i sal (2026-10-03)
- [x] Zestaw testów jednostkowych na przygotowanych fixtures (różne wydziały i tryby) (2026-10-03)
- [x] Utworzenie dokumentacji parsera `docs/PARSER.md` (2026-10-03)
- [x] Weryfikacja: zielone testy JVM w module `:parser` (11/11 testów zaliczonych) (2026-10-03)

### Etap 3: Warstwa danych (`:app`)
- [x] Konfiguracja sieci (OkHttp z dekodowaniem ISO-8859-2, obsługa HTTP / `networkSecurityConfig`, retry z backoffem) (2026-10-03)
- [x] Baza danych Room (encje, DAO, migracje) dla cache planu i metadanych (2026-10-03)
- [x] DataStore Preferences (wybrana grupa, podgrupa, preferencje motywu, filtry, tryb widoku) (2026-10-03)
- [x] Repozytorium z obsługą trybu offline i wykrywaniem zmian (poprzedni vs aktualny stan) (2026-10-03)
- [x] Zadanie WorkManager do okresowej synchronizacji planu w tle (2026-10-03)
- [x] Testy jednostkowe repozytorium i mechanizmu wykrywania zmian (2026-10-03)

### Etap 4: Onboarding z dropdownami
- [ ] Implementacja ViewModel i stanu wyboru kaskadowego: Wydział → Kierunek → Rok → Grupa → Wyszukaj
- [ ] Ekran Onboardingu z ciemnym motywem, „szklanymi” polami i animacjami
- [ ] Okno wyboru podgrupy po wyszukaniu
- [ ] Obsługa uprawnień systemowych (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM)

### Etap 5: Ekran Plan (główny widok)
- [ ] Widok „Najbliższe” (lista chronologiczna od teraz, trwające zajęcia, przyklejane nagłówki dni, okienka)
- [ ] Widok „Dzień po dniu” (pager z paskiem dni)
- [ ] Karty zajęć (godziny, chip typu, chip sali / kamery Online, prowadzący, łącznik wieloblokowy)
- [ ] Wyróżnienie zajęć `Online_<nr>` (kolorystyka, ikona)
- [ ] Obsługa stanów: ładowanie (shimmer), błąd, brak zajęć (pusty stan)

### Etap 6: Silnik powiadomień
- [ ] Czysta klasa wyznaczająca stan powiadomienia i harmonogram alarmów (pure domain logic)
- [ ] Testy jednostkowe logiki harmonogramu (różne pory dnia, dni bez zajęć, odwołane bloki)
- [ ] Odbiornik `BroadcastReceiver` i harmonogramowanie `AlarmManager.setExactAndAllowWhileIdle`
- [ ] Aktualizacja powiadomienia w miejscu (`setOnlyAlertOnce(true)`) bez trwałego serwisu
- [ ] Obsługa zdarzeń `BOOT_COMPLETED` oraz zmiany strefy/zegara

### Etap 7: Kalendarz, Szukaj, klikalne szczegóły
- [ ] Ekran Kalendarza (widok miesiąca ze znacznikami dni z zajęciami, siatka tygodnia)
- [ ] Ekran Szukaj (wyszukiwanie grup, sal, prowadzących, wolnych sal)
- [ ] Klikalne sale i prowadzący z poziomu karty zajęć (przejście do ich planu)
- [ ] Oznaczanie zmian na kartach (nowe, zmieniona sala/godzina, odwołane)

### Etap 8: Widżet, animacje, dopracowanie UI
- [ ] Widżet ekranu głównego (Jetpack Glance) pokazujący najbliższe zajęcia
- [ ] Ekran Ustawień (zmiana grupy, filtry online/stacjonarne, scalanie bloków, instrukcje baterii)
- [ ] Dopracowanie animacji, ciemnego motywu AMOLED, tokenów designu
- [ ] Sprawdzenie dostępności (TalkBack, skalowanie czcionek, strefy dotykowe min 48dp)
- [ ] Wektorowa ikona aplikacji

### Etap 9: GitHub Actions i pełna dokumentacja
- [ ] Konfiguracja `.github/workflows/release.yml` (automatyczna budowa release APK z obsługą sekretów lub debug fallback)
- [ ] Pełna dokumentacja: `README.md`, `docs/ARCHITECTURE.md`, `docs/PARSER.md`, `docs/NOTIFICATIONS.md`, `docs/UI_DESIGN.md`, `docs/RELEASE.md`, `docs/DEVELOPMENT.md`, `docs/TESTING.md`
- [ ] Pliki `CHANGELOG.md`, `CONTRIBUTING.md`, `LICENSE` (MIT)

### Etap 10: Przegląd końcowy
- [ ] Pełna kompilacja projektu (`./gradlew assembleRelease` / `assembleDebug`)
- [ ] Uruchomienie wszystkich testów (`./gradlew test`)
- [ ] Walidacja lint / ktlint
- [ ] Sprawdzenie zgodności dokumentacji z kodem
- [ ] Końcowe podsumowanie wykonanych prac
