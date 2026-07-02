package edu.doc_ti.designcriteria.common.objects;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.JsonObject;


public class SOBPingData implements ObjStreaming {

	private static final String SEPARATOR = "|";
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SOBPingData.class);

	private static final String fieldsStr[] = {
			"@ts_process",
			"@hostname_process",
			"@timestamp",
			"IPV4" ,
			"IPV6", 
			"SerialNumber",
			"Firmware",
			"Telefono",
			"site",
			"PacketLossV4",
			"PacketLossV6",
			"pingV4",
			"pingV6"
	} ;

	
//	pings: [{"PacketLossV4":"0%",
//	"site":"x.com",
//	"pingV4":"0.145",
//	"pingV6":"-1"},
//{"PacketLossV4":"0%","site":"github.com","pingV4":"1.676","pingV6":"-1"},{"site":"youtube.com","PacketLossV6":"0%","pingV4":"-1","pingV6":"0.404"},{"site":"instagram.com","PacketLossV6":"0%","pingV4":"-1","pingV6":"1.493"},{"PacketLossV4":"1%","site":"www.microsoft.com","pingV4":"0.082","pingV6":"-1"},{"PacketLossV4":"0%","site":"www.google.com","pingV4":"1.252","pingV6":"-1"}]
	
	
	
//	pings: [{"PacketLossV4":"0%","site":"x.com","pingV4":"0.145","pingV6":"-1"},{"PacketLossV4":"0%","site":"github.com","pingV4":"1.676","pingV6":"-1"},{"site":"youtube.com","PacketLossV6":"0%","pingV4":"-1","pingV6":"0.404"},{"site":"instagram.com","PacketLossV6":"0%","pingV4":"-1","pingV6":"1.493"},{"PacketLossV4":"1%","site":"www.microsoft.com","pingV4":"0.082","pingV6":"-1"},{"PacketLossV4":"0%","site":"www.google.com","pingV4":"1.252","pingV6":"-1"}]

	Map<String, Object> sobDataPing = new HashMap<String, Object>();
	JsonObject jsonObject = null ;

	@SuppressWarnings("unused")
	private String rawData;
	
	public SOBPingData() {
	}
	
	public SOBPingData(String rawData) {
		process(rawData) ;
	}

	@Override
	public void process(String rawDataIn) {
		if ( rawDataIn == null ) {
			return ;
		}
		
		String aux[] = rawDataIn.split("\\" + SEPARATOR, -1) ;
		if ( aux.length != fieldsStr.length ) {
			return ;
		}
		
		rawData = rawDataIn ;
		for (int pos = 0 ; pos < fieldsStr.length; pos++ ) {
			sobDataPing.put(fieldsStr[pos], aux[pos]) ;
		}
	}
	
	@Override
	public String toDataString() {
		StringBuffer buf = new StringBuffer() ;
        for (int pos = 0 ; pos < fieldsStr.length; pos++) {
        	buf.append(sobDataPing.get(fieldsStr[pos])) ;
        	if ( pos < fieldsStr.length -1) {
        		buf.append(SEPARATOR) ;
        	}
        }

		return buf.toString() ;
	}
	
	@Override
	public String toJSonString() {
		if ( jsonObject != null ) {
			return jsonObject.toString();
		} 
		
		return "xxxx" ;
	}
	
	@Override
	public String getHeader() {
		StringBuffer buf = new StringBuffer() ;
        for (int pos = 0 ; pos < fieldsStr.length; pos++) {
        	buf.append(fieldsStr[pos]) ;
        	if ( pos < fieldsStr.length -1) {
        		buf.append(SEPARATOR) ;
        	}
        }

        return buf.toString() ;
     }
	
	public void loadCommon(Map<String, Object> sobDataIn, JsonObject pingObj) {
		sobDataPing.putAll(sobDataIn);
		
		for (int pos = 8 ; pos < fieldsStr.length ; pos++) {
			String val = "" ;
			if ( pingObj.get(fieldsStr[pos]) != null ) {
				val = pingObj.getString(fieldsStr[pos]) ;
			}
			sobDataPing.put(fieldsStr[pos], val) ;
			
		}
		
	}

	
	public Map<String, Object> getData() {
		return sobDataPing ;
	}
	
	public static void main(String[] args) {
		
		String aux = "MXULOMJS|1743964635775|2025-04-06T20:37:38|194.235.104.118|5d5b:47ec:1d5e:f0cf:1707:3535:81fa:5b15/41|ARLT132A839|03.01.02.032L|9327787572|x.com|0%||0.063|-1" ;
		
		SOBPingData auxObj = new SOBPingData(aux) ;
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			System.out.println (  mapper.writeValueAsString( auxObj.getData() ) ) ;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
