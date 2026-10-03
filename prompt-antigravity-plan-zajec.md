# PROMPT DLA ANTIGRAVITY AI

Działasz w istniejącym repozytorium GitHub. Twoim zadaniem jest zbudować od zera kompletną, natywną aplikację na Androida do przeglądania planu zajęć PWSZ Legnica, skonfigurować GitHub Actions budujący plik `.apk` finalnej wersji oraz napisać pełną dokumentację projektu. Pracuj samodzielnie, od początku do końca. Pytaj mnie tylko wtedy, gdy jesteś naprawdę zablokowany. W pozostałych przypadkach przyjmij rozsądne założenie, zapisz je w `docs/DECISIONS.md` i idź dalej.

Język interfejsu aplikacji: polski. Język dokumentacji: polski. Kod, nazwy klas i komentarze techniczne: angielski.

---

## 0. Zasady pracy

1. Zacznij od zapoznania się z repozytorium (struktura, istniejące pliki, README, konfiguracja). Nie nadpisuj istniejących plików bez powodu. Jeśli repozytorium zawiera już jakiś kod, dopasuj się do niego lub zaproponuj w `docs/DECISIONS.md` jak go wykorzystałeś.
2. Pracuj etapami (sekcja 9). Po każdym etapie: zbuduj projekt, uruchom testy, popraw błędy, zrób osobny commit z czytelnym opisem (Conventional Commits, np. `feat(parser): ...`). Nie przechodź dalej z niedziałającym buildem.
3. Wszystko, co da się zweryfikować, zweryfikuj (build, testy jednostkowe, lint). Nie twierdź, że coś działa, jeśli tego nie uruchomiłeś. Jeśli czegoś nie da się sprawdzić w Twoim środowisku (np. zachowanie powiadomień na fizycznym telefonie), napisz to wprost w dokumentacji w sekcji „Weryfikacja ręczna".
4. Nie wymyślaj danych. Strukturę strony źródłowej poznaj, pobierając prawdziwe strony (sekcja 2), i oprzyj parser na tym, co faktycznie zobaczysz.
5. Na koniec podsumuj, co zrobiłeś, co jest niezweryfikowane i co ja muszę zrobić ręcznie (np. dodać sekrety do repozytorium).

### Tryb autonomiczny: pracuj w pętli, aż projekt będzie skończony

Masz pracować **bez przerwy i bez czekania na mnie**, w pętli, dopóki nie zostanie spełniona „Definicja ukończenia" (poniżej). Nie kończ tury po jednym etapie. Nie pytaj „czy kontynuować?", nie proponuj „następnych kroków" zamiast ich wykonania i nie zatrzymuj się z podsumowaniem, dopóki są niezrobione zadania.

**Źródło prawdy: `docs/PROGRESS.md`.** Na samym początku (przed Etapem 0) utwórz ten plik jako listę kontrolną wszystkich etapów z sekcji 9 rozbitych na konkretne zadania (z polami: status `[ ]` / `[~]` w toku / `[x]` zrobione / `[!]` zablokowane, krótka notatka, data). Aktualizuj go po każdym zadaniu i commituj razem ze zmianami.

**Pętla (powtarzaj w kółko):**
1. Przeczytaj `docs/PROGRESS.md` i `docs/DECISIONS.md` (rób to na początku **każdej** iteracji, także po utracie kontekstu lub wznowieniu pracy; to Twoja pamięć).
2. Wybierz następne niezakończone zadanie (najpierw zablokowane przez nic, potem według kolejności etapów).
3. Zaimplementuj je.
4. Zweryfikuj: build, testy jednostkowe, lint. Jeśli coś nie przechodzi, napraw i uruchom ponownie. Nie przechodź dalej na czerwonym buildzie.
5. Zrób commit, zaktualizuj `docs/PROGRESS.md` (i dokumentację, jeśli zmieniło się zachowanie).
6. Wróć do kroku 1.

