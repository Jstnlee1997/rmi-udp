package centralserver;

import common.*;
import field.ILocationSensor;
/*
 * Created on Feb 2022
 */
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class CentralServer implements ICentralServer {

  private ILocationSensor locationSensor;

  List<MessageInfo> receivedMessages;
  List<Integer> missingMessages;

  int msgCounter = 0;
  int msgTot;

  private static long startTime = 0;
  private static long endTime = 0;

  protected CentralServer() throws RemoteException {

    super();
    /* initialise Array receivedMessages*/
    receivedMessages = null;
  }

  public static void main(String[] args) throws RemoteException {
    /* initialise central server */
    ICentralServer cs = new CentralServer();
    System.setProperty("java.security.policy", "file:./policy\n");

    /* configure security manager */
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new RMISecurityManager());
    }

    /* create central server stub */
    ICentralServer stub =
        (ICentralServer) UnicastRemoteObject.exportObject(cs, 0);
    /* create (or Locate) Registry */
    Registry registry = LocateRegistry.createRegistry(5000);

    /* Bind the remote object's stub in the registry */
    registry.rebind("ICentralServer", stub);
    System.out.println("Central Server is running...");
  }


  @Override
  public void receiveMsg(MessageInfo msg) {
    /* print messages as received */
    System.out.println("[Central Server] Received message "
        + (msg.getMessageNum()) + " out of " +
        msg.getTotalMessages() + ". Measure = " + msg.getMessage());

    /* if this is the first message, initialise data structures. */
    if (msgCounter == 0) {
      msgTot = msg.getTotalMessages();
      receivedMessages = new ArrayList<>();
      missingMessages = new ArrayList<>();


      // Start counting duration of receiving messages
      startTime = System.nanoTime();

      // Check if first message is not message number 1
      if (msg.getMessageNum() != 1) {
        // Add all missing messages before this into missingMessages
        for (int i = 1; i < msg.getMessageNum(); i++) {
          missingMessages.add(i);
        }
      }
    }

    /* Check if there are missing messages */
    // Compare current message number with last message in receivedMessages
    if (receivedMessages.size() > 0
        && msg.getMessageNum()
        != receivedMessages.get(msgCounter - 1).getMessageNum() + 1) {
      for (int i = receivedMessages.get(msgCounter - 1).getMessageNum() + 1;
           i < msg.getMessageNum(); i++) {
        missingMessages.add(i);
      }
    }

    /* save current message */
    receivedMessages.add(msg);
    msgCounter++;

    /* Print stats if all messages have been received */
    if (msg.getMessageNum() == msgTot) {
      // Record the end time after receiving last message
      endTime = System.nanoTime();
      printStats();

      // Reset message counter
      msgCounter = 0;
    }
  }

  public void printStats() {
    /* find out how many messages were missing */
    int numberOfMissingMessages = msgTot - receivedMessages.size();

    /* print stats */
    System.out.printf("Total Missing Messages = %d out of %d\n",
        numberOfMissingMessages, msgTot);

    /* print out message numbers that were lost */
    if (numberOfMissingMessages > 0) {
      System.out.printf("Missing message numbers are: %s \n",
          missingMessages);
    }

    /* print the location of the Field Unit that sent the messages */
    try {
      printLocation();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    /* now re-initialise data structures for next time */
    receivedMessages = null;
    missingMessages = null;

    // Print duration for communication
    long duration = (endTime - startTime) / 1000000;  // get milliseconds.
    System.out.printf("Duration for RMI communication is: %d milliseconds\n",
        duration);

  }

  @Override
  public void setLocationSensor(ILocationSensor locationSensor) throws RemoteException {

    /* set location sensor */
    this.locationSensor = locationSensor;
    System.out.println("Location Sensor Set");
  }

  public void printLocation() throws RemoteException {
    /* print location on screen from remote reference */
    try {
      System.out.printf("[Field Unit] Current Location: lat = %f long = %f\n",
          locationSensor.getCurrentLocation().getLatitude(),
          locationSensor.getCurrentLocation().getLongitude());
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
}
