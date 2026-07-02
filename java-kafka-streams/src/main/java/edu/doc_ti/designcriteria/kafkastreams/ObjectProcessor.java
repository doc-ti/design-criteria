package edu.doc_ti.designcriteria.kafkastreams;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.util.BinaryData;
import co.elastic.clients.util.ContentType;
import edu.doc_ti.designcriteria.common.objects.CDRData;
import edu.doc_ti.designcriteria.common.objects.CDRDataReducido;
import edu.doc_ti.designcriteria.common.objects.MyESUtils;
import edu.doc_ti.designcriteria.common.objects.MyFileWriter;
import edu.doc_ti.designcriteria.common.objects.ObjStreaming;
import edu.doc_ti.designcriteria.common.objects.PropertyLoader;
import edu.doc_ti.designcriteria.common.objects.SOBData;
import edu.doc_ti.designcriteria.common.objects.SOBPingData;


public class ObjectProcessor implements Processor<String, String, String, String> {
	private static final Logger LOG = LoggerFactory.getLogger(ObjectProcessor.class);

	private static final int MODE_CDRS = 1;
	private static final int MODE_BITSTREAM = 2;
	private static final int MODE_CDRS_REDUCIDO = 3;
	private static final int MODE_BITSTREAM_PING = 4;

	
    private CqlSession session;
    private String keyspace = "my_keyspace";
    private List<String> columns = new ArrayList<>();	
    private BatchStatementBuilder batchCassandra = null ;	
    private PreparedStatement ps = null ;
    
    RestClientBuilder builderES ;
    String indexName = "index_test" ;

    ProcessorContext<String, String> _context ;
    
//    String baseID = UUID.randomUUID().toString() ;
    String baseID = "str_" + RandomStringUtils.randomAlphanumeric(12) ;
    
    int counterTags = 1 ;
    int counterRecords = 0 ;
    String currentTag = baseID + "_" + String.format( "%06d", counterTags) ;

//    RestClient restClient = null ;
    BulkIngester<String> ingester = null ;
    MyFileWriter fr ;
    private ObjectMapper mapper  ;

	private boolean forwardDataToTopic = false ;

	private int MODE = -1 ;

	private boolean spoolInfoFichero;
	private boolean loadIntoCassandra = false ;

	private double percentErrores = 0 ;
    
	@Override
    public void init(final ProcessorContext<String, String> context) {

		_context = context ;
		Properties props = PropertyLoader.getProps() ;
		
		MODE = MODE_CDRS ;
		String filePrefix = "" ;
		String aux = PropertyLoader.getProperty("mode", "cdrs") ;

		if ( aux.compareToIgnoreCase("cdrs") == 0 ) {
			MODE = MODE_CDRS ;
			filePrefix = "kafka-streams-cdr" ;
		}
		if ( aux.compareToIgnoreCase("bitstream") == 0 ) {
			MODE = MODE_BITSTREAM ;
			filePrefix = "kafka-streams-bitstream" ;
		}
		if ( aux.compareToIgnoreCase("cdrs_red") == 0 ) {
			MODE = MODE_CDRS_REDUCIDO ;
			filePrefix = "kafka-streams-cdr-reducido" ;
		}
		if ( aux.compareToIgnoreCase("bitstream_ping") == 0 ) {
			MODE = MODE_BITSTREAM_PING ;
			filePrefix = "kafka-streams-bitstream-ping" ;
		}
		
		fr = new MyFileWriter(
				PropertyLoader.getProperty("pathOutFiles", ".") , 
				PropertyLoader.getProperty("nameOutFiles", filePrefix), 
				PropertyLoader.getIntProperty("recordsOutFiles", 10000) , 
				false) ;

		indexName = PropertyLoader.getProperty("indexName", indexName) ;
		mapper = new ObjectMapper();
		
//		ingester = MyESUtils.init(props) ;
		
		forwardDataToTopic  = (PropertyLoader.getProperty("topicOut" ) != null )  ;
		
		spoolInfoFichero = (PropertyLoader.getProperty("info_fichero", "false").compareToIgnoreCase("true") == 0 ); 

		loadIntoCassandra = (PropertyLoader.getProperty("load_cassandra", "false").compareToIgnoreCase("true") == 0 ); 


		try {
				percentErrores  = Double.parseDouble(PropertyLoader.getProperty("%discards", "0") ); 
		} catch (Exception ex) {}
		
		LOG.info("%records discarded: {}", percentErrores);


		if ( loadIntoCassandra ) {
			prepareCassandra() ;
		}
	}

	
	private void prepareCassandra() {
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("10.0.0.3", 9042))
                .withLocalDatacenter("dc1")
                .withKeyspace(keyspace)
                .build();
        
        if (session != null ) {
        	getTableColumns("usuarios") ;
        }
        
        StringBuilder query = new StringBuilder("INSERT INTO ")
                .append("usuarios").append(" (")
                .append(String.join(", ", columns))
                .append(") VALUES (")
                .append(String.join(", ", Collections.nCopies(columns.size(), "?")))
                .append(")");