**Gdy utkniesz (unikanie pętli bez końca):**
- Jeśli ten sam błąd lub ta sama próba nie działa **3 razy z rzędu**, zmień podejście (inna biblioteka, inny sposób, uproszczenie), zamiast powtarzać to samo.
- Jeśli po kilku różnych podejściach zadanie nadal jest zablokowane, oznacz je w `docs/PROGRESS.md` jako `[!]` z opisem przyczyny i tym, co próbowałeś, **przejdź do innych zadań** i wróć do zablokowanego na końcu, z nową wiedzą.
- Jeśli zadanie wymaga czegoś, co mogę zrobić tylko ja (sekrety w repozytorium, fizyczny telefon, decyzja biznesowa), zapisz to na liście „Do zrobienia ręcznie" w `docs/PROGRESS.md`, przyjmij rozsądne założenie i kontynuuj. To **nie** jest powód do zatrzymania pracy.

**Definicja ukończenia (zatrzymaj się dopiero, gdy WSZYSTKO poniżej jest prawdą):**
- [ ] Wszystkie zadania w `docs/PROGRESS.md` są `[x]`, a jedyne wyjątki to `[!]` z uzasadnieniem, których naprawdę nie dało się rozwiązać bez mnie.
- [ ] `./gradlew assembleRelease` (lub odpowiednik) przechodzi, a APK się buduje.
- [ ] Wszystkie testy i lint są zielone.
- [ ] Workflow `ci.yml` i `release.yml` istnieją i zostały zwalidowane (np. `actionlint`, jeśli dostępny, lub staranny przegląd składni i uprawnień).
- [ ] Dokumentacja z sekcji 11 jest kompletna i **zgodna z kodem** (przejrzyj ją na koniec i popraw rozbieżności).
- [ ] Wykonałeś jeden pełny, końcowy przebieg tej listy kontrolnej od zera. Jeśli cokolwiek z niej nie przechodzi, pętla trwa dalej.

**Uczciwość statusów:** nie oznaczaj zadania jako `[x]`, jeśli go nie zweryfikowałeś. Rzeczy, których nie da się sprawdzić w Twoim środowisku (np. działanie powiadomień na fizycznym telefonie), oznacz jako `[x] (niezweryfikowane ręcznie)` i dopisz do „Weryfikacja ręczna" w `docs/TESTING.md`.

Dopiero po spełnieniu Definicji ukończenia napisz końcowe podsumowanie (sekcja 14).

---

## 1. Cel i założenia

| Obszar | Decyzja |
|---|---|
| Platforma | Natywny Android: Kotlin + Jetpack Compose |
| Minimalne API | 26 (Android 8.0), `targetSdk`/`compileSdk` najnowsze stabilne |
| Zakres | Wszystkie wydziały, kierunki i tryby studiów (stacjonarne i niestacjonarne) |
| Dystrybucja | Prywatna: podpisany APK, bez Google Play |
| Styl | Własny: ciemny motyw z kolorowymi gradientowymi akcentami. Bez Material You / dynamicznych kolorów |
| Onboarding | Kaskadowe dropdowny: Wydział → Kierunek → Rok → Grupa → Wyszukaj, wybór zapamiętywany |
| Powiadomienia | Stałe powiadomienie „następne zajęcia" aktualizowane w miejscu |
| Kolejność zajęć | Od najbliższych do najdalszych czasowo, z podziałem na dni |
| Zajęcia online | Sala w formacie `Online_<liczba>` wyraźnie oznaczona |
| Źródło danych | Parsowanie HTML strony http://www.plan.pwsz.legnica.edu.pl/ (brak API) |

---

## 2. Źródło danych (zbadaj i zweryfikuj samodzielnie)

Strona to stary serwis PHP, bez API, bez iCal i bez JSON-a.

### Co już wiadomo (zweryfikuj)

- Strona główna: lista wydziałów pod adresami `schedule_view.php?site=show_kierunek.php&id=<id>`:
  - id=7 Wydział Nauk o Zdrowiu i Kulturze Fizycznej
  - id=1 Wydział Nauk Technicznych i Ekonomicznych
  - id=2 Wydział Nauk Społecznych i Humanistycznych
  - id=10 Wydział Erasmus
  - id=11 Wychowanie fizyczne
