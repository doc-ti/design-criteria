
package edu.doc_ti.designcriteria.gendata.cdrs ;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.datafaker.transformations.CsvTransformer;
import net.datafaker.transformations.Field;
import net.datafaker.transformations.Schema;

public class CdrsFileGenerator {

	public static String DELIMITER = ";" ;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMdd_HHmmss");
	static SimpleDateFormat sdfNice = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static Date tsFrom = null ;
	static Date tsTo = null ;
	static String path = "." ;
	static int maxFiles = 20 ;

	static MyCustomFaker myFaker = new MyCustomFaker();
	
	public static void main(String[] args) {
		
		Options options = new Options();
		
		int numRecords = 5000 ;
		
		options.addOption(new Option("h", "help", false, "Print this help"));
		options.addOption(new Option("d", "date", true, "Date for the input data, format [yyyy-mm-dd]"));
		options.addOption(new Option("s", "seconds", true, "Generate a file every s seconds (default 30)"));
		options.addOption(new Option("n", "numrecords", true, "Number of records in file (default 5000)"));
		options.addOption(new Option("m", "maxfiles", true, "Number of files to generate (default 20)"));
		options.addOption(new Option("p", "path", true, "Path to write the file"));
		

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
			formatter.printHelp("FileGenerator", options);
			System.exit(0) ;
		}
		
		
		try {
			tsFrom = sdf.parse(sdf.format(new Date() ));
		} catch (ParseException e) {}
		
		if ( cmd.hasOption('d')  ) {
			try {
				tsFrom = sdf.parse(cmd.getParsedOptionValue("date").toString());
			} catch (Exception e) {
			}
		} 
		tsTo = new Date( tsFrom.getTime() + 24*3600*1000 - 1000);
		
		if ( cmd.hasOption('n')  ) {
			try {
				numRecords = Integer.parseInt(cmd.getParsedOptionValue("numrecords").toString());
			} catch (Exception e) {
			}
		} 

		if ( cmd.hasOption('m')  ) {
			try {
				maxFiles = Integer.parseInt(cmd.getParsedOptionValue("m").toString());
			} catch (Exception e) {
			}
		} 

		if ( cmd.hasOption('p')  ) {
			try {
				path = cmd.getParsedOptionValue("path").toString();
			} catch (Exception e) {}
		} 

//		CdrsFileGenerator myGen = new CdrsFileGenerator() ; 
//		System.out.println( myGen.getData(1).replaceAll("\"", "").replace("\n", "")) ;
//		System.exit(0);

