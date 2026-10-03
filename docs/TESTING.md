# Strategia Testowania & Weryfikacja Jakości — cwupSchedule

Dokument opisuje zestaw zautomatyzowanych testów oraz procedury manualnej weryfikacji na fizycznych urządzeniach z systemem Android.

---

## 1. Zestaw Zautomatyzowanych Testów Jednostkowych

Wszystkie testy jednostkowe są w pełni zautomatyzowane i wykonywane w procesie CI (GitHub Actions):

### Moduł `:parser` (Pure JVM)
- **`DepartmentParserTest`:** Weryfikuje poprawne wyciąganie wydziałów i identyfikatorów z menu nawigacyjnego uczelni.
- **`CourseGroupParserTest`:** Weryfikuje parsowanie kierunków i grup studenckich w widoku semestralnym.
- **`ScheduleParserTest`:**
  - Parsowanie widoku tygodniowego (`plan_s3PAM.html`).
  - Parsowanie widoku semestralnego ze strippingiem znaczników `<div>` w tabelach (`semestr_s3PAM.html`).
  - Wykrywanie wariantów ("Różne" / grupy laboratoryjne Legenda1, Legenda2).
  - Obsługa obu formatów dat (`DD.MM.YYYY` oraz `YYYY-MM-DD`).
- **`TeacherAndRoomParserTest`:** Weryfikuje ekstrakcję wykładowców oraz sal dydaktycznych (`nauczyciel_11.html`, `sala_11.html`).

### Moduł `:app` (Android Domain & Data Layer)
- **`ChangeDetectorTest`:** Weryfikuje detekcję nowych lekcji, zmian sali/godziny oraz odwołanych zajęć w porównaniu do poprzedniego stanu bazy.
- **`NotificationSchedulerEngineTest`:**
  - Test 1: Planowanie powiadomienia na 60 min przed zajęciami.
  - Test 2: Brak planowania, gdy pierwsze zajęcia są za więcej niż 12 godzin.
  - Test 3: Przejście do stanu trwania w trakcie bloku zajęć.
  - Test 4: Przejście do kolejnych zajęć po zakończeniu pierwszych.
  - Test 5: Wygaszenie powiadomienia po zakończeniu ostatniej lekcji w danym dniu.
  - Test 6: Poprawne oznaczanie zajęć online (`isOnline = true`) w powiadomieniu.
  - Test 7: Ignorowanie odwołanych lekcji (`ChangeFlag.CANCELLED`).

Uruchomienie wszystkich testów:
```bash
./gradlew test
```

---

## 2. Ręczne Testy na Urządzeniach Fizycznych (Wymagane)

Z uwagi na specyfikę systemu Android i agresywne mechanizmy optymalizacji baterii poszczególnych producentów OEM, przed wdrożeniem produkcyjnym zaleca się weryfikację na urządzeniach fizycznych:

### A. Test Niezawodności Powiadomień (OEM Doze & Battery Kill)
1. **Xiaomi / Redmi / POCO (HyperOS / MIUI):**
   - Sprawdź, czy powiadomienie pojawia się dokładnie 60 min przed zajęciami przy wygaszonym ekranie telefonu w trybie uśpienia.
   - Jeśli powiadomienie nie pojawia się: włącz *Autostart* i ustaw *Oszczędzanie energii: Bez ograniczeń* zgodnie z instrukcją w ekranie Ustawień aplikacji.
2. **Samsung Galaxy (One UI):**
   - Dodaj aplikację do *Nigdy nieusypiane aplikacje* i zweryfikuj działanie po 8 godzinach bezczynności w nocy.

### B. Test Działania 100% Offline
1. Wybierz grupę i załaduj plan zajęć przy włączonym Wi-Fi.
2. Włącz tryb samolotowy (brak Wi-Fi, brak danych komórkowych).
3. Zamknij aplikację z pamięci RAM i uruchom ponownie:
   - Sprawdź, czy plan wyświetla się natychmiastowo z bazy Room.
   - Sprawdź, czy przełączanie między zakładkami "Plan" i "Kalendarz" działa płynnie.

### C. Test Widgetu Pulpitu (Glance AppWidget)
1. Dodaj widget aplikacji na ekran główny urządzenia.
2. Przetestuj zmianę rozmiaru:
   - Rozmiar 2x2: widoczna 1 najbliższa lekcja i sala.
   - Rozmiar 4x2: widoczny nagłówek z datą i 2 lekcje.
   - Rozmiar 4x4: widoczna lista do 5 lekcji na cały dzień.
3. Kliknij w widget — zweryfikuj czy otwiera główny ekran planu zajęć.

### D. Test Filtrowania Podgrup Laboratoryjnych
1. W aplikacji wybierz grupę z podziałem laboratoryjnym (np. `s3PAM`).
2. Kliknij ikonę wyboru podgrupy w pasku nagłówka i wybierz `L1`.
3. Zweryfikuj, czy zajęcia grupy `L2` zniknęły z widoku planu i widgetu.
