# Plan aplikacji mobilnej: Plan zajęć PWSZ Legnica (Android, natywna)

Źródło danych: http://www.plan.pwsz.legnica.edu.pl/
Data opracowania: 3 października 2026

---

## 1. Założenia i decyzje

| Obszar | Decyzja |
|---|---|
| Platforma | Natywny Android (Kotlin + Jetpack Compose) |
| Zakres | Wszystkie wydziały, kierunki i tryby studiów (stacjonarne i niestacjonarne) |
| Dystrybucja | Prywatna (podpisany APK), bez Google Play |
| Styl | Własny: ciemny motyw z kolorowymi gradientowymi akcentami, bez Material You |
| Onboarding | Kaskadowe dropdowny: Wydział → Kierunek → Rok → Grupa → Wyszukaj, wybór zapamiętywany |
| Powiadomienia | Stałe powiadomienie „następne zajęcia", aktualizowane w miejscu |
| Kolejność zajęć | Od najbliższych do najdalszych czasowo (z podziałem na dni) |
| Zajęcia online | Sala w formacie `Online_<liczba>` jest wyraźnie oznaczona |
| Kontakt z administratorem strony | Nie (dane pobieramy przez parsowanie HTML) |
| Minimalne API (założenie) | Android 8.0 (API 26), do potwierdzenia |

---

## 2. Co wiadomo o źródle danych

Strona to starszy serwis PHP bez API, bez eksportu iCal i bez JSON-a. Dane trzeba pobierać i parsować z HTML.

### Nawigacja i adresy

- Strona główna: lista wydziałów (`schedule_view.php?site=show_kierunek.php&id=<id wydziału>`)
  - id=7: Wydział Nauk o Zdrowiu i Kulturze Fizycznej
  - id=1: Wydział Nauk Technicznych i Ekonomicznych
  - id=2: Wydział Nauk Społecznych i Humanistycznych
  - id=10: Wydział Erasmus
  - id=11: Wychowanie fizyczne
- Lista kierunków wydziału: wpisy typu „Informatyka 3 – studia stacjonarne (s3INF)", a w nich specjalności (grupy), np. `s3GK`, `s3PAM`.
- Plan tygodniowy: `checkSpecjalnosc.php?specjalnosc=<kod>`
- Plan na cały semestr: `checkSpecjalnoscStac.php?specjalnosc=<kod>`
- Widok pracownika: `schedule_view.php?site=show_nauczyciel.php&id=11`
- Widok obciążenia sali: `schedule_view.php?site=show_sala.php&id=11`

### Format danych

