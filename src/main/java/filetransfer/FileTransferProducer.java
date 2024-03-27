package filetransfer;

import constants.Constants;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileTransferProducer {
    public static void main(String[] args) {
        String topic = "file_transfer_topic";
        String fileToSend = "src/main/java/filetransfer/TestFile.txt"; // Replace this with the path to your file

        // Send file
        sendFile(topic, fileToSend);
    }

    public static void sendFile(String topic, String filePath) {
        try {
            String endpoint = Constants.BROKER_ADDRESS_PORT;
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
            ClientConfiguration configuration = builder.build();
            Producer producer1 = provider.newProducerBuilder()
                    .setTopics(topic)
                    .setClientConfiguration(configuration)
                    .build();
//            DefaultMQProducer producer = new DefaultMQProducer("file_transfer_producer_group");
//            producer.setNamesrvAddr(Constants.NAME_SERVER_ADDRESS_PORT);
//            producer.start();

            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileData = new byte[(int) file.length()];
            fileInputStream.read(fileData);
            Message message = provider.newMessageBuilder()
                    .setTopic(topic)
                    .setKeys(file.getName())
                            .setBody(fileData)
                                    .build();
//            Message message = new Message(topic, fileData);
//            message.setKeys(file.getName());
            SendReceipt send = producer1.send(message);
            fileInputStream.close();
//            producer1.shutdown();
//            logger.info("Send message successfully, messageId={}", send.getMessageId());
            System.out.printf("Send message successfully, messageId=%s", send.getMessageId());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
