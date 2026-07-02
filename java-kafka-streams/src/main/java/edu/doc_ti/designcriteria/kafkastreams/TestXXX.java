package edu.doc_ti.designcriteria.kafkastreams;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class TestXXX {
	public static void main(String[] args) {
		
		final CredentialsProvider credentialsProvider =
	    new BasicCredentialsProvider();
	credentialsProvider.setCredentials(AuthScope.ANY,
		    new UsernamePasswordCredentials("lambda", "pass4lambda"));
//    new UsernamePasswordCredentials("elastic", "4vd_2Ck9ZdbLueXrTlzc"));
			
		RestClient restClient = RestClient
			    .builder(HttpHost.create("http://192.168.80.34:9201"))
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


			Reader input = new StringReader(
				    "{'@timestamp': '2022-04-08T13:55:32Z', 'level': 'warn', 'message': 'Some log message'}"
				    .replace('\'', '"'));

				IndexRequest<JsonData> request = IndexRequest.of(i -> i
				    .index("borrame3")
				    .withJson(input)
				);
				

				IndexResponse response;
				try {
					response = esClient.index(request);
					System.out.println("Indexed with version " + response.version());
					
				} catch (ElasticsearchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

//			Product product = new Product("bk-1", "City bike", 333, 123.0);
//			
//			IndexRequest<Product> request2 = IndexRequest.of(i -> i
//				    .index("borrame4")
//				    .document(product)
//				);
//
//				IndexResponse response2;
//				try {
//					response2 = esClient.index(request2);
//					System.out.println ("Indexed with version " + response2.version());
//				} catch (ElasticsearchException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			
			
			
			BulkListener<String> listener = new BulkListener<String>() { 
			    @Override
			    public void beforeBulk(long executionId, BulkRequest request, List<String> contexts) {
			    }

			    @Override
			    public void afterBulk(long executionId, BulkRequest request, List<String> contexts, BulkResponse response) {
			        // The request was accepted, but may contain failed items.
			        // The "context" list gives the file name for each bulk item.
			        System.out.println ("Bulk request " + executionId + " completed");
			        for (int i = 0; i < contexts.size(); i++) {
			            BulkResponseItem item = response.items().get(i);
			            if (item.error() != null) {
			                // Inspect the failure cause
			            	System.out.println("Failed to index file " + contexts.get(i) + " - " + item.error().reason());
			            }
			        }
			    }

			    @Override
			    public void afterBulk(long executionId, BulkRequest request, List<String> contexts, Throwable failure) {
			        // The request could not be sent
			    	System.out.println("Bulk request " + executionId + " failed" +  failure);
			    }
			};

			BulkIngester<String> ingester = BulkIngester.of(b -> b
			    .client(esClient)
			    .maxOperations(100)
			    .flushInterval(1, TimeUnit.SECONDS)
			    .listener(listener) 
			);

//			for (int n=1 ; n<= 20 ; n++) {
//				Product product = new Product("bk-1", "City bike", n, 123.0);
//
//			    ingester.add(op -> op
//			            .index(idx -> idx
//			                .index("borrame5")
//			                .document(product)
//			            )
////			            ,file.getName() 
//			        );
//
//			}

			ingester.close();

			
//			Product product = new Product("bk-1", "City bike", 333, 123.0);
			
			String product = "xxx" ;
			
			IndexRequest<String> request2 = IndexRequest.of(i -> i
				    .index("borrame4")
				    .document(product)
				);

				IndexResponse response2;
				try {
					response2 = esClient.index(request2);
					System.out.println ("Indexed with version " + response2.version());
				} catch (ElasticsearchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
				try {
//					transport.close();
					restClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	}
}
