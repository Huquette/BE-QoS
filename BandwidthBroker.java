package openjsip;

import java.util.HashMap;

public class BandwidthBroker implements Runnable {
	
	static HashMap <String, Integer> sla = new HashMap<String, Integer>();
	static HashMap <String, Integer> usedResources = new HashMap<String, Integer>();
	static HashMap <String, Integer> calls = new HashMap<String, Integer>();
	
	//Function to check if the bandwidth is available for each request based on the ip address
	private static boolean checkBandwidth(String ip, Integer bandwidthToUse) {
		Boolean free = false;
		Integer bandwidthAvailable;
		if(usedResources.get(ip) != null) {
			bandwidthAvailable = usedResources.get(ip);
			if(bandwidthAvailable - bandwidthToUse > 0) {
				free = true;
			}
		}
		return free;
	}
	
	//Function to add an SLA in the already existing SLA hashmap
	private static void addSLA(String ip, Integer resource) {
		sla.put(ip, resource);
	}
	
	private static boolean addBandwidth(String ip, Integer bandwidthToAdd) {
		
		try {
			Integer bandwidthAvailable;
			boolean firstTimeRequesting = !usedResources.containsKey(ip);
			boolean SLAContracted = sla.containsKey(ip);
			if(SLAContracted && firstTimeRequesting) {
				usedResources.put(ip, bandwidthToAdd);
				return true;
			}
			else if(SLAContracted && !firstTimeRequesting) {
				
				bandwidthAvailable = usedResources.get(ip);
				//if the user with the corresponding ip respects the contract, we update the bandwidth to add in usedResources
				if(bandwidthAvailable + bandwidthToAdd < sla.get(ip)) {
					usedResources.replace(ip, bandwidthAvailable + bandwidthToAdd);
				}
				return true;
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static void reduceBandwidth(String ip, Integer bandwidthToUse) {
		
		try {
			Integer bandwidthAvailable;
			boolean firstTimeRequesting = !usedResources.containsKey(ip);
			boolean SLAContracted = sla.containsKey(ip);
			
			if(checkBandwidth(ip, bandwidthToUse ) == true) {
				
				if(SLAContracted && firstTimeRequesting) {
					System.out.println("You tried to reduce bandwidth of a ressource that is not in the usedResources database.");
				}
				else if(SLAContracted && !firstTimeRequesting) {
					bandwidthAvailable = usedResources.get(ip);
					usedResources.replace(ip, bandwidthAvailable - bandwidthToUse);
				}	
				
			}
			else {
				System.out.println("Not enough bandwidth for resource");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void acceptCall(String ip, Integer bw) {
		
		//reduceBandwidth(ip, bw.getBandwidth());
		System.out.println("Call incoming from : "+ip);
		
		//if it is not in the incoming calls and the incoming call corresponds to the sla of the network specified
		if(!calls.containsKey(ip)) {
			if(sla.containsKey(ip)) {
				if(addBandwidth(ip, bw)) {
					calls.put(ip, bw);
				}
			}
			
		}
		//if it is already in the calls hashmap
		else {
			
		}
	}
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
