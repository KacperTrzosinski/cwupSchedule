# Silnik i architektura powiadomień (NOTIFICATIONS.md)

Stałe powiadomienie „następne zajęcia” informuje studenta o najbliższym bloku zajęć, czasie pozostałym do rozpoczęcia oraz bieżącym stanie dnia akademickiego.

---

## 1. Zasada działania i cykl życia

- **Czas pojawienia się:** Dokładnie 60 minut przed pierwszym blokiem zajęć danego dnia.
- **Dni wolne:** W dni bez zajęć powiadomienie nie pojawia się w ogóle.
- **Aktualizacja w miejscu:** Powiadomienie używa stałego identyfikatora `NOTIFICATION_ID = 1001` oraz flagi `setOnlyAlertOnce(true)`. Kanał powiadomień ma priorytet `IMPORTANCE_LOW` (brak sygnału dźwiękowego i wibracji), dzięki czemu aktualizacja treści na kolejny blok odbywa się dyskretnie.
- **Przełączenie bloków:** W momencie rozpoczęcia danego bloku powiadomienie przełącza się na kolejny nadchodzący blok, a blok trwający staje się widoczny w rozwiniętej treści jako `Teraz trwa: ...`.
- **Czas zniknięcia:** Po zakończeniu ostatniego bloku dnia powiadomienie jest automatycznie usuwane.

---

## 2. Realizacja techniczna (`AlarmManager` vs `ForegroundService`)

Zgodnie z wymaganiami projektowymi aplikacja **nie wykorzystuje ciągłego Foreground Service**, co pozwala zaoszczędzić baterię urządzenia:

1. **Czysta domena (`NotificationSchedulerEngine`):**
   - Klasa bez zależności od frameworka Androida.
   - Wejście: lista lekcji `List<LessonEntity>` oraz bieżący czas `LocalDateTime`.
   - Wyjście: obiekt `NotificationState` oraz lista precyzyjnych alarmów `List<ScheduledAlarm>` (`SHOW_FIRST`, `SWITCH_BLOCK`, `DISMISS_DAY`).
2. **Harmonogramowanie alarmów:**
   - Wykorzystanie `AlarmManager.setExactAndAllowWhileIdle(...)` (dla API 23+) lub fallback na `setAndAllowWhileIdle(...)` w przypadku braku zgody na dokładne alarmy w Androidzie 12+.
3. **Reakcja na zdarzenia systemowe (`ScheduleAlarmReceiver`):**
   - Alarmy są przeliczane po każdej synchronizacji planu.
   - Odbiornik nasłuchuje zdarzeń `BOOT_COMPLETED`, `TIME_SET` oraz `TIMEZONE_CHANGED`, gwarantując natychmiastowe przywrócenie harmonogramu po ponownym uruchomieniu urządzenia lub zmianie zegara.

---

## 3. Uprawnienia systemowe

| Uprawnienie | Wersja Androida | Przeznaczenie |
|---|---|---|
| `POST_NOTIFICATIONS` | Android 13+ (API 33+) | Zezwolenie na wysyłanie powiadomień w systemie. |
| `SCHEDULE_EXACT_ALARM` | Android 12+ (API 31+) | Precyzyjne budzenie w Doze Mode (60 min przed, początek bloku). |
| `RECEIVE_BOOT_COMPLETED` | Wszystkie | Przywrócenie harmonogramu po restarcie telefonu. |

---

## 4. Ograniczenia platformy Android i optymalizacja baterii

### 4.1 Odrzucanie powiadomień w Androidzie 14+
W systemie Android 14 (API 34) powiadomienia typu `setOngoing(true)` niepowiązane z aktywną usługą pierwszoplanową mogą zostać odrzucone przez użytkownika gestem przesunięcia (swipe). W takim przypadku powiadomienie powraca automatycznie przy następnym przełączeniu bloku lub przy najbliższym alarmie.

### 4.2 Agresywne zarządzanie baterią u producentów (OEM)
Niektórzy producenci urządzeń wprowadzają niestandardowe mechanizmy zabijania procesów w tle i blokowania `AlarmManager`:

- **Xiaomi / Redmi (MIUI / HyperOS):**
  - Ustawienia → Aplikacje → Zarządzanie aplikacjami → Plan Zajęć PWSZ → *Autostart: Włączony*.
  - Oszczędzanie energii → *Bez ograniczeń*.
- **Samsung (One UI):**
  - Ustawienia → Bateria → Limity użycia w tle → *Nigdy nieusypiane aplikacje* → Dodaj aplikację.
- **Huawei (EMUI):**
  - Ustawienia → Bateria → Uruchamianie aplikacji → Wyłącz *Zarządzaj automatycznie*, włącz *Autouruchomienie*, *Uruchomienie wtórne* i *Działanie w tle*.
- **OnePlus / Oppo / Realme (OxygenOS / ColorOS):**
  - Informacje o aplikacji → Zużycie baterii → Włącz *Zezwalaj na aktywność w tle* i *Zezwalaj na automatyczne uruchamianie*.
