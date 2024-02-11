package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthenticationService extends Remote {
    boolean authenticate(String username, String password) throws RemoteException;
}