- Kodowanie strony: **ISO-8859-2** (bez jawnego dekodowania pojawiają się krzaki, np. „Rďż˝ne").
- Protokół: tylko **HTTP**. Android domyślnie blokuje taki ruch, więc potrzebny jest wyjątek `networkSecurityConfig` dla tej jednej domeny (najpierw próba HTTPS).
- Blok zajęć ma postać: `Pig (wyk) | dr Marek Miedziński | C212` (skrót przedmiotu i typ w nawiasie, prowadzący z tytułem i pełnym imieniem, sala w osobnej kolumnie).
- Nazwa przedmiotu jest **skrócona** (`Pig`). Pełna nazwa jest w legendzie pod tabelą („Projektowanie interfejsów graficznych").
- Lista tygodni (z numerami i zakresami dat) jest na stronie, a bieżący tydzień jest oznaczony gwiazdką.
- Plan zawiera dni od poniedziałku do niedzieli. Studia niestacjonarne mają zajęcia w sobotę i niedzielę.

### Nietypowe przypadki, które parser musi obsłużyć

1. **Podgrupy:** plan ma osobne kolumny (np. `s3PAM1(1)`, `s3PAM2(1)u`, `s3PAM2(2)u`).
2. **Wpisy „Różne":** odsyłają do tabel „Legenda1/Legenda2" z osobnymi przedmiotami, nauczycielami i salami (np. lektoraty angielskiego).
3. **Tygodnie parzyste i nieparzyste:** gdy w bloku są dwa przedmioty, górny wpis oznacza tydzień parzysty.
4. **Bloki 45-minutowe:** zapis `1h [x/-]` lub `1h [-/x]` oznacza zajęcia w pierwszej lub drugiej połowie bloku.
5. **Zmienne godziny bloków:** np. w tygodniu 13:30–15:00, a w sobotę 13:45–15:15. Godzin nie wolno zakładać na sztywno.
6. **Puste komórki:** oznaczone `-`.
7. **Sala online:** format `Online_<liczba>` (do potwierdzenia na realnych danych).
8. **Skróty typów zajęć:** widziane dotąd `wyk`, `lab`, `sem`, `p`. Pełną listę (np. ćwiczenia, konwersatorium, lektorat) trzeba zebrać z różnych kierunków.

---

## 3. Stos technologiczny

- **Język i UI:** Kotlin + Jetpack Compose (własny motyw, bez Material You).
- **Architektura:** MVVM + Hilt, jednokierunkowy przepływ danych (`StateFlow`).
- **Sieć:** OkHttp (z wymuszonym dekodowaniem ISO-8859-2).
- **Parser:** Jsoup, osobny moduł z testami jednostkowymi na zapisanych kopiach HTML z różnych kierunków.
- **Dane lokalne:** Room (cache planu, tryb offline) i DataStore (wybór użytkownika, ustawienia).
- **Tło i powiadomienia:** WorkManager (synchronizacja) + AlarmManager (dokładne alarmy powiadomień).
- **Widżet:** Jetpack Glance.
- **Animacje:** Compose Animation API (`AnimatedContent`, `animate*AsState`, shared element transitions), Lottie dla pustych stanów.

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

Dodatkowo: tabela wydziałów, kierunków, lat i grup oraz zapisana kopia poprzedniego stanu planu (do wykrywania zmian) i surowy HTML ostatniego pobrania.

Mapowanie typów: `wyk` → Wykład, `lab` → Laboratoria, `ćw` → Ćwiczenia, `sem` → Seminarium, `p` → Projekt. Nieznany skrót jest wyświetlany bez zmian, a mapowanie uzupełniane po zebraniu pełnej listy.

---

## 5. Nawigacja i ekrany

Dolny pasek z 4 zakładkami:

1. **Plan** (ekran główny)
2. **Kalendarz**
3. **Szukaj**
4. **Ustawienia**

### 5.1 Onboarding (dropdowny)

Kolejność: **Wydział → Kierunek → Rok → Grupa → [Wyszukaj]**

- Każdy kolejny dropdown jest nieaktywny, dopóki nie wybierzesz poprzedniego. Zmiana wyższego poziomu czyści niższe.
- Rok i tryb studiów są zakodowane w nazwie kierunku (np. „Informatyka 3 – studia stacjonarne"), więc dropdown „Rok" ma etykiety w stylu „3 rok · stacjonarne".
- W długich listach jest pole filtrowania.
- Wygląd: ciemne „szklane" pola z gradientową obwódką w stanie aktywnym, animowane rozwijanie listy, sprężyste pojawianie się kolejnych pól. Przycisk „Wyszukaj" ma gradient i włącza się dopiero po wypełnieniu wszystkich pól.
- Wybór jest zapisywany w DataStore. Przy następnym uruchomieniu aplikacja od razu otwiera plan, a zmiana grupy jest w Ustawieniach (z wypełnionymi dropdownami).
- Po „Wyszukaj" pojawia się jednorazowy wybór **podgrupy** (z kolumn planu, np. `s3PAM1(1)`), również zapamiętywany. Można go pominąć („Pokaż wszystkie").
- W onboardingu aplikacja prowadzi użytkownika przez zgody: powiadomienia (Android 13+) oraz „Alarmy i przypomnienia" (dokładne alarmy).

### 5.2 Plan (ekran główny)

- Przełącznik widoku: **„Najbliższe"** (domyślny) i **„Dzień po dniu"**.
- **Najbliższe:** jedna chronologiczna lista od „teraz" w przód, od najbliższych do najdalszych zajęć, z przyklejanymi nagłówkami dni („Dziś", „Jutro", „Pon 5.10") z liczbą zajęć.
  - Trwające zajęcia są przypięte na górze z paskiem postępu.
  - Zajęcia z dzisiaj, które już się skończyły, są zwinięte w „Zakończone (n)".
  - Dni bez zajęć są pomijane. Kolejne tygodnie doładowują się podczas przewijania.
  - Kolejne bloki tego samego przedmiotu są osobnymi kartami połączonymi łącznikiem i oznaczeniem „blok 1/2" (opcja scalania w ustawieniach).
  - Długie przerwy są oznaczone jako „okienko".
- **Dzień po dniu:** pager z paskiem dni. Pasek pokazuje Pn–Pt, a sobotę i niedzielę tylko wtedy, gdy są w nich zajęcia. Jest przycisk „Dziś" i „Następne zajęcia".

### 5.3 Karta zajęć

- **Lewa kolumna:** godzina rozpoczęcia dużą czcionką (cyfry o stałej szerokości), pod nią godzina zakończenia.
- **Prawa kolumna:**
  - pełna nazwa przedmiotu (do 2 linii),
  - chip typu (Wykład / Laboratoria / Ćwiczenia / Seminarium / Projekt),
  - chip sali z ikoną (np. `C222`),
  - pełne imię prowadzącego z tytułem.
- Prowadzący i sala są klikalne i otwierają ich plan.
- Trwające zajęcia mają wyróżnioną kartę i pasek postępu, a zakończone są przygaszone.
- Zmiany (nowa sala, odwołane) mają znacznik na karcie.

### 5.4 Zajęcia online

Sala w formacie `Online_<liczba>` (rozpoznawana bez względu na wielkość liter, z `_`, spacją lub bez separatora) jest osobnym trybem:

- zamiast chipa sali jest wyraźny **chip „ONLINE"** z ikoną kamery i własnym gradientem (np. cyjan → fiolet), a numer jest podany mniejszą czcionką obok,
- karta ma zmieniony akcent, żeby w liście „Najbliższe" było to widoczne z daleka (ikona i tekst, nie tylko kolor),
- w powiadomieniu zamiast sali jest „Online_3", a w widżecie i kalendarzu ikona kamery,
- w ustawieniach jest filtr „tylko stacjonarne" lub „tylko online".

### 5.5 Kalendarz

Widok miesiąca z kropkami w dniach z zajęciami (skraca przeglądanie planu zjazdowego), po przełączeniu siatka tygodnia. Tapnięcie dnia przenosi do planu tego dnia.

### 5.6 Szukaj

Wyszukiwanie grup, prowadzących i sal (strona ma takie widoki) oraz „wolne sale teraz". Grupa, prowadzący i sala to ten sam typ „plan".

### 5.7 Ustawienia

Zmiana grupy i podgrupy (dropdowny), powiadomienia, wygląd (opcja AMOLED), filtr online/stacjonarne, scalanie bloków, widżet, instrukcje dla optymalizacji baterii.

---

## 6. Stałe powiadomienie „następne zajęcia"

### Zachowanie

- **Treść:**
  - tytuł: „Następne: Metody sztucznej inteligencji",
  - linia: „18:45 · E1 · dr inż. Piotr Nadybski" (dla online: „Online_3" zamiast sali),
  - odliczanie „za 42 min",
  - po rozwinięciu: „Teraz trwa: …" i „Potem: …".
