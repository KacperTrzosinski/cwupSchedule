# Procedura Wydania & CI/CD — cwupSchedule

Dokument opisuje proces budowania, podpisywania oraz publikowania wydań produkcyjnych aplikacji **cwupSchedule**.

---

## 1. Konfiguracja Podpisywania (Signing Config)

Aplikacja wspiera elastyczną konfigurację podpisywania w `app/build.gradle.kts`:

1. **Gdy zdefiniowane są zmienne środowiskowe:**
   - `KEYSTORE_FILE`: ścieżka do pliku `.keystore` lub `.jks`
   - `KEYSTORE_PASSWORD`: hasło do magazynu kluczy
   - `KEY_ALIAS`: alias klucza
   - `KEY_PASSWORD`: hasło do aliasu klucza
   Aplikacja podpisuje plik APK / AAB kluczem produkcyjnym.

2. **Gdy zmienne nie są zdefiniowane (Fallback):**
   - Aplikacja automatycznie używa klucza `debug` do podpisania `assembleRelease`, umożliwiając lokalne budowanie i testowanie kompilacji release bez błędów.

---

## 2. Generowanie Klucza Produkcyjnego (Jednorazowo)

W konsoli wykonaj:
```bash
keytool -genkeypair -v -keystore release.keystore -alias cwup_release_key -keyalg RSA -keysize 2048 -validity 10000
```

Konwersja pliku `.keystore` na format Base64 do konfiguracji w GitHub Secrets:
- **Linux/macOS:**
  ```bash
  base64 -w 0 release.keystore > keystore_base64.txt
  ```
- **Windows (PowerShell):**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File -Encoding ascii keystore_base64.txt
  ```

---

## 3. Konfiguracja GitHub Secrets

W repozytorium GitHub przejdź do: **Settings → Secrets and variables → Actions** i dodaj:

| Nazwa Secretu | Opis |
|---|---|
| `KEYSTORE_BASE64` | Zawartość pliku `keystore_base64.txt` |
| `KEYSTORE_PASSWORD` | Hasło podane podczas tworzenia magazynu |
| `KEY_ALIAS` | Alias klucza (np. `cwup_release_key`) |
| `KEY_PASSWORD` | Hasło do aliasu |

---

## 4. Automatyczne Wydanie (GitHub Actions Workflow)

Wydanie produkcyjne uruchamiane jest automatycznie po wypchnięciu taga w formacie `v*` (np. `v1.0.0`):

```bash
git tag v1.0.0
git push origin v1.0.0
```

Workflow `.github/workflows/release.yml`:
1. Pobiera repozytorium z pełną historią commitów.
2. Konfiguruje środowisko Java 21 Temurin i cache Gradle.
3. Odszyfrowuje magazyn kluczy ze zmiennej `KEYSTORE_BASE64`.
4. Wykonuje komendy `./gradlew :app:assembleRelease :app:bundleRelease`.
5. Zmienia nazwy plików wyjściowych na czytelne:
   - `PlanZajec-CollegiumWitelona-v1.0.0.apk`
   - `PlanZajec-CollegiumWitelona-v1.0.0.aab`
6. Tworzy oficjalne wydanie na GitHubie (**GitHub Release**) i dołącza skompilowane artefakty.

Możliwe jest również ręczne uruchomienie procesu z poziomu karty **Actions → Release → Run workflow** z podaniem numeru wersji.
