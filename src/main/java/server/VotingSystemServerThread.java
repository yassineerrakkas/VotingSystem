package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class VotingSystemServerThread extends Thread {
    private Socket clientSocket;
    private VotingSystem votingSystem;

    public VotingSystemServerThread(Socket clientSocket, VotingSystem votingSystem) {
        this.clientSocket = clientSocket;
        this.votingSystem = votingSystem;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            List<String> candidateList = votingSystem.getCandidateList();
            oos.writeObject(candidateList);

            while (true) {
                try {
                    String messageType = (String) ois.readObject();
                    System.out.println(messageType);
                    switch (messageType) {
                        case "submitVote":
                            int selectedCandidateIndex = ois.readInt();
                            String result = votingSystem.vote(selectedCandidateIndex);
                            System.out.println(result);
                            votingSystem.displayResults();
                            break;
                        case "getResults":
                            // Send the current voting results to the client
                            oos.writeObject(votingSystem.getResults());
                            break;
                        case "getCandidates":
                            oos.writeObject(votingSystem.getCandidateList());
                            break;
                        case "exit":
                            // Client requested to exit, break out of the loop
                            return;
                        default:
                            System.out.println("Unknown message type: " + messageType);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
