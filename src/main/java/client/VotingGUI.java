package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class VotingGUI implements ActionListener {
    private JFrame frame;
    private JPanel mainPanel;
    private JList<String> candidateList;
    private JButton submitButton;
    private VotingResultsGUI resultsGUI;
    private VotingClient votingClient;

    public VotingGUI(List<String> candidates, VotingClient votingClient) {
        this.votingClient = votingClient;
        frame = new JFrame("Voting System");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        candidateList = new JList<>(candidates.toArray(new String[0]));
        JScrollPane scrollPane = new JScrollPane(candidateList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        submitButton = new JButton("Submit Vote");
        submitButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        centerFrameOnScreen();
        frame.setVisible(true);
    }

    private void centerFrameOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedCandidateIndex = candidateList.getSelectedIndex();
        try {
            votingClient.submitVote(selectedCandidateIndex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        frame.dispose();
    }
}