- Strona kierunków wydziału zawiera wpisy typu „Informatyka 3 – studia stacjonarne (s3INF)", a w nich specjalności (grupy), np. `s3GK`, `s3PAM`. Rok i tryb studiów (stacjonarne/niestacjonarne) są zakodowane w nazwie kierunku i w kodzie (prefiks `s`/`n` + cyfra roku).
- Plan tygodniowy: `checkSpecjalnosc.php?specjalnosc=<kod>`.
- Plan na cały semestr: `checkSpecjalnoscStac.php?specjalnosc=<kod>` (przy wstępnej próbie zwrócił tylko nagłówek, więc zbadaj, czy dane są ładowane inaczej).
- Widok pracownika: `schedule_view.php?site=show_nauczyciel.php&id=11`.
- Widok obciążenia sali: `schedule_view.php?site=show_sala.php&id=11`.
- Kodowanie strony: **ISO-8859-2**. Dekoduj jawnie, w przeciwnym razie pojawiają się krzaki (np. „Rďż˝ne").
- Strona działa po **HTTP**. Najpierw spróbuj HTTPS, a jeśli nie działa, dodaj `networkSecurityConfig` z wyjątkiem cleartext **ograniczonym do domeny** `www.plan.pwsz.legnica.edu.pl`.
- Strona planu zawiera listę tygodni (numer tygodnia i zakres dat, bieżący oznaczony gwiazdką). Ustal, jak wybiera się konkretny tydzień (parametr w adresie, formularz, link).

### Format tabeli planu (na przykładzie specjalności s3PAM)

- Kolumny to podgrupy, np. `s3PAM1(1)`, `s3PAM2(1)u`, `s3PAM2(2)u`. Każda podgrupa ma trzy podkolumny: przedmiot, prowadzący, sala.
- Wiersze to dni (np. „Czwartek 01.10.2026") i bloki godzinowe (np. „15:15-16:45").
- Blok zajęć: `Pig (wyk) | dr Marek Miedziński | C212`. Skrót przedmiotu i typ w nawiasie, prowadzący z tytułem i pełnym imieniem, sala w osobnej kolumnie.
- Puste komórki: `-`.
- Pod tabelą jest **legenda** z mapowaniem skrótów na pełne nazwy przedmiotów (np. `Pig` → „Projektowanie interfejsów graficznych", `Msi` → „Metody sztucznej inteligencji"). Użyj jej, aby wyświetlać pełną nazwę.
- Dni obejmują poniedziałek–niedzielę (studia niestacjonarne mają zajęcia w sobotę i niedzielę).
- Godziny bloków **różnią się** między dniami (np. w tygodniu 13:30-15:00, w sobotę 13:45-15:15). Nie zakładaj stałych godzin.

### Przypadki szczególne, które parser MUSI obsłużyć

1. **Podgrupy:** osobne kolumny w tabeli.
2. **Wpisy „Różne"** (np. lektoraty): odsyłają do tabel „Legenda1/Legenda2" z listą przedmiotów, nauczycieli i sal. Zbadaj dokładny format i obsłuż (pokaż opcje albo pozwól użytkownikowi zapamiętać wybór).
3. **Tygodnie parzyste i nieparzyste:** gdy w bloku są dwa przedmioty, górny wpis oznacza tydzień parzysty. Pokazuj tylko zajęcia z danego tygodnia.
4. **Bloki 45-minutowe:** zapis `1h [x/-]` lub `1h [-/x]` oznacza zajęcia w pierwszej lub drugiej połowie bloku. Policz rzeczywiste godziny (np. 08:15–09:00).
5. **Typy zajęć:** zaobserwowane skróty to `wyk`, `lab`, `sem`, `p`. Przejrzyj kilka kierunków z różnych wydziałów, zbierz pełną listę skrótów (np. ćwiczenia, konwersatorium, lektorat) i zmapuj: `wyk` → Wykład, `lab` → Laboratoria, `ćw`/`c` → Ćwiczenia, `sem` → Seminarium, `p` → Projekt. Nieznany skrót wyświetlaj bez zmian.
6. **Sala online:** `Online_<liczba>`. Rozpoznawaj bez względu na wielkość liter, z `_`, spacją lub bez separatora. Zweryfikuj na realnych danych (sprawdź kilka kierunków, szczególnie niestacjonarne).
7. **Prowadzący:** zachowaj tytuł i pełne imię tak, jak podaje strona. Jeśli gdzieś pojawia się inicjał zamiast imienia, zapisz to w dokumentacji parsera jako znane ograniczenie.

### Rekonesans (Etap 0)

Zanim napiszesz parser:
1. Pobierz prawdziwe strony: listy kierunków **wszystkich 5 wydziałów**, plany kilku grup (stacjonarnych i niestacjonarnych, różnych wydziałów), widok nauczyciela, widok sali, widok semestru.
2. Zapisz je jako **fixtures** w `parser/src/test/resources/fixtures/` (zachowaj oryginalne kodowanie ISO-8859-2). To baza testów parsera.
3. Spisz ustalenia w `docs/DATA_SOURCE.md` (adresy, parametry wyboru tygodnia, format, przypadki szczególne, znane ograniczenia).

---

## 3. Stos technologiczny

- Kotlin, Jetpack Compose, własny motyw (`MaterialTheme` z ręcznie zdefiniowaną paletą, **bez** `dynamicColorScheme`).
- Architektura: MVVM + Hilt, jednokierunkowy przepływ danych (`StateFlow`), warstwy: `data` / `domain` / `ui`.
- Sieć: OkHttp (wymuszone dekodowanie ISO-8859-2, rozsądne timeouty, ponowienia z backoffem).
- Parser: Jsoup, **osobny moduł Gradle** (`:parser`) bez zależności od Androida, testowany czystymi testami JVM na fixtures.
- Dane lokalne: Room (cache planu, tryb offline) i DataStore (wybór użytkownika, ustawienia).
- Tło: WorkManager (synchronizacja) + AlarmManager (dokładne alarmy powiadomień).
- Widżet: Jetpack Glance.
- Animacje: Compose Animation API (`AnimatedContent`, `animate*AsState`, shared element transitions), Lottie dla pustych stanów.
- Narzędzia: Gradle Kotlin DSL, version catalog (`libs.versions.toml`), ktlint lub detekt, Android Lint.
- Testy: JUnit 5 lub JUnit 4 + MockK/Turbine dla logiki, testy parsera na fixtures, testy logiki harmonogramu powiadomień.

---

## 4. Model danych

Pojedyncza lekcja (`Lesson`):

| Pole | Opis |
|---|---|
| `date` | data zajęć |
| `startTime`, `endTime` | godziny (policzone również dla bloków 45-minutowych) |
| `subjectShort` | skrót z planu (np. `Pig`) |
| `subjectFull` | pełna nazwa z legendy |
| `type` | Wykład / Ćwiczenia / Laboratoria / Seminarium / Projekt / Inne (surowy skrót, jeśli nieznany) |
| `room` | sala (np. `C222`) |
| `isOnline`, `onlineId` | wynik rozpoznania `Online_<liczba>` |
| `teacher` | prowadzący z tytułem i pełnym imieniem |
| `subgroup` | podgrupa |
| `weekParity` | tydzień parzysty / nieparzysty / każdy |
| `changeFlag` | brak / nowe / zmieniona sala lub godzina / odwołane |

Dodatkowo: tabele wydziałów, kierunków, lat i grup; zapisana kopia poprzedniego stanu planu (do wykrywania zmian); surowy HTML ostatniego pobrania.

---

## 5. Ekrany i funkcje

Dolny pasek z 4 zakładkami: **Plan**, **Kalendarz**, **Szukaj**, **Ustawienia**.

### 5.1 Onboarding (dropdowny)

- Kolejność: **Wydział → Kierunek → Rok → Grupa → [Wyszukaj]**.
- Każdy kolejny dropdown jest nieaktywny, dopóki nie wybrano poprzedniego. Zmiana wyższego poziomu czyści niższe.
- Rok i tryb studiów wynikają z nazwy kierunku, więc dropdown „Rok" ma etykiety w stylu „3 rok · stacjonarne".
- W długich listach pole filtrowania.
- Wygląd: ciemne „szklane" pola z gradientową obwódką w stanie aktywnym, animowane rozwijanie listy, sprężyste pojawianie się kolejnych pól. Przycisk „Wyszukaj" z gradientem, aktywny dopiero po wypełnieniu wszystkich pól.
- Wybór zapisywany w DataStore. Przy kolejnym uruchomieniu aplikacja od razu otwiera plan. Zmiana grupy w Ustawieniach (dropdowny wypełnione aktualnym wyborem).
- Po „Wyszukaj" jednorazowy wybór **podgrupy** (z kolumn planu, np. `s3PAM1(1)`), zapamiętywany, z opcją „Pokaż wszystkie".
- W onboardingu prowadź użytkownika przez zgody: powiadomienia (Android 13+) oraz „Alarmy i przypomnienia" (dokładne alarmy).

### 5.2 Plan (ekran główny)

- Przełącznik widoku: **„Najbliższe"** (domyślny) i **„Dzień po dniu"**.
- **Najbliższe:** jedna chronologiczna lista od „teraz" w przód, od najbliższych do najdalszych zajęć, z przyklejanymi nagłówkami dni („Dziś", „Jutro", „Pon 5.10") z liczbą zajęć.
  - Trwające zajęcia przypięte na górze z paskiem postępu.
  - Zajęcia z dzisiaj, które się już skończyły, zwinięte w „Zakończone (n)".
  - Dni bez zajęć pomijane. Kolejne tygodnie doładowują się podczas przewijania.
  - Kolejne bloki tego samego przedmiotu to osobne karty połączone łącznikiem z oznaczeniem „blok 1/2" (opcja scalania w ustawieniach).
  - Długie przerwy oznaczone jako „okienko".
