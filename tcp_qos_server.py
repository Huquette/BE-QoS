import socket
from subprocess import call

# Constants
HOST = '0.0.0.0'    #addresse du routeur Linux en question
PORT = 12345
BUFFER_SIZE = 1024

def process_request(request):
    request_type, request_data = request.split(':', 1)
    if request_type == 'reservation':
        mark, flowid, dest_addr, dest_port = request_data.split(',')
        call(["tc", "filter", "add", "dev", "eth0", "parent", "1:0", "protocol", "ip", "prio", "1", "handle", mark, "fw", "flowid", flowid])
        call(["iptables", "-A", "PREROUTING", "-t", "mangle", "-d", dest_addr, "--dport", dest_port, "-j", "MARK", "--set-mark", mark])
    elif request_type == 'free':
        mark, flowid, dest_addr, dest_port = request_data.split(',')
        call(["tc", "filter", "del", "dev", "eth0", "parent", "1:0", "protocol", "ip", "prio", "1", "handle", mark, "fw", "flowid", flowid])
        call(["iptables", "-D", "PREROUTING", "-t", "mangle", "-d", dest_addr, "--dport", dest_port, "-j", "MARK", "--set-mark", mark])
    else:
        print("Invalid request type")

def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()

        while True:
            conn, addr = s.accept()
            with conn:
                print(f'Connected by {addr}')
                data = conn.recv(BUFFER_SIZE).decode('utf-8')
                if data:
                    process_request(data)

if __name__ == "__main__":
    main()

