package edu.doc_ti.phivalidation.common.objects;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;


public class CDRData implements ObjStreaming {

	public static final int NUM_FIELDS_BASE = 55 ;
	public static int NUM_FIELDS_TOTAL = NUM_FIELDS_BASE ;
	private static String HOSTNAME = "???";

	
	
	static {
		HOSTNAME = HostnameFetcher.getHostname() ;
	}
	
	private static final String SEPARATOR = "|";
	
	Map<String, Object> cdrData = new HashMap<String, Object>();
	
	private static Logger logger = LoggerFactory.getLogger(CDRData.class);
	
	public String[] splitData(String data){
		String[] dataObjects = null;
		logger.debug("Splitting tuple data: {}", data);
		if (!Strings.isNullOrEmpty(data)){
			dataObjects = data.split(Constants.DELIMITER,-1);
		}
		return dataObjects;
	}
	public Map<String, Object> getCdrData() {
		return cdrData;
	}
	
	
	public CDRData(String rawData) {
		process(rawData) ;
	}
	
	
	String fields2Double[] = {"duracion", "field_48", "field_49", "field_50"};

	@Override
	public void process(String rawData) {
    	String[] dataArray = splitData(rawData.toString());
    	
//    	System.out.println(  "FELIPE-TAM" + dataArray.length );

		getCdrData().put(Constants.PROCESSINGDATE, System.currentTimeMillis() ) ;
		for ( int index = 0 ; index < dataArray.length; index++) {
			String val =  dataArray[index] ;
			if ( val != null && val.length() > 0 ) {
				if ( CDRFieldManager.fieldNames.get(index) != null ) {
					getCdrData().put(CDRFieldManager.fieldNames.get(index), val) ;
				}
			}
		}

		if ( dataArray.length >= 25 && dataArray[24] != null ) {
			getCdrData().put(Constants.TIMESTAMP, dataArray[24]);
		}
		
		Hashtable<String, String> fakes = LookupFakerCDR.generateHash() ;
		for (int nn = NUM_FIELDS_BASE ; nn < NUM_FIELDS_TOTAL; nn++) {
			String name = CDRFieldManager.fieldNames.get(nn) ;
			if ( name != null ) {
				String val = fakes.get(name) ;
				if ( val == null ) val = "" ;
				getCdrData().put(name, val);
			}
		}
		
		for (String field : fields2Double ) {
			Object  aux = getCdrData().get(field) ;
			
			double val = -1 ;
			if ( aux != null ) {
				try {val = Double.parseDouble(aux.toString()) ; } catch (Exception e) {}
			}
			getCdrData().put(field, val) ;
		}
		
		getCdrData().put(CDRFieldManager.F_TS_PROCESS, System.currentTimeMillis());
		getCdrData().put(CDRFieldManager.F_HOST_PROCESS, HOSTNAME);

		String hostSRC = (String) getCdrData().get(CDRFieldManager.F_HOST_SRC) ;
		String tsSRC = (String) getCdrData().get(CDRFieldManager.F_TS_SRC) ;
		long latency = -1 ;
		try {
			if ( hostSRC != null  && tsSRC != null ) {
				if (hostSRC.compareTo(HOSTNAME) == 0 ) {
					latency = (System.currentTimeMillis() - Long.parseLong(tsSRC))  ;
				}
			}
		} catch (Exception ex) {}
		
		getCdrData().put(CDRFieldManager.F_LATENCY, latency);
		
		
	}
	
	@Override
	public String toDataString() {

		StringBuffer buff = new StringBuffer() ;
		for (int pos = 0 ; pos < NUM_FIELDS_TOTAL ; pos++) {
			if (pos > 0 ) {
				buff.append(SEPARATOR) ;
			}
			buff.append(getCdrData().get(CDRFieldManager.fieldNames.get(pos))) ;
		}
		
		return buff.toString() ;
	}
	@Override
	public String toJSonString() {
		return "";
	}
	@Override
	public String getHeader() {
		StringBuffer buff = new StringBuffer() ;
		for (int pos = 0 ; pos < NUM_FIELDS_TOTAL ; pos++) {
			if (pos > 0 ) {
				buff.append(SEPARATOR) ;
			}
			buff.append(CDRFieldManager.fieldNames.get(pos)) ;
		}
		
		return buff.toString() ;
	}
	
	
	public String getReducedCDR() {
		
		StringBuffer buff = new StringBuffer() ;
		for (int pos = 0 ; pos < CDRFieldManager.FIELDS_REDUCED.length ; pos++) {
			if (pos > 0 ) {
				buff.append(SEPARATOR) ;
			}
			buff.append(getCdrData().get(CDRFieldManager.FIELDS_REDUCED[pos])) ;
		}
		
		return buff.toString() ;
	}	
	
	public static void main(String[] args) {
		String str = "18;19;3100048790875661;11.48.240.193;585;97.205.248.124;hazel_nutt;12345;81.211.157.23;945;86;177;166;4;787;1167;9;2;78;399;4;8;3;2025-04-03T05:24:05;2025-04-03T13:40:48;12;2;187.984;vsku97qt08;1670478680;5;0;79;3100046491393285;7;2;0;2;9;01;0;1;5;2;AC;BK;CIY;DU15;2.95;0.34;56.91;2025-04-03T07:38:54;5G;FZFKUKMQJS;1743702277008" ;

		CDRData cdr = new CDRData(str) ;
		
		System.out.println ( cdr.getHeader() ) ;
		System.out.println ( cdr.toDataString()) ;
		System.out.println ( cdr.toJSonString()) ;
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			System.out.println (  mapper.writeValueAsString( ((CDRData) cdr).getCdrData() ) ) ;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println ( cdr.getReducedCDR()) ;
		
		
	}

}
