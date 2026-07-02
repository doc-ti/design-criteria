package edu.doc_ti.designcriteria.gendata.cdrs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.apache.kafka.common.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.doc_ti.designcriteria.gendata.common.CoreElements;
import edu.doc_ti.designcriteria.gendata.common.DistribData;
import edu.doc_ti.designcriteria.gendata.common.HostnameFetcher;
import net.datafaker.providers.base.BaseProviders;

public class MyElements extends CoreElements {
    private static final Logger LOG = LoggerFactory.getLogger(MyElements.class);

    private static final String hostname = HostnameFetcher.getHostname();
	private static String[] d1Names = {"N1", "N2", "N3"} ;
    private static int[] d1Vals = { 1, 1, 1 } ;
    
    private static int BASE_SEED = 1234 ;

    SimpleDateFormat sdfID = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    
    static Hashtable<String, DistribData> distributions = new Hashtable<String, DistribData> () ;
    static {
    	loadDistrib( "id1", d1Names, d1Vals ) ;

    	final String[] id3N = {"18", "19"};
        final int[]    id3V = { 1, 1 } ;
    	loadDistrib( "id3", id3N, id3V ) ;
    	
    	DistribData data = new DistribData() ;
    	data.addElementsWithExponential(500, 1000, BASE_SEED, DistribData.MODE_CELL);
    	distributions.put ( "id4" , data) ;
    	
    	data= new DistribData() ;
    	data.addElementsWithExponential(6, 1000000, BASE_SEED, DistribData.MODE_IPV4);
    	distributions.put ( "id5" , data) ;

    	data = new DistribData() ;
    	data.addElementsWithExponential(2, 1000, BASE_SEED, DistribData.MODE_IPV4);
    	distributions.put ( "id7" , data) ;

    	data = new DistribData() ;
    	data.addElementsWithExponential(8, 1000, BASE_SEED, DistribData.MODE_RANDOM_NAME);
    	distributions.put ( "id8" , data) ;

    	final String[] id9N = {"12345", "12346"};
        final int[]    id9V = { 4035032, 34390 } ;
    	loadDistrib( "id9", id9N, id9V ) ;
    	
    	data= new DistribData() ;
    	data.addElementsWithExponential(1000, 10000, BASE_SEED, DistribData.MODE_IPV4);
    	distributions.put ( "id10" , data) ;    
    	
    	data = new DistribData() ;
    	data.addElementsWithExponential(400, 10000, BASE_SEED, DistribData.MODE_RANDOM_GROUP);
    	distributions.put ( "id30" , data) ;

    	final String[] id41N = {"01", "02", "03", "04", "05", "06"};
        final int[]    id41V = { 3169134 , 659245 , 167812, 27169, 7170, 4317} ;
    	loadDistrib( "id41", id41N, id41V ) ;
    	
    	
    	final String[] id54N = {"2G", "3G", "4G", "5G"};
        final int[]    id54V = { 1000 , 5000, 50000, 15000} ;
    	loadDistrib( "id54", id54N, id54V ) ;
   }
    

    public MyElements(BaseProviders faker)  {
        super(faker);
    }
    

	private static void loadDistrib(String name, String[] names, int[] vals) {
    	DistribData data = new DistribData() ;
    	data.add(names, vals) ;
    	distributions.put ( name , data) ;
	}

    public String nextDeterminedDistribElement( String name) {
    	DistribData d = distributions.get(name) ;
    	if ( d == null ) {
    		return "N/A";
    	}
    	
        return d.searchNext(faker) ;
    } 

    public String currentTS() {
    	return Long.toString(System.currentTimeMillis()) ;
    }

    public String hostname() {
    	return hostname ;
    }


    Hashtable<String, SimpleDateFormat> formatters = new Hashtable<String, SimpleDateFormat>() ; 
    
	public String sysdate(String format) {

		SimpleDateFormat auxDateFormatter = formatters.get(format) ;
		if ( auxDateFormatter == null ) {
			auxDateFormatter = new SimpleDateFormat(format) ;
			formatters.put(format, auxDateFormatter) ;
		}
		
		return auxDateFormatter.format(new Date() );
	}
    
	
	public static void main(String[] args) {
		
		
		SimpleDateFormat auxDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") ;
		System.out.println ( auxDateFormatter.format(new Date())) ;
		
	}


	String FileID = null ;
	int contador = 0 ;
	static int countID = 0 ;
	
	public String getFileID() {
		contador++ ;
		if ( FileID == null || contador >= 100000) {
			contador = 0 ;
//			FileID = Uuid.randomUuid().toString() ;
			countID++ ;
			FileID = sdfID.format(new Date()) + "_" + String.format("%06d", countID) ;
			LOG.info("NEW_FILEID: " + FileID);
		}
		return FileID;
	}
}
