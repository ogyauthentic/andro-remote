# Android project initial setup

Project ini adalah aplikasi Android modern berbasis Kotlin dan Jetpack Compose untuk mengirim data ke server TCP.

## Fitur
- koneksi ke server TCP
- input IP server dan port
- kirim pesan ke server
- menampilkan balasan server

## Struktur project
- `app/` : project Android
- `server.py` : server pendukung untuk testing
- `README.md` : panduan project

## Cara menjalankan aplikasi Android
1. Buka project di Android Studio.
2. Tunggu Gradle sync selesai.
3. Hubungkan device atau jalankan emulator.
4. Jalankan aplikasi.

## Default konfigurasi server
- Emulator Android Studio: `10.0.2.2`
- Device nyata: gunakan IP komputer yang menjalankan server di jaringan yang sama
- Port default: `9999`

## Cara menjalankan server
Jalankan perintah ini di terminal:

```bash
python server.py
```

Server akan berjalan di port `9999`.

## Contoh server test
```python
import socket

HOST = "0.0.0.0"
PORT = 9999

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((HOST, PORT))
server.listen(5)

print(f"Server berjalan di {HOST}:{PORT}")

while True:
    conn, addr = server.accept()
    print(f"Client terhubung: {addr}")
    data = conn.recv(4096)

    if data:
        message = data.decode("utf-8").strip()
        print(f"Pesan dari client: {message}")
        conn.sendall(f"Server menerima: {message}".encode("utf-8"))

    conn.close()
```

## Catatan
- Android membutuhkan izin `INTERNET` untuk socket TCP.
- Untuk emulator, gunakan `10.0.2.2` sebagai IP server.
- Untuk device fisik, gunakan IP laptop/komputer pada jaringan yang sama.
