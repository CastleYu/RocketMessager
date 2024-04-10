package groupchat;

import constants.Constants;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import translators.YoudaoTranslator;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 进行群聊的设置
 * 使用订阅机制，应该要群发进行获得内容
 */

public class GroupChatProducer {
    private static final Logger logger = LoggerFactory.getLogger(GroupChatProducer.class);
    private static final Map<String, String> translationCache = new HashMap<>(); // 存储翻译结果的哈希表

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
            // 检查翻译结果是否已经存在于哈希表中
            String translatedText = translationCache.get(input);
            if (translatedText == null) {
                // 如果哈希表中不存在翻译结果，则调用 API 进行翻译并存储到哈希表中
                translatedText = YoudaoTranslator.translateAndPrint(input);
                translationCache.put(input, translatedText);
            }

            // 输出原文和翻译结果，原文使用橙色，翻译结果使用蓝色
            printWithColor("原文: ", input, Color.ORANGE); // 输出原文并设置为橙色
            System.out.print(" "); // 添加空格分隔
            printWithColor("翻译结果: ", translatedText, Color.BLUE); // 输出翻译结果并设置为蓝色
            System.out.println(); // 换行

            // 消息特征构建
            String topic = group.getTopic();
            String tag = group.getUniqueIdString();
            String msgKeys = String.valueOf(System.currentTimeMillis());
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

    // 辅助方法：使用指定颜色输出文本
    private static void printWithColor(String prefix, String content, Color color) {
        System.out.print("\u001B[38;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m" + prefix + content + "\u001B[0m");
    }
}
