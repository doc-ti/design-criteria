package edu.doc_ti.designcriteria.common.objects;

import java.util.HashMap;
import java.util.List;

public class CDRFieldManager {

	public static HashMap<Integer, String> fieldNames = new HashMap<Integer, String>() ;
	private static HashMap<String, Integer> fieldPos   = new HashMap<String, Integer>() ;

	public static String F_FILENAME = "filename" ;
	public static String F_LINE_NUMBER = "line_number" ;
	public static String F_DATE_END = "date_end" ;
	
//	public static String F_ = "" ;
	public static String F_TS_PROCESS = "@ts_process" ;
	public static String F_HOST_PROCESS = "@host_process" ;
	public static String F_TS_SRC = "@ts_source" ;
	public static String F_HOST_SRC = "@host_source" ;
	
	public static String F_DURACION = "duration" ;
	public static String F_TECNO = "tecnology" ;
	public static String F_LATENCY = "latency_ms" ;
	
	
	public static String FIELDS_REDUCED[] = {
			F_HOST_SRC,
			F_TS_SRC,
			F_HOST_PROCESS,
			F_TS_PROCESS,
			F_LATENCY,
			"municipality",
			"operator",
			"country",
			"region",
			"population_range",
			"record_type",
			"tecnologia",
			"duration",
			"@timestamp"
	};
	
	static {
		addBase() ;
		addFakeLookup();
		addExtraFields() ;
	}

	private static void addFakeLookup() {

		List<String> headers = LookupFakerCDR.getAllHeaders() ;
		CDRData.NUM_FIELDS_TOTAL = CDRData.NUM_FIELDS_BASE ; 
		for ( String header : headers) {
			fieldNames.put( CDRData.NUM_FIELDS_TOTAL, header) ;
			fieldPos.put( header, CDRData.NUM_FIELDS_TOTAL ) ;
			CDRData.NUM_FIELDS_TOTAL++ ;
		}

	}

	private static void addBase() {
		for (int index = 0 ; index < CDRData.NUM_FIELDS_BASE ; index++) {
			fieldNames.put(index, "field_" + index ) ;
			fieldPos.put("field_" + index, index ) ;
		}
		
		fieldNames.put( 0, F_FILENAME) ;
		fieldPos.put( F_FILENAME, 0 ) ;
		fieldNames.put( 1, F_LINE_NUMBER) ;
		fieldPos.put( F_LINE_NUMBER, 1 ) ;
		fieldNames.put(24, F_DATE_END) ;
		fieldPos.put(F_DATE_END, 24) ;		
		fieldNames.put(27, F_DURACION) ;
		fieldPos.put(F_DURACION, 27) ;		
		fieldNames.put(52, F_TECNO) ;
		fieldPos.put(F_TECNO, 52) ;		
		fieldNames.put(53, F_HOST_SRC) ;
		fieldPos.put(F_HOST_SRC, 53) ;		
		fieldNames.put(54, F_TS_SRC) ;
		fieldPos.put(F_TS_SRC, 54) ;
		
		fieldNames.put(45, "@file_id") ;
		fieldPos.put("@file_id", 45) ;
		
	}

	private static void addExtraFields() {
		String fieldName ;
		
		fieldName = F_HOST_PROCESS;
		fieldNames.put( CDRData.NUM_FIELDS_TOTAL, fieldName) ;
		fieldPos.put( fieldName, CDRData.NUM_FIELDS_TOTAL ) ;
		CDRData.NUM_FIELDS_TOTAL++ ;

		fieldName = F_TS_PROCESS;
		fieldNames.put( CDRData.NUM_FIELDS_TOTAL, fieldName) ;
		fieldPos.put( fieldName, CDRData.NUM_FIELDS_TOTAL ) ;
		CDRData.NUM_FIELDS_TOTAL++ ;

		fieldName = F_LATENCY;
		fieldNames.put( CDRData.NUM_FIELDS_TOTAL, fieldName) ;
		fieldPos.put( fieldName, CDRData.NUM_FIELDS_TOTAL ) ;
		CDRData.NUM_FIELDS_TOTAL++ ;

	}
	
}
