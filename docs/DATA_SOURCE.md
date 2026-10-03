# Specyfikacja źródła danych (DATA_SOURCE.md)

Źródło danych: **http://www.plan.pwsz.legnica.edu.pl/** (Collegium Witelona Uczelnia Państwowa, d. PWSZ im. Witelona w Legnicy).
System nie udostępnia oficjalnego REST API, eksportu iCalendar (.ics) ani formatu JSON. Wszystkie dane pozyskiwane są drogą parsowania kodu HTML.

---

## 1. Protokół i kodowanie znaków

- **Brak obsługi HTTPS:** Serwer odrzuca handshake TLS (`SSL/TLS connection failed`). Komunikacja musi odbywać się po **czystym HTTP** (`http://www.plan.pwsz.legnica.edu.pl/`). W konfiguracji Androida (`AndroidManifest.xml`) wymagany jest plik `networkSecurityConfig` z wyjątkiem `cleartextTrafficPermitted="true"` ograniczonym wyłącznie do tej domeny.
- **Kodowanie ISO-8859-2:** Wszystkie strony serwowane są w standardzie ISO-8859-2 (`Content-Type: text/html; charset=iso-8859-2`). Każda odpowiedź HTTP oraz fixtures testowe muszą być jawnie dekodowane z użyciem `Charset.forName("ISO-8859-2")`, aby uniknąć uszkodzenia polskich znaków diakrytycznych (np. ą, ć, ę, ł, ń, ó, ś, ź, ż).

---

## 2. Mapa adresów URL i nawigacja

| Zasób | Metoda | Adres URL | Parametry / Treść |
|---|---|---|---|
| Strona główna / Lista wydziałów | GET | `/index.html` lub `/` | Brak |
| Lista kierunków i grup wydziału | GET | `/schedule_view.php?site=show_kierunek.php&id={wydzialId}` | `id`: ID wydziału (1, 2, 7, 10, 11) |
| Plan tygodniowy grupy (bieżący tydzień) | GET | `/checkSpecjalnosc.php?specjalnosc={kod}` | `specjalnosc`: kod grupy (np. `s3PAM`) |
| Plan tygodniowy grupy (konkretny tydzień) | POST | `/checkSpecjalnosc.php?specjalnosc={kod}` | Form-data: `dzien=DD-MM-YYYY` (data poniedziałku tygodnia) |
| Plan semestralny grupy | GET | `/checkSpecjalnoscStac.php?specjalnosc={kod}` | `specjalnosc`: kod grupy |
| Lista nauczycieli | GET | `/schedule_view.php?site=show_nauczyciel.php&id=11` | Lista wszystkich nauczycieli w podziale na wydziały |
| Plan tygodniowy nauczyciela | GET/POST | `/checkNauczycielAll.php?pracownik={id}&wydzial={wid}` | `pracownik`: ID pracownika, `wydzial`: ID wydziału |
| Plan semestralny nauczyciela | GET | `/checkNauczycielWszystko.php?pracownik={id}&wydzial={wid}` | `pracownik`: ID pracownika, `wydzial`: ID wydziału |
| Lista sal i budynków | GET | `/schedule_view.php?site=show_sala.php&id=11` | Lista sal w podziale na budynki (A, C, E itd.) |
| Obciążenie sali | GET | `/checkSala.php?sala={salaId}` | `sala`: ID sali (np. 26) |
| Obciążenie budynku | GET | `/checkBudynek.php?Budynek={symbol}` | `Budynek`: litera budynku (np. A, C) |

---

## 3. Struktura wydziałów i grup

### Główne wydziały (`id`):
1. **id=1:** Wydział Nauk Technicznych i Ekonomicznych
2. **id=2:** Wydział Nauk Społecznych i Humanistycznych
3. **id=7:** Wydział Nauk o Zdrowiu i Kulturze Fizycznej
4. **id=10:** Wydział Erasmus
5. **id=11:** Wychowanie fizyczne

### Konwencja kodów kierunków i grup:
- Przedrostek trybu i roku:
  - `s1`, `s2`, `s3`, `s4` – studia stacjonarne (rok 1, 2, 3, 4)
  - `n1`, `n2`, `n3` – studia niestacjonarne (rok 1, 2, 3)
- Skrót kierunku / specjalności (np. `INF` = Informatyka, `PAM` = Programowanie Aplikacji Mobilnych, `GK` = Grafika Komputerowa, `BW` = Bezpieczeństwo Wewnętrzne, `RM` = Ratownictwo Medyczne, `P` = Pielęgniarstwo).
- Etykieta na stronie kierunków zawiera pełną nazwę kierunku i trybu, np. `Informatyka 3 – studia stacjonarne (s3INF)`.

---

## 4. Wybór tygodnia

