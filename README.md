# Remote Control Project

Project ini adalah versi final remote control untuk Android client dan server TCP yang dapat di-deploy ke VPS/public server.

## Fitur akhir
- autentikasi token
- koneksi TCP antar client dan server
- command queue pada client side
- real battery check
- real GPS check (last known location)
- file manager sederhana untuk list file di storage umum
- command set lengkap:
  - `PING`
  - `INFO`
  - `TOAST|text`
  - `URL|https://...`
  - `VIBRATE`
  - `BATTERY`
  - `GPS`
  - `LIST_FILES`
  - `REBOOT`
  - `FLASH_ON`
  - `FLASH_OFF`
  - `SCREEN_ON`
  - `SCREEN_OFF`
  - `GET_STATUS`
  - `QUEUE:cmd1|cmd2|cmd3`

## Struktur project
- `server.py` : server Python untuk protokol remote control
- `app/` : project Android Kotlin + Jetpack Compose
- `README.md` : dokumentasi utama

## Konfigurasi default
- Host: `0.0.0.0`
- Port: `9999`
- Token: `mysecret123`

## Jalankan server (VPS)
```bash
python3 server.py
```

## Jalankan Android app
1. Buka folder `app` di Android Studio.
2. Tunggu Gradle sync selesai.
3. Pastikan perangkat punya izin internet, lokasi, dan storage.
4. Jalankan aplikasi.

## Pengaturan default Android
```kotlin
private val serverIp = "203.0.113.10"
private val serverPort = 9999
private val token = "mysecret123"
```

Ganti ke IP public server / VPS Anda.

## Protokol
Client mengirim:
```text
AUTH:mysecret123
CMD:BATTERY
CMD:GPS
CMD:LIST_FILES
CMD:QUEUE:BATTERY|GPS|LIST_FILES
```

Server membalas:
```text
AUTH_OK
EXEC:BATTERY
EXEC:GPS
EXEC:LIST_FILES
ACK:QUEUE|3
```

Client kemudian mengirim hasil balik:
```text
RESULT:BATTERY:85
RESULT:GPS:lat=-6.200000|lng=106.816666
RESULT:FILES:DCIM;Download;Pictures
```

## Command queue
Fitur command queue ada di sisi Android client. Ketika ada beberapa perintah yang masuk berurutan, aplikasi akan menampungnya di `ConcurrentLinkedQueue` lalu memproses satu per satu secara berurutan.

## Fitur real yang sudah ditambahkan
- Battery: membaca kapasitas baterai via `BatteryManager`
- GPS: membaca `lastKnownLocation` dari GPS atau network provider
- File manager: list file dari storage umum `/sdcard`
- Command queue: memproses perintah satu per satu

## Catatan keamanan
- Token default hanya untuk demo.
- Untuk production, ganti token menjadi unik.
- Buka port 9999 di firewall / VPS.
- Untuk jaringan berbeda, gunakan VPS public, port forwarding, atau ngrok.

## Catatan penggunaan illegal
Project ini dibuat untuk tujuan pembelajaran, eksperimen, dan demo. Penggunaan untuk aktivitas ilegal atau merugikan orang lain sangat dilarang.