        ps = session.prepare(query.toString());
			
	}


	long lastRecordPrinted = 0 ;
	int countRegsFile = 0 ;
	long tsStartFile = 0 ;
	String lastFile = "" ;
    @Override
    public void process(final Record<String, String> record) {
    	
    	counterRecords++ ;
    	
    	String input = record.value();
    	
		if ( counterRecords%10000 == 0 ) {
			LOG.info ( (new Date().toString() ) + "VOY por " + counterRecords +  " / " + Thread.currentThread().getName() ) ;
		}

		ObjStreaming objStr = null ;
		switch (MODE) {
			case MODE_CDRS :
				objStr = new CDRData(input);
				
				if ( spoolInfoFichero ) {
					String currentFile = ((CDRData) objStr).getCdrData().get("field_46").toString() ;
					if ( currentFile.compareTo(lastFile) != 0 ) {
						if ( lastFile.length() > 0 ) {
							LOG.info("INFO_FILE:" + lastFile + " " + ( System.currentTimeMillis() - tsStartFile ) + " " + countRegsFile) ;
						}
						lastFile = currentFile;
						tsStartFile = System.currentTimeMillis();
						countRegsFile = 0 ;
					}
					countRegsFile++ ;
				}
				
				break ;
			case MODE_BITSTREAM :
				objStr = new SOBData(input);
				break ;
			case MODE_CDRS_REDUCIDO :
				objStr = new CDRDataReducido(input);
				break ;
			case MODE_BITSTREAM_PING :
				objStr = new SOBPingData(input);
				break ;
		}
		
		if ( counterRecords%10000 == 1 || System.currentTimeMillis()- lastRecordPrinted > 5000 ) {
			LOG.info( "DATA: " + objStr.toDataString() ) ;
			if (MODE == MODE_CDRS ) {
				LOG.info( "DATA: " + ((CDRData) objStr).getCdrData() ) ;
			}
			lastRecordPrinted = System.currentTimeMillis() ;
		}
		
		fr.writeToFile( objStr ) ;
		
		if ( forwardDataToTopic ) {
			switch (MODE) {
				case MODE_CDRS :
					try {
						 if ( (double) ((CDRData) objStr).getCdrData().get("duracion") > 180 ) {
							String fwData1 = ((CDRData) objStr).getReducedCDR()  ; 
							_context.forward(new Record<>(record.key(), fwData1, record.timestamp()));
						 }
					} catch (Exception e) {}
					
					break ;
				case MODE_BITSTREAM:
					for ( int pos = 0 ; pos < ((SOBData) objStr).arrPings.size(); pos++ ) {
						SOBPingData objAux = ((SOBData) objStr).arrPings.get(pos) ;
						String fwData2 = objAux.toDataString() ;
						_context.forward(new Record<>(record.key(), fwData2, record.timestamp()));
					}
					break ;
				}
		}

        try {
        	String jsonData = "{\"vacio\" : \"vacio\" }" ;
        	if ( ingester != null ) {
        		switch (MODE) {
					case MODE_CDRS :
						jsonData = mapper.writeValueAsString( ((CDRData) objStr).getCdrData() ) ;
						break ;
					case MODE_BITSTREAM :
						jsonData = objStr.toJSonString() ;
						break ;
					case MODE_CDRS_REDUCIDO :
						jsonData = mapper.writeValueAsString( ((CDRDataReducido) objStr).getCdrData() ) ;
						break ;
					case MODE_BITSTREAM_PING :
						jsonData = mapper.writeValueAsString( ((SOBPingData) objStr).getData() ) ;
						break ;
        		}
        	}

        	Map<String, Object> auxMap =null ;
        	if ( session != null ) {
        		switch (MODE) {
					case MODE_CDRS :
						auxMap = ( ((CDRData) objStr).getCdrData() ) ;
						break ;
//					case MODE_BITSTREAM :
//						jsonData = objStr.toJSonString() ;
//						break ;
					case MODE_CDRS_REDUCIDO :
						auxMap = ( ((CDRDataReducido) objStr).getCdrData() ) ;
						break ;
					case MODE_BITSTREAM_PING :
						auxMap = ( ((SOBPingData) objStr).getData() ) ;
						break ;
        		}
        	}
    		
    		if ( Math.random() >= percentErrores ) {
    			
    			if ( ingester != null ) {
					BinaryData data = BinaryData.of(jsonData.getBytes(), ContentType.APPLICATION_JSON);
					ingester.add(op -> op 
					            .create(idx -> idx
					                .index(indexName)
					                .document(data)
					            )
					        );
    			}
    			
    			if ( loadIntoCassandra && session != null && auxMap != null ) {
    				loadIntoCassandra(auxMap) ;
    			}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }


    int countInsertsCassandra = 0 ;
	private synchronized void loadIntoCassandra(Map<String, Object> record ) {

		record.put("uuid", Uuids.timeBased() ) ;
		
		if ( batchCassandra == null) {
	        batchCassandra = BatchStatement.builder(DefaultBatchType.LOGGED);
		}

		List<Object> valoresInsert = new ArrayList<>();
		for ( String col: columns ) {
        	valoresInsert.add( record.get(col.toLowerCase()) ) ;
        }

        BoundStatement bs = ps.bind(valoresInsert.toArray());
        batchCassandra.addStatement(bs);

        countInsertsCassandra++ ;
        if ( countInsertsCassandra  >= 10 ) {
            session.execute(batchCassandra.build());
            batchCassandra = null ;
            countInsertsCassandra = 0 ;
        }

	}


	@Override
    public void close() {
    	
	   	System.out.println("CLOSING PROCESSOR " + this.toString());

	   	if (ingester != null ) {
	   		ingester.flush();
	   	}

	   	fr.closeFile();
		try {
			ingester.close();
//			restClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	System.out.println("CLOSING PROCESSOR " + this.toString());
    }

    public void getTableColumns(String tableName) {
        Optional<TableMetadata> tableMetadata = session.getMetadata()
                .getKeyspace(keyspace)
                .flatMap(ks -> ks.getTable(tableName));

//        if (tableMetadata.isEmpty()) {
//            throw new IllegalArgumentException("Tabla no encontrada: " + tableName);
//        }

        for (ColumnMetadata col : tableMetadata.get().getColumns().values()) {
            columns.add(col.getName().asInternal());
            System.out.println( col.getName().asInternal() ) ;
        }
        
    }
}
