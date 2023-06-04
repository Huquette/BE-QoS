//package openjsip;

//static Reseau.getCIDRToSubnetMask;

public class ClientsConnection {
    private Integer requestType;
    private String ipSrc;
    private String ipDest;
    private Integer numPortSrc;
    private Integer numPortDest;
    private Integer bandwidth;
    public static Integer flowID = 0;
    private Integer thisFlowID;
    private boolean reservationFree;

    public ClientsConnection(Integer requestType, String ipSrc, String ipDest, Integer numPortSrc, Integer numPortDest, Integer bandwidth) {
        this.requestType = requestType;
        this.ipSrc       = ipSrc;
        this.ipDest      = ipDest;
        this.numPortSrc  = numPortSrc;
        this.numPortDest = numPortDest;
        this.bandwidth   = bandwidth;
        this.reservationFree = false;
        this.thisFlowID = flowID++;
    }

    public Integer getRequestType() {
        return this.requestType;
    }
    
    public void setRequestType(int requestType) {
        this.requestType=requestType;
    }
    public String getIpSrc() {
        return this.ipSrc;
    }

    public String getIpDest() {
        return this.ipDest;
    }

    public Integer getPortSrc() {
        return this.numPortSrc;
    }

    public Integer getPortDest() {
        return this.numPortDest;
    }

    public Integer getBandwidth() {
        return this.bandwidth;
    }
    
    public Integer getFlowID() {
        return this.thisFlowID;
    }

    public boolean isReservation() {
        return this.reservationFree;
    }

    public void setReservation(boolean reservationFree){
        this.reservationFree = reservationFree;
    }
}
