package openjsip;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static openjsip.Reseau.getCIDRToSubnetMask;
import static openjsip.Reseau.ipToNetwork;

public class BandwidthBroker implements Runnable {
	//hashmap representing the SLA contracts made with each of the remote networks. The SLA hashmap records the remote network, and the bandwidth allocated for this network
	static HashMap <Integer, Integer> sla = new HashMap<Integer, Integer>(); 
	//hashmap representing the used resources, which records each ip and the bandwidth allocated to this ip. A REVOIR CA PEUT ETRE PAS BESOIN
	static HashMap <Integer, Integer> usedResources = new HashMap<Integer, Integer>();

	static HashMap <String, String> routersMap = new HashMap<String, String>();

	public static final int TOTAL_BANDWIDTH = 1000; // Kbps
	public static final int PORT_PROXY = 5098; 

	// bandwidth total utilisé sur le réseau pour l'instant
	private static Integer sommeBandwidthTotal(){ 
		Integer somme = 0;
		for(int flowID : usedResources.keySet()){
			somme += usedResources.get(flowID);
		}
		return somme;
	}
	
	//method to add an SLA in the already existing SLA hashmap
	private static void addSLA(Integer flowID, Integer resource) throws UnknownHostException {
		sla.put(flowID, resource);
	}

	private static boolean checkSLA(Integer flowID, Integer bandwidth) {
		Integer bandwidthAvailable = usedResources.get(flowID);

		if (bandwidthAvailable + bandwidth < sla.get(flowID)) {
			System.out.println("The SLA contract is respected");
			return true;
		} else {
			System.out.println("The SLA contract is not respected");
			return false;
		}
	}

