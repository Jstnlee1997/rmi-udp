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

public class CentralServer implements ICentralServer {

  private ILocationSensor locationSensor;

  List<MessageInfo> receivedMessages;
  int msgCounter;
  int msgTot;

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
    ICentralServer stub = (ICentralServer) UnicastRemoteObject.exportObject(cs, 0);
    /* create (or Locate) Registry */
    Registry registry = LocateRegistry.createRegistry(5000);

    /* Bind the remote object's stub in the registry */
    registry.rebind("ICentralServer", stub);
    System.out.println("Central Server is running...");
  }


  @Override
  public void receiveMsg(MessageInfo msg) {
    /* print messages as received */
    System.out.println("[Central Server] Received message " + (msg.getMessageNum()) + " out of " +
        msg.getTotalMessages() + ". Measure = " + msg.getMessage());

    /* if this is the first message, reset counter and initialise data structure. */
    if (msg.getMessageNum() == 1) {
      msgCounter = 0;
      msgTot = msg.getTotalMessages();
      receivedMessages = new ArrayList<>();
    }

    /* save current message */
    receivedMessages.add(msg);
    msgCounter++;

    /* if I received everything that there was to be received, prints stats. */
    if (msg.getMessageNum() == msgTot) printStats();
  }

  public void printStats() {
    /* find out how many messages were missing */
    int numMissing = msgTot - receivedMessages.size();

    /* print stats */
    System.out.printf("Total Missing Messages = %d out of %d\n", numMissing, msgTot);

    /* print the location of the Field Unit that sent the messages */
    try {
      printLocation();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    /* now re-initialise data structures for next time */
    receivedMessages = null;

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
