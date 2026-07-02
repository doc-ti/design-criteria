package edu.doc_ti.designcriteria.gendata.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameFetcher {
    public static void main(String[] args) {
    	System.out.println (getHostname() ) ; 
    }
    
    public static String getHostname() {
    	String hostname = "UNKNOWN" ;
    	String hostnameAux = null ;
        try {
            // Obtener el nombre de la máquina
            hostnameAux = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {}
        
        if ( hostnameAux != null && hostnameAux.length() > 0 ) {
        	hostname = hostnameAux ;
        }
        
        return hostname ;
    }
}
