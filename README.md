# Remote Control Project

Project ini adalah versi remote control yang lebih lengkap untuk client Android dan server TCP.

## Fitur utama
- autentikasi token
- koneksi TCP antara Android client dan server
- perintah remote control seperti:
  - `INFO`
  - `PING`
  - `TOAST|text`
  - `URL|https://example.com`
  - `VIBRATE`
  - `BATTERY`
  - `GPS`
  - `REBOOT`
  - `FLASH_ON`
  - `FLASH_OFF`
  - `SCREEN_ON`
  - `SCREEN_OFF`
  - `GET_STATUS`

## Struktur project
- `server.py` : server Python untuk menangani koneksi dan perintah
- `app/` : project Android Kotlin/Jetpack Compose
- `README.md` : dokumentasi project

## Konfigurasi default
- Host: `0.0.0.0`
- Port: `9999`
- Token: `mysecret123`

## Cara menjalankan server
Pada VPS atau komputer server, jalankan:

```bash
python3 server.py
```

## Cara menjalankan Android app
1. Buka folder `app` di Android Studio.
2. Tunggu Gradle sync selesai.
3. Ubah konfigurasi server di file `MainActivity.kt` jika perlu.
4. Jalankan ke emulator atau device Android.

Setelan default Android:
```kotlin
private val serverIp = "203.0.113.10"
private val serverPort = 9999
private val token = "mysecret123"
```

Ganti angka tersebut sesuai IP server public/VPS Anda.

## Protokol komunikasi
Client mengirim:
```text
AUTH:mysecret123
CMD:INFO
CMD:TOAST|Halo dari server
CMD:URL|https://google.com
CMD:BATTERY
```

Server membalas:
```text
AUTH_OK
DEVICE_INFO:AndroidRemoteClient|SDK=34|Model=Demo
ACK:TOAST|Halo dari server
BATTERY:85
GPS:lat=-6.200000|lng=106.816666
```

## Command list
| Command | Arti |
|--------|------|
| `PING` | cek koneksi |
| `INFO` | ambil info perangkat |
| `TOAST|text` | tampilkan toast di Android |
| `URL|https://...` | buka URL di browser |
| `VIBRATE` | getar telepon |
| `BATTERY` | cek status baterai |
| `GPS` | kirim lokasi GPS contoh |
| `REBOOT` | perintah reboot |
| `FLASH_ON` | nyalakan flash / lampu |
| `FLASH_OFF` | matikan flash |
| `SCREEN_ON` | hidupkan layar |
| `SCREEN_OFF` | matikan layar |
| `GET_STATUS` | cek status sistem |

## Catatan keamanan
- Token default hanya untuk demo.
- Untuk production, gunakan token yang unik dan aman.
- Pastikan port server dibuka dengan benar di firewall/VPS.
- Untuk jaringan beda jaringan, gunakan VPS public, port forwarding, atau ngrok.

## Contoh koneksi beda jaringan
Server di VPS/public:
```kotlin
serverIp = "203.0.113.10"
serverPort = 9999
```

Jika server di belakang NAT:
- gunakan port forwarding di router, atau
- gunakan ngrok tcp 9999

## Lisensi
Project ini dibuat untuk kebutuhan demo dan pembelajaran remote-control client/server.
Penggunaan untuk tujuan ilegal atau merugikan pihak lain dilarang.
