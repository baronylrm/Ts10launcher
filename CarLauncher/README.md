# 🚗 CarLauncher — TS10 Head Unit

Android Auto tarzı araç başlatıcı. TS10 (Android 10) için optimize edilmiştir.

---

## 📋 Özellikler

- ✅ Ana ekran launcher (HOME intent)
- ✅ Araçta açık kalan ekran (FLAG_KEEP_SCREEN_ON)
- ✅ Yatay mod zorunlu (landscape)
- ✅ Tüm kurulu uygulamaları grid olarak listeler
- ✅ Telefon, Harita (Google Maps/Waze), Müzik, Ayarlar hızlı erişim
- ✅ Sistem medya kontrolleri (önceki/oynat/sonraki)
- ✅ Gerçek zamanlı batarya göstergesi
- ✅ Saat & tarih
- ✅ Araç açılışında otomatik başlar (BootReceiver)
- ✅ Geri tuşu kapatmaz (launcher davranışı)

---

## 🛠 Derleme (Windows/Mac/Linux)

### Gereksinimler
- [Android Studio](https://developer.android.com/studio) — ücretsiz
- JDK 11+ (Android Studio ile gelir)

### Adımlar

1. **Projeyi Aç**
   ```
   Android Studio → Open → CarLauncher klasörünü seç
   ```

2. **Gradle Sync**
   - "Sync Now" butonuna tıkla (otomatik çıkar)
   - İnternete bağlı olman gerekiyor (bağımlılıklar indirilir)

3. **APK Derle**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   APK şurada oluşur:
   ```
   CarLauncher/app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📲 TS10'a Yükleme

### Yöntem 1 — USB Flash Bellek (Önerilen)
1. `app-debug.apk` dosyasını USB belleğe kopyala
2. USB'yi TS10'a tak
3. TS10'da **Dosya Yöneticisi** aç
4. APK'ya dokun → **Yükle**
5. Bilinmeyen kaynaklara izin ver (Settings → Security → Unknown Sources)
6. Yükledikten sonra **CarLauncher**'ı varsayılan başlatıcı yap

### Yöntem 2 — ADB (Bilgisayar bağlantısı)
```bash
# TS10'da USB Hata Ayıklama'yı etkinleştir
# Settings → About → Build Number'a 7 kez dokun
# Settings → Developer Options → USB Debugging → ON

adb install app-debug.apk
```

### Yöntem 3 — WiFi ADB
```bash
# TS10 IP adresini öğren (Settings → WiFi → IP)
adb connect 192.168.x.x:5555
adb install app-debug.apk
```

---

## ⚙️ Varsayılan Launcher Yapma

1. Yükledikten sonra HOME tuşuna bas
2. "Hangi uygulama ile açmak istersiniz?" çıkar
3. **CarLauncher**'ı seç → **Her Zaman**

Artık araç her açıldığında CarLauncher başlar.

---

## 🗺 Harita Entegrasyonu

Öncelik sırası:
1. Google Maps (`com.google.android.apps.maps`)
2. Waze (`com.waze`)
3. Tarayıcıda maps.google.com

---

## 🎵 Müzik Entegrasyonu

Öncelik sırası:
1. Spotify
2. YouTube Music
3. Apple Music
4. Amazon Music
5. Sistem varsayılan müzik uygulaması

Medya kontrol butonları sistem düzeyinde çalışır — hangi uygulama açık olursa olsun kontrol eder.

---

## 🔧 Özelleştirme

`MainActivity.java` içinde değiştirilebilecekler:

| Satır | Ne değiştirir |
|-------|--------------|
| `new GridLayoutManager(this, 5)` | Uygulama grid sütun sayısı |
| `handler.postDelayed(this, 15000)` | Saat güncelleme sıklığı |
| `musicApps[]` dizisi | Öncelikli müzik uygulamaları |

---

## ❓ Sorun Giderme

**APK yüklenmiyor:**
→ Settings → Security → Unknown Sources'u etkinleştir

**Uygulamalar görünmüyor:**
→ AndroidManifest'teki `QUERY_ALL_PACKAGES` izni Android 11+ için gerekli. TS10 Android 10 olduğu için sorun çıkmaz.

**Medya kontrolleri çalışmıyor:**
→ Müzik uygulamasını arka planda açık bırak, CarLauncher'a dön.

**Harita açılmıyor:**
→ Google Maps veya Waze'in yüklü olduğundan emin ol.
