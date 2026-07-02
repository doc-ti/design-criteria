package edu.doc_ti.designcriteria.gendata.bitstreamstats;

import java.util.Hashtable;

import edu.doc_ti.designcriteria.gendata.common.CoreElements;
import edu.doc_ti.designcriteria.gendata.common.DistribData;
import net.datafaker.providers.base.BaseProviders;

public class ElementsBitstreamStats extends CoreElements {
    
    private static int BASE_SEED = 1234 ;
    
    static Hashtable<String, DistribData> distributions = new Hashtable<String, DistribData> () ;
    static {
    	final String[] id3N = {"18", "19"};
        final int[]    id3V = { 1, 1 } ;
    	loadDistrib( "id3", id3N, id3V ) ;
    	
    	DistribData data = new DistribData() ;
    	data.addElementsWithExponential(500, 1000, BASE_SEED, DistribData.MODE_CELL);
    	distributions.put ( "id4" , data) ;
    	
    	
    	final String[] id54N = {"2G", "3G", "4G", "5G"};
        final int[]    id54V = { 1000 , 5000, 50000, 15000} ;
    	loadDistrib( "id54", id54N, id54V ) ;
   }
    

    public ElementsBitstreamStats(BaseProviders faker)  {
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


    public int nextInt(int min, int max) {
    	return faker.random().nextInt(min, max) ;
    }


	public boolean getProbabilityLowerThan(double val) {
		return faker.random().nextDouble() < val ;
	}
    

    
}
