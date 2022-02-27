package sensor;
/*
 * Created on Feb 2022
 */

import common.MessageInfo;
import field.FieldUnit;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
    /* build Sensor Object */
    this.address = address;
    this.port = port;
    this.totMsg = totMsg;

    try {
      this.s = new DatagramSocket(port);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run(int N) throws InterruptedException {
    /* send N measurements */
    for (int i = 1; i <= N; i++) {
      float measurement = this.getMeasurement();
      MessageInfo msg = new MessageInfo(N, i, measurement);

      /* call sendMessage() to send the msg to destination */
      sendMessage(address, port, msg);
      printMessage(msg);
    }
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

    /* call constructor of sensor to build Sensor object*/
    Sensor sensor = new Sensor(address, port, totMsg);

    /* use Run to send the messages */
    try {
      sensor.run(sensor.totMsg);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void sendMessage(String address, int port, MessageInfo msg) {
    /* convert message to string */
    String toSend = msg.toString();

    try {
      /* build the destination address object */
      InetAddress dst_addr = InetAddress.getByName(address);

      /* create datagram packet for send */
      DatagramPacket p = new DatagramPacket(toSend.getBytes(StandardCharsets.UTF_8),
          toSend.getBytes(StandardCharsets.UTF_8).length, dst_addr, port);

      /* send the message */
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
  }

  @Override
  public float getMeasurement() {
    /* measures generated at random */
    Random r = new Random();
    return r.nextFloat() * (max_measure - min_measure) + min_measure;
  }

  public void printMessage(MessageInfo msg) {
    /* print measure info */
    System.out.printf("[Sensor] Sending message %d out of %d. Measure = %f\n",
        msg.getMessageNum(), msg.getTotalMessages(), msg.getMessage());
  }
}
