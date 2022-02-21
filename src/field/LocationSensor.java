package field;
/*
 * Created on Feb 2022
 */

import common.Location;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/* TODO extend appropriate classes and implement the appropriate interfaces */
public class LocationSensor implements ILocationSensor {
  private Location location;

  public LocationSensor() throws RemoteException {
    super();
    location = new Location(40.831436, 14.242832);
  }

  public LocationSensor(Location location) throws RemoteException {
    super();
    this.location = location;
  }

  @Override
  public Location getCurrentLocation() throws RemoteException {
    return location;
  }
}
