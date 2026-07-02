package edu.doc_ti.phivalidation.common.objects;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;


public class SOBData implements ObjStreaming {

	private static String HOSTNAME = "???";

	static {
		HOSTNAME = HostnameFetcher.getHostname() ;
	}
	
	private static final String SEPARATOR = "|";
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SOBData.class);

	private static final String fieldsStr[] = {
			"@ts_process",
			"@hostname_process",
			"@timestamp",
			"IPV4" ,
			"IPV6", 
			"SerialNumber",
			"Firmware",
			"Telefono",
	} ;

	private static final String fieldsInt[] = {
			"uptime", 
			"PaquetesTx", 
			"PaquetesRx",
			"ErroresTx",
			"ErroresRx"
	} ;
	
//	pings: [{"PacketLossV4":"0%","site":"x.com","pingV4":"0.145","pingV6":"-1"},{"PacketLossV4":"0%","site":"github.com","pingV4":"1.676","pingV6":"-1"},{"site":"youtube.com","PacketLossV6":"0%","pingV4":"-1","pingV6":"0.404"},{"site":"instagram.com","PacketLossV6":"0%","pingV4":"-1","pingV6":"1.493"},{"PacketLossV4":"1%","site":"www.microsoft.com","pingV4":"0.082","pingV6":"-1"},{"PacketLossV4":"0%","site":"www.google.com","pingV4":"1.252","pingV6":"-1"}]

	Map<String, Object> sobData = new HashMap<String, Object>();
	JsonObject jsonObject = null ;

	public ArrayList<SOBPingData> arrPings = new ArrayList<SOBPingData>();
	
	public SOBData(String rawData) {
		process(rawData) ;
	}

	@Override
	public void process(String rawData) {

		JsonReader reader = Json.createReader(new StringReader(rawData));
		jsonObject = reader.readObject();
        reader.close();

        sobData.put(fieldsStr[0], HOSTNAME) ;
        sobData.put(fieldsStr[1], System.currentTimeMillis()) ;
        
        for (int pos = 2 ; pos < fieldsStr.length; pos++) {
        	if ( jsonObject.get(fieldsStr[pos]) != null ) {
        		sobData.put(fieldsStr[pos], jsonObject.getString(fieldsStr[pos])) ;
        	}
        }

        for (int pos = 0 ; pos < fieldsInt.length; pos++) {
        	sobData.put(fieldsInt[pos], jsonObject.getInt(fieldsInt[pos])) ;
        }

        
        
        JsonArray pingsArray = jsonObject.getJsonArray("pings");

        for (JsonValue pingVal : pingsArray) {
            JsonObject pingObj = pingVal.asJsonObject();
            SOBPingData pingArrData= new SOBPingData() ;
            pingArrData.loadCommon( sobData , pingObj ) ;
            arrPings.add(pingArrData) ;
        }
        
	}
	
	@Override
	public String toDataString() {
		StringBuffer buf = new StringBuffer() ;
        for (int pos = 0 ; pos < fieldsStr.length; pos++) {
        	buf.append(sobData.get(fieldsStr[pos])) ;
        	buf.append(SEPARATOR) ;
        }

        for (int pos = 0 ; pos < fieldsInt.length; pos++) {
        	buf.append(sobData.get(fieldsInt[pos])) ;
        	if ( pos < fieldsInt.length -1) {
        		buf.append(SEPARATOR) ;
        	}
        }
		
		return buf.toString() ;
	}
	
	@Override
	public String toJSonString() {
		return jsonObject.toString();
	}
	
	@Override
	public String getHeader() {
		StringBuffer buf = new StringBuffer() ;
        for (int pos = 0 ; pos < fieldsStr.length; pos++) {
        	buf.append(fieldsStr[pos]) ;
        	buf.append(SEPARATOR) ;
        }

        for (int pos = 0 ; pos < fieldsInt.length; pos++) {
        	buf.append(fieldsInt[pos]) ;
        	if ( pos < fieldsInt.length -1) {
        		buf.append(SEPARATOR) ;
        	}
        }
        
        return buf.toString() ;
     }
	
	public static void main(String[] args) {
		String str = "{\"ErroresRx\":0,\"ErroresTx\":0,\"IPV6\":\"7341:3f70:9ee6:f85d:7240:bf8f:588d:6609\\/111\",\"IPV4\":\"208.218.196.250\",\"MAC\":\"AC:6E:B4:B0:00:A0\",\"uptime\":67,\"@timestamp\":\"2025-04-06T11:33:52\",\"PaquetesRx\":591657,\"SerialNumber\":\"ARLT652B599\",\"Telefono\":\"9316798818\",\"pings\":[{\"PacketLossV4\":\"0%\",\"site\":\"x.com\",\"pingV4\":\"0.145\",\"pingV6\":\"-1\"},{\"PacketLossV4\":\"0%\",\"site\":\"github.com\",\"pingV4\":\"1.676\",\"pingV6\":\"-1\"},{\"site\":\"youtube.com\",\"PacketLossV6\":\"0%\",\"pingV4\":\"-1\",\"pingV6\":\"0.404\"},{\"site\":\"instagram.com\",\"PacketLossV6\":\"0%\",\"pingV4\":\"-1\",\"pingV6\":\"1.493\"},{\"PacketLossV4\":\"1%\",\"site\":\"www.microsoft.com\",\"pingV4\":\"0.082\",\"pingV6\":\"-1\"},{\"PacketLossV4\":\"0%\",\"site\":\"www.google.com\",\"pingV4\":\"1.252\",\"pingV6\":\"-1\"}],\"Firmware\":\"03.02.03.031L\",\"PaquetesTx\":99988}\r\n" ;

		SOBData sobObj = new SOBData(str) ;
		
		System.out.println ( sobObj.getHeader() ) ;
		System.out.println ( sobObj.toDataString()) ;
		System.out.println ( sobObj.toJSonString()) ;
		
		System.out.println() ;
		
		for ( SOBPingData ping : sobObj.arrPings) {
			System.out.println (ping.getHeader()) ;
			System.out.println (ping.toDataString()) ;
		}
		
	}
}
