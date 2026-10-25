import socket
import threading

HOST = "0.0.0.0"
PORT = 9999
TOKEN = "mysecret123"
command_queue = []


def send_response(conn, message):
    try:
        conn.sendall((message + "\n").encode("utf-8"))
    except Exception as exc:
        print(f"[ERROR] send_response: {exc}")


def queue_command(command):
    command_queue.append(command)


def handle_command(conn, command):
    cmd = command.strip()
    if not cmd:
        return

    print(f"[INFO] Command received: {cmd}")

    if cmd == "PING":
        send_response(conn, "PONG")
    elif cmd == "INFO":
        send_response(conn, "EXEC:INFO")
    elif cmd.startswith("TOAST|"):
        text = cmd.split("|", 1)[1]
        send_response(conn, f"EXEC:TOAST|{text}")
    elif cmd.startswith("URL|"):
        url = cmd.split("|", 1)[1]
        send_response(conn, f"EXEC:URL|{url}")
    elif cmd == "VIBRATE":
        send_response(conn, "EXEC:VIBRATE")
    elif cmd == "BATTERY":
        send_response(conn, "EXEC:BATTERY")
    elif cmd == "GPS":
        send_response(conn, "EXEC:GPS")
    elif cmd == "LIST_FILES":
        send_response(conn, "EXEC:LIST_FILES")
    elif cmd == "REBOOT":
        send_response(conn, "EXEC:REBOOT")
    elif cmd == "FLASH_ON":
        send_response(conn, "EXEC:FLASH_ON")
    elif cmd == "FLASH_OFF":
        send_response(conn, "EXEC:FLASH_OFF")
    elif cmd == "SCREEN_ON":
        send_response(conn, "EXEC:SCREEN_ON")
    elif cmd == "SCREEN_OFF":
        send_response(conn, "EXEC:SCREEN_OFF")
    elif cmd == "GET_STATUS":
        send_response(conn, "EXEC:GET_STATUS")
    elif cmd.startswith("QUEUE:"):
        items = cmd.split(":", 1)[1].split("|")
        for item in items:
            if item:
                queue_command(item)
        send_response(conn, f"ACK:QUEUE|{len(items)}")
    else:
        send_response(conn, "ERR:UNKNOWN_COMMAND")


def handle_client(conn, addr):
    print(f"[INFO] Client connected: {addr}")
    try:
        data = conn.recv(4096)
        if not data:
            return

        auth = data.decode("utf-8", errors="ignore").strip()
        if not auth.startswith("AUTH:"):
            send_response(conn, "AUTH_REQUIRED")
            return

        provided_token = auth.split(":", 1)[1]
        if provided_token != TOKEN:
            send_response(conn, "AUTH_FAILED")
            return

        send_response(conn, "AUTH_OK")
        print(f"[INFO] Client authenticated: {addr}")

        while True:
            data = conn.recv(4096)
            if not data:
                break

            msg = data.decode("utf-8", errors="ignore").strip()
            if not msg:
                continue

            if msg.startswith("RESULT:"):
                print(f"[RESULT] {addr}: {msg}")
                continue

            if msg.startswith("CMD:"):
                command = msg.split(":", 1)[1].strip()
                handle_command(conn, command)
            else:
                handle_command(conn, msg)

    except Exception as exc:
        print(f"[ERROR] Client {addr} error: {exc}")
    finally:
        conn.close()
        print(f"[INFO] Client disconnected: {addr}")


def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen(10)

    print(f"[INFO] Remote control server running on {HOST}:{PORT}")
    print(f"[INFO] Token: {TOKEN}")

    while True:
        conn, addr = server.accept()
        thread = threading.Thread(target=handle_client, args=(conn, addr), daemon=True)
        thread.start()


if __name__ == "__main__":
    main()
