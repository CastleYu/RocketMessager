package filetransfer;

import constants.Constants;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.FileOutputStream;

public class FileTransferConsumer {

    public static void main(String[] args) {
        String topic = "file_transfer_topic";

        // Receive file
        receiveFile(topic);
    }

    public static void receiveFile(String topic) {
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("file_transfer_consumer_group");
            String endpoints = Constants.BROKER_ADDRESS_PORT;
            consumer.setNamesrvAddr(Constants.NAME_SERVER_ADDRESS_PORT);
            ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                    .setEndpoints(endpoints)
                    .build();
            consumer.subscribe(topic, "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt msg : msgs) {
                    try {
                        String fileName = msg.getKeys()+"_re";
                        byte[] fileData = msg.getBody();
                        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                        fileOutputStream.write(fileData);
                        fileOutputStream.close();
                        System.out.println("File received: " + fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;//标记已经成功消费消息
            });
            consumer.start();
            System.out.println("Waiting for files...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
