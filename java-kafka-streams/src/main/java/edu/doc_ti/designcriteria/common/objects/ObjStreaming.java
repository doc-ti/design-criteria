package edu.doc_ti.designcriteria.common.objects;

public interface ObjStreaming {
	
	public void process(String in) ;
	public String toDataString() ;
	public String toJSonString() ;
	public String getHeader() ;

}
