package edu.doc_ti.designcriteria.kafkastreams;


import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.doc_ti.designcriteria.common.objects.PropertyLoader;

public class CdrsTopologyV2 {
	private static final Logger LOG = LoggerFactory.getLogger(ObjectProcessor.class);

	public static int numThreads = 1 ;
	public static String esHosts= "127.0.0.1:9200" ;
	
	static SimpleDateFormat sdf = new SimpleDateFormat("u-HH:mm") ;
	

    public static void main(String[] args) {
    	
		String topicIn = "topic_in" ; 
		String topicOut = "topic_out" ; 
		String bootstrapServers = "127.0.0.1:9092" ;

		if ( args.length <1 ) {
			System.err.println("Needed config file as parameter") ;
			System.exit(-1) ;
		}
		
		PropertyLoader.loadProperties(args[0]);
		
		numThreads = PropertyLoader.getIntProperty("numThreads", numThreads) ;
		topicIn = PropertyLoader.getProperty("topicIn", topicIn	) ;
		topicOut = PropertyLoader.getProperty("topicOut", topicOut) ;

		bootstrapServers = PropertyLoader.getProperty("brokers", bootstrapServers) ;
		esHosts= PropertyLoader.getProperty("esHosts", esHosts) ;
		
		String applicationID = PropertyLoader.getProperty("applicationID", "test-topology");
		
		
		LOG.info("#########################################################################################");
		LOG.info("property [{}] : value [{}]", "topicIn", topicIn);
		LOG.info("property [{}] : value [{}]", "topicOut", topicOut);
		LOG.info("property [{}] : value [{}]", "bootstrapServers", bootstrapServers);
		LOG.info("property [{}] : value [{}]", "esHosts", esHosts);
		LOG.info("property [{}] : value [{}]", "applicationID", applicationID);
		LOG.info("#########################################################################################");
		

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID );
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, Integer.toString(numThreads)) ;
        
        Topology builder = new Topology();

     builder.addSource("source", topicIn)
         .addProcessor("process", () -> new ObjectProcessor(), "source")
         .addSink("sink", topicOut, "process") 
         ;        

     final KafkaStreams streams = new KafkaStreams(builder, props);
     
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
