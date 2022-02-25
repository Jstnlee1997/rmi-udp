package sensor;
/*
 * Created on Feb 2022
 */

import common.MessageInfo;
import field.FieldUnit;

import java.io.IOException;
import java.net.*;
import java.util.Random;

/* You can add/change/delete class attributes if you think it would be
 * appropriate.
 * You can also add helper methods and change the implementation of those
 * provided if you think it would be appropriate, as long as you DO NOT
 * CHANGE the provided interface.
 */

public class Sensor implements ISensor {
  private final String address;
  private final int port;
  private final int totMsg;

  private final static int max_measure = 50;
  private final static int min_measure = 10;

  private DatagramSocket s;
  private byte[] buffer;

  /* Note: Could you discuss in one line of comment what do you think can be
   * an appropriate size for buffsize?
   * (Which is used to init DatagramPacket?)
   *
   * A Chunk size is usually around 2MB hence the buffer size, and packets are usually much less in size.
   */
  private static final int buffsize = 2048;

  public Sensor(String address, int port, int totMsg) {
    /* TODO: Build Sensor Object */
    this.address = address;
    this.port = port;
    this.totMsg = totMsg;
  }

  @Override
  public void run(int N) throws InterruptedException {
    /* TODO: Send N measurements */
    for (int i = 1; i < N + 1; i++) {
      float measurement = this.getMeasurement();
      MessageInfo msg = new MessageInfo(N, i, measurement);
      sendMessage(address, port, msg);
    }

    /* Hint: You can pick ONE measurement by calling
     *
     * float measurement = this.getMeasurement();
     */

    /* TODO: Call sendMessage() to send the msg to destination */

  }

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Usage: ./sensor.sh field_unit_address port number_of_measures");
      return;
    }

    /* Parse input arguments */
    String address = args[0];
    int port = Integer.parseInt(args[1]);
    int totMsg = Integer.parseInt(args[2]);

    /* TODO: Call constructor of sensor to build Sensor object*/
    Sensor sensor = new Sensor(address, port, totMsg);

    /* TODO: Use Run to send the messages */
    try {
      sensor.run(sensor.totMsg);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void sendMessage(String address, int port, MessageInfo msg) {
    String toSend = msg.toString();

    /* TODO: Build destination address object */
    try {
      /* Build the destination address object */
      InetAddress dst_addr = InetAddress.getByName(address);

      /* Second, we create datagram packet for send */
      DatagramPacket p = new DatagramPacket(toSend.getBytes(), toSend.getBytes().length, dst_addr, port);

      /* Third, we send the message */
      try {
        s = new DatagramSocket();
        s.connect(dst_addr, port);
      } catch (SocketException e) {
        e.printStackTrace();
      }
      s.send(p);

    } catch (IOException e) {
      e.printStackTrace();
    }

    /* TODO: Build datagram packet to send */

    /* TODO: Send packet */

  }

  @Override
  public float getMeasurement() {
    Random r = new Random();

    return r.nextFloat() * (max_measure - min_measure) + min_measure;
  }
}
