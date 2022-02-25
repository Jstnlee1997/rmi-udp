package centralserver;

import common.*;
import field.ILocationSensor;
/*
 * Created on Feb 2022
 */
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.rmi.Naming;

/* You can add/change/delete class attributes if you think it would be
 * appropriate.
 * You can also add helper methods and change the implementation of those
 * provided if you think it would be appropriate, as long as you DO NOT
 * CHANGE the provided interface.
 */
/* TODO extend appropriate classes and implement the appropriate interfaces */
public class CentralServer implements ICentralServer {

  private ILocationSensor locationSensor;

  List<MessageInfo> receivedMessages;
  int msgCounter;
  int msgTot;

  protected CentralServer() throws RemoteException {
    super();

    /* TODO: Initialise Array receivedMessages <<-- I DON'T SEE WHY WE DO THAT HERE*/

  }

  public static void main(String[] args) throws RemoteException {
    ICentralServer cs = new CentralServer();

    /* If you are running the program within an IDE instead of using the
     * provided bash scripts, you can use the following line to set
     * the policy file
     */

    System.setProperty("java.security.policy", "file:./policy\n");

    /* TODO: Configure Security Manager */
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new RMISecurityManager());
    }

    /* TODO: Create (or Locate) Registry */

    ICentralServer stub = (ICentralServer) UnicastRemoteObject.exportObject(cs, 0);
    Registry registry = LocateRegistry.createRegistry(5000);

    /* TODO: Bind to Registry */
    // Bind the remote object's stub in the registry
    // CHECK THIS
    registry.rebind("ICentralServer", stub);
//      Naming.rebind("CentralServer", )

    System.out.println("Central Server is running...");

  }


  @Override
  public void receiveMsg(MessageInfo msg) {
    System.out.println("[Central Server] Received message " + (msg.getMessageNum()) + " out of " +
        msg.getTotalMessages() + ". Measure = " + msg.getMessage());

    /* TODO: If this is the first message, reset counter and initialise data structure. */
    if (msg.getMessageNum() == 1)
    {
      msgCounter = 0;
      msgTot = msg.getTotalMessages();
      receivedMessages = new ArrayList<>();
    }

    /* TODO: Save current message */
    receivedMessages.add(msg);
    msgCounter++;

    /* TODO: If I received everything that there was to be received, prints stats. */
    // THIS NEEDS TO CHANGE
    if(msgCounter == msgTot)
    {
      printStats();
    }
  }

  public void printStats() {
    /* TODO: Find out how many messages were missing */
    int numMissing = msgTot - receivedMessages.size();

    /* TODO: Print stats (i.e. how many message missing? */
    System.out.printf("Total Missing Messages = %d out of %d", numMissing, msgTot);

    /* TODO: Print the location of the Field Unit that sent the messages */
    /* NOT SURE HOW TO DEAL WITH LOCATION STUFF */
    try {
      printLocation();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    /* TODO: Now re-initialise data structures for next time */
    receivedMessages = null;

  }

  @Override
  public void setLocationSensor(ILocationSensor locationSensor) throws RemoteException {

    /* TODO: Set location sensor */
    this.locationSensor = locationSensor;
    System.out.println("Location Sensor Set");
  }

  public void printLocation() throws RemoteException {
    /* TODO: Print location on screen from remote reference */
    try {
      System.out.printf("[Field Unit] Current Location: lat = %f long = %f",
          locationSensor.getCurrentLocation().getLatitude(), locationSensor.getCurrentLocation().getLongitude());
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
}
