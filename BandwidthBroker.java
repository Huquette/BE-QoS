//package openjsip;

import java.net.UnknownHostException;
//import ClientsConnection;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
//import openjsip.ClientsConnection;
import java.io.DataOutputStream;
import java.io.DataInputStream;
//import openjsip.Reseau;
//static Reseau.getCIDRToSubnetMask;
//static Reseau.ipToNetwork;
import java.util.*;

public class BandwidthBroker {
	//hashmap representing the SLA contracts made with each of the remote networks. The SLA hashmap records the remote network, and the bandwidth allocated for this network
	static HashMap <Integer, Integer> sla = new HashMap<Integer, Integer>(); 
	//hashmap representing the used resources, which records each ip and the bandwidth allocated to this ip. A REVOIR CA PEUT ETRE PAS BESOIN
	static HashMap <Integer, Integer> usedResources = new HashMap<Integer, Integer>();

	static HashMap <String, String> routersMap = new HashMap<String, String>();
	
	//static HashMap <String, Integer> firstTimeConnectionSrc = new HashMap<String, Integer>();
	
	//static HashMap <String, Integer> firstTimeConnectionDest = new HashMap<String, Integer>();
	
	static ArrayList <ClientsConnection> listClients = new ArrayList<ClientsConnection>();

	public static final int TOTAL_BANDWIDTH = 1000; // Kbps
	public static final int PORT_PROXY = 9650; 

	// bandwidth total utilisé sur le réseau pour l'instant
	private static Integer sommeBandwidthTotal(){ 
		Integer somme = 0;
		for(int flowID : usedResources.keySet()){
			somme += usedResources.get(flowID);
		}
		return somme;
	}
	
	//method to add an SLA in the already existing SLA hashmap
	private static void addSLA(ClientsConnection client, Integer resource) throws UnknownHostException {
		sla.put(client.getFlowID(), resource);
	}

	private static boolean checkSLA(ClientsConnection client, Integer bandwidth) throws UnknownHostException {
		Integer bandwidthAvailable = 0;
		if(usedResources.containsKey(client.getFlowID())) {
			bandwidthAvailable = usedResources.get(client.getFlowID());
			System.out.println("Connection de clients est deja dans used resources");
		} else {
			usedResources.put(client.getFlowID(), 0);
			System.out.println("Premiere connection de clients");
		}
		
		if(sla.containsKey(client.getFlowID())) {
			if (bandwidthAvailable + bandwidth < sla.get(client.getFlowID())) {
				System.out.println("The SLA contract is respected - sla exists");
				return true;
			} else {
				System.out.println("The SLA contract is not respected - sla exists");
				return false;
			}
		} else {
			addSLA(client, 100);
			if(sla.containsKey(client.getFlowID())) {
				if (bandwidthAvailable + bandwidth < sla.get(client.getFlowID())) {
					System.out.println("The SLA contract is respected - sla doesn't exist");
					return true;
				} else {
					System.out.println("The SLA contract is not respected - sla doesn't exist");
					return false;
				}
			}
		}
		return false;
	}

