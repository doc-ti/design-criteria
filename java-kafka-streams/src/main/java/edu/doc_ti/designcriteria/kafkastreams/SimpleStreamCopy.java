package edu.doc_ti.designcriteria.kafkastreams;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class SimpleStreamCopy {
	private static final Logger LOG = LoggerFactory.getLogger(ObjectProcessor.class);

    public static void main(String[] args) {
		Options options = new Options();
		String bootstrapServers = "127.0.0.1:9200";
		String topicIn = "topic_in";
		String topicOut = "topic_out";
		String appId = "KafkaStreamsSimpleCopy";
		
		options.addOption(new Option("h", "help", false, "Print this help"));
		options.addOption(new Option("b", "broker", true, "List of kafka bootstrap servers (default: " + bootstrapServers + ")" ));
		options.addOption(new Option("i", "topic_in", true, "Topic name (default: "+ topicIn +")"));
		options.addOption(new Option("o", "topic_out", true, "Topic name (default: "+ topicOut +")"));
		options.addOption(new Option("a", "app_id", true, "Application ID (default: "+ appId +")"));

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
			formatter.printHelp("SimpleStreamCopy", options);
			System.exit(0) ;
		}

		if ( cmd.hasOption('i')  ) {
			try {
				topicIn = cmd.getParsedOptionValue("i").toString() ;
			} catch (Exception e) {
			}
		} 
		if ( cmd.hasOption('o')  ) {
			try {
				topicOut = cmd.getParsedOptionValue("o").toString() ;
			} catch (Exception e) {
			}
		} 
		if ( cmd.hasOption('b')  ) {
			try {
				bootstrapServers = cmd.getParsedOptionValue("b").toString() ;
			} catch (Exception e) {
			}
		} 
		if ( cmd.hasOption('a')  ) {
			try {
				appId = cmd.getParsedOptionValue("a").toString() ;
			} catch (Exception e) {
			}
		} 
		
		
		LOG.info("#########################################################################################");
		LOG.info("property [{}] : value [{}]", "topicIn", topicIn);
		LOG.info("property [{}] : value [{}]", "topicOut", topicOut);
		LOG.info("property [{}] : value [{}]", "bootstrapServers", bootstrapServers);
		LOG.info("property [{}] : value [{}]", "applicationID", appId);
		LOG.info("#########################################################################################");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId );
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, Integer.toString(2)) ;

        // Crear el builder y definir la topología
        StreamsBuilder builder = new StreamsBuilder();

        // Leer desde el tópico de entrada
        KStream<String, String> inputStream = builder.stream(topicIn);

        // Escribir directamente al tópico de salida
        inputStream.to(topicOut);

        // Construir y lanzar la aplicación
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
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