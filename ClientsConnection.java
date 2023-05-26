package openjsip;

import static openjsip.Reseau.getCIDRToSubnetMask;

public class ClientsConnection {
    private final Integer requestType;
    private String ipSrc;
    private String ipDest;
    private Integer numPortSrc;
    private Integer numPortDest;
    private Integer bandwidth;
    private static Integer flowId = 0;
    private boolean reservationFree;

    public ClientsConnection(Integer requestType, String ipSrc, String ipDest, Integer numPortSrc, Integer numPortDest, Integer bandwidth) {
        this.requestType = requestType;
        this.ipSrc       = ipSrc;
        this.ipDest      = ipDest;
        this.numPortSrc  = numPortSrc;
        this.numPortDest = numPortDest;
        this.bandwidth   = bandwidth;
        this.reservationFree = false;
        flowId++;
    }

    public Integer getRequestType() {
        return this.requestType;
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

    public boolean isReservation() {
        return this.reservationFree;
    }

    public void setReservation(boolean reservationFree){
        this.reservationFree = reservationFree;
    }
}
