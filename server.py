import socket
import threading

HOST = "0.0.0.0"
PORT = 9999
TOKEN = "mysecret123"


def send_response(conn, message):
    try:
        conn.sendall((message + "\n").encode("utf-8"))
    except Exception as exc:
        print(f"[ERROR] send_response: {exc}")


def handle_command(conn, command):
    command = command.strip()
    if not command:
        return

    print(f"[INFO] Command received: {command}")

    if command == "PING":
        send_response(conn, "PONG")
    elif command == "INFO":
        send_response(conn, "DEVICE_INFO:AndroidRemoteClient|SDK=34|Model=Demo")
    elif command.startswith("TOAST|"):
        text = command.split("|", 1)[1]
        send_response(conn, f"ACK:TOAST|{text}")
    elif command.startswith("URL|"):
        url = command.split("|", 1)[1]
        send_response(conn, f"ACK:URL|{url}")
    elif command == "VIBRATE":
        send_response(conn, "ACK:VIBRATE")
    elif command == "BATTERY":
        send_response(conn, "BATTERY:85")
    elif command == "GPS":
        send_response(conn, "GPS:lat=-6.200000|lng=106.816666")
    elif command == "REBOOT":
        send_response(conn, "ACK:REBOOT")
    elif command == "FLASH_ON":
        send_response(conn, "ACK:FLASH_ON")
    elif command == "FLASH_OFF":
        send_response(conn, "ACK:FLASH_OFF")
    elif command == "SCREEN_ON":
        send_response(conn, "ACK:SCREEN_ON")
    elif command == "SCREEN_OFF":
        send_response(conn, "ACK:SCREEN_OFF")
    elif command == "GET_STATUS":
        send_response(conn, "STATUS:ONLINE|TOKEN_OK|READY")
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

            message = data.decode("utf-8", errors="ignore").strip()
            if not message:
                continue

            if message.startswith("CMD:"):
                command = message.split(":", 1)[1].strip()
                handle_command(conn, command)
            elif message == "PING":
                send_response(conn, "PONG")
            else:
                handle_command(conn, message)

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
