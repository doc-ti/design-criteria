package edu.doc_ti.phivalidation.gendata.bitstreamstats;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitstreamKafkaGeneratorThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(BitstreamKafkaGeneratorThread.class);

    public int idThread = -1 ;
    
    Random randomX = new Random() ;
    
	@Override
	public void run() {
        // create Producer properties
       Properties properties = new Properties();
       properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BitstreamKafkaGenerator.bootstrapServers);
       properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
       properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//       properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(BitstreamKafkaGenerator.numSetOfBox)  );
       properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 1000000  );
       properties.put(ProducerConfig.LINGER_MS_CONFIG, 10  );

       // create the producer
       @SuppressWarnings("resource")
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

       ArrayList<BitstreamElement> arraySOBs = new ArrayList<BitstreamElement>() ;
       
	   for ( int nn = 1 ; nn<= BitstreamKafkaGenerator.numSetOfBox; nn++ ) {
	   		arraySOBs.add(new BitstreamElement()) ;
	   }

       long t0 = System.currentTimeMillis();
       long tmax = t0 - t0%(BitstreamKafkaGenerator.interval*1000) + BitstreamKafkaGenerator.interval*1000 ;
       while (System.currentTimeMillis() < tmax) {
       	try {
				Thread.sleep(10) ;
			} catch (InterruptedException e) {}
       }
   	
       while ( true ) {
    	    int recordsLoaded = 0 ;
	       	t0 = System.currentTimeMillis();
	       	LOG.info("Start processing records") ;
		    for ( BitstreamElement sob : arraySOBs ) {
		       		sob.generateData(BitstreamKafkaGenerator.interval);
		       		
		       		String key = sob.getMAC() ;
		       		
		       		if ( randomX.nextDouble() < BitstreamKafkaGenerator.percentageNullKey) {
//		       			LOG.info ( "null key for: " + key) ;
		       			key = null ;
		       		}

		       		if ( randomX.nextDouble() > BitstreamKafkaGenerator.percentageLostRecords) {

		       			String message =  "" ;
		       			if ( BitstreamKafkaGenerator.asJSON ) {
		       				message = sob.getAsJSONString(BitstreamKafkaGenerator.pretty, BitstreamKafkaGenerator.reset) ;
		       			} else {
		       				message = sob.getAsPlain() ;
		       			}

		       			recordsLoaded++ ;
			       		ProducerRecord<String, String> producerRecord =
		                       new ProducerRecord<>(
		                    		   	BitstreamKafkaGenerator.topic,
		                    		   	key, 
		                    		   	message
		                    		   	);
			       		producer.send(producerRecord);
		       		}
		    }

		    // flush data - synchronous
	        producer.flush();
	           
	        LOG.info("Flush to " + BitstreamKafkaGenerator.topic + " "+ recordsLoaded + " records, thread (" + idThread + ")" ) ;
	           
	        while (System.currentTimeMillis() - t0 < BitstreamKafkaGenerator.interval * 1000) {
	           	try {
					Thread.sleep(100) ;
				} catch (InterruptedException e) {}
	        }
       	}	
   }
	
}
