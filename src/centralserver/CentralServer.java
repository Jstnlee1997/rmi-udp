package centralserver;

import common.*;
import field.ILocationSensor;
/*
 * Created on Feb 2022
 */
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

 /* You can add/change/delete class attributes if you think it would be
  * appropriate.
  * You can also add helper methods and change the implementation of those
  * provided if you think it would be appropriate, as long as you DO NOT
  * CHANGE the provided interface.
  */

public class CentralServer /* TODO extend appropriate classes and implement the appropriate interfaces */ {

    private ILocationSensor locationSensor;

    protected CentralServer () throws RemoteException {
        super();

        /* TODO: Initialise Array receivedMessages */
    }

    public static void main (String[] args) throws RemoteException {
        CentralServer cs = new CentralServer();

        /* If you are running the program within an IDE instead of using the
         * provided bash scripts, you can use the following line to set
         * the policy file
         */

        /* System.setProperty("java.security.policy","file:./policy\n"); */

        /* TODO: Configure Security Manager */

        /* TODO: Create (or Locate) Registry */

        /* TODO: Bind to Registry */

        System.out.println("Central Server is running...");


    }


    @Override
    public void receiveMsg (MessageInfo msg) {
        System.out.println("[Central Server] Received message " + (msg.getMessageNum()) + " out of " +
                msg.getTotalMessages() + ". Measure = " + msg.getMessage());


        /* TODO: If this is the first message, reset counter and initialise data structure. */


        /* TODO: Save current message */

        /* TODO: If I received everything that there was to be received, prints stats. */


    }

    public void printStats() {
      /* TODO: Find out how many messages were missing */

      /* TODO: Print stats (i.e. how many message missing?
       * do we know their sequence number? etc.) */

      /* TODO: Print the location of the Field Unit that sent the messages */

      /* TODO: Now re-initialise data structures for next time */

    }

    @Override
    public void setLocationSensor (ILocationSensor locationSensor) throws RemoteException {

        /* TODO: Set location sensor */

         System.out.println("Location Sensor Set");
    }

    public void printLocation() throws RemoteException {
        /* TODO: Print location on screen from remote reference */
    }
}
