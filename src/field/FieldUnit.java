package field;
/*
 * Created on Feb 2022
 */

import centralserver.ICentralServer;
import common.MessageInfo;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldUnit implements IFieldUnit {
  private ICentralServer central_server;
  private LocationSensor locationSensor;

  /* QUESTION: Could you discuss in one line of comment what do you think can be
   * an appropriate size for buffsize?
   *
   * ANSWER: A Chunk size is usually around 2MB hence the buffer size, and packets are usually much less in size.
   */

  private static final int buffsize = 2048;
  private int timeout = 10000; // 10 seconds
  private static final int k = 7;
  private static boolean isListening = false;

  private static long startTime = 0;
  private static long endTime = 0;

  List<MessageInfo> receivedMessages;
  List<Float> movingAverages;
  List<Integer> missingMessages;


  public FieldUnit() {
    /* initialise data structures */
    movingAverages = null;
    receivedMessages = null;
    missingMessages = null;

    try {
      /* initialise location sensor for field unit */
      locationSensor = new LocationSensor();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    /* set field unit to be listening for measures */
    isListening = true;
  }

  @Override
  public void addMessage(MessageInfo msg) {
    /* save received message in receivedMessages */
    receivedMessages.add(msg);
  }

  @Override
  public void sMovingAverage(int k) {
    /* create a list to hold SMA */
    movingAverages = new ArrayList<>(Collections.nCopies(receivedMessages.size(), 0.0f));
    for (int i = 0; i < receivedMessages.size(); i++) {
      /* for the first k messages, store original value as average */
      if (i < k) {
        movingAverages.set(i, receivedMessages.get(i).getMessage());
      } else {
        /* compute k-=moving average of data and store in list */
        float sum = 0;
        for (int j = 0; j < k; j++) {
          sum += receivedMessages.get(i - j).getMessage();
        }
        movingAverages.set(i, sum / k);
      }
    }
  }


  @Override
  public void receiveMeasures(int port, int timeout) throws SocketException {

    this.timeout = timeout;
    boolean listen = true;
    byte[] buffer = new byte[buffsize];
    int msgCounter = 0;
    int msgTot = 0;

    /* create UDP socket and bind to local port 'port' */
    DatagramSocket aSocket;
    try {
      aSocket = new DatagramSocket(port);
    } catch (SocketException e) {
      System.out.println("Socket: " + e.getMessage());
      throw new SocketException();
    }

    System.out.println("[Field Unit] Listening on port: " + port);

    while (listen) {
      /*  receive until all messages in the transmission (msgTot) have been received or
          until there is nothing more to be received */
      DatagramPacket request = new DatagramPacket(buffer, buffer.length);

      /* set timeout */
      try {
        aSocket.setSoTimeout(this.timeout);
        aSocket.receive(request);
      } catch (IOException ex) {
        System.err.println("Exception: Set timeout failed in field unit message receiver.");
        ex.printStackTrace();
        System.exit(1);
      }

      /* read buffer and form message info structure */
      MessageInfo msg = null;
      try {
        msg = new MessageInfo(new String(buffer, StandardCharsets.UTF_8));
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      /* if this is the first message, initialise the receivedMessages data structure before storing it. */
      if (msgCounter == 0) {
        assert msg != null;
        msgTot = msg.getTotalMessages();
        receivedMessages = new ArrayList<>();
        missingMessages = new ArrayList<>();

        // Start timing duration of communication
        startTime = System.nanoTime();

        // Check if first message is not message number 1
        if (msg.getMessageNum() != 1) {
          // Add all missing messages before this into missingMessages
          for (int i=1; i<msg.getMessageNum(); i++) {
            missingMessages.add(i);
          }
        }

      }

      /* print messages as they come in */
      assert msg != null;
      System.out.printf("[Field Unit] Received message %d out of %d received. Value = %f\n",
              msg.getMessageNum(), msgTot, msg.getMessage());

      /* Check if there are missing messages */
      // Compare current message number with last message in receivedMessages
      if (receivedMessages.size() > 0
          && msg.getMessageNum() != receivedMessages.get(msgCounter-1).getMessageNum() + 1) {
        for (int i=receivedMessages.get(msgCounter-1).getMessageNum() + 1; i<msg.getMessageNum(); i++) {
          missingMessages.add(i);
        }
      }

      /* store the message */
      addMessage(msg);
      msgCounter++;

      /* keep listening until done with receiving  */
      if (msg.getMessageNum() == msgTot) listen = false;
    }

    // Record the end time after receiving last message
    endTime = System.nanoTime();

    /* close socket  */
    aSocket.close();
  }

  public static void main(String[] args) throws SocketException {
    if (args.length < 2) {
      System.out.println("Usage: ./fieldunit.sh <UDP rcv port> <RMI server HostName/IPAddress>");
      return;
    }

    /* parse arguments */
    int port = Integer.parseInt(args[0]);
    String address = args[1];

    /* construct Field Unit Object */
    FieldUnit fieldUnit = new FieldUnit();

    /* call initRMI on the Field Unit Object */
    fieldUnit.initRMI(address);

    /* wait for incoming transmission */
    while (isListening()) {
      fieldUnit.receiveMeasures(port, fieldUnit.timeout);

      /* compute Averages - call sMovingAverage() on Field Unit object */
      fieldUnit.sMovingAverage(k);

      /* send data to the Central Serve via RMI and wait for incoming transmission again */
      fieldUnit.sendAverages();

      /* compute and print stats */
      fieldUnit.printStats();

      /* Stop fieldUnit from listening: */
      // fieldUnit.stopListening();
    }
  }


  @Override
  public void initRMI(String address) {
    System.setProperty("java.security.policy", "file:./policy\n");

    /* initialise security manager */
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }

    /* bind to RMIServer */
    try {
      Registry registry = LocateRegistry.getRegistry(address, 5000);
      central_server = (ICentralServer) registry.lookup("ICentralServer");

    } catch (RemoteException | NotBoundException e) {
      System.out.println("Server exception: " + e);
      e.printStackTrace();
    }

    /* send pointer to LocationSensor to RMI Server */
    try {
      /* ensure that fieldUnit hosts a locationSensor */
      assert locationSensor != null;
      ILocationSensor stub = (ILocationSensor) UnicastRemoteObject.exportObject(this.locationSensor, 0);
      central_server.setLocationSensor(stub);
    } catch (RemoteException e) {
      System.out.println("Server exception: " + e);
      e.printStackTrace();
    }
  }

  @Override
  public void sendAverages() {
    /* attempt to send messages the specified number of times */
    int numberOfAverages = movingAverages.size();
    int totalMessages = receivedMessages.get(0).getTotalMessages();
    for (int i = 0; i < numberOfAverages; i++) {
      /* create new MessageInfo where the message is the respective movingAverage */
      int messageNum = receivedMessages.get(i).getMessageNum();
      MessageInfo msg = new MessageInfo(totalMessages, messageNum, movingAverages.get(i));
      try {
        this.central_server.receiveMsg(msg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void printStats() {
    /* find out number of missing messages */
    int msgTot = receivedMessages.get(0).getTotalMessages();
    int numberOfMissingMessages = msgTot - receivedMessages.size();

    /* print number of missing messages */
    System.out.printf("Total Missing Messages = %d out of %d\n", numberOfMissingMessages, msgTot);

    /* print out message numbers that were lost */
    if (numberOfMissingMessages > 0) {
      System.out.printf("Missing message numbers are: %s \n", missingMessages);
    }

    /* reinitialise data structures for next time */
    receivedMessages = null;
    movingAverages = null;

    // Print duration for communication
    long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
    System.out.printf("Duration for UDP communication is: %d milliseconds\n", duration);

  }

  public static boolean isListening() {
    /* checks if fieldUnit is still listening */
    return isListening;
  }

  public void stopListening() {
    /* stop fieldUnit from listening */
    isListening = false;
  }
}
