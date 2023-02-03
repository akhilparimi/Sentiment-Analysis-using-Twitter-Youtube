import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class YouTubeComments {
	
    private static final String DEVELOPER_KEY = "AIzaSyA99jF5nH7P2tWr3hFwXdVrDYziZL7kTU0";

    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
    
    @SuppressWarnings("deprecation")
	public static void main(String[] args)
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.CommentThreads.List request = youtubeService.commentThreads()
            .list("snippet");
        request.setMaxResults((long) 100);
        CommentThreadListResponse response = request.setKey(DEVELOPER_KEY)
            .setVideoId("kJQP7kiw5Fk")
            .execute();
        //System.out.println(response.getPageInfo());
        int count=0;
        com.mongodb.MongoClient mongoClient= new com.mongodb.MongoClient("localhost", 27017);				
        DB db = mongoClient.getDB("YouTubeDB");
        DBCollection collection = db.getCollection("YTComments");
        
        DBObject dbObject;
        while(count<10000) {
        	count= count+response.getItems().size();
        	for(int j=0;j<response.getItems().size();j++) {
            	//System.out.println(response.getItems().get(j).getSnippet().getTopLevelComment().getSnippet().getTextOriginal());
            	dbObject = (DBObject)JSON.parse(response.getItems().get(j).getSnippet().getTopLevelComment().getSnippet().getTextOriginal());            	
            	if(dbObject!=null) {
					collection.insert(dbObject);
				}
            }
        	request.setPageToken(response.getNextPageToken());
        	response = request.setKey(DEVELOPER_KEY)
                    .setVideoId("kJQP7kiw5Fk")
                    .execute();
        	//System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
        }
        //System.out.println(count); 
        mongoClient.close();
    }
}