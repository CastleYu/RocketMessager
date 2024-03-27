package groupchat;

import constants.Constants;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 进行群聊的设置
 * 使用订阅机制，应该要群发进行获得内容
 */


public class GroupChatProducer {
    private static final Logger logger = LoggerFactory.getLogger(GroupChatProducer.class);

    public static void main(String[] args) throws ClientException, IOException {
        // 服务获取
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        // 配置生产者
        ClientConfiguration clientConfig = ClientConfiguration.newBuilder()
                .setEndpoints(Constants.BROKER_ADDRESS_PORT)
                .build();

        Producer producer = provider.newProducerBuilder()
                .setClientConfiguration(clientConfig)
                .build();

        // 创建输入流
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input;
        System.out.println("输入消息：('exit' to quit):");
        Group group = new Group("10000", "TestGroup");

        while (!(input = reader.readLine()).equals("exit")) {
            // 消息特征构建
            String topic = group.getTopic();
            String tag = group.getUniqueIdString();
            Random random = new Random();
            String msgKeys = String.format("%08d", random.nextInt(100000000));
            byte[] msgBody = input.getBytes();

            // 消息体与发送单元构建
            Message message = provider.newMessageBuilder()
                    .setTopic(topic)
                    .setTag(tag)
                    .setBody(msgBody)
                    .setKeys(msgKeys)
                    .build();

            // 执行发送
            try {
                SendReceipt sendReceipt = producer.send(message);
                logger.info("Send message successfully, messageId={}, input={}", sendReceipt.getMessageId(), input);
            } catch (ClientException e) {
                logger.error("Failed to send message", e);
            }
            System.out.println("Enter your message ('exit' to quit):");
        }

        // 关闭生产者和输入流
        producer.close();
        reader.close();
    }
}
