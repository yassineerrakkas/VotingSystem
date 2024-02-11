package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

public class VotingResultsGUI {
    private JPanel mainPanel;
    private JFrame frame;
    private JTextArea resultsTextArea;
    private VotingClient votingClient;

    public VotingResultsGUI(VotingClient votingClient) throws IOException, ClassNotFoundException {
        this.votingClient = votingClient;
        frame = new JFrame("Voting Results");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        resultsTextArea = new JTextArea();
        resultsTextArea.setEditable(false);
        resultsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Use a monospaced font
        mainPanel.add(new JScrollPane(resultsTextArea), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        centerFrameOnScreen();
        frame.setVisible(true);
        updateResults(getNewResults());
    }

    private void centerFrameOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }

    public void updateResults(Map<String, Integer> results) {

        if (results != null) {
            resultsTextArea.setText("Voting Results:\n");
            results.forEach((candidate, votes) ->
                    resultsTextArea.append(String.format("%-20s : %d votes%n", candidate, votes))
            );
        }
    }

    public Map<String, Integer> getNewResults() throws IOException, ClassNotFoundException {
        return votingClient.getResults();
    }
}
