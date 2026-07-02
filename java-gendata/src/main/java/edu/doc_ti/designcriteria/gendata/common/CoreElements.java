package edu.doc_ti.phivalidation.gendata.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

import net.datafaker.providers.base.AbstractProvider;
import net.datafaker.providers.base.BaseProviders;

public class CoreElements extends AbstractProvider<BaseProviders> {

    public CoreElements(BaseProviders faker)  {
        super(faker);
    }
 	
	public String exponentialDistributedNumber(double lambda) {
    	return exponentialDistributedNumber(lambda,  Long.MAX_VALUE ) ;
    }

    public String exponentialDistributedNumber(double lambda, long maxValue ) {
    	return exponentialDistributedNumber(lambda,  maxValue, 3 ) ;
    }
    
    public String exponentialDistributedNumber(double lambda, long maxValue, int maxNumberOfDecimals ) {
    	
    	final BigDecimal max = BigDecimal.valueOf(maxValue) ;
    	
    	while ( true ) { 
	    	final BigDecimal random = BigDecimal.valueOf( -lambda * Math.log(1-faker.random().nextDouble())  );
	     
	    	if ( random.compareTo(max ) < 0 ) {
		    	return random.setScale(maxNumberOfDecimals, RoundingMode.HALF_DOWN)
		                .toString();
	    	}
    	}
    }  

    public double exponentialDistributedNumberAsDouble(double lambda) {
    	return -lambda * Math.log(1-faker.random().nextDouble())   ;
    }
    
    
    public double exponentialDistributedNumberAsLong(double lambda) {
    	return (long) (-lambda * Math.log(1-faker.random().nextDouble()))  ;
    }
}
