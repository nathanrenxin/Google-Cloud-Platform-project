package project.id2210.project_id2210;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.Tuple;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BucketSourceOption;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.CopyWriter;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.ComposeRequest;
import com.google.cloud.storage.Storage.CopyRequest;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MyStorage {
	
	private static final Map<String, StorageAction> ACTIONS = new HashMap<>();
	
    static double fileSize = 0;
	
	private abstract static class StorageAction<T> {

	    abstract void run(Storage storage, T arg) throws Exception;

	    abstract T parse(String... args) throws Exception;

	    protected String params() {
	      return "";
	    }
	}
	
	  private abstract static class BlobsAction extends StorageAction<BlobId[]> {

		    @Override
		    BlobId[] parse(String... args) {
		      if (args.length < 2) {
		        throw new IllegalArgumentException();
		      }
		      BlobId[] blobs = new BlobId[args.length - 1];
		      for (int i = 1; i < args.length; i++) {
		        blobs[i - 1] = BlobId.of(args[0], args[i]);
		      }
		      return blobs;
		    }

		    @Override
		    public String params() {
		      return "<bucket> <path>+";
		    }
		  }
	
	  private static class UploadAction extends StorageAction<Tuple<Path, BlobInfo>> {
		    @Override
		    public void run(Storage storage, Tuple<Path, BlobInfo> tuple) throws Exception {
		      run(storage, tuple.x(), tuple.y());
		    }

		    private void run(Storage storage, Path uploadFrom, BlobInfo blobInfo) throws IOException {
		      fileSize = Files.size(uploadFrom);
		      if ( fileSize > 1_000_000) {
		        // When content is not available or large (1MB or more) it is recommended
		        // to write it in chunks via the blob's channel writer.
		        try (WriteChannel writer = storage.writer(blobInfo)) {
		          byte[] buffer = new byte[1024];
		          try (InputStream input = Files.newInputStream(uploadFrom)) {
		            int limit;
		            while ((limit = input.read(buffer)) >= 0) {
		              try {
		                writer.write(ByteBuffer.wrap(buffer, 0, limit));
		              } catch (Exception ex) {
		                ex.printStackTrace();
		              }
		            }
		          }
		        }
		      } else {
		        byte[] bytes = Files.readAllBytes(uploadFrom);
		        // create the blob in one request.
		        storage.create(blobInfo, bytes);
		      }
		      System.out.printf("Blob was created with file %s%n", uploadFrom.getFileName().toString());
		    }

		    @Override
		    Tuple<Path, BlobInfo> parse(String... args) throws IOException {
		      if (args.length < 2 || args.length > 3) {
		        throw new IllegalArgumentException();
		      }
		      Path path = Paths.get(args[0]);
		      String contentType = Files.probeContentType(path);
		      String blob = args.length < 3 ? path.getFileName().toString() : args[2];
		      return Tuple.of(path, BlobInfo.newBuilder(args[1], blob).setContentType(contentType).build());
		    }

		    @Override
		    public String params() {
		      return "<local_file> <bucket> [<path>]";
		    }
		  }
	
	  private static class DownloadAction extends StorageAction<Tuple<BlobId, Path>> {

		    @Override
		    public void run(Storage storage, Tuple<BlobId, Path> tuple) throws IOException {
		      run(storage, tuple.x().getBucket(), tuple.x().getName(), tuple.y());
		    }

		    private void run(Storage storage, String bucketName, String objectName, Path downloadTo) throws IOException {
		      BlobId blobId = BlobId.of(bucketName, objectName);
		      Blob blob = storage.get(blobId);
		      if (blob == null) {
		        System.out.println("No such object");
		        return;
		      }
		      PrintStream writeTo = System.out;
		      if (downloadTo != null) {
		        writeTo = new PrintStream(new FileOutputStream(downloadTo.toFile()));
		      }
		      fileSize = blob.getSize();
		      if ( fileSize < 1_000_000) {
		        // Blob is small read all its content in one request
		        byte[] content = blob.getContent();
		        writeTo.write(content);
		      } else {
		        // When Blob size is big or unknown use the blob's channel reader.
		        try (ReadChannel reader = blob.reader()) {
		          WritableByteChannel channel = Channels.newChannel(writeTo);
		          ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
		          while (reader.read(bytes) > 0) {
		            bytes.flip();
		            channel.write(bytes);
		            bytes.clear();
		          }
		        }
		      }
		      if (downloadTo == null) {
		        writeTo.println();
		      } else {
		        writeTo.close();
		      }
		    }

		    @Override
		    Tuple<BlobId, Path> parse(String... args) {
		      if (args.length < 2 || args.length > 3) {
		        throw new IllegalArgumentException();
		      }
		      Path path;
		      if (args.length > 2) {
		        path = Paths.get(args[2]);
		        if (Files.isDirectory(path)) {
		          path = path.resolve(Paths.get(args[1]).getFileName());
		        }
		      } else {
		        path = null;
		      }
		      return Tuple.of(BlobId.of(args[0], args[1]), path);
		    }

		    @Override
		    public String params() {
		      return "<bucket> <path> [local_file]";
		    }
		  }
	  
	  private static class DeleteAction extends BlobsAction {
		    @Override
		    public void run(Storage storage, BlobId... blobIds) {
		      // use batch operation
		      List<Boolean> deleteResults = storage.delete(blobIds);
		      int index = 0;
		      for (Boolean deleted : deleteResults) {
		        if (deleted) {
		          // request order is maintained
		          System.out.printf("Blob %s was deleted%n", blobIds[index]);
		        }
		        index++;
		      }
		    }
		  }
	  
	  static {
		    ACTIONS.put("upload", new UploadAction());
		    ACTIONS.put("download", new DownloadAction());
		    ACTIONS.put("delete", new DeleteAction());
		  }
	  
  public static void main(String... args) throws Exception {


    // Instantiates a client
    Storage storage = StorageOptions.getDefaultInstance().getService();
    String self = "process"+args[0];
    args = Arrays.copyOfRange(args, 1, args.length);
 
    if (args.length < 1) {
        System.out.println("Missing required project id and action");
        //printUsage();
        return;
      }
    if (args[0].equals("createBucket")){
        // Creates the new bucket
    	String bucketName = args[1];
        Bucket bucket = storage.create(BucketInfo.newBuilder(bucketName).setStorageClass(StorageClass.REGIONAL).setLocation("europe-west2").build());
        System.out.printf("Bucket %s created.%n", bucket.getName());
    } else if (args[0].equals("deleteBucket")){
    	// delete the bucket
    	String bucketName = args[1];
    	storage.delete(bucketName);
        System.out.printf("Bucket %s deleted.%n", bucketName);
    } else {
      StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();
      StorageAction action;
      String actionName;
      if (args.length >= 2 && !ACTIONS.containsKey(args[0])) {
        actionName = args[1];
        optionsBuilder.setProjectId(args[0]);
        action = ACTIONS.get(args[1]);
        args = Arrays.copyOfRange(args, 2, args.length);
      } else {
        actionName = args[0];
        action = ACTIONS.get(args[0]);
        args = Arrays.copyOfRange(args, 1, args.length);
      }
      if (action == null) {
        System.out.println("Unrecognized action.");
        //printUsage();
        return;
      }
      Object arg;
      try {
        arg = action.parse(args);
      } catch (IllegalArgumentException ex) {
        System.out.printf("Invalid input for action '%s'. %s%n", actionName, ex.getMessage());
        System.out.printf("Expected: %s%n", action.params());
        return;
      } catch (Exception ex) {
        System.out.println("Failed to parse arguments.");
        ex.printStackTrace();
        return;
      }
      if(actionName.equals("upload")) {
    	  System.out.printf(self+" starts upload %n");
      } else if(actionName.equals("download")) {
    	  System.out.printf(self+" starts download %n");
      }
      Long start=System.currentTimeMillis();
      action.run(storage, arg);
      Long end=System.currentTimeMillis();
      if(actionName.equals("upload")) {
    	  System.out.printf(self+" Upload Time: %s%n", end-start);
    	  //System.out.printf(self+" Upload file size: %s%n", fileSize);
    	  System.out.printf(self+" Upload Speed(mb/s): %s%n", ((fileSize/1.024)/1024)/(end-start));
      } else if(actionName.equals("download")) {
    	  System.out.printf(self+" Download Time: %s%n", end-start);
    	  //System.out.printf(self+" Download file size: %s%n", fileSize);
    	  System.out.printf(self+" Download Speed(mb/s): %s%n", ((fileSize/1.024)/1024)/(end-start));
      }
      
    }
  }
}

