package edu.doc_ti.phivalidation.gendata.cdrs;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdrsKafkaGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CdrsKafkaGenerator.class);
    
    static int speed = 500 ;
    static int numThreads = 1 ;
    static String bootstrapServers = "127.0.0.1:9092";
    static String topic = "topic_in_stream" ;    

    public static void main(String[] args) {
        LOG.info("I am a Kafka Producer");

		Options options = new Options();
		
		options.addOption(new Option("h", "help", false, "Print this help"));
		options.addOption(new Option("d", "date", true, "Date for the input data, format [yyyy-mm-dd]"));
		options.addOption(new Option("n", "numrecords", true, "Generate n records per second (default " + speed + ")"));
//		options.addOption(new Option("r", "numthreads", true, "Number of threads (default " + numThreads + ")"));
		options.addOption(new Option("b", "broker", true, "List of kafka bootstrap servers (default: " + bootstrapServers + ")" ));
		options.addOption(new Option("t", "topic", true, "Topic name (default: "+ topic +")"));
        
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
       
		try {
			CdrsFileGenerator.tsFrom = CdrsFileGenerator.sdf.parse(CdrsFileGenerator.sdf.format(new Date() ));
		} catch (ParseException e) {}
		if ( cmd.hasOption('d')  ) {
			try {
				CdrsFileGenerator.tsFrom = CdrsFileGenerator.sdf.parse(cmd.getParsedOptionValue("date").toString());
			} catch (Exception e) {
			}
		} 
		
		CdrsFileGenerator.tsTo = new Date( CdrsFileGenerator.tsFrom.getTime() + 24*3600*1000 - 1000);
        
		
		if ( cmd.hasOption('n')  ) {
			try {
				speed = Integer.parseInt(cmd.getParsedOptionValue("n").toString());
			} catch (Exception e) {
			}
		} 
		
		
		if ( cmd.hasOption('r')  ) {
			try {
				numThreads = Integer.parseInt(cmd.getParsedOptionValue("r").toString());
			} catch (Exception e) {
			}
		} 
		
//		if (speed > 5000) {
//			speed = 5000 ;
//		}
		
		if ( cmd.hasOption('t')  ) {
			try {
				topic = cmd.getParsedOptionValue("t").toString() ;
			} catch (Exception e) {
			}
		} 
		if ( cmd.hasOption('b')  ) {
			try {
				bootstrapServers = cmd.getParsedOptionValue("b").toString() ;
			} catch (Exception e) {
			}
		} 
		
//		-Dorg.slf4j.simpleLogger.defaultLogLevel=debug
		
		for (int idTh = 1 ; idTh <= numThreads ; idTh++ ) {
			CdrsKafkaGeneratorThread threadGen = new CdrsKafkaGeneratorThread() ;
			threadGen.setID(idTh);
			threadGen.start(); 
		}

    }
}