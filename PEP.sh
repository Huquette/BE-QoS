#!/bin/bash

# Constants
IFACE="eth1"                  # Replace with the actual network interface
HTB_ROOT_HANDLE="1:"          # HTB root handle
HTB_DEFAULT_CLASS="20"        # Default class ID
RESERVATION_CLASS="1:10"      # Class ID for reservation requests
DEFAULT_CLASS="1:11"          # Class ID for default traffic

# Function to set up the initial HTB configuration
setup_htb() {
  # Delete existing root qdisc
  tc qdisc del dev $IFACE root

  # Create root qdisc
  tc qdisc add dev $IFACE root handle $HTB_ROOT_HANDLE htb default $HTB_DEFAULT_CLASS

  local rate=512
  local ceil=512

  # Create child classes
  tc class add dev $IFACE parent $HTB_ROOT_HANDLE classid $RESERVATION_CLASS htb rate ${rate}kbit ceil ${ceil}kbit
  tc class add dev $IFACE parent $HTB_ROOT_HANDLE classid $DEFAULT_CLASS htb rate 2000kbit ceil 500kbit
}

# Function to start the TCP server to listen for reservation requests
start_tcp_server() {
  python3 qos_tcp_server.py
}

# Main function
main() {
  setup_htb
  start_tcp_server
}

main

