package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.AuthenticationService;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class VotingClient {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private VotingGUI votingSystemGUI;
    VotingResultsGUI resultsGUI;
    private AuthenticationService authService;
    private Socket socket;
    private JFrame loginFrame;
    private static final Logger log = LoggerFactory.getLogger(VotingClient.class.getSimpleName());
    private KafkaConsumer<String, String> kafkaConsumer;
    private Map<String, Integer> resultMap;

    public VotingClient() {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "csvm:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,java.util.UUID.randomUUID().toString());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        kafkaConsumer = new KafkaConsumer<String, String>(properties);
        kafkaConsumer.subscribe(Collections.singletonList("vote"));

        try {
            SwingUtilities.invokeLater(() -> showLoginGUI());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void consumeResults() {
        String last = "";
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String kafkaResultData = record.value();
                last = record.value();

            }
            try {
                // Use Jackson ObjectMapper to convert JSON string to HashMap
                ObjectMapper objectMapper = new ObjectMapper();
                this.resultMap = objectMapper.readValue(last, new TypeReference<HashMap<String, Integer>>() {});
                if (resultsGUI != null && resultMap != null) {
                    resultsGUI.updateResults(resultMap);
                }


            } catch (IOException e) {
                System.out.println("No data to read");
            }
        }
    }
    public void showLoginGUI() {
        LoginPage loginpage = new LoginPage(this);
    }
    public void createSession() throws IOException {
        socket = new Socket("localhost", 8888);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }
    public List<String> getCandidateList() throws IOException, ClassNotFoundException {
        oos.writeObject("getCandidates");
        return (List<String>) ois.readObject();
    }

    public Map<String, Integer> getResults() throws IOException, ClassNotFoundException {
//        oos.writeObject("getResults");
//        Map<String, Integer> receivedData = (Map<String, Integer>) ois.readObject();
//        return receivedData;
        return this.resultMap;
    }

    public void submitVote(int selectedCandidateIndex) throws IOException, ClassNotFoundException {
        try {
            oos.writeObject("submitVote");
            oos.writeInt(selectedCandidateIndex);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultsGUI = new VotingResultsGUI(this);
        Thread kafkaConsumerThread = new Thread(this::consumeResults);
        kafkaConsumerThread.start();
    }

    public static void main(String[] args) {
        new VotingClient();
    }
}
