import socket

HOST = "0.0.0.0"
PORT = 9999

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((HOST, PORT))
server.listen(5)

print(f"Server TCP aktif di {HOST}:{PORT}")

while True:
    conn, addr = server.accept()
    print(f"Client terhubung: {addr}")

    data = conn.recv(4096)
    if data:
        message = data.decode("utf-8").strip()
        print(f"Pesan dari client: {message}")
        conn.sendall(f"Server menerima: {message}".encode("utf-8"))

    conn.close()