- **Dzień po dniu:** pager z paskiem dni. Pasek pokazuje Pn–Pt, a sobotę i niedzielę tylko wtedy, gdy są w nich zajęcia. Przyciski „Dziś" i „Następne zajęcia".

### 5.3 Karta zajęć

- **Lewa kolumna:** godzina rozpoczęcia dużą czcionką (cyfry o stałej szerokości), pod nią godzina zakończenia.
- **Prawa kolumna:** pełna nazwa przedmiotu (do 2 linii), chip typu (Wykład/Laboratoria/Ćwiczenia/Seminarium/Projekt), chip sali z ikoną (np. `C222`), pełne imię prowadzącego z tytułem.
- Prowadzący i sala klikalne, otwierają ich plan.
- Trwające zajęcia mają wyróżnioną kartę i pasek postępu, zakończone są przygaszone.
- Zmiany (nowa sala, odwołane) mają znacznik na karcie.

### 5.4 Zajęcia online

Sala w formacie `Online_<liczba>` to osobny tryb:
- zamiast chipa sali wyraźny **chip „ONLINE"** z ikoną kamery i własnym gradientem (cyjan → fiolet), numer mniejszą czcionką obok,
- karta ma zmieniony akcent, żeby na liście „Najbliższe" było to widoczne z daleka (zawsze ikona i tekst, nie sam kolor),
- w powiadomieniu zamiast sali „Online_3", w widżecie i kalendarzu ikona kamery,
- w ustawieniach filtr „tylko stacjonarne" / „tylko online".

