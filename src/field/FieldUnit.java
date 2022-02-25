package field;
/*
 * Created on Feb 2022
 */

import centralserver.ICentralServer;
import common.MessageInfo;

import java.io.IOException;
import java.net.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* You can add/change/delete class attributes if you think it would be
 * appropriate.
 * You can also add helper methods and change the implementation of those
 * provided if you think it would be appropriate, as long as you DO NOT
 * CHANGE the provided interface.
 */

public class FieldUnit implements IFieldUnit {
  private ICentralServer central_server;
  private LocationSensor locationSensor;

  /* Note: Could you discuss in one line of comment what do you think can be
   * an appropriate size for buffsize?
   * (Which is used to init DatagramPacket?)
   *
   * A Chunk size is usually around 2MB hence the buffer size, and packets are usually much less in size.
   */

  private static final int buffsize = 2048;
  private int timeout = 50000;
  private static final int k = 7;

  List<MessageInfo> receivedMessages;
  List<Float> movingAverages;


    public FieldUnit () {
      /* TODO: Initialise data structures */
      try {
        locationSensor = new LocationSensor();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }

  @Override
  public void addMessage(MessageInfo msg) {
    /* TODO: Save received message in receivedMessages */
    receivedMessages.add(msg);
  }

  @Override
  public void sMovingAverage(int k) {
    /* TODO: Compute SMA and store values in a class attribute */
    movingAverages = new ArrayList<>(Collections.nCopies(receivedMessages.size(), 0.0f));
    for (int i = 0; i < receivedMessages.size(); i++) {
      if (i < k) {
        movingAverages.set(i, receivedMessages.get(i).getMessage());
      } else {
        float sum = 0;
        for (int j = 0; j < 7; j++) {
          sum += receivedMessages.get(i - j).getMessage();
        }
        movingAverages.set(i, 1 / k * sum);
      }
    }
  }


  @Override
  public void receiveMeasures(int port, int timeout) throws SocketException {

    this.timeout = timeout;
    boolean listen = true;
    byte[] buffer = new byte[buffsize];
    int msgCounter = 0;

    /* TODO: Create UDP socket and bind to local port 'port' */
    DatagramSocket aSocket;
    try {
      aSocket = new DatagramSocket(port);
    } catch (SocketException e) {
      System.out.println("Socket: " + e.getMessage());
      throw new SocketException();
    }

    System.out.println("[Field Unit] Listening on port: " + port);

    while (listen) {
            /* TODO: Receive until all messages in the transmission (msgTot) have been received or
                until there is nothing more to be received */
      DatagramPacket request = new DatagramPacket(buffer, buffer.length);

      try {
        // Set timeout
        aSocket.setSoTimeout(this.timeout);
        aSocket.receive(request);
      } catch (IOException ex) {
        ex.printStackTrace();
      }

      MessageInfo msg = null;
      try {
        msg = new MessageInfo(Arrays.toString(buffer));
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      /* TODO: If this is the first message, initialise the receive data structure before storing it. */
      if (msgCounter == 0) {
        assert msg != null;
        receivedMessages = new ArrayList<>();
      }

      /* TODO: Store the message */
      addMessage(msg);
      msgCounter++;

      /* TODO: Keep listening UNTIL done with receiving  */
      assert msg != null;
      if (msg.getMessageNum() == msg.getTotalMessages()) listen = false;
    }

    /* TODO: Close socket  */
    aSocket.close();
  }

  public static void main(String[] args) throws SocketException {
    if (args.length < 2) {
      System.out.println("Usage: ./fieldunit.sh <UDP rcv port> <RMI server HostName/IPAddress>");
      return;
    }

    /* TODO: Parse arguments */
    int port = Integer.parseInt(args[0]);
    String address = args[1];

    /* TODO: Construct Field Unit Object */
    FieldUnit fieldUnit = new FieldUnit();

    /* TODO: Call initRMI on the Field Unit Object */
    fieldUnit.initRMI(address);

    /* TODO: Wait for incoming transmission */
    fieldUnit.receiveMeasures(port, fieldUnit.timeout);

            /* TODO: Compute Averages - call sMovingAverage()
                on Field Unit object */

    /* TODO: Compute and print stats */
    fieldUnit.sMovingAverage(k);
    fieldUnit.printStats();


    /* TODO: Send data to the Central Serve via RMI and
     *        wait for incoming transmission again
     */
    fieldUnit.sendAverages();
  }


  @Override
  public void initRMI(String address) {
    /* If you are running the program within an IDE instead of using the
     * provided bash scripts, you can use the following line to set
     * the policy file
     */

    System.setProperty("java.security.policy", "file:./policy\n");

    /* TODO: Initialise Security Manager */
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }

    /* TODO: Bind to RMIServer */
    /* TODO: Check this! */
    // Difference between the two below is that the first one is implementing of the client

//    try {
//      Registry registry = LocateRegistry.getRegistry(address);
//      ICentralServer stub = (ICentralServer) registry.lookup("Central Server");
//    } catch (RemoteException | NotBoundException e) {
//      System.out.println("Server exception: " + e);
//      e.printStackTrace();

    try {
      central_server = (ICentralServer) Naming.lookup(address);
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      e.printStackTrace();
    }


    /* TODO: Send pointer to LocationSensor to RMI Server */
    /* TODO: ensure that fieldUnit hosts a locationSensor */
    try {
      central_server.setLocationSensor(this.locationSensor);
    } catch (RemoteException e) {
      System.out.println("Server exception: " + e);
      e.printStackTrace();
    }
  }

  @Override
  public void sendAverages() {
    /* TODO: Attempt to send messages the specified number of times */
    int numberOfAverages = receivedMessages.size();
    int totalMessages = receivedMessages.get(0).getTotalMessages();
    for (int i = 0; i < numberOfAverages; i++) {
      // Create new MessageInfo where the message is the respective movingAverage
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
    /* TODO: Find out how many messages were missing */
    int msgTot = receivedMessages.get(0).getTotalMessages();
    int missingMessages = msgTot - receivedMessages.size();

    /* TODO: Print stats (i.e. how many message missing?
     * do we know their sequence number? etc.) */

    for (int i = 0; i < movingAverages.size(); i++) {
      System.out.printf("[Field Unit] Received message %d out of %d received. Value = %f",
          receivedMessages.get(i).getMessageNum(), msgTot, movingAverages.get(i));
    }

    System.out.printf("Total Missing Messages = %d out of %d", missingMessages, msgTot);


    /* TODO: Now re-initialise data structures for next time */
    receivedMessages = null;
    movingAverages = null;

  }


}
