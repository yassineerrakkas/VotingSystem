package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationServiceImpl extends UnicastRemoteObject implements AuthenticationService {
    private static final long serialVersionUID = 1L;

    private Map<String, String> voterCredentials;

    public AuthenticationServiceImpl() throws RemoteException {
        voterCredentials = new HashMap<>();
    }

    @Override
    public boolean authenticate(String username, String password) throws RemoteException {
        return voterCredentials.containsKey(username) && voterCredentials.get(username).equals(password);
    }
    public void addVoter(String username, String password) {
        voterCredentials.put(username, password);
    }

    public static void main(String[] args) {
        try {
            AuthenticationServiceImpl authService = new AuthenticationServiceImpl();

            // Adding voters
            authService.addVoter("user1", "user1");
            authService.addVoter("user2", "user2");
            authService.addVoter("user3", "user3");

            // Start RMI registry on default port 1099
            java.rmi.registry.LocateRegistry.createRegistry(1099);

            // Bind the AuthenticationServiceImpl instance to the registry
            java.rmi.Naming.rebind("AuthenticationService", authService);

            System.out.println("Authentication Service Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
