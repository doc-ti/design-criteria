package edu.doc_ti.phivalidation.kafkastreams;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;


public class RTProcessorV2 implements Processor<String, String, String, String> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(RTProcessorV2.class);

	SimpleDateFormat sdf = new SimpleDateFormat("u-HH:mm") ;
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ") ;

	
    RestClientBuilder builderES ;
    String indexName = "lambda_felipe" ;

    ProcessorContext<String, String> _context ;
    
//    String baseID = UUID.randomUUID().toString() ;
    String baseID = "str_" + RandomStringUtils.randomAlphanumeric(12) ;
    
    int counterTags = 1 ;
    int counterRecords = 0 ;
    String currentTag = baseID + "_" + String.format( "%06d", counterTags) ;

    
    
    RestClient restClient = null ;
    BulkIngester<String> ingester = null ;
    
	@Override
    public void init(final ProcessorContext<String, String> context) {

		_context = context ;

//		sdf2.setTimeZone(TimeZone.getTimeZone("CET"));
//		
//		final CredentialsProvider credentialsProvider =
//			    new BasicCredentialsProvider();
//			credentialsProvider.setCredentials(AuthScope.ANY,
//				    new UsernamePasswordCredentials("lambda", "pass4lambda"));
////		    new UsernamePasswordCredentials("elastic", "4vd_2Ck9ZdbLueXrTlzc"));
//			
//
//			HttpHost auxH[] = new HttpHost[3];
//			auxH[0] = HttpHost.create("http://192.168.80.34:9201") ;
//			auxH[1] = HttpHost.create("http://192.168.80.35:9201") ;
//			auxH[2] = HttpHost.create("http://192.168.80.37:9201") ;
//					
//				restClient = RestClient
//					    .builder(auxH)
//					    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
//					        @Override
//					        public HttpAsyncClientBuilder customizeHttpClient(
//					                HttpAsyncClientBuilder httpClientBuilder) {
//					            return httpClientBuilder
//					                .setDefaultCredentialsProvider(credentialsProvider);
//					        }
//					    }).build();
//				
//				ElasticsearchTransport transport = new RestClientTransport(
//					    restClient, new JacksonJsonpMapper());
//
//					// And create the API client
//					ElasticsearchClient esClient = new ElasticsearchClient(transport);		
//				
//
//					BulkListener<String> listener = new BulkListener<String>() { 
//					    @Override
//					    public void beforeBulk(long executionId, BulkRequest request, List<String> contexts) {
//					    }
//
//					    @Override
//					    public void afterBulk(long executionId, BulkRequest request, List<String> contexts, BulkResponse response) {
//					        // The request was accepted, but may contain failed items.
//					        // The "context" list gives the file name for each bulk item.
//					        LOG.info ("Bulk request " + executionId + " completed, operations: " + request.operations().size());
//					        for (int i = 0; i < contexts.size(); i++) {
//					            BulkResponseItem item = response.items().get(i);
//					            if (item.error() != null) {
//					                // Inspect the failure cause
//					            	LOG.error("Failed to index file " + contexts.get(i) + " - " + item.error().reason());
//					            }
//					        }
//					    }
//
//					    @Override
//					    public void afterBulk(long executionId, BulkRequest request, List<String> contexts, Throwable failure) {
//					        // The request could not be sent
//					    	LOG.info("Bulk request " + executionId + " failed" +  failure);
//					    	failure.printStackTrace() ;
//					    }
//					};
//
//					ingester = BulkIngester.of(b -> b
//					    .client(esClient)
//					    .maxOperations(500)
//					    .flushInterval(1, TimeUnit.SECONDS)
//					    .listener(listener) 
//					);
//					
					
    	
//        context.schedule(Duration.ofSeconds(1), PunctuationType.STREAM_TIME, timestamp -> {
//            try (final KeyValueIterator<String, Integer> iter = kvStore.all()) {
//                while (iter.hasNext()) {
//                    final KeyValue<String, Integer> entry = iter.next();
//                    context.forward(new Record<>(entry.key, entry.value.toString(), timestamp));
//                }
//            }
//        });
//        kvStore = context.getStateStore("Counts");
        
    }

    @Override
    public void process(final Record<String, String> record) {
    	
    	counterRecords++ ;
    	
    	String input = record.value();
    	
		if ( counterRecords%500 == 0 ) {
			System.out.println ( (new Date().toString() ) + "VOY por " + counterRecords +  " / " + Thread.currentThread().getName() + " /" + input) ;
		}

    }


	@Override
    public void close() {
    	
		ingester.close();
		try {
			restClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	System.out.println("CLOSING PROCESSOR " + this.toString());
    }

//    private List<String> makeHttpRequest(String url) {
//        //System.out.println("Inicio de la peticion a las " + System.nanoTime());
//        HttpGet httpGet = new HttpGet(url);
//        try {
//            HttpResponse response = httpClient.execute(httpGet);
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode >= 200 && statusCode < 300) {
//                HttpEntity entity = response.getEntity();
//                //Formato JSON
//                String responseString = EntityUtils.toString(entity);
//                //System.out.println("Final de la petici�n a las " + System.nanoTime());
//                //Convierte responseString (JSON) al tipo de typeRef (List<String>)
//                return this.mapper.readValue(responseString, this.typeRef);
//            } else {
//                System.out.println("Error: Received HTTP status code " + statusCode + " for URL: " + url);
//                return Collections.emptyList();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Collections.emptyList();  // Devolver una lista vac�a en caso de error
//        }
//    }
}