- Na stronie planu grupy znajduje się formularz:
  ```html
  <form action="checkSpecjalnosc.php?specjalnosc=s3PAM" method="post">
      <select onChange="this.form.submit();" name="dzien">
          <option value="28-09-2026" selected="selected">28-09-2026 - 04-10-2026 (40 tydzien roku)</option>
          ...
      </select>
  </form>
  ```
- Wartość pola `dzien` to data poniedziałku w formacie `DD-MM-YYYY`.
- Przesłanie żądania `POST` z parametrem `dzien` zwraca plan na wybrany tydzień z zaznaczonym `selected="selected"`.

---

## 5. Format tabeli planu i reguły parsowania

Plan umieszczony jest w tabeli o klasie `TabPlan` (`<table class="TabPlan">`):

### 5.1 Nagłówki kolumn i podgrupy
- Pierwszy wiersz nagłówkowy zawiera:
  - Komórkę z informacją o dacie/godzinie lub pustą komórkę narożną.
  - Kolejne komórki odpowiadają **podgrupom**, np. `s3PAM1(1)`, `s3PAM2(1)u`, `s3PAM2(2)u`.
  - Każda podgrupa rozkłada się logicznie na 3 podkolumny:
    1. Przedmiot i typ zajęć
    2. Prowadzący
    3. Sala

### 5.2 Wiersze tabeli: Dni i godziny
- Wiersze dzielą się na dni tygodnia (nagłówek dnia, np. `Czwartek 01.10.2026` z atrybutem `colspan`).
- Każdy dzień zawiera bloki czasowe (np. `08:15-09:45`, `10:00-11:30`, `11:45-13:15`, `13:30-15:00`, `15:15-16:45`, `17:00-18:30`, `18:45-20:15`).
- **Uwaga:** Godziny bloków w soboty i niedziele różnią się od dni roboczych (np. start 13:45 zamiast 13:30). Parser nie może opierać się na stałej siatce godzin, lecz odczytuje godziny bezpośrednio z komórki czasu każdego wiersza.

### 5.3 Zawartość komórki zajęć
- Standardowy wpis: `SkrótPrzedmiotu (typ) | Tytuł Imię Nazwisko | Sala`
  - Przykład: `Pig (wyk)` w kolumnie przedmiotu, `dr Marek Miedziński` w kolumnie prowadzącego, `C212` w kolumnie sali.
- Puste komórki oznaczone są pojedynczym myślnikiem `-`.

---

## 6. Przypadki szczególne

### 6.1 Parzystość tygodni (dwa wpisy w jednej komórce)
Gdy w ramach jednego bloku znajdują się dwa przedmioty oddzielone separatorem (zazwyczaj `<hr>` lub nowy wiersz), górny wpis oznacza **tydzień parzysty**, a dolny **tydzień nieparzysty**. Parser przypisuje odpowiednią flagę parzystości.

### 6.2 Bloki 45-minutowe
- Notacja `1h [x/-]` oznacza zajęcia trwające 45 minut w **pierwszej połowie** bloku 90-minutowego (np. z bloku 08:15–09:45 powstaje 08:15–09:00).
- Notacja `1h [-/x]` oznacza zajęcia w **drugiej połowie** bloku (np. 09:00–09:45).
- Godziny rzeczywiste są automatycznie przeliczane przez parser.

### 6.3 Legenda i pełne nazwy przedmiotów
Pod tabelą `TabPlan` znajduje się tabela `TabLegenda`, zawierająca mapowanie:
- Skrót przedmiotu (np. `Pig`) → Pełna nazwa (np. `Projektowanie interfejsów graficznych`).
- Parser buduje słownik mapowania i automatycznie uzupełnia pole `subjectFull` w lekcjach.

### 6.4 Wpisy „Różne” (Lektoraty / Zajęcia ogólnouczelniane)
- Gdy w komórce występuje słowo `Różne`, odsyła ono do tabel `Legenda1` / `Legenda2` umieszczonych pod planem.
- Tabele te zawierają szczegółowe warianty (język obcy, poziom, prowadzący, sala).
- Parser oznacza taki wpis jako wariantowy, umożliwiając prezentację wyboru użytkownikowi.

### 6.5 Sale online
- Format sali: `Online_<liczba>` (np. `Online_3`, `Online 3`, `online_1`).
- Parser wykrywa prefiks `online` (bez względu na wielkość liter i obecność podkreślnika) i ustawia flagę `isOnline = true` oraz identyfikator wirtualnego pokoju `onlineId = 3`.

### 6.6 Typy zajęć
Zaobserwowane skróty typów w nawiasach:
- `wyk` → Wykład
- `lab` → Laboratoria
- `ćw`, `c` → Ćwiczenia
- `sem` → Seminarium
- `p` → Projekt
- `warszt` → Warsztaty
- `lekt` → Lektorat
W przypadku napotkania nieznanego skrótu parser zachowuje oryginalną wartość tekstową.
