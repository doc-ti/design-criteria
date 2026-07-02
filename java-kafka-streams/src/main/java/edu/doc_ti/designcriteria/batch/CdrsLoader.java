package edu.doc_ti.designcriteria.batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.util.BinaryData;
import co.elastic.clients.util.ContentType;
import edu.doc_ti.designcriteria.common.objects.CDRData;
import edu.doc_ti.designcriteria.common.objects.MyESUtils;
import edu.doc_ti.designcriteria.common.objects.MyFileWriter;
import edu.doc_ti.designcriteria.common.objects.ObjStreaming;
import edu.doc_ti.designcriteria.common.objects.PropertyLoader;

public class CdrsLoader {

	private static final Logger LOG = LoggerFactory.getLogger(CdrsLoader.class);
	
    static BulkIngester<String> ingester = null ;
    static MyFileWriter fr ;
    static private ObjectMapper mapper  ;
	public static String indexName;
	
	public static int counter = 0 ;
    
    
	public static void main(String[] args) {
		PropertyLoader.loadProperties(args[0]);

		long t0 = System.currentTimeMillis() ;
		
		String filePrefix = "batch-out-files" ;
		fr = new MyFileWriter(
				PropertyLoader.getProperty("pathOutFiles", ".") , 
				PropertyLoader.getProperty("nameOutFiles", filePrefix), 
				PropertyLoader.getIntProperty("recordsOutFiles", 10000) , 
				false) ;		

		mapper = new ObjectMapper();
		Properties props = PropertyLoader.getProps() ;
		ingester = MyESUtils.init(props) ;		
		indexName = PropertyLoader.getProperty("indexName", indexName) ;
		
		String pathFiles = PropertyLoader.getProperty("inputpath") ;
        Path path = Paths.get(pathFiles); 

        try (Stream<Path> stream = Files.list(path)) {
            stream.filter(Files::isRegularFile)
                  .forEach(filex-> processFile(filex));
        } catch (IOException e) {
            System.out.println("Error reading path : " + e.getMessage());
        }
        
        fr.closeFile();
        ingester.flush();
        ingester.close();
        
        
        LOG.info("Tiempo de ejecucion " + (System.currentTimeMillis() - t0) + " msegs." );
        
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.exit(-1) ;
        
    }

	private static void processFile(Path filex) {
		
		LOG.info("processing file: " + filex.toString());
		
        try (Stream<String> lines = Files.lines(filex)) {
            lines.forEach(line -> processLine(line)); 
        } catch (IOException e) {
            System.err.println("Error reading file " + e.getMessage());
        }		
	}

	private static void processLine(String line) {

		counter ++ ;
		ObjStreaming objStr = null ;

		objStr = new CDRData(line);
		fr.writeToFile( objStr ) ;
		
		String jsonData;
		try {
			jsonData = mapper.writeValueAsString( ((CDRData) objStr).getCdrData() );
			BinaryData data = BinaryData.of(jsonData.getBytes(), ContentType.APPLICATION_JSON);
			ingester.add(op -> op 
			            .create(idx -> idx
			                .index(indexName)
			                .document(data)
			            )
			        );
		} catch (JsonProcessingException e) {
//			e.printStackTrace();
		}
		
		if ( counter%100000 == 0 ) {
			LOG.info("Processed " + counter + " records");
		}
	}
	
}
