package edu.doc_ti.phivalidation.gendata.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.datafaker.Faker;
import net.datafaker.transformations.Field;
import net.datafaker.transformations.JsonTransformer;
import net.datafaker.transformations.Schema;

public class GenerateJson4ESLoad {
    private static final Logger log = LoggerFactory.getLogger(GenerateJson4ESLoad.class);
	static SimpleDateFormat sdfNice = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    private static String index = "default_index" ; 
    private static int numRecords = 1000 ; 
    private static String server = null ;
	public static void main(String[] args) {
		
		Options options = new Options();
		
		
		options.addOption(new Option("h", "help", false, "Print this help"));
		options.addOption(new Option("i", "index", true, "Index to load into (default: "+ index +")"));
		options.addOption(new Option("n", "numrecords", true, "Generate n numrecords (default " + numRecords + ")"));
		options.addOption(new Option("s", "es server", true, "Load into server (default " + server + ")"));
		Option op = new Option("d", "date", true, "Fecha en formato yyyymmdd");
		op.setRequired(true) ;
			
		options.addOption(op);

//		options.addOption(new Option("p", "pretty", false, "Pretty format for JSON (default: "+ pretty +")"));
//		options.addOption(new Option("r", "reset", false,  "Include reset flag in JSON (default: "+ reset +")"));
		
        
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
			formatter.printHelp("GenerateJson4ESLoad", options);
			System.exit(0) ;
		}
       
		if ( cmd.hasOption('i')  ) {
			try {
				index = cmd.getParsedOptionValue("i").toString() ;
			} catch (Exception e) {}
		}
		
		if ( cmd.hasOption('n')  ) {
			try {
				numRecords= Integer.parseInt ( cmd.getParsedOptionValue("n").toString()) ;
			} catch (Exception e) {}
		}

		if ( cmd.hasOption('s')  ) {
			try {
				server = cmd.getParsedOptionValue("s").toString() ;
			} catch (Exception e) {}
		}		
		
		Date tsStart = new Date();
		Date tsEnd = new Date();
		try {
			String straux= cmd.getParsedOptionValue("d").toString() ;
			
			tsStart = sdfNice.parse(straux + "T000000") ;
			tsEnd =  sdfNice.parse(straux + "T235959") ;
		} catch (Exception e) {}
		
		
		final Date tsStartF = tsStart ;
		final Date tsEndF = tsEnd ;
		
		
		Faker myFaker = new Faker() ;

		Schema<String, Object> schema =
        Schema.of(
//        		Field.field("@timestamp", () -> myFaker.date().past(30, TimeUnit.DAYS, "yyyy-MM-dd'T'HH:mm:ss"))
        		Field.field("@timestamp", () -> myFaker.date().between(tsStartF, tsEndF, "yyyy-MM-dd'T'HH:mm:ss"))
        		, Field.field("first_name", () -> myFaker.name().firstName())
        		, Field.field("last_name", () -> myFaker.name().lastName())
        		, Field.field("address", () -> myFaker.address().streetAddress())
        		, Field.field("country", () -> myFaker.address().countryCode())
        		, Field.field("god", () -> myFaker.ancient().god())
        		, Field.field("animal", () -> myFaker.animal().name())
        		, Field.field("author", () -> myFaker.book().author())
        		, Field.field("genre", () -> myFaker.book().genre())
        		, Field.field("title", () -> myFaker.book().title())
        		, Field.field("phone", () -> myFaker.phoneNumber().phoneNumberNational())
        		, Field.field("superhero", () -> myFaker.superhero().name())
        		, Field.field("url", () -> myFaker.internet().url())
        		, Field.field("group", () -> myFaker.regexify("grupo[1-6]"))
        		
        		);

		JsonTransformer<String> transformer = JsonTransformer.<String>builder().build();

		
		String filename = "data_" + sdfNice.format(new Date()) + ".json" ;
		log.info("Trying to write " + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter( filename ))) {
        	
        	for ( int n = 1 ; n <= numRecords ; n++) {
	    		writer.write("{\"index\":{\"_index\":\""+ index + "\"}}") ; 
	            writer.newLine(); 
	    		writer.write (transformer.generate(schema, 1) ) ;
	            writer.newLine();
	            
	            if ( n%1000 == 0  && n != numRecords) {
	            	log.info("Writed " + n + " records");
	            }
        	}
        	
        	writer.close() ;
    		log.info("Writed " + numRecords + " records to file : "+ filename );

    		if ( server != null ) {
    			String command = "curl -XPOST " + server + "/test_index/_bulk -H \"Content-Type: application/x-ndjson\" --data-binary \"@" + filename + "\"" ;
    			log.info(command);
    			

    	        ProcessBuilder processBuilder = new ProcessBuilder();
    	        processBuilder.command("bash", "-c", command); // Para sistemas UNIX/Linux/macOS
    	        // processBuilder.command("cmd.exe", "/c", command); // Para Windows
    	        
    	        try {
    	            // Iniciar el proceso
    	            Process process = processBuilder.start();

    	            // Leer la salida del programa externo
    	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    	            String line;
    	            while ((line = reader.readLine()) != null) {
    	                System.out.println(line);
    	            }

    	            // Esperar a que el proceso termine
    	            int exitCode = process.waitFor();
    	            System.out.println("Proceso terminado con codigo de salida: " + exitCode);

    	        } catch (IOException | InterruptedException e) {
    	            e.printStackTrace();
    	        }
    			
    			
    		}
        	
        	
        } catch (IOException e) {
            e.printStackTrace();
        }		
		

//        
		
	}

}