		String filename = "" ;
		if ( cmd.hasOption('s')  ) {
			int seconds = 30 ;
			try {
				seconds = Integer.parseInt(cmd.getParsedOptionValue("seconds").toString());
			} catch (Exception e) {}
			
			if ( seconds < 1 ) {
				seconds = 1 ;
			}
			
			int counter = 0 ;
			while ( counter < maxFiles )  {
				
				counter++ ;
				long tnext = System.currentTimeMillis()  + 1000*seconds ;
				if ( !cmd.hasOption('d')  ) {
					tsFrom = new Date() ;
					tsTo = new Date( tsFrom.getTime() + 1000*seconds - 100);
				}
				
				filename = "file_" + sdfFileName.format(new Date() ) + "_" + String.format( "%06d", counter); ;
				generateFile( filename, numRecords ) ;

				while ( System.currentTimeMillis() < tnext) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {}
				}
			}
		} else {
			
			filename = "data_" + sdfFileName.format(new Date() ) ;
			generateFile( filename, numRecords ) ;
		}
	}
	
	private static void generateFile(String filename, int numRecords) {
		
		
		String fullFileName = path + "/" + filename + ".tmp" ;
		System.out.println (String.format( "%s - Generating file: %s, records: %d, date from: %s, to: %s",
				sdfNice.format(new Date() ), 
				fullFileName, numRecords, sdfNice.format(tsFrom), sdfNice.format(tsTo) )) ;
		
		int printed = 0 ;
		BufferedOutputStream bfout = null ;
		
		File myFile = new File(fullFileName) ;
		
		try {
			bfout = new BufferedOutputStream (new FileOutputStream(myFile ) ) ;
		
			while ( printed < numRecords ) {
				int numToPrint = numRecords - printed ;
				
				if ( numToPrint > 100 ) {
					numToPrint = 100 ;
				}
				printed += numToPrint ;
				
				String data = getDataStatic(numToPrint).replaceAll("\"", "") ;
				bfout.write( data.getBytes() );
			}
			bfout.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println () ;
		}
		
		myFile.renameTo(new File(path + "/" + filename)) ;
	}

	
	
	Schema<Object, ?> schema = null ;
	CsvTransformer<Object> transformerCSV = null  ;
	
	public void initialize() {
		schema =
	        Schema.of(
	        		Field.field("f1",  () -> myFaker.MyElements().nextDeterminedDistribElement("id3")),
	                Field.field("f2",  () -> myFaker.MyElements().nextDeterminedDistribElement("id3")),
	                Field.field("f3",  () -> myFaker.MyElements().nextDeterminedDistribElement("id4")),
	                Field.field("f4",  () -> myFaker.MyElements().nextDeterminedDistribElement("id5")),
	                Field.field("f5",  () -> myFaker.MyElements().exponentialDistributedNumber(500, 10000, 0) ),
	                Field.field("f6",  () -> myFaker.MyElements().nextDeterminedDistribElement("id7")),
	                Field.field("f7",  () -> myFaker.MyElements().nextDeterminedDistribElement("id8")),
	                Field.field("f8",  () -> myFaker.MyElements().nextDeterminedDistribElement("id9")),
	                Field.field("f9", () -> myFaker.MyElements().nextDeterminedDistribElement("id10")),
	                Field.field("f10", () -> myFaker.expression("#{numerify '###'}")) ,
	                Field.field("f11", () -> myFaker.expression("#{numerify '##'}")) ,
	                Field.field("f12", () -> myFaker.expression("#{numerify '###'}")) ,
	                Field.field("f13", () -> myFaker.expression("#{numerify '###'}")) ,
	                Field.field("f14", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f15", () -> myFaker.expression("#{numerify '###'}")) ,
	                Field.field("f16", () -> myFaker.expression("#{numerify '####'}")) ,
	                Field.field("f17", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f18", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f19", () -> myFaker.expression("#{numerify '##'}")) ,
	                Field.field("f20", () -> myFaker.expression("#{numerify '###'}")) ,
	                Field.field("f21", () -> myFaker.MyElements().exponentialDistributedNumber(20, 200, 0) ),
	                Field.field("f22", () -> myFaker.MyElements().exponentialDistributedNumber(5, 99, 0) ),
	                Field.field("f23", () -> myFaker.expression("#{numerify '#'}")) ,
//	                Field.field("f24", () -> myFaker.date().between(tsFrom, tsTo, "yyyy-MM-dd'T'HH:mm:ss") ), 
	                Field.field("f24", () -> myFaker.MyElements().sysdate("yyyy-MM-dd'T'HH:mm:ssZ") ), 
//	                Field.field("f25", () -> myFaker.date().between(tsFrom, tsTo, "yyyy-MM-dd'T'HH:mm:ss") ),
	                Field.field("f25", () -> myFaker.MyElements().sysdate("yyyy-MM-dd'T'HH:mm:ssZ") ), 
	                Field.field("f26", () -> myFaker.MyElements().exponentialDistributedNumber(25, 99, 0) ),
	                Field.field("f27", () -> myFaker.MyElements().exponentialDistributedNumber(5, 19, 0) ),
	                Field.field("f28", () -> myFaker.MyElements().exponentialDistributedNumber(100, 200) ),
	                Field.field("f29", () -> myFaker.MyElements().nextDeterminedDistribElement("id30")),
	                Field.field("f30", () -> myFaker.expression("#{numerify '16704#####'}")) ,
	                Field.field("f31", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f32", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f33", () -> myFaker.expression("#{numerify '##'}")) ,	
	                Field.field("f34", () -> myFaker.expression("#{numerify '310004##########'}")) ,
	                Field.field("f35", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f36", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f37", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f38", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f39", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f40", () -> myFaker.MyElements().nextDeterminedDistribElement("id41")),
	                Field.field("f41", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f42", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f43", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f44", () -> myFaker.expression("#{numerify '#'}")) ,
	                Field.field("f45", () -> myFaker.expression("#{letterify 'A?'}").toUpperCase()) ,
	                Field.field("f46", () -> myFaker.expression("#{letterify 'B?'}").toUpperCase()) ,
//	                Field.field("f47", () -> myFaker.expression("#{letterify 'C??'}").toUpperCase()) ,
	                Field.field("f47", () -> myFaker.MyElements().getFileID()) ,
	                Field.field("f48", () -> myFaker.expression("#{bothify 'D?##'}").toUpperCase()) ,
	                Field.field("f49", () -> myFaker.MyElements().exponentialDistributedNumber(20, 80, 2) ),
	                Field.field("f50", () -> myFaker.MyElements().exponentialDistributedNumber(20, 80, 2) ),
	                Field.field("f51", () -> myFaker.MyElements().exponentialDistributedNumber(20, 80, 2) ),
	                Field.field("f52", () -> myFaker.date().between(tsFrom, tsTo, "yyyy-MM-dd'T'HH:mm:ss") ), 
	                Field.field("f53", () -> myFaker.MyElements().nextDeterminedDistribElement("id54")),
	                Field.field("f54", () -> myFaker.MyElements().hostname()),
	                Field.field("f55", () -> myFaker.MyElements().currentTS())
	        	);
		 
		transformerCSV = CsvTransformer.<Object>builder().header(false).separator(DELIMITER).build();
			 
		
	}

	
	public String getData(int numToPrint) {
		
		if (schema == null ) {
			initialize();
		}
		return 	transformerCSV.generate(schema, numToPrint);
	}
	
	
	static CdrsFileGenerator myStaticGenerator = new CdrsFileGenerator() ;  
	
	public static String getDataStatic(int numToPrint) {
		
		if (myStaticGenerator.schema == null ) {
			myStaticGenerator.initialize();
		}
		
		return 	myStaticGenerator.transformerCSV.generate(myStaticGenerator.schema, numToPrint);
		
//        return Format.toCsv(
//        		
//                Csv.Column.of("f3",  () -> myFaker.MyElements().nextDeterminedDistribElement("id3")),
//                Csv.Column.of("f4",  () -> myFaker.MyElements().nextDeterminedDistribElement("id4")),
//                Csv.Column.of("f5",  () -> myFaker.MyElements().nextDeterminedDistribElement("id5")),
//                Csv.Column.of("f6",  () -> myFaker.MyElements().exponentialDistributedNumber(500, 10000, 0) ),
//                Csv.Column.of("f7",  () -> myFaker.MyElements().nextDeterminedDistribElement("id7")),
//                Csv.Column.of("f8",  () -> myFaker.MyElements().nextDeterminedDistribElement("id8")),
//                Csv.Column.of("f9",  () -> myFaker.MyElements().nextDeterminedDistribElement("id9")),
//                Csv.Column.of("f10", () -> myFaker.MyElements().nextDeterminedDistribElement("id10")),
//                Csv.Column.of("f11", () -> myFaker.expression("#{numerify '###'}")) ,
//                Csv.Column.of("f12", () -> myFaker.expression("#{numerify '##'}")) ,
//                Csv.Column.of("f13", () -> myFaker.expression("#{numerify '###'}")) ,
//                Csv.Column.of("f14", () -> myFaker.expression("#{numerify '###'}")) ,
//                Csv.Column.of("f15", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f16", () -> myFaker.expression("#{numerify '###'}")) ,
//                Csv.Column.of("f17", () -> myFaker.expression("#{numerify '####'}")) ,
//                Csv.Column.of("f18", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f19", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f20", () -> myFaker.expression("#{numerify '##'}")) ,
//                Csv.Column.of("f21", () -> myFaker.expression("#{numerify '###'}")) ,
//                Csv.Column.of("f22", () -> myFaker.MyElements().exponentialDistributedNumber(20, 200, 0) ),
//                Csv.Column.of("f23", () -> myFaker.MyElements().exponentialDistributedNumber(5, 99, 0) ),
//                Csv.Column.of("f24", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f25", () -> myFaker.date().between(tsFrom, tsTo, "yyyy-MM-dd'T'HH:mm:ss") ), 
//                Csv.Column.of("f26", () -> myFaker.date().between(tsFrom, tsTo, "yyyy-MM-dd'T'HH:mm:ss") ),
//                Csv.Column.of("f27", () -> myFaker.MyElements().exponentialDistributedNumber(25, 99, 0) ),
//                Csv.Column.of("f28", () -> myFaker.MyElements().exponentialDistributedNumber(5, 19, 0) ),
//                Csv.Column.of("f29", () -> myFaker.MyElements().exponentialDistributedNumber(100, 200) ),
//                Csv.Column.of("f30", () -> myFaker.MyElements().nextDeterminedDistribElement("id30")),
//                Csv.Column.of("f31", () -> myFaker.expression("#{numerify '16704#####'}")) ,
//                Csv.Column.of("f32", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f33", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f34", () -> myFaker.expression("#{numerify '##'}")) ,	
//                Csv.Column.of("f35", () -> myFaker.expression("#{numerify '310004##########'}")) ,
//                Csv.Column.of("f36", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f37", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f38", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f39", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f40", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f41", () -> myFaker.MyElements().nextDeterminedDistribElement("id41")),
//                Csv.Column.of("f42", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f43", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f44", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f45", () -> myFaker.expression("#{numerify '#'}")) ,
//                Csv.Column.of("f45", () -> myFaker.expression("#{letterify 'A?'}").toUpperCase()) ,
//                Csv.Column.of("f45", () -> myFaker.expression("#{letterify 'B?'}").toUpperCase()) ,
//                Csv.Column.of("f45", () -> myFaker.expression("#{letterify 'C??'}").toUpperCase()) ,
//                Csv.Column.of("f49", () -> myFaker.expression("#{bothify 'D?##'}").toUpperCase()) ,
//                Csv.Column.of("f50", () -> myFaker.MyElements().exponentialDistributedNumber(20, 80, 2) ),
//                Csv.Column.of("f51", () -> myFaker.MyElements().exponentialDistributedNumber(20, 80, 2) ),
//                Csv.Column.of("f52", () -> myFaker.MyElements().exponentialDistributedNumber(20, 80, 2) ),
//                Csv.Column.of("f53", () -> myFaker.date().between(tsFrom, tsTo, "yyyy-MM-dd'T'HH:mm:ss") ), 
//                Csv.Column.of("f54", () -> myFaker.MyElements().nextDeterminedDistribElement("id54"))
//                )
//            .separator(DELIMITER)
//            .header(false)
//            .limit(numToPrint).build().get();		
	}

}
