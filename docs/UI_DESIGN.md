# Design System & UI/UX — cwupSchedule

Dokument opisuje wytyczne wizualne, paletę kolorystyczną, typografię oraz komponenty interfejsu aplikacji **cwupSchedule**.

---

## 1. Filozofia Wizualna

Interfejs aplikacji zaprojektowano w konwencji **Dark Neomorphic / Cyber Minimal**:
- Dominacja głębokich, chłodnych grafitów i czerni z subkulturą IDE dla programistów.
- Celowa rezygnacja z pastelowych, losowych kolorów Material You na rzecz stałych, wyrazistych gradientów dla każdego typu zajęć akademickich.
- Neonowe akcenty cyjanu (`#00D2FF`) i indygo (`#3A7BD5`) sygnalizujące interaktywność i skupienie.
- Wyraźna hierarchia wizualna z subtelnymi obramowaniami (`1dp` ramki kart o barwie `#21262D`).

---

## 2. Paleta Kolorów (Tokens)

| Token | Wartość Hex | Zastosowanie |
|---|---|---|
| `DarkBackground` | `#0B0E14` | Główne tło ekranów i widoków |
| `AmoledBackground` | `#000000` | Tło w trybie AMOLED (głęboka czerń) |
| `DarkSurface` | `#161B22` | Tło kart lekcji, paneli, kontenerów nawigacji |
| `DarkSurfaceElevated`| `#1E2530` | Tło okien dialogowych, dropdownów, aktywnych chipów |
| `DarkSurfaceBorder` | `#21262D` | Subtelne krawędzie oddzielające komponenty |
| `AccentCyan` | `#00D2FF` | Główny kolor akcentowy, ikony, zaznaczone zakładki |
| `AccentIndigo` | `#3A7BD5` | Drugorzędny kolor akcentowy |
| `TextPrimary` | `#F0F6FC` | Główny tekst, tytuły przedmiotów, godziny |
| `TextSecondary` | `#8B949E` | Prowadzący, sale, opisy, etykiety pomocnicze |
| `TextMuted` | `#64748B` | Daty archiwalne, nieaktywne ikony |
| `StatusOnline` | `#38BDF8` | Etykieta i ikona zajęć zdalnych (Teams/Meet) |
| `StatusCancelled` | `#EF4444` | Oznaczenie zajęć odwołanych / zastępstw |

---

## 3. Kolorystyka Typów Zajęć (Lesson Gradients)

Każdy typ zajęć akademickich posiada unikalny gradient na lewej krawędzi karty:

- **Wykład:** Fioletowo-indygo (`#8B5CF6` → `#6366F1`)
- **Ćwiczenia / Audytoryjne:** Cyjanowo-błękitny (`#00D2FF` → `#3A7BD5`)
- **Laboratorium / Warsztaty:** Szmaragdowo-zielony (`#10B981` → `#059669`)
- **Projekt / Seminarium:** Bursztynowo-pomarańczowy (`#F59E0B` → `#D97706`)
- **Lektorat (Język obcy):** Różowo-czerwony (`#EC4899` → `#BE185D`)
- **Zajęcia Zdalne (Online):** Jasnoniebieski (`#38BDF8` → `#0284C7`) z ikoną kamery wideo

---

## 4. Komponenty Kluczowe

### Karta Lekcji (`LessonCard`)
- **Pasek czasu:** start i koniec z wyliczonym czasem trwania (np. `90 min`).
- **Wskaźnik trwania:** dla trwających zajęć wyświetlana jest pulsująca zielona kropka ("TRWA TERAZ") oraz liniowy pasek postępu czasu upływającego z zajęć.
- **Pill sali/online:** wyraźne wyróżnienie czy zajęcia odbywają się w murach uczelni (np. sala 214, budynek A), czy na platformie zdalnej.
- **Karta "Okienko":** jeżeli przerwa między kolejnymi zajęciami wynosi co najmniej 15 minut, na liście renderowana jest estetyczna karta informacyjna z czasem trwania przerwy ("Przerwa / okienko 1 godz. 30 min").

### Widok Dualny Planu
1. **Tryb "Najbliższe":**
   - Przyklejane nagłówki dni (Sticky Headers).
   - Dynamiczny licznik u góry: "Najbliższe zajęcia: za 45 minut".
   - Dostęp do wyboru tygodnia z semestru z natychmiastowym przeładowaniem.
2. **Tryb "Dzień po dniu":**
   - Horyzontalny pager lub lista dni roboczych.
   - Idealny do sprawdzania planu na jutro lub konkretny dzień tygodnia.

### Proces Onboarding
- Kaskadowy wybór: Wydział → Kierunek studiów → Grupa studencka.
- Automatyczne wykrycie grup laboratoryjnych z planu (L1, L2 itp.) i zapytanie użytkownika o preferowaną podgrupę.
- Wyjaśnienie uprawnienia do powiadomień w Androidzie 13+ przed wyświetleniem promptu systemowego.
