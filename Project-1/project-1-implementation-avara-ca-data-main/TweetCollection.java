import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class TweetCollection {

	public static void main(String[] args) {
		
		//Authenticating using bearer token from the developer account
		String bToken = System.getenv("BEARER_TOKEN");
		if(bToken!=null) {
			//System.out.println(bToken);
			try {
				//set up request
				HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
				URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/sample/stream#lang=en");
				
				//Authorization
				HttpGet httpGet = new HttpGet(uriBuilder.build());
				httpGet.setHeader("Authorization", String.format("Bearer %s", bToken));
				httpGet.setHeader("Content-Type", "application/json");
				
				//response
				HttpResponse response = httpClient.execute(httpGet);
				
				//push the data from response to mongodb
				if(response.getEntity()!= null) {
					BufferedReader br= new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
					String line= br.readLine();	
					
					com.mongodb.MongoClient mongoClient= new com.mongodb.MongoClient("localhost", 27017);					
			        DB db = mongoClient.getDB("twitterDB");
			        DBCollection collection = db.getCollection("tweets");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}			
		}else 
			System.out.println("There is a problem in Authentication. Please check your Bearer Token!");
	}
}
