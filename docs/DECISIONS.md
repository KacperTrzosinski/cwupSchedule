# Rejestr decyzji architektonicznych i projektowych (DECISIONS.md)

Poniższy dokument rejestruje wszystkie istotne założenia i decyzje techniczne podjęte podczas tworzenia aplikacji.

---

### [2026-10-03] Decyzja 1: Struktura modułów projektu
- **Kontekst:** Wymóg separacji parsera bez zależności od frameworka Android.
- **Decyzja:** Podział na dwa moduły Gradle:
  1. `:parser` – moduł czysto Javowy/Kotlinowy (JVM) zależny wyłącznie od Jsoup oraz bibliotek standardowych. Umożliwia szybkie testowanie jednostkowe na JVM bez uruchamiania emulatora ani mockowania Android SDK.
  2. `:app` – moduł aplikacji na system Android (Jetpack Compose, Room, WorkManager, Hilt, Glance).

### [2026-10-03] Decyzja 2: Obsługa kodowania znaków ISO-8859-2
- **Kontekst:** Strona źródłowa serwuje treść zakodowaną w standardzie `ISO-8859-2`. Domyślne parsowanie jako UTF-8 powoduje błędy dekodowania polskich znaków diakrytycznych.
- **Decyzja:** Zarówno pobieranie sieciowe (OkHttp), jak i wczytywanie fixtures z zasobów testowych jawnie wymuszają dekodowanie z `Charset.forName("ISO-8859-2")`.

### [2026-10-03] Decyzja 3: Architektura powiadomień bez ciągłego Foreground Service
- **Kontekst:** Ciągły Foreground Service zużywa zasoby i baterię użytkownika, a w nowoczesnych wersjach Androida (14+) stałe powiadomienia i tak mogą być odrzucane.
- **Decyzja:** Wykorzystanie `AlarmManager.setExactAndAllowWhileIdle` do precyzyjnego budzenia aplikacji 60 minut przed zajęciami, na start każdego bloku oraz na zakończenie dnia. Aktualizacja powiadomienia następuje w miejscu za pomocą stałego ID i `setOnlyAlertOnce(true)`.

### [2026-10-03] Decyzja 4: Pakiety aplikacji i licencja
- **Decyzja:** Główny pakiet aplikacji to `pl.legnica.planzajec`, a pakiet parsera `pl.legnica.planzajec.parser`. Projekt wydawany jest na licencji MIT z wymogiem zachowania widocznej atrybucji pierwotnego autorstwa (Autor: Kacper Trzosiński).
