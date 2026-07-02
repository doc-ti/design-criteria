package edu.doc_ti.phivalidation.common.objects;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;


public class MyESUtils {
	private static final Logger LOG = LoggerFactory.getLogger(MyESUtils.class);

	public static final String PROP_ESHOSTS  = "bulk.hosts" ;
	public static final String PROP_USER    = "bulk.user" ;
	public static final String PROP_PASSWD  = "bulk.passwd" ;
	public static final String PROP_THREADS = "bulk.threads" ;
	public static final String PROP_SECONDS = "bulk.seconds" ;
	public static final String PROP_BYTES   = "bulk.bytes" ;
	public static final String PROP_BATCH   = "bulk.batch" ;
	
	@SuppressWarnings("rawtypes")
	public static BulkIngester<String> init(Map props) {

		Properties properties = new Properties();
		properties.putAll(props);
		
		return init(properties);
		
	}

    @SuppressWarnings("removal")
	public static BulkIngester<String> init(Properties props) {
        RestClient restClient = null ;
        BulkIngester<String> ingester = null ;

    	String VAL_ESHOSTS  = null ;
    	String VAL_USER    = "" ;
    	String VAL_PASSWD  = "" ;
    	int    VAL_THREADS = 1 ;
    	int    VAL_SECONDS = 30 ;
    	int    VAL_BYTES   = 1000000 ;
    	int    VAL_BATCH   = 1000  ;
 	
    	VAL_ESHOSTS = props.getProperty(PROP_ESHOSTS) ;
    	if ( VAL_ESHOSTS == null || VAL_ESHOSTS == "" ) {
    		System.err.println("Needed parameter " + PROP_ESHOSTS);
    		LOG.error("Needed parameter " + PROP_ESHOSTS);
    		return null ;
    	}

    	VAL_USER   = props.getProperty(PROP_USER, "") ;
    	VAL_PASSWD = props.getProperty(PROP_PASSWD, "") ;
    	
    	try {
    		VAL_THREADS = Integer.parseInt(props.getProperty(PROP_THREADS)) ;
    	} catch (Exception ex) {}
    	try {
    		VAL_SECONDS = Integer.parseInt(props.getProperty(PROP_SECONDS)) ;
    	} catch (Exception ex) {}
    	try {
    		VAL_BYTES = Integer.parseInt(props.getProperty(PROP_BYTES)) ;
    	} catch (Exception ex) {}
    	try {
    		VAL_BATCH = Integer.parseInt(props.getProperty(PROP_BATCH)) ;
    	} catch (Exception ex) {}
    	
    	
		final CredentialsProvider credentialsProvider =
			    new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY,
				    new UsernamePasswordCredentials(VAL_USER, VAL_PASSWD));
//		    new UsernamePasswordCredentials("elastic", "4vd_2Ck9ZdbLueXrTlzc"));
			

			String[] hosts = VAL_ESHOSTS.split(",") ;
			HttpHost auxH[] = new HttpHost[hosts.length];
			
			for (int pos=0; pos<hosts.length; pos++) {
				auxH[pos] = HttpHost.create(hosts[pos]) ;
			}
					
				restClient = RestClient
					    .builder(auxH)
					    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
					        @Override
					        public HttpAsyncClientBuilder customizeHttpClient(
					                HttpAsyncClientBuilder httpClientBuilder) {
					            return httpClientBuilder
					                .setDefaultCredentialsProvider(credentialsProvider);
					        }
					    }).build();
				
				ElasticsearchTransport transport = new RestClientTransport(
					    restClient, new JacksonJsonpMapper());

					// And create the API client
					ElasticsearchClient esClient = new ElasticsearchClient(transport);		
				

					BulkListener<String> listener = new BulkListener<String>() { 
					    @Override
					    public void beforeBulk(long executionId, BulkRequest request, List<String> contexts) {
					    }

					    @Override
					    public void afterBulk(long executionId, BulkRequest request, List<String> contexts, BulkResponse response) {
					        // The request was accepted, but may contain failed items.
					        // The "context" list gives the file name for each bulk item.
					        LOG.info ("Bulk request " + executionId + " completed, operations: " + request.operations().size());
					        for (int i = 0; i < contexts.size(); i++) {
					            BulkResponseItem item = response.items().get(i);
					            if (item.error() != null) {
					                // Inspect the failure cause
					            	LOG.error("Failed to index file " + contexts.get(i) + " - " + item.error().reason());
					            }
					        }
					    }

					    @Override
					    public void afterBulk(long executionId, BulkRequest request, List<String> contexts, Throwable failure) {
					        // The request could not be sent
					    	LOG.info("Bulk request " + executionId + " failed" +  failure);
					    	failure.printStackTrace() ;
					    }
					};

			Integer valSeconds = new Integer(VAL_SECONDS);
			Integer valBytes   = new Integer(VAL_BYTES);
			Integer valBatch   = new Integer(VAL_BATCH);
			Integer valThreads = new Integer(VAL_THREADS);
			
			ingester = BulkIngester.of(b -> b
			    .client(esClient)
			    .maxSize(valBytes)
			    .maxOperations(valBatch)
			    .maxConcurrentRequests(valThreads)
			    .flushInterval(valSeconds, TimeUnit.SECONDS)
			    .listener(listener) 
			);
			
			
			return ingester ;
    }


    public static void close(BulkIngester<String> ingester) {
    	ingester.flush();
		ingester.close();
    	System.out.println("CLOSING PROCESSOR " + ingester.toString());
    }


}
