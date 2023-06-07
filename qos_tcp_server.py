import socket
from subprocess import call

# Constants
HOST = '192.168.2.254'
PORT = 7500
BUFFER_SIZE = 1024
FLOW_ID = "1:10" #Identifier of the Reservation requets queue
DSCP_ID = "46" #DSCP ID

def process_request(request):
    request_type, request_data = request.split(':', 1)
    print("request " + request)
    print(request_type)
    if 'reservation' in request_type:
        mark, dest_addr, dest_port = request_data.split(',')
        call(["tc", "filter", "add", "dev", "eth0", "parent", "1:0", "protocol", "ip", "prio", "1", "handle", mark, "fw", "flowid", FLOW_ID])
        call(["iptables", "-A", "PREROUTING", "-t", "mangle", "-d", dest_addr, "-p","udp", "--dport", dest_port, "-j", "MARK", "--set-mark", mark])
        call(["iptables", "-A", "POSTROUTING", "-t", "mangle", "-d", dest_addr, "-p","udp", "--dport", dest_port, "-j", "DSCP", "--set-dscp", DSCP_ID])

    elif 'free' in request_type:
        mark, dest_addr, dest_port = request_data.split(',')
        call(["tc", "filter", "del", "dev", "eth0", "parent", "1:0", "protocol", "ip", "prio", "1", "handle", mark, "fw", "flowid", FLOW_ID])
        call(["iptables", "-D", "PREROUTING", "-t", "mangle", "-d", dest_addr,"-p udp", "--dport", dest_port, "-j", "MARK", "--set-mark", mark])
        call(["iptables", "-D", "POSTROUTING", "-t", "mangle", "-d", dest_addr, "-p udp", "--dport", dest_port, "-j", "DSCP", "--set-dscp", DSCP_ID])
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

