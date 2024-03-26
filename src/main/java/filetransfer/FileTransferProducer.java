package filetransfer;

import constants.Constants;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileTransferProducer {
    public static void main(String[] args) {
        String topic = "file_transfer_topic";
        String fileToSend = "TestFile.txt"; // Replace this with the path to your file

        // Send file
        sendFile(topic, fileToSend);
    }

    public static void sendFile(String topic, String filePath) {
        try {
//            好像没有topic？
            DefaultMQProducer producer = new DefaultMQProducer("file_transfer_producer_group");
            producer.setNamesrvAddr(Constants.NAME_SERVER_ADDRESS_PORT);
            producer.start();

            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileData = new byte[(int) file.length()];
            fileInputStream.read(fileData);

            Message message = new Message(topic, fileData);
            message.setKeys(file.getName());
            producer.send(message);

            fileInputStream.close();
            producer.shutdown();
            System.out.println("File sent successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