- **Pojawia się:** 60 minut przed pierwszym blokiem dnia. W dni bez zajęć nie pojawia się wcale.
- **Zmiana bez nowego powiadomienia:** jedno powiadomienie ze stałym ID aktualizowane w miejscu (`setOnlyAlertOnce`, kanał bez dźwięku i wibracji). Przełącza się na kolejny blok w chwili rozpoczęcia obecnego (obecny jest wtedy w rozwiniętej treści jako „Teraz trwa").
- **Znika:** po zakończeniu ostatniego bloku dnia.

### Realizacja techniczna

- Zamiast całodziennej usługi pierwszoplanowej (obciąża baterię i bywa ubijana przez producentów) ustawiane są dokładne alarmy (`AlarmManager.setExactAndAllowWhileIdle`):
  - godzina przed pierwszym blokiem dnia,
  - początek każdego bloku (przełączenie na kolejny),
  - koniec ostatniego bloku (usunięcie powiadomienia).
- Alarmy są przeliczane po każdej synchronizacji planu (zmiana sali czy odwołanie od razu zmienia powiadomienie) oraz po restarcie telefonu (`BOOT_COMPLETED`).
- Powiadomienie używa standardowego stylu systemu z kolorem akcentu zależnym od typu zajęć (własny widok z gradientem psułby spójność i zgodność z nowszymi Androidami).

### Ograniczenia Androida

- Od Androida 13 potrzebna jest zgoda na powiadomienia, a dokładne alarmy wymagają zgody „Alarmy i przypomnienia".
- Od Androida 14 powiadomienie „ongoing" bez usługi pierwszoplanowej da się odrzucić gestem. Wraca przy następnym przełączeniu bloku. Pełnej niezawodności bez usługi pierwszoplanowej nie da się zagwarantować.
- U niektórych producentów (Xiaomi, Samsung, Huawei) trzeba wyłączyć optymalizację baterii dla aplikacji. W Ustawieniach będzie krótka instrukcja dla danego producenta.

---

## 7. Styl wizualny (ciemny z gradientami)

Zasada nadrzędna: **gradienty tylko akcentują, a tekst leży na jednolitej ciemnej powierzchni** (kontrast tekstu min. 4,5:1).

- **Tło:** prawie czarne (opcja AMOLED), karty jako lekko jaśniejsza jednolita powierzchnia, delikatna poświata gradientowa tylko w nagłówku dnia.
- **Gradienty na:** pasku akcentu karty, obwódce chipa typu, zaznaczonym dniu, pasku postępu i przycisku „Wyszukaj".
- **Kolory typów (propozycja):**

| Typ | Gradient |
|---|---|
| Wykład | indygo → cyjan |
| Ćwiczenia | szmaragd → limonka |
| Laboratoria | pomarańcz → róż |
| Seminarium | fiolet → magenta |
| Projekt | turkus → błękit |
| Online (chip) | cyjan → fiolet |
| Inne | szary |

- Typ jest zawsze podany też tekstem i ikoną, więc kolor nie jest jedyną informacją.

---

## 8. Animacje

- Kaskadowe pojawianie kart (40–50 ms odstępu, fade + lekki slide).
- Sprężynowe przesuwanie wskaźnika dnia, `AnimatedContent` przy zmianie dnia.
- Shared element transition z karty do ekranu szczegółów.
- Shimmer przy ładowaniu, pull-to-refresh z animowanym wskaźnikiem, delikatny puls przy trwających zajęciach.
- Animacja Lottie dla pustych stanów.
- Czas trwania 200–350 ms, animowane tylko przesunięcie i przezroczystość, brak blokowania interakcji. Animacje wyłączają się przy systemowym „Usuń animacje".

---

## 9. Dane, offline i niezawodność

- **Offline-first:** zawsze pokazujemy zapisany plan i napis „zaktualizowano 14:02". Bieżący tydzień i następne są pobierane w tle (WorkManager), co jest też podstawą wykrywania zmian.
- **Wykrywanie zmian:** porównanie z poprzednim stanem. Karta dostaje znacznik („zmieniona sala", „nowe zajęcia", „odwołane"), a powiadomienie mówi, co się zmieniło.
- **Odporność parsera:** przechowywanie surowego HTML ostatniego pobrania, wersjonowanie parsera, testy na plikach z różnych kierunków. Gdy format strony się zmieni, aplikacja pokazuje stary plan i komunikat o błędzie, a nie pusty ekran.
- **HTTP/HTTPS:** najpierw próba HTTPS, potem HTTP z wyjątkiem w `networkSecurityConfig` ograniczonym do tej domeny.
- **Dystrybucja:** podpisany APK (np. GitHub Releases) z prostym sprawdzaniem nowej wersji.

---

## 10. Dostępność

- Skalowanie czcionek systemowych.
- Cele dotykowe min. 48 dp.
- Opisy dla TalkBack (np. „Wykład, Metody sztucznej inteligencji, od 18:45 do 20:15, sala E1").
- Tryb zmniejszonego ruchu.

---

## 11. Etapy realizacji

**Etap 0: Rekonesans (krótki, ważny)**
- Sprawdzenie, jak strona wybiera tydzień (lista tygodni jest, ale parametru w adresie jeszcze nie potwierdzono).
- Sprawdzenie widoku „na cały semestr" (przy pierwszej próbie zwrócił tylko nagłówek).
- Widoki nauczyciela i sali.
- Struktura pozostałych wydziałów (Zdrowie, Humanistyczny, Erasmus, WF). Szczegółowo obejrzano na razie wydział techniczny.
- Potwierdzenie formatu `Online_<liczba>` i zebranie wszystkich skrótów typów zajęć.

**Etap 1: Parser i testy**
- Grupy (podgrupy, parzystość tygodni, bloki 45-minutowe, legenda, wpisy „Różne"), potem nauczyciele i sale.

**Etap 2: Szkielet aplikacji**
- Room, repozytorium, onboarding z dropdownami, zapamiętywanie wyboru, synchronizacja i tryb offline.

**Etap 3: Ekran Plan**
- Lista „Najbliższe", widok „Dzień po dniu", karty, oznaczenia Online, stany ładowania, błędu i pusty.

**Etap 4: Silnik powiadomień**
- Alarmy, aktualizacja w miejscu, przeliczanie po synchronizacji i po restarcie.
- Testy w trybie Doze, po restarcie telefonu i na kilku producentach.

**Etap 5: Kalendarz, Szukaj, wykrywanie zmian**
- Klikalne sale i prowadzący, wolne sale, znaczniki zmian.

**Etap 6: Widżet, animacje, dopracowanie**
- Widżet Glance, animacje, finalny styl, dostępność, testy na urządzeniach, ikona, podpisany APK.

---

## 12. Ryzyka

| Ryzyko | Działanie |
|---|---|
| Zmiana struktury HTML na stronie psuje parser | Testy na zapisanych plikach, wersjonowanie, komunikat o błędzie i stary plan z cache |
| Strona tylko po HTTP | Wyjątek `networkSecurityConfig` ograniczony do domeny |
| Powiadomienie „ongoing" odrzucane gestem (Android 14+) | Odtwarzanie przy następnym przełączeniu bloku, jasna informacja w ustawieniach |
| Optymalizacja baterii u producentów ubija alarmy | Instrukcje w ustawieniach, prośba o zgodę na dokładne alarmy |
| Nieznane skróty typów zajęć i nietypowe sale | Wyświetlanie surowej wartości, uzupełnianie mapowania po zebraniu danych |
| Nieoficjalne pobieranie danych z serwisu uczelni | Rozsądna częstotliwość odświeżania, cache, brak masowych zapytań |

---

## 13. Założenia do potwierdzenia

1. **Powiadomienie w trakcie bloku:** przełącza się na następny blok w chwili rozpoczęcia obecnego (obecny jest w rozwiniętej treści jako „Teraz trwa"). Do potwierdzenia, czy nie ma przełączać się dopiero po zakończeniu bloku.
2. **Telefon:** minimalne API 26 (Android 8+). Do potwierdzenia: faktyczna wersja Androida i producent telefonu, od których zależy dopracowanie obejść optymalizacji baterii.
3. **Podgrupy:** domyślnie wybierane jednorazowo po „Wyszukaj", z opcją „Pokaż wszystkie".
4. **Scalanie kolejnych bloków** tego samego przedmiotu: domyślnie osobne karty, scalanie jako opcja w ustawieniach.

---

## 14. Następny krok

Do wyboru:
- interaktywny mockup onboardingu, listy „Najbliższe" i karty zajęć (w tym karty Online) w docelowym stylu, do oceny wyglądu przed kodowaniem,
- albo od razu Etap 0 (rekonesans) i Etap 1 (parser).
