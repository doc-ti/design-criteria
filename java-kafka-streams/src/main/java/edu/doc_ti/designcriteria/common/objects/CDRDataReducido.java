package edu.doc_ti.designcriteria.common.objects;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;


public class CDRDataReducido implements ObjStreaming {

	private static String HOSTNAME = "???";
	static {
		HOSTNAME = HostnameFetcher.getHostname() ;
	}
	
	private static final String SEPARATOR = "|";
	
	static final String[] fieldsExtra = { 
			CDRFieldManager.F_HOST_PROCESS + "_step2", 
			CDRFieldManager.F_TS_PROCESS + "_step2" ,  
			CDRFieldManager.F_LATENCY + "_step2"
	}; 

	Map<String, Object> cdrData = new HashMap<String, Object>();
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(CDRDataReducido.class);
	
	public String[] splitData(String data){
		String[] dataObjects = null;
		if (!Strings.isNullOrEmpty(data)){
			dataObjects = data.split("\\|",-1);
		}
		return dataObjects;
	}
	public Map<String, Object> getCdrData() {
		return cdrData;
	}
	
	
	public CDRDataReducido(String rawData) {
		process(rawData) ;
	}
	
	String fields2Double[] = {"duracion", "latency_ms"};
	

	@Override
	public void process(String rawData) {
    	String[] dataArray = splitData(rawData.toString());
    	
		getCdrData().put(Constants.PROCESSINGDATE, System.currentTimeMillis() ) ;
		for ( int index = 0 ; index < dataArray.length; index++) {
			String val =  dataArray[index] ;
			if ( val != null && val.length() > 0 ) {
				getCdrData().put(
						CDRFieldManager.FIELDS_REDUCED[index], 
						val) ;
			}
		}
		
		for (String field : fields2Double ) {
			Object  aux = getCdrData().get(field) ;
			double val = -1 ;
			if ( aux != null ) {
				try {val = Double.parseDouble(aux.toString()) ; } catch (Exception e) {}
				getCdrData().put(field, val) ;
			}
		}
		

		getCdrData().put(fieldsExtra[0], HOSTNAME);
		getCdrData().put(fieldsExtra[1], System.currentTimeMillis());

		String hostSRC = (String) getCdrData().get(CDRFieldManager.F_HOST_PROCESS) ;
		String tsStepPrev = (String) getCdrData().get(CDRFieldManager.F_TS_PROCESS) ;
		String tsSRC = (String) getCdrData().get(CDRFieldManager.F_TS_SRC) ;
		
		long latency = -1 ;
		long latency_total = -1 ;
		try {
			if ( hostSRC != null  && tsStepPrev != null ) {
				if (hostSRC.compareTo(HOSTNAME) == 0 ) {
					latency = (System.currentTimeMillis() - Long.parseLong(tsStepPrev))  ;
					latency_total = (System.currentTimeMillis() - Long.parseLong(tsSRC))  ;
				}
			}
		} catch (Exception ex) {}
		
		getCdrData().put(fieldsExtra[2], latency);
		getCdrData().put("latency_ms_total", latency_total);

	}
	
	
	@Override
	public String toDataString() {

		StringBuffer buff = new StringBuffer() ;
		for (int pos = 0 ; pos < CDRFieldManager.FIELDS_REDUCED.length ; pos++) {
			if (pos > 0 ) {
				buff.append(SEPARATOR) ;
			}
			buff.append(getCdrData().get(CDRFieldManager.FIELDS_REDUCED[pos])) ;
		}
		
		for (int pos = 0 ; pos < fieldsExtra.length ; pos++) {
				buff.append(SEPARATOR) ;
			buff.append(getCdrData().get(fieldsExtra[pos])) ;
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
		for (int pos = 0 ; pos < CDRFieldManager.FIELDS_REDUCED.length ; pos++) {
			if (pos > 0 ) {
				buff.append(SEPARATOR) ;
			}
			buff.append(CDRFieldManager.FIELDS_REDUCED[pos]) ;
		}
		
		for (int pos = 0 ; pos < fieldsExtra.length ; pos++) {
			buff.append(SEPARATOR) ;
			buff.append(fieldsExtra[pos]) ;
		}		
		return buff.toString() ;
	}
	

	public static void main(String[] args) {
		String str = "FZFKUKMQJS|1743702277008|FZFKUKMQJS|1743941471761|239194753|NAVEZUELAS|Emtel|Isla_Mauricio|CACERES|<1K|sgsnSMTRecord|5G|187.984" ;

		CDRDataReducido cdr = new CDRDataReducido(str) ;
		
		System.out.println ( cdr.getHeader() ) ;
		System.out.println ( cdr.toDataString()) ;
		System.out.println ( cdr.toJSonString()) ;
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			System.out.println (  mapper.writeValueAsString(  cdr.getCdrData() ) ) ;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
	}

}
