# Dokumentacja modułu parsera (PARSER.md)

Moduł `:parser` to niezależny moduł JVM (Kotlin), pozbawiony zależności od Android SDK.
Głównym zadaniem modułu jest konwersja surowego kodu HTML ze strony uczelni na silnie typowane obiekty domenowe.

---

## 1. Architektura modułu

```
parser/
├── src/main/java/pl/legnica/planzajec/parser/
│   ├── model/
│   │   ├── Department.kt
│   │   ├── StudyCourseAndGroup.kt (StudyCourse, StudyGroup)
│   │   ├── LessonEnums.kt (LessonType, WeekParity, ChangeFlag)
│   │   ├── Lesson.kt
│   │   └── ScheduleResult.kt (ScheduleResult, WeekOption, Teacher, Room, VariantOption)
│   ├── DepartmentParser.kt
│   ├── CourseGroupParser.kt
│   ├── ScheduleParser.kt
│   ├── TeacherParser.kt
│   └── RoomParser.kt
└── src/test/
    ├── resources/fixtures/ (*.html z kodowaniem ISO-8859-2)
    └── java/pl/legnica/planzajec/parser/
        ├── FixtureLoader.kt
        ├── DepartmentParserTest.kt
        ├── CourseGroupParserTest.kt
        ├── ScheduleParserTest.kt
        └── TeacherAndRoomParserTest.kt
```

---

## 2. Główne parsery i algorytmy

### 2.1 `DepartmentParser`
- **Wejście:** HTML strony głównej (`/` lub `index.html`).
- **Ekstrakcja:** Odnośniki zawierające `show_kierunek.php?id={id}`.
- **Wynik:** Lista obiektów `Department(id, name)`.

### 2.2 `CourseGroupParser`
- **Wejście:** HTML widoku wydziału (`schedule_view.php?site=show_kierunek.php&id={id}`).
- **Struktura:** Elementy akordeonu `<ul class="accordion"> <li> <a href="#">Nazwa kierunku</a> <div>...</div> </li>`.
- **Ekstrakcja:**
  - Nazwa kierunku, rok studiów (np. `Informatyka 3- studia stacjonarne` -> rok 3).
  - Tryb studiów (`isFullTime = false` dla wystąpień słowa `niestacjonarne`, domyślnie `true`).
  - Kody grup z odnośników `checkSpecjalnosc.php?specjalnosc={kod}`.

### 2.3 `ScheduleParser`
- **Wejście:** HTML planu tygodniowego (`checkSpecjalnosc.php?specjalnosc={kod}`) lub semestralnego (`checkSpecjalnoscStac.php`).
- **Kluczowe kroki sanityzacji:**
  - Wycofanie uszkodzonych znaczników `<div>` wstrzykiwanych przez stary kod PHP wewnątrz tabeli (`<table>`), co w standardzie HTML5 powodowało wypychanie wierszy (`<tr>`) poza tabelę (tzw. foster-parenting).
- **Ekstrakcja nagłówków:**
  - Odczyt podgrup z pierwszego wiersza tabeli `TabPlan` (komórki z klasą `nazwaSpecjalnosci`).
  - Każda podgrupa zajmuje 3 podkolumny (przedmiot, prowadzący, sala).
- **Ekstrakcja wierszy:**
  - Wiersze dni: komórki `nazwaDnia`. Obsługa zarówno formatu tygodniowego `DD.MM.YYYY`, jak i semestralnego `YYYY-MM-DD`.
  - Wiersze godzin: komórki `godzina` w formacie `HH:mm-HH:mm`.
- **Przetwarzanie komórek lekcji:**
  - **Sale online:** Wykrycie wzorca `Online_<liczba>`, `Online <liczba>` -> flaga `isOnline = true`, `onlineId = "<liczba>"`.
  - **Parzystość tygodni:** Wykrycie separatora `<hr>` w komórce bloku -> górna lekcja otrzymuje `WeekParity.EVEN`, dolna `WeekParity.ODD`.
  - **Bloki 45-minutowe:**
    - Notacja `1h [x/-]` -> czas rozpoczęcia bez zmian, zakończenie: `start + 45 min`.
    - Notacja `1h [-/x]` -> czas rozpoczęcia: `end - 45 min`, zakończenie bez zmian.
  - **Typ zajęć:** Wykrycie skrótu w nawiasach (np. `wyk`, `lab`, `ćw`, `sem`, `p`, `warszt`, `lekt`) i zmapowanie do `LessonType`.
  - **Wpisy „Różne” i tabele wariantowe:** Oznaczenie `isVariant = true` oraz odczyt dodatkowych tabel `Legenda1`, `Legenda2` itd.

---

## 3. Jak dodać nowy fixture i rozszerzyć testy

1. Pobierz stronę z zachowaniem kodowania:
   ```powershell
   $client = [System.Net.Http.HttpClient]::new()
   $bytes = $client.GetByteArrayAsync("http://www.plan.pwsz.legnica.edu.pl/checkSpecjalnosc.php?specjalnosc=NOWY_KOD").Result
   [System.IO.File]::WriteAllBytes("parser/src/test/resources/fixtures/plan_NOWY_KOD.html", $bytes)
   ```
2. Załaduj plik w teście za pomocą `FixtureLoader.load("plan_NOWY_KOD.html")`.
3. Uruchom testy komendą `./gradlew :parser:test`.
