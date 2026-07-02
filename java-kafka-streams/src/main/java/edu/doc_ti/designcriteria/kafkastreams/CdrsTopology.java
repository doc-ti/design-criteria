package edu.doc_ti.designcriteria.kafkastreams;


import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;


public class CdrsTopology {
	public static int numThreads = 1 ;
	public static String esHosts= "127.0.0.1:9200" ;
	
	static SimpleDateFormat sdf = new SimpleDateFormat("u-HH:mm") ;
	

    public static void main(String[] args) {
    	
		String topicIn = "topic_in" ; 
		String topicOut = "topic_out" ; 
		String bootstrapServers = "127.0.0.1:9092" ;

	
		Options options = new Options();
		options.addOption(new Option("h", "help", false, "Print this help"));
		options.addOption(new Option("n", "numrthreads", true, "number of threads records (def: " + numThreads + ")"));
		options.addOption(new Option("b", "broker", true, "List of kafka bootstrap servers (def: " + bootstrapServers + ")"));
		options.addOption(new Option("i", "topic_in", true, "Input topic name (default: " + topicIn + ")"));
		options.addOption(new Option("o", "topic_out", true, "Output topic name (default: " + topicOut + ")"));
		options.addOption(new Option("e", "eshost", true, "Elasticsearch target hosts (default: " + esHosts + ")"));
    	
    	
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null ;
		try {
			cmd = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		if ( cmd.hasOption('h') || cmd.getOptions().length < 0 ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(120) ;
			formatter.printHelp("TestTopology", options);
			System.exit(0) ;
		}
       
		
		if ( cmd.hasOption('n')  ) {
			try {
				numThreads = Integer.parseInt(cmd.getParsedOptionValue("n").toString());
			} catch (Exception e) {}
		} 

		if ( cmd.hasOption('i')  ) {
			try {
				topicIn = cmd.getParsedOptionValue("i").toString() ;
			} catch (Exception e) {}
		} 

		if ( cmd.hasOption('o')  ) {
			try {
				topicOut = cmd.getParsedOptionValue("o").toString() ;
			} catch (Exception e) {}
		}
		
		if ( cmd.hasOption('b')  ) {
			try {
				bootstrapServers = cmd.getParsedOptionValue("b").toString() ;
			} catch (Exception e) {}
		} else {
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(120) ;
			formatter.printHelp("TestTopology", options);
			System.exit(0) ;
		}

		if ( cmd.hasOption('e')  ) {
			try {
				esHosts = cmd.getParsedOptionValue("e").toString() ;
			} catch (Exception e) {}
		}
				
  
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-topology" );
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1") ;
        
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
