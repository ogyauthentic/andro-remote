# Android project initial setup

This project is a modern Android Kotlin app built with Jetpack Compose for a TCP client connection.

## Features
- connect to a TCP server
- input server IP and port
- send a message to the server
- display the server response

## How to run
1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Connect a device or start an emulator.
4. Run the app.

## Default server
- Emulator: use `10.0.2.2`
- Real device: use your laptop/PC IP on the same network
- Port: `9999`

## Test server example
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
