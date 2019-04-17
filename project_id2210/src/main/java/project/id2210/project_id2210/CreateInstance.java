package project.id2210.project_id2210;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.deprecated.AttachedDisk;
import com.google.cloud.compute.deprecated.Compute;
import com.google.cloud.compute.deprecated.ComputeOptions;
import com.google.cloud.compute.deprecated.ImageId;
import com.google.cloud.compute.deprecated.Instance;
import com.google.cloud.compute.deprecated.InstanceId;
import com.google.cloud.compute.deprecated.InstanceInfo;
import com.google.cloud.compute.deprecated.MachineTypeId;
import com.google.cloud.compute.deprecated.NetworkId;
import com.google.cloud.compute.deprecated.NetworkInterface;
import com.google.cloud.compute.deprecated.Operation;


public class CreateInstance {

  public static void main(String... args) throws InterruptedException, TimeoutException {
	  createInstance();
  }
  public static void createInstance() throws InterruptedException{
	   ComputeOptions.Builder optionsBuilder = ComputeOptions.newBuilder();
	    optionsBuilder.setProjectId("id2210-199507");
	    try {
			optionsBuilder.setCredentials(GoogleCredentials.fromStream(new 
			         FileInputStream("resources/serviceAccount.json"))).build();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Compute compute = optionsBuilder.build().getService();

	    ImageId imageId = ImageId.of("debian-cloud", "debian-8-jessie-v20160329");
	    NetworkId networkId = NetworkId.of("default");
	    AttachedDisk attachedDisk = AttachedDisk.of(AttachedDisk.CreateDiskConfiguration.of(imageId));
	    NetworkInterface networkInterface = NetworkInterface.of(networkId);
	    InstanceId instanceId = InstanceId.of("us-central1-a", "remotely-created-instance");
	    MachineTypeId machineTypeId = MachineTypeId.of("us-central1-a", "n1-standard-1");
	    Operation operation = compute.create(InstanceInfo.of(instanceId, machineTypeId, attachedDisk, networkInterface));
	    operation = operation.waitFor();
	    System.out.println("Instance created!");
	    if (operation.getErrors() == null) {
	      // use instance
	      Instance instance = compute.getInstance(instanceId);
	    } 
  }
}