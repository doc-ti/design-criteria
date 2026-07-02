package edu.doc_ti.designcriteria.kafkastreams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyObject {

	public String value ;
	public String nodo;
	public String indic;
	public String grupo;
	public String periodo;
	@JsonProperty("@timestamp")
	public String timestamp;
	public Integer pos ;
	public Integer partition;
	public Long offset; 
	@JsonProperty("indic_main")
	public String indicMain ;
	
	
	public MyObject(String val, String nodo, String indic, String grupo,
			String periodo, String timestamp, int numx, int partition, long offset, String indicMain) {
		
//		MyObject obj = String.format("{ \"value\": %s, \"nodo\": \"%s\", \"indic\": \"%s\", \"grupo\": \"%s\", \"periodo\": \"%s\", \"@timestamp\" : \"%s\", \"pos\" : %d , \"partition\" : %d, \"offset\" : %d}", 

		this.value = val;
		this.nodo = nodo;
		this.indic = indic;
		this.grupo = grupo;
		this.periodo = periodo ;
		this.timestamp = timestamp ;
		this.pos = numx;
		this.partition = partition;
		this.offset = offset ;
		this.indicMain = indicMain  ;
		
	}

}