	private boolean allocateBandwidth(ClientsConnection client, Integer bandwidthToAllocate) {
		try {
				//the bandwidth available is the bandwidth allocated for each flowID that is trying to use in the network
				Integer userBandwidth = usedResources.get(client.getFlowID());

				// true if the flowID is not in the usedResources hashmap
				boolean firstTimeRequesting = !usedResources.containsKey(client.getFlowID());
				boolean SLAContracted = sla.containsKey(client.getFlowID());

				//if the SLA is contracted
				if (SLAContracted) {
					//if the client with the corresponding flowID respects the contract, we update the bandwidth to allocate in usedResources
					if (checkSLA(client, bandwidthToAllocate)) {
						if (bandwidthToAllocate < (TOTAL_BANDWIDTH - sommeBandwidthTotal())) {
							if (firstTimeRequesting) {
								usedResources.put(client.getFlowID(), bandwidthToAllocate);
							}
							//if it is not the first time requesting
							else {
								usedResources.replace(client.getFlowID(), userBandwidth + bandwidthToAllocate);
							}
							return true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

	//method to liberate the bandwidth that was allocated to an incoming user with the ip specified in the 1st parameter and the bandwidth to liberate in the 2nd
	//remark : not very practical to re-specify the bandwidth to liberate. However, if bandwidth was allocated to the user from multiple sources, you need to be
	//able to track which bandwidth was allocated to which source. It is thus simpler to just specify the bandwidth directly in the parameters.
	private boolean liberateBandwidth(ClientsConnection client, Integer bandwidthToLiberate) {
		try {
			
			Integer bandwidthUser;
			boolean firstTimeRequesting = !usedResources.containsKey(client.getFlowID());
			boolean SLAContracted = sla.containsKey(client.getFlowID());
			Integer userBandwidth = usedResources.get(client.getFlowID());
			// The method checks if the user with the IP address "ip" has not made any previous requests by using the variable firstTimeRequesting.
			// If that is the case, it also verifies if the corresponding SLA contract for the network IP address "ip" exists.
			if(SLAContracted && firstTimeRequesting) {
				System.out.println("Error : you tried to liberate bandwidth that was never allocated in the first place");
				return false;
			} else if(SLAContracted) { // if the SLA is contracted, and it is not the first time requesting 
				usedResources.replace(client.getFlowID(), userBandwidth - bandwidthToLiberate);
				return true;
			}	else {
				System.out.println("SLA not contracted yet");
				return false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	// The above code is defining a method named "acceptCall" that takes two parameters: "flowID" and "bw"
	// both of type Integer. The method also throws an exception of type UnknownHostException. The purpose
	// of the method is not clear from the given code snippet alone, but it is likely related to accepting
	// a call or connection in a network or communication system.
	public void acceptCall(ClientsConnection client, Integer bw) throws UnknownHostException {
		//if the packet received has a requestType = 'ACCEPT' (i.e. he accepts the call)
		System.out.println("Call incoming from the client n° : "+ client.getFlowID());
		if(sla.containsKey(client.getFlowID())) {
			
			if(this.allocateBandwidth(client, bw)) {
				System.out.println("Bandwidth can be allocated to the following client : " + client.getFlowID());
			} else {
				System.out.println("Couldn't allocate bandwidth to the client n° : " + client.getFlowID() + " check if the SLA is respected.");
			}
		} else {
			
			if(this.allocateBandwidth(client, bw)){
				addSLA(client, 100);
				System.out.println("Bandwidth can be allocated to the following client : " + client.getFlowID());
			} else{
				System.out.println("Couldn't allocate bandwidth to the client n° : " + client.getFlowID() + " check if the SLA is respected.");
			}
		}	
	}
	

	//when a machine belonging to another network wants to hang up with a machine in the network of the bandwidth broker, this method is called by the BB
	// The above code is a Java method called `hangUpCall` that takes two parameters: `flowID` and `bw`.
	// It throws an `UnknownHostException` exception.
	public void hangUpCall(ClientsConnection client, Integer bw) throws UnknownHostException {
		
		System.out.println("Call from the client n° : " + client.getFlowID() + " has ended");

		//if the packet received has a requestType = 'BYE' (i.e. he hangs up the call)
		if(sla.containsKey(client.getFlowID())){
			if(this.liberateBandwidth(client, bw)){
				System.out.println("Bandwidth liberated from the following client : " + client.getFlowID());
				
			} else{
				System.out.println("Couldn't liberate bandwidth to the client : " + client.getFlowID() + ".");
			}
		} else{
			System.out.println("Error: IP is not specified in the SLA.");
		}
	}
			
	/**
	 * The BBThread class listens for incoming proxy connections on a specified port and handles them in
	 * separate threads.
	 */
	public class BBThread extends Thread {
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT_PROXY);
				System.out.println("BandwidthBroker listening on the proxy port " + PORT_PROXY);
	 
				while (true) {
					Socket proxySocket = serverSocket.accept();
					System.out.println("New proxy connected: " + proxySocket.getInetAddress().getHostAddress());
	
					//handle the client socket in a separate thread
					Thread clientThread = new Thread(() -> handleProxy(proxySocket));
					clientThread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * This function handles a proxy socket connection by reading a request from the client, processing
	 * it, and sending a response.
	 * 
	 * @param proxySocket A Socket object representing the connection between the proxy server and the
	 * client. It is used to send and receive data between the two endpoints.
	 */
	private void handleProxy(Socket proxySocket) {
		try {
			//create the output stream reader to read data sent to the client
			DataOutputStream writer = new DataOutputStream(proxySocket.getOutputStream());
            
			//create the input stream writer to send data by the client
			DataInputStream inputStream = new DataInputStream(proxySocket.getInputStream());
            
			//read the incoming request from the client
			//the client needs to send a request with this format: ip/mask/bandwidth
			String request = inputStream.readUTF();
			System.out.println("Received request from client: " + request);

			//process the request and send a response
			//if false => not enough bandwidth or sla not respected
			//if true => enough bw and sla respected, client can access the resource
			boolean response = processRequest(request, writer);

			proxySocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ClientsConnection isClient(String ipSrc,String ipDest) {
		for(ClientsConnection c: listClients) {
	    	if ((c.getIpSrc().equals(ipSrc) && c.getIpDest().equals(ipDest))) {
	    		System.out.println("xlient exists");
	    		return c;
	    	}
	    	else if (c.getIpDest().equals(ipSrc) && c.getIpSrc().equals(ipDest)) {
	    		System.out.println("xlient exists");
	    		return c;
	    	}
		}
		return null;
	}
	
	private boolean processRequest(String request, DataOutputStream writer) throws IOException {
		//array that allows to split the request into three variables : ip, mask and bandwidth
		ClientsConnection clientsConnection =  null;
	    System.out.println("Paquet recu");
	    //String splitRequest = request.substring(1);
	    String[] requestTab = request.split(";");
	    Integer requestType   = Integer.parseInt(requestTab[0]);
	    String  ipSrc         = requestTab[1];
	    Integer portSrc       = Integer.parseInt(requestTab[2]);
	    String  ipDest        = requestTab[3];
	    Integer portDest      = Integer.parseInt(requestTab[4]);
	    Integer bandwidth     = Integer.parseInt(requestTab[5]);

	    //String[] requestTab = request.split(":");
	    //String ipSrc = requestTab[0];
	    //Integer portSrc = Integer.parseInt(requestTab[1]);
	    //String ipDest       = requestTab[2];
	    //Integer  portDest        = Integer.parseInt(requestTab[3]);
	    //Integer bandwidth = requestTab[4]
	    
		// bandwidth utilisée inférieure à bandwidth restante dans le réseau 
	    ClientsConnection c = isClient(ipSrc,ipDest);
		//accepting the call since everything is fine
		if(requestType == 1) {
			if(c==null) {
				clientsConnection = new ClientsConnection(requestType,ipSrc,ipDest,portSrc,portDest,bandwidth);
				listClients.add(clientsConnection);
				if (bandwidth > (TOTAL_BANDWIDTH - sommeBandwidthTotal())) { // bandwidth not respected
					clientsConnection.setReservation(false);
					writer.writeUTF("0");
					System.out.println("bandwidth not respected");
					System.out.println("Sent response to proxy: " + false);
					return false;
				}
				if (!checkSLA(clientsConnection, bandwidth)) { // SLA not respected
					clientsConnection.setReservation(false);
					writer.writeUTF("0");
					System.out.println("sla not respected");
					System.out.println("Sent response to proxy: " + false);
					return false;
				}
				clientsConnection.setReservation(true);
				acceptCall(clientsConnection, bandwidth);
				writer.writeUTF("1");
				//add routers corresponding to the network of the packet we just received
				if((clientsConnection.getIpSrc()  != null) && (clientsConnection.getIpDest() != null)) {
					addRouter(clientsConnection.getIpSrc());
					addRouter(clientsConnection.getIpDest());
				} else {
					System.out.println("ERROR IP NULL");
				}
				//create sockets to send the packet to the routers of each client
				Socket routerSocketSrc = new Socket( (Reseau.ipToNetwork(clientsConnection.getIpSrc()) + ".254"), 7500);
				Socket routerSocketDest = new Socket( (Reseau.ipToNetwork(clientsConnection.getIpDest()) + ".254"), 7500);
				sendRouterTCPSrc(routerSocketSrc, clientsConnection);
				System.out.println("Envoi request type : " + clientsConnection.getRequestType() + " vers " + (Reseau.ipToNetwork(clientsConnection.getIpSrc()) + ".254"));
				sendRouterTCPDest(routerSocketDest, clientsConnection);
				System.out.println("Envoi request type : " + clientsConnection.getRequestType() + " vers " + (Reseau.ipToNetwork(clientsConnection.getIpDest()) + ".254"));
				return true;
			}
		} else if (requestType == 0) {
			System.out.println("Liberating resources");
			if(c!=null) {
				clientsConnection =c;
				clientsConnection.setRequestType(0);
				hangUpCall(clientsConnection, bandwidth);
				listClients.remove(c);
				writer.writeUTF("1");
				//add routers corresponding to the network of the packet we just received
				if((clientsConnection.getIpSrc()  != null) && (clientsConnection.getIpDest() != null)) {
					if(addRouter(clientsConnection.getIpSrc())){
						System.out.println("Router added for the source client.");
					}	
					if(addRouter(clientsConnection.getIpDest())){
						System.out.println("Router added for the destination client.");
					}
				} else {
					System.out.println("ERROR IP NULL");
				}
				//create sockets to send the packet to the routers of each client
				Socket routerSocketSrc = new Socket( (Reseau.ipToNetwork(clientsConnection.getIpSrc()) + ".254"), 7500);
				Socket routerSocketDest = new Socket( (Reseau.ipToNetwork(clientsConnection.getIpDest()) + ".254"), 7500);
				sendRouterTCPSrc(routerSocketSrc, clientsConnection);
				System.out.println("Envoi request type : " + clientsConnection.getRequestType()+ " vers " + (Reseau.ipToNetwork(clientsConnection.getIpSrc()) + ".254"));
				sendRouterTCPDest(routerSocketDest, clientsConnection);
				System.out.println("Envoi request type : " + clientsConnection.getRequestType() + " vers " + (Reseau.ipToNetwork(clientsConnection.getIpDest()) + ".254"));
				return true;
			}
		} else {
			writer.writeUTF("0");
		}
		return false;
	}

	private boolean addRouter(String ip) throws UnknownHostException {
		String ipNetwork = Reseau.ipToNetwork(ip);
		if(!routersMap.containsKey(ip)){
			routersMap.put(ip, ipNetwork); //on choisi un masque de 24 
			return true;
		} else{
			System.out.println("Error: Router already exists for the given IP.");
			return false;
		}
	}

	//quand on recoit un paquet du proxy, on regarde le prefix reseau de l'ipSrc et le prefix reseau de l'ipDest,
	//et on créé un routeur pour chaque reseau pour pouvoir apres leur envoyer l'ip de l'autre client
	private void sendRouterTCPDest(Socket routerSocket, ClientsConnection clientsConnection) {
		try {
			
			DataOutputStream writer = new DataOutputStream(routerSocket.getOutputStream());
            
			//create the input stream writer to send data by the client
			//DataInputStream inputStream = new DataInputStream(routerSocket.getInputStream());
			
			/*
			 * OutputStream outputStream = routerSocket.getOutputStream();
			 * 
			 * //create the output stream writer to send data to the client PrintWriter
			 * writer = new PrintWriter(routerSocket.getOutputStream(), true);
			 */	
			
			String concatMessage;
			//if the router corresponding to the source client is in the hashmap of routers
			if(clientsConnection.getRequestType()==1) {
				if (routersMap.containsKey(clientsConnection.getIpDest())){
					concatMessage = "reservation:" + clientsConnection.getFlowID() + "," + clientsConnection.getIpSrc() + "," + clientsConnection.getPortSrc();
					System.out.println(concatMessage);
					writer.writeUTF(concatMessage);
				}
			} else if(clientsConnection.getRequestType()==0) {
				if (routersMap.containsKey(clientsConnection.getIpDest())){
					concatMessage = "free:" + clientsConnection.getFlowID() + "," + clientsConnection.getIpSrc() + "," + clientsConnection.getPortSrc();
					System.out.println(concatMessage);
					writer.writeUTF(concatMessage);
				}
			}
			
			routerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendRouterTCPSrc(Socket routerSocket, ClientsConnection clientsConnection) {
		try {
			
			DataOutputStream writer = new DataOutputStream(routerSocket.getOutputStream());
            
			//create the input stream writer to send data by the client
			//DataInputStream inputStream = new DataInputStream(routerSocket.getInputStream());
			
			/*
			 * OutputStream outputStream = routerSocket.getOutputStream();
			 * 
			 * //create the output stream writer to send data to the client PrintWriter
			 * writer = new PrintWriter(routerSocket.getOutputStream(), true);
			 */	
			
			String concatMessage;
			//if the router corresponding to the source client is in the hashmap of routers
			if(clientsConnection.getRequestType()==1) {
				if(routersMap.containsKey(clientsConnection.getIpSrc())){
					concatMessage = "reservation:" + clientsConnection.getFlowID() + "," + clientsConnection.getIpDest() + "," + clientsConnection.getPortDest();
					System.out.println(concatMessage);
					writer.writeUTF(concatMessage);
				}
			} else if(clientsConnection.getRequestType()==0) {
				if(routersMap.containsKey(clientsConnection.getIpSrc())){
					concatMessage = "free:" + clientsConnection.getFlowID() + "," + clientsConnection.getIpDest() + "," + clientsConnection.getPortDest();
					System.out.println(concatMessage);
					writer.writeUTF(concatMessage);
				}
			}
			
			routerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		BandwidthBroker bb = new BandwidthBroker();
		BBThread bbThread = bb.new BBThread();
		bbThread.start();
	}
}