	private boolean allocateBandwidth(Integer flowID, Integer bandwidthToAllocate) {
		try {
				//the bandwidth available is the bandwidth allocated for each flowID that is trying to use in the network
				Integer userBandwidth = usedResources.get(flowID);

				// true if the flowID is not in the usedResources hashmap
				boolean firstTimeRequesting = !usedResources.containsKey(flowID);
				boolean SLAContracted = sla.containsKey(flowID);

				//if the SLA is contracted
				if (SLAContracted) {
					//if the client with the corresponding flowID respects the contract, we update the bandwidth to allocate in usedResources
					if (checkSLA(flowID, bandwidthToAllocate)) {
						if (bandwidthToAllocate < (TOTAL_BANDWIDTH - sommeBandwidthTotal())) {
							if (firstTimeRequesting) {
								usedResources.put(flowID, bandwidthToAllocate);
							}
							//if it is not the first time requesting
							else {
								usedResources.replace(flowID, userBandwidth + bandwidthToAllocate);
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
	private boolean liberateBandwidth(Integer flowID, Integer bandwidthToLiberate) {
		try {
			
			Integer bandwidthUser;
			boolean firstTimeRequesting = !usedResources.containsKey(flowID);
			boolean SLAContracted = sla.containsKey(flowID);
			Integer userBandwidth = usedResources.get(flowID);
			// The method checks if the user with the IP address "ip" has not made any previous requests by using the variable firstTimeRequesting.
			// If that is the case, it also verifies if the corresponding SLA contract for the network IP address "ip" exists.
			if(SLAContracted && firstTimeRequesting) {
				System.out.println("Error : you tried to liberate bandwidth that was never allocated in the first place");
				return false;
			} else if(SLAContracted) { // if the SLA is contracted, and it is not the first time requesting 
				usedResources.replace(flowID, userBandwidth - bandwidthToLiberate);
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
	
	//method that is called when a call initiated from a remote network is accepted. The Bandwidth Broker thus needs to allocate the bandwidth that is available
	//depending on the SLA made, or not, with the incoming network. The flowID of the incoming call is specified with its mask, and the bandwidth to allocate as well.
	public void acceptCall(Integer flowID, Integer bw) throws UnknownHostException {
		//if the packet received has a requestType = 'ACCEPT' (i.e. he accepts the call)
		if(clientsConnection.getRequestType() == 1) {
			System.out.println("Call incoming from the client n° : "+ flowID);
			if(sla.containsKey(flowID)) {
				
				if(this.allocateBandwidth(flowID, bw)) {
					System.out.println("Bandwidth can be allocated to the following client : " + flowID);
				} else {
					System.out.println("Couldn't allocate bandwidth to the client n° : " + flowID + " check if the SLA is respected.");
				}
			} else {
				
				if(this.allocateBandwidth(flowID, bw)){
					addSLA(flowID, bw);
					System.out.println("Bandwidth can be allocated to the following client : " + flowID);
				} else{
					System.out.println("Couldn't allocate bandwidth to the client n° : " + flowID + " check if the SLA is respected.");
				}
			}	
		} else{
			System.out.println("Error: Request type is not 'ACCEPT'.");
		}
	}

	//when a machine belonging to another network wants to hang up with a machine in the network of the bandwidth broker, this method is called by the BB
	public void hangUpCall(Integer flowID, Integer bw) throws UnknownHostException {
		
		System.out.println("Call from the client n° : " + flowID + " has ended");
		

		//if the packet received has a requestType = 'BYE' (i.e. he hangs up the call)
		if(clientsConnection.getRequestType() == 0){
			if(sla.containsKey(flowID)){
				if(this.liberateBandwidth(flowID, bw)){
					System.out.println("Bandwidth liberated from the following client : " + flowID);
					
				} else{
					System.out.println("Couldn't liberate bandwidth to the client : " + flowID + ".");
				}
			} else{
				System.out.println("Error: IP is not specified in the SLA.");
			}
		} else{
			System.out.println("Error: Request type is not 'BYE'.");
		}
			
	}

	@Override
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
	

	private void handleProxy(Socket proxySocket) {
		try {
			//create the input stream reader to read data sent by the client
			BufferedReader reader = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()));

			//create the output stream writer to send data to the client
			PrintWriter writer = new PrintWriter(proxySocket.getOutputStream(), true);

			//read the incoming request from the client
			//the client needs to send a request with this format: ip/mask/bandwidth
			String request = reader.readLine();
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

	private boolean processRequest(String request, PrintWriter writer) throws UnknownHostException {
		//array that allows to split the request into three variables : ip, mask and bandwidth
		String[] requestArray = request.split(";");
		Integer requestType   = Integer.parseInt(requestTab[0]);
		String  ipSrc         = requestTab[1];
		Integer portSrc       = Integer.parseInt(requestTab[2]);
		String  ipDest        = requestTab[3];
		Integer portDest      = Integer.parseInt(requestTab[4]);
		Integer bandwidth     = Integer.parseInt(requestTab[5]);

		ClientsConnection clientsConnection = new ClientsConnection(requestType, ipSrc, portSrc, ipDest, portDest, bandwidth);

		// bandwidth utilisée inférieure à bandwidth restante dans le réseau 
		if (clientsConnection.getBandwidth() < TOTAL_BANDWIDTH - sommeBandwidthTotal()) { // bandwidth not respected
			clientsConnection.setReservation(false);
			writer.println(false);
			System.out.println("Sent response to client: " + false);
			return false;
		} else if (!checkSLA(flowID, bandwidth)) { // SLA not respected
			clientsConnection.setReservation(false);
			writer.println(false);
			System.out.println("Sent response to client: " + false);
			return false;
		} else {
			clientsConnection.setReservation(true);
			//accepting the call since everything is fine
			if(clientsConnection.getRequestType() == 1) {
				acceptCall(flowID, bandwidth);
				writer.println(true);
			} else if (clientsConnection.getRequestType() == 0) {
				hangUpCall(flowID, bandwidth);
				writer.println(true);
			} else {
				writer.println(false);
			}
			// to implement: possibilité de savoir quand ils ont raccroché pour rendre les ressources

			//add routeurs corresponding to the network of the packet we just received
			addRouter(clientsConnection.getIPSrc());	
			addRouter(clientsConnection.getIPDest());

			//create sockets to send the packet to the routers of each client
			Socket routerSocketSrc = new Socket(clientsConnection.getIPSrc(), clientsConnection.getPortSrc());
			Socket routerSocketDest = new Socket(clientsConnection.getIPDest(), clientsConnection.getPortDest());
			
			//we send to both routers, one router for the source client network and the other for the destination client network
			sendRouterTCP(routerSocketSrc, clientsConnection);
			sendRouterTCP(routerSocketDest, clientsConnection);
			return true;
		}
	}

	private boolean addRouter(String ip) {
		String ipNetwork = ipToNetwork(ip, 24);
		if(!routersMap.containsKey(ip)){
			routersMap.put(ip, ipNetwork); //on choisi un masque de 24 a voir s'il faut un different on peut le changer ici
		} else{
			System.out.println("Error: Router already exists for the given IP.");
		}
	}

	//quand on recoit un paquet du proxy, on regarde le prefix reseau de l'ipSrc et le prefix reseau de l'ipDest,
	//et on créé un routeur pour chaque reseau pour pouvoir apres leur envoyer l'ip de l'autre client
	private void sendRouterTCP(Socket routerSocket, clientsConnection clientsConnection) {
		try {
			OutputStream outputStream = routerSocket.getOutputStream();
			//create the output stream writer to send data to the client
			PrintWriter writer = new PrintWriter(routerSocket.getOutputStream(), true);
			String concatMessage;
			//if the router corresponding to the source client is in the hashmap of routers
			if(routersMap.containsKey(clientsConnection.getIPSrc())){
				concatMessage = flowID + ";" + clientsConnection.getIPDest() + ";" + clientsConnection.getPortDest() + ";" + clientsConnection.isReservation();
				writer.println(concatMessage);
			} else if (routersMap.containsKey(clientsConnection.getIPDest()){
				concatMessage = flowID + ";" + clientsConnection.getIPSrc() + ";" + clientsConnection.getPortSrc() + ";" + clientsConnection.isReservation();
				writer.println(concatMessage);
			}

			routerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		BBThread bbThread = new BBThread();
		bbThread.start();
	}
}