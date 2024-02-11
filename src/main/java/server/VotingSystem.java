package server;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class VotingSystem {
    private Map<String, Integer> candidateVotes;
    private List<String> candidateList;
    private KafkaProducer<String, String> producer;
    private static final Logger log = LoggerFactory.getLogger(VotingSystem.class.getSimpleName());

    public VotingSystem() {
        candidateVotes = new HashMap<>();
        candidateList = new ArrayList<>();


        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "csvm:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<String, String>(props);
    }

    public synchronized String vote(int candidateIndex) {
        if (candidateIndex >= 0 && candidateIndex < candidateList.size()) {
            String candidateName = candidateList.get(candidateIndex);
            candidateVotes.put(candidateName, candidateVotes.getOrDefault(candidateName, 0) + 1);

            ObjectMapper objectMapper = new ObjectMapper();
            String resultsJson = "";
            try {
                resultsJson = objectMapper.writeValueAsString(candidateVotes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("vote", resultsJson);
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e == null) {
                        // the record was successfully sent
                        log.info("Received new metadata \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp());
                    } else {
                        log.error("Error while producing", e);
                    }
            }
        } );
            return "Vote for " + candidateName + " recorded.";
        } else {
            return "Invalid candidate index.";
        }
    }

    public List<String> getCandidateList() {
        return new ArrayList<>(candidateList);
    }
    public Map<String, Integer> getResults() {
        return new HashMap<>(candidateVotes);
    }
    public void displayResults() {
        System.out.println("Voting Results:");
        for (Map.Entry<String, Integer> entry : candidateVotes.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " votes");
        }
    }

    public static void main(String[] args) {
        VotingSystem votingSystem = new VotingSystem();

        // Adding candidates
        votingSystem.addCandidate("Candidate 1");
        votingSystem.addCandidate("Candidate 2");
        votingSystem.addCandidate("Candidate 3");
        votingSystem.addCandidate("Candidate 4");

        // Start the socket-based server for voting
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("Voting System Server is ready for voting.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new VotingSystemServerThread(clientSocket, votingSystem).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCandidate(String candidateName) {
        candidateList.add(candidateName);
        candidateVotes.put(candidateName, 0);
    }


}
