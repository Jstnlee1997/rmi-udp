package centralserver;
/*
 * Created on Feb 2022
 */
import common.MessageInfo;
import field.ILocationSensor;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICentralServer extends Remote {
    /* Receive Message. Called by the client to copy a value into a Central Server object - aka send through RMI*/
    public void receiveMsg(MessageInfo m) throws RemoteException;
    /* Copy remote reference into the Central Server so that the latter is able to call functions on the remote object */
    public void setLocationSensor(ILocationSensor locationSensor) throws RemoteException;
}
