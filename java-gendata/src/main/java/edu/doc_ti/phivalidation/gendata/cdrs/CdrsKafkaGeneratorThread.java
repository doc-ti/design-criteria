package edu.doc_ti.phivalidation.gendata.cdrs;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdrsKafkaGeneratorThread extends Thread {
	
	private int myID = -1;

	public void setID (int id) {
		myID  = id ;
	}
	
    private static final Logger LOG = LoggerFactory.getLogger(CdrsKafkaGeneratorThread.class);
	
	@Override
	public void run() {

        // create Producer properties
       Properties properties = new Properties();
       properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CdrsKafkaGenerator.bootstrapServers);
       properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
       properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//       properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(CdrsKafkaGenerator.speed*1000)  );
       properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 1000000 );
       properties.put(ProducerConfig.LINGER_MS_CONFIG, 10 );

       // create the producer
       @SuppressWarnings("resource")
       KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
       CdrsFileGenerator myGen = new CdrsFileGenerator() ;
       
       while ( true ) {
	       	long t0 = System.currentTimeMillis();
	       	for ( int nn = 1 ; nn<= CdrsKafkaGenerator.speed; nn++ ) {
	               ProducerRecord<String, String> producerRecord =
	                       new ProducerRecord<>(CdrsKafkaGenerator.topic, myGen.getData(1).replaceAll("\"", "").replace("\n", ""));
	               producer.send(producerRecord);
	       	}
            // flush data - synchronous
            producer.flush();
           
            LOG.info("Flush to " + CdrsKafkaGenerator.topic + " "+ CdrsKafkaGenerator.speed + " records thread (" + myID + ")") ;
           
            while (System.currentTimeMillis() - t0 < 1000) {
	           	try {
						Thread.sleep(0, 10000) ;
					} catch (InterruptedException e) {}
            }
       }
	}

}