### 5.5 Kalendarz

Widok miesiąca z kropkami w dniach z zajęciami, po przełączeniu siatka tygodnia. Tapnięcie dnia przenosi do planu tego dnia.

### 5.6 Szukaj

Wyszukiwanie grup, prowadzących i sal (jeśli strona to udostępnia) oraz „wolne sale teraz". Grupa, prowadzący i sala to ten sam typ „plan".

### 5.7 Ustawienia

Zmiana grupy i podgrupy (dropdowny), powiadomienia, wygląd (opcja AMOLED), filtr online/stacjonarne, scalanie bloków, widżet, krótkie instrukcje wyłączenia optymalizacji baterii dla popularnych producentów.

### 5.8 Wykrywanie zmian

Porównuj nowy plan z poprzednim zapisanym stanem. Oznaczaj karty („zmieniona sala", „nowe zajęcia", „odwołane"), a powiadomienie o zmianie ma mówić, co się zmieniło.

---

## 6. Stałe powiadomienie „następne zajęcia"

### Zachowanie

- **Treść:** tytuł „Następne: Metody sztucznej inteligencji"; linia „18:45 · E1 · dr inż. Piotr Nadybski" (dla online: „Online_3" zamiast sali); odliczanie „za 42 min"; po rozwinięciu „Teraz trwa: …" i „Potem: …".
- **Pojawia się:** 60 minut przed pierwszym blokiem dnia. W dni bez zajęć w ogóle się nie pojawia.
- **Zmiana bez nowego powiadomienia:** jedno powiadomienie ze stałym ID aktualizowane w miejscu (`setOnlyAlertOnce(true)`, kanał bez dźwięku i wibracji). Przełącza się na kolejny blok w chwili rozpoczęcia obecnego (obecny jest wtedy w rozwiniętej treści jako „Teraz trwa").
- **Znika:** po zakończeniu ostatniego bloku dnia.

### Realizacja techniczna

- **Nie używaj** całodziennej usługi pierwszoplanowej. Użyj dokładnych alarmów (`AlarmManager.setExactAndAllowWhileIdle`) na: godzinę przed pierwszym blokiem dnia, początek każdego bloku (przełączenie na kolejny) i koniec ostatniego bloku (usunięcie powiadomienia).
- Alarmy przeliczaj po każdej synchronizacji planu (zmiana sali lub odwołanie od razu zmienia powiadomienie) oraz po restarcie telefonu (`BOOT_COMPLETED`) i zmianie strefy czasowej lub zegara.
- Obsłuż uprawnienia: `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM` (z przekierowaniem do ustawień, gdy brak zgody, i sensownym zachowaniem zastępczym, np. alarm niedokładny).
- Powiadomienie używa standardowego stylu systemu z kolorem akcentu zależnym od typu zajęć (bez własnego widoku z gradientem).
- Logikę wyznaczania „co i kiedy pokazać" wydziel do czystej, testowalnej klasy (wejście: lista zajęć i aktualny czas, wyjście: stan powiadomienia i lista alarmów). Napisz do niej testy jednostkowe (m.in. pierwszy blok dnia, kolejne bloki, ostatni blok, dzień bez zajęć, zajęcia online, zmiana planu w trakcie dnia).

### Ograniczenia Androida do opisania w dokumentacji

- Od Androida 14 powiadomienie „ongoing" bez usługi pierwszoplanowej da się odrzucić gestem. Wraca przy następnym przełączeniu bloku. Pełnej niezawodności bez usługi pierwszoplanowej nie da się zagwarantować.
- U niektórych producentów (Xiaomi, Samsung, Huawei) trzeba wyłączyć optymalizację baterii dla aplikacji.

---

## 7. Styl wizualny (ciemny z gradientami)

Zasada nadrzędna: **gradienty tylko akcentują, a tekst leży na jednolitej ciemnej powierzchni** (kontrast tekstu min. 4,5:1).

- **Tło:** prawie czarne (opcja AMOLED), karty jako lekko jaśniejsza jednolita powierzchnia, delikatna poświata gradientowa tylko w nagłówku dnia.
- **Gradienty na:** pasku akcentu karty, obwódce chipa typu, zaznaczonym dniu, pasku postępu i przycisku „Wyszukaj".
- **Kolory typów (propozycja, możesz doprecyzować):**

| Typ | Gradient |
|---|---|
| Wykład | indygo → cyjan |
| Ćwiczenia | szmaragd → limonka |
| Laboratoria | pomarańcz → róż |
| Seminarium | fiolet → magenta |
| Projekt | turkus → błękit |
| Online (chip) | cyjan → fiolet |
| Inne | szary |

- Typ jest zawsze podany też tekstem i ikoną, więc kolor nigdy nie jest jedyną informacją.
- Zdefiniuj tokeny designu (kolory, typografia, odstępy, zaokrąglenia) w jednym miejscu (`ui/theme`).

---

## 8. Animacje i dostępność

### Animacje

- Kaskadowe pojawianie kart (40–50 ms odstępu, fade + lekki slide).
- Sprężynowe przesuwanie wskaźnika dnia, `AnimatedContent` przy zmianie dnia.
- Shared element transition z karty do ekranu szczegółów.
- Shimmer przy ładowaniu, pull-to-refresh z animowanym wskaźnikiem, delikatny puls przy trwających zajęciach.
- Animacja Lottie dla pustych stanów (użyj lekkiej, licencyjnie bezpiecznej animacji albo narysuj własną w Compose).
- Czas trwania 200–350 ms, animuj tylko przesunięcie i przezroczystość, nie blokuj interakcji. Szanuj systemowe „Usuń animacje".

### Dostępność

- Skalowanie czcionek systemowych.
- Cele dotykowe min. 48 dp.
- Opisy dla TalkBack (np. „Wykład, Metody sztucznej inteligencji, od 18:45 do 20:15, sala E1").
- Tryb zmniejszonego ruchu.

---

## 9. Etapy realizacji (wykonuj po kolei, każdy kończ buildem, testami i commitem)

**Etap 0: Rekonesans.** Pobranie i zapisanie fixtures, `docs/DATA_SOURCE.md` (sekcja 2).

**Etap 1: Szkielet projektu i CI.** Struktura repozytorium, moduły (`:app`, `:parser`), Gradle Kotlin DSL, version catalog, ktlint/detekt, podstawowy workflow CI (build + testy + lint). `.gitignore`, licencja (zapytaj w `DECISIONS.md` lub użyj MIT, jeśli repo nie ma licencji).

**Etap 2: Parser i testy.** Moduł `:parser`: listy wydziałów, kierunków i grup, plan grupy (podgrupy, parzystość tygodni, bloki 45-minutowe, legenda, wpisy „Różne", sala online, typy zajęć), potem nauczyciele i sale. Testy na fixtures z min. kilkoma kierunkami z różnych wydziałów.

**Etap 3: Warstwa danych.** Room, repozytorium, klient sieciowy (ISO-8859-2, HTTPS→HTTP fallback z `networkSecurityConfig`), DataStore, synchronizacja WorkManager, tryb offline, wykrywanie zmian.

**Etap 4: Onboarding z dropdownami** i zapamiętywanie wyboru (wraz z wyborem podgrupy i prośbą o zgody).

**Etap 5: Ekran Plan.** Lista „Najbliższe", widok „Dzień po dniu", karty, oznaczenia Online, stany ładowania/błędu/pusty.

**Etap 6: Silnik powiadomień.** Czysta logika + testy, alarmy, aktualizacja w miejscu, przeliczanie po synchronizacji, restarcie i zmianie czasu, obsługa uprawnień.

**Etap 7: Kalendarz, Szukaj, klikalne sale i prowadzący, znaczniki zmian.**

**Etap 8: Widżet, animacje, dopracowanie stylu, dostępność, ikona aplikacji.**

**Etap 9: GitHub Actions do budowy APK** (sekcja 10) **i pełna dokumentacja** (sekcja 11).

**Etap 10: Przegląd końcowy.** Pełny build release lokalnie (o ile to możliwe), przegląd lintem, sprawdzenie dokumentacji pod kątem zgodności z kodem, podsumowanie.

---

## 10. GitHub Actions (budowa APK)

Utwórz w `.github/workflows/`:

### `ci.yml` (na push i pull request)
- Checkout, JDK (Temurin, wersja zgodna z projektem), `gradle/actions/setup-gradle` (cache),
- `./gradlew ktlintCheck` lub `detekt`, `./gradlew lint`, `./gradlew test`,
- zapis raportów testów i lintu jako artefaktów przy porażce.

### `release.yml` (budowa finalnego APK)
- Wyzwalacze: **tag `v*`** (np. `v1.0.0`) oraz `workflow_dispatch` (ręczne uruchomienie).
- Kroki: checkout, JDK, cache Gradle, testy, `./gradlew assembleRelease`.
- **Podpisywanie:** APK release musi być podpisane. Użyj kluczy z sekretów repozytorium:
  - `KEYSTORE_BASE64` (keystore zakodowany w base64),
  - `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
  - Workflow odtwarza keystore z base64 w katalogu tymczasowym, a `build.gradle.kts` wczytuje dane podpisu ze zmiennych środowiskowych.
  - **Jeśli sekrety nie są ustawione**, workflow nie może się wywrócić bez wyjaśnienia: ma zbudować APK podpisany kluczem debug (z wyraźnym ostrzeżeniem w logu i w opisie wydania), żebym mógł od razu zainstalować aplikację do testów.
  - Nigdy nie commituj keystore ani haseł.
- Wynik: plik `PlanZajec-<wersja>.apk` (czytelna nazwa) wrzucony jako **artefakt workflow** oraz, przy tagu, dołączony do **GitHub Release** (automatycznie wygenerowane notatki z wydania, `gh release create` lub `softprops/action-gh-release`) wraz z sumą kontrolną SHA-256.
- Wersjonowanie: `versionName` z tagu, `versionCode` z numeru uruchomienia lub liczony ze znacznika (opisz w dokumentacji).
- Ustaw minimalne uprawnienia workflow (`permissions:`), przypnij wersje akcji.
- Dodaj do `docs/RELEASE.md` instrukcję krok po kroku:
  1. jak wygenerować keystore (`keytool`),
  2. jak zakodować go do base64 i dodać sekrety do repozytorium (GitHub → Settings → Secrets and variables → Actions),
  3. jak utworzyć tag i uruchomić wydanie,
  4. skąd pobrać APK i jak zainstalować go na telefonie (zezwolenie na instalację z nieznanych źródeł).

---

## 11. Dokumentacja projektu (pełna, po polsku)

Utwórz i utrzymuj zgodnie z kodem:

- `README.md`: opis projektu, zrzuty ekranu lub makiety (jeśli możliwe), funkcje, szybki start, instalacja APK, budowanie lokalnie, link do dokumentacji.
- `docs/ARCHITECTURE.md`: warstwy, moduły, przepływ danych, diagram (Mermaid), decyzje architektoniczne.
- `docs/DATA_SOURCE.md`: opis strony źródłowej, adresy, format, przypadki szczególne, znane ograniczenia.
- `docs/PARSER.md`: jak działa parser, model danych, jak dodać nowy fixture lub obsłużyć nowy przypadek.
- `docs/NOTIFICATIONS.md`: logika powiadomienia i alarmów, uprawnienia, ograniczenia Androida, optymalizacja baterii u producentów.
- `docs/UI_DESIGN.md`: system designu (kolory, gradienty, typografia, animacje, zasady dostępności).
- `docs/RELEASE.md`: sekcja 10.
- `docs/DEVELOPMENT.md`: wymagania (JDK, Android SDK), uruchamianie, testy, lint, konwencje commitów.
- `docs/TESTING.md`: strategia testów, fixtures, lista testów ręcznych (urządzenie fizyczne, Doze, restart telefonu, różni producenci).
- `docs/DECISIONS.md`: dziennik założeń i decyzji podjętych samodzielnie podczas pracy.
- `CHANGELOG.md`: format „Keep a Changelog".
- `CONTRIBUTING.md` i `LICENSE` (jeśli brak).
- KDoc przy publicznych klasach i funkcjach kluczowych modułów.

Dokumentacja ma być spójna z kodem i aktualna na koniec pracy. Nie opisuj funkcji, których nie zaimplementowałeś.

---

## 12. Ryzyka i wymagania jakościowe

- **Zmiana struktury HTML strony** nie może wywracać aplikacji: gdy parsowanie się nie powiedzie, pokaż ostatni zapisany plan i czytelny komunikat, a nie pusty ekran lub crash.
- **Rozsądna częstotliwość zapytań** do serwera uczelni (cache, brak masowego pobierania, backoff). Serwis jest nieoficjalnie odpytywany, więc bądź łagodny.
- **Brak sieci:** aplikacja ma działać na zapisanym planie z informacją „zaktualizowano HH:mm".
- **Prywatność:** aplikacja nie zbiera danych osobowych, nie ma analityki ani reklam. Zapisz to w README.
- **Wydajność:** płynne przewijanie list, brak ciężkich operacji w głównym wątku.

---

## 13. Założenia, które przyjmij (zapisz w `docs/DECISIONS.md`)

1. Powiadomienie przełącza się na kolejny blok w chwili rozpoczęcia obecnego (obecny widoczny w rozwiniętej treści jako „Teraz trwa").
2. Minimalne API 26. Jeśli wybór innego poziomu jest uzasadniony, zapisz powód.
3. Podgrupa wybierana jednorazowo po „Wyszukaj", z opcją „Pokaż wszystkie".
4. Kolejne bloki tego samego przedmiotu to osobne karty, a scalanie to opcja w ustawieniach.
5. Nazwa pakietu: zaproponuj spójną (np. `pl.legnica.planzajec`), jeśli repozytorium nie narzuca innej.

---

## 14. Co mi dostarczysz na koniec

1. Działający projekt w repozytorium z historią commitów po etapach.
2. Workflow `ci.yml` i `release.yml` oraz `docs/RELEASE.md` z instrukcją, co mam skonfigurować (sekrety, tag).
3. Kompletną dokumentację (sekcja 11).
4. Krótkie podsumowanie: co zrobione, co niezweryfikowane (np. zachowanie powiadomień na fizycznym telefonie), znane ograniczenia i lista rzeczy, które muszę zrobić ręcznie.

Zacznij od utworzenia `docs/PROGRESS.md`, potem Etap 0, i pracuj w pętli (patrz „Tryb autonomiczny" w sekcji 0), aż spełnisz Definicję ukończenia.
