package project.id2210.project_id2210;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.*;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.compute.*;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;
//import com.google.cloud.compute.deprecated.ComputeOptions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class ListInstances {

  private static final String APPLICATION_NAME = "Test";
  private static final String projectId = "id2210-199507";
  private static final String zoneName = "us-east1-b";
  private static final java.io.File DATA_STORE_DIR =  new java.io.File(System.getProperty("user.home"), ".store/storage");

  private static FileDataStoreFactory dataStoreFactory;
  private static HttpTransport httpTransport;
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Arrays.asList(ComputeScopes.COMPUTE_READONLY);

  public static void main(String[] args) {
    // Start Authorisation process
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
      // Authorisation
      Credential credential = authorize();

      // Create compute engine object for listing instances
      Compute compute = new Compute.Builder(
          httpTransport, JSON_FACTORY, null).setApplicationName(APPLICATION_NAME)
          .setHttpRequestInitializer(credential).build();

      // List out instances
      printInstances(compute, projectId);
      // Success!
      return;
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }

  public static Credential authorize() throws Exception {
    // initialise client secrets object
    GoogleClientSecrets clientSecrets;
    // load client secrets
    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(
        ListInstances.class.getResourceAsStream("/client_secrets.json")));
   
    // set up authorisation code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(dataStoreFactory)
        .build();
    // authorise
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }


  public static void printInstances(Compute compute, String projectId) throws IOException, InterruptedException {
    System.out.println("================== Listing Compute Engine Instances ==================");
    Compute.Instances.List instances = compute.instances().list(projectId, zoneName);
    InstanceList list = instances.execute();
    if (list.getItems() == null) {
      System.out.println("No instances found.\nCreating new instance...");
      	CreateInstance.createInstance();
    } else {
      for (Instance instance : list.getItems()) {
        System.out.println(instance.toPrettyString());
      }
      new  CreateInstance();
    }
  }
}
