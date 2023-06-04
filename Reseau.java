//package openjsip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.lang.Math;

public class Reseau {
    private String ip;
    private String mask;

    //constructor
    public Reseau(String ip, String mask) {
        this.ip = ip;
        this.mask = mask;
    }

    public String getIP() {
        return this.ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    //returns null if you couldn't change ip to network, returns the network part of the ip if successful
    public static String ipToNetwork(String ip) throws UnknownHostException {
		/*
		 * byte[] ipNetwork = null; if(ip != null){ System.out.println("ip pas null");
		 * //converting ip and mask from string to byte byte[] ipAddressByte =
		 * InetAddress.getByName(ip).getAddress(); System.out.println("IP Address Byte "
		 * + ipAddressByte); byte[] ipMaskByte =
		 * InetAddress.getByName(maskIP).getAddress(); System.out.println("Mask Byte " +
		 * ipMaskByte); //System.out.println("a"); //performing the logical operation &
		 * between the ip address and the mask for(int j=0; j<ipAddressByte.length;
		 * j++){ System.out.println("Dans la boucle"); ipNetwork[j] = (byte)
		 * (ipAddressByte[j] & ipMaskByte[j]); }
		 * 
		 * System.out.println("avant convert"); //converting from byte to inet address
		 * InetAddress netInetAddress = InetAddress.getByAddress(ipNetwork);
		 * System.out.println("failed to convert"); //and from inet address to string
		 * return netInetAddress.getHostAddress(); } else { return null; }
		
		 */
    	String networkConcat="";
    	String ipTab[] = ip.split("\\.");
    	if(ipTab.length>2) {
        	networkConcat = ipTab[0]+"."+ipTab[1]+"."+ipTab[2];
    	}
        return networkConcat;
    }

    // method used to convert numerical mask to subnet mask
	/*
	 * public static String getCIDRToSubnetMask(int cidr) { StringBuilder
	 * subnetMaskBuilder = new StringBuilder();
	 * 
	 * int remainingBits = 32 - cidr; int octet = 0; for (int i = 0; i < 4; i++) {
	 * if (remainingBits >= 8) { octet = 255; remainingBits -= 8; } else { octet =
	 * (int) (256 - Math.pow(2, remainingBits)); remainingBits = 0; }
	 * 
	 * subnetMaskBuilder.append(octet);
	 * 
	 * if (i < 3) { subnetMaskBuilder.append("."); } } return
	 * subnetMaskBuilder.toString(); }
	 */
}
