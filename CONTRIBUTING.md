# Jak Współtworzyć Projekt — cwupSchedule

Dziękujemy za zainteresowanie rozwojem aplikacji **cwupSchedule**! Poniżej znajdują się wskazówki dotyczące zgłaszania błędów, propozycji nowych funkcji oraz tworzenia Pull Requestów.

---

## 1. Zgłaszanie Błędów (Bug Reports)

Przed otwarciem nowego zgłoszenia:
1. Sprawdź, czy problem nie został już zgłoszony w zakładce **Issues**.
2. W opisie podaj:
   - Model urządzenia i wersję systemu Android (np. *Xiaomi 13, Android 14 / HyperOS*).
   - Wersję aplikacji (np. *v1.0.0*).
   - Wybrany wydział, kierunek i grupę studencką, dla której wystąpił błąd.
   - Kroki do zreprodukowania problemu oraz (jeśli to możliwe) zrzuty ekranu.

---

## 2. Rozwój Kodu i Pull Requests

1. **Fork & Branch:**
   Stwórz fork repozytorium i załóż nową gałąź o opisowej nazwie:
   ```bash
   git checkout -b feature/nowa-funkcjonalnosc
   # lub
   git checkout -b fix/naprawa-bledu-parsowania
   ```
2. **Standardy Kodu:**
   - Wcięcia: 4 spacje.
   - Nazwy zmiennych i funkcji: `lowerCamelCase`.
   - Klasy: `PascalCase`.
   - Język kodu technicznego i komentarzy: **angielski**.
   - Język tekstów w UI i dokumentacji: **polski**.
   - Zero wrażliwych danych (brak kluczy i haseł w repozytorium).
3. **Konwencja Commitów:**
   Stosujemy format [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat(...)`: nowa funkcjonalność
   - `fix(...)`: poprawka błędu
   - `docs(...)`: zmiany w dokumentacji
   - `test(...)`: dodanie lub aktualizacja testów
   - `refactor(...)`: refaktoryzacja bez zmian w zachowaniu
4. **Weryfikacja Przed Wysłaniem:**
   Upewnij się, że projekt kompiluje się i wszystkie testy przechodzą:
   ```bash
   ./gradlew test assembleDebug
   ```
5. **Otwarcie Pull Requesta:**
   Opisz krótko cel wprowadzonych zmian i podlinkuj powiązane zgłoszenia (`Closes #...`).
