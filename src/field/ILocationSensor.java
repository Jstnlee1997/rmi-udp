package field;
/*
 * Created on Feb 2022
 */
import common.Location;

import java.rmi.Remote;
import java.rmi.RemoteException;

/* Location Sensor is a remote reference, interface needs to extend Remote */
public interface ILocationSensor extends Remote {
    public Location getCurrentLocation() throws RemoteException;
}
