package edu.doc_ti.designcriteria.gendata.bitstreamstats;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitstreamKafkaGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BitstreamKafkaGenerator.class);

    static int numSetOfBox = 5 ;
    static String bootstrapServers = "127.0.0.1:9092";
    static String topic = "topic_in_stream" ;
    static int interval = 15 ;
    
    static boolean pretty = false ;
    static boolean reset = false ;
    static boolean asJSON = false ;
    static double percentageNullKey = 0.0 ; 
    static double percentageLostRecords = 0.0 ; 
    
    public static void main(String[] args) {
        LOG.info("I am a Kafka Producer");
        int numThreads = 1 ;

		Options options = new Options();
		
		options.addOption(new Option("h", "help", false, "Print this help"));
//		options.addOption(new Option("d", "date", true, "Date for the input data, format [yyyy-mm-dd]"));
		options.addOption(new Option("b", "broker", true, "List of kafka bootstrap servers (default: " + bootstrapServers + ")" ));
		options.addOption(new Option("t", "topic", true, "Topic name (default: "+ topic +")"));

		options.addOption(new Option("n", "numsetofbox", true, "Generate data for n set of boxes (default " + numSetOfBox + ")"));
		options.addOption(new Option("i", "interval", true, "Generate data every i seconds (default: "+ interval +")"));

		options.addOption(new Option("p", "pretty", false, "Pretty format for JSON (default: "+ pretty +")"));
		options.addOption(new Option("r", "reset", false,  "Include reset flag in JSON (default: "+ reset +")"));
		options.addOption(new Option("x", "numthreads", true,  "Number of threads (default: "+ numThreads +")"));
		options.addOption(new Option("z", "nullkeys", true,  "% of null keys (default: "+ percentageNullKey +")"));
		options.addOption(new Option("l", "lost", true,  "% of lost records (default: "+ percentageLostRecords +")"));
		options.addOption(new Option("a", "as_json", true, "Output as JSON or test (default: "+ asJSON +")"));
		
        
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
			formatter.printHelp("KafkaGenerator", options);
			System.exit(0) ;
		}
       
		if ( cmd.hasOption('n')  ) {
			try {
				numSetOfBox = Integer.parseInt(cmd.getParsedOptionValue("n").toString());
			} catch (Exception e) {}
		} 
		
//		if (numSetOfBox > 5000) {
//			numSetOfBox = 5000 ;
//		}
		
		if ( cmd.hasOption('t')  ) {
			try {
				topic = cmd.getParsedOptionValue("t").toString() ;
			} catch (Exception e) {}
		} 
		if ( cmd.hasOption('b')  ) {
			try {
				bootstrapServers = cmd.getParsedOptionValue("b").toString() ;
			} catch (Exception e) {}
		}
		
		if ( cmd.hasOption('i')  ) {
			try {
				interval = Integer.parseInt(cmd.getParsedOptionValue("i").toString());
			} catch (Exception e) {}
		}

		if ( cmd.hasOption('x')  ) {
			try {
				numThreads = Integer.parseInt(cmd.getParsedOptionValue("x").toString());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		if ( cmd.hasOption('z')  ) {
			try {
				percentageNullKey= Double.parseDouble ( cmd.getParsedOptionValue("z").toString() ) /100 ;
			} catch (Exception e) {}
		} 
		
		if ( cmd.hasOption('l')  ) {
			try {
				percentageLostRecords= Double.parseDouble ( cmd.getParsedOptionValue("l").toString() ) /100 ;
			} catch (Exception e) {}
		} 
		
//		if ( interval < 15) interval = 15 ;
		
		pretty = cmd.hasOption('p')  ;
		reset  = cmd.hasOption('r')  ;
		asJSON = cmd.hasOption('a')  ;
		
//		-Dorg.slf4j.simpleLogger.defaultLogLevel=debug
		
		LOG.info("Number of threads = " + numThreads ) ;
		
		for (int idTh = 1 ; idTh <= numThreads ; idTh++ ) {
			BitstreamKafkaGeneratorThread threadGen = new BitstreamKafkaGeneratorThread() ;
			LOG.info("Starting thread " + idTh);
			threadGen.idThread = idTh ;
			threadGen.start(); 
		}				

    }
}