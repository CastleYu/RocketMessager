package groupchat;

import constants.Constants;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import translators.BaiduTranslator;
import translators.YoudaoTranslator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GroupChatConsumer {
    private static final Logger logger = LoggerFactory.getLogger(GroupChatConsumer.class);
    private static final BlockingQueue<MessageExt> messageQueue = new ArrayBlockingQueue<>(100); // 使用队列存储消息
    private static final Map<String, String> translationCache = new HashMap<>(); // 存储翻译结果的哈希表

    public static void main(String[] args) {
        try {
            // 服务获取
            final ClientServiceProvider provider = ClientServiceProvider.loadService();

            // 配置消费者
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setNamesrvAddr(Constants.NAME_SERVER_ADDRESS_PORT);
            Group group = new Group("10000", "TestGroup");
            String consumerGroup = "Group_" + group.getGroupID();

            // 消息特征构建
            String topic = group.getTopic();
            String tag = group.getUniqueIdString();

            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
            consumer.setConsumerGroup(consumerGroup);
            consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.setNamesrvAddr(Constants.NAME_SERVER_ADDRESS_PORT);
            consumer.resetClientConfig(clientConfig);
            consumer.subscribe(topic, MessageSelector.byTag(tag));

            // 设置消息监听器
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt messageExt : msgs) {
                    try {
                        messageQueue.put(messageExt); // 将接收到的消息加入队列
                    } catch (InterruptedException e) {
                        logger.error("Failed to add message to the queue", e);
                        Thread.currentThread().interrupt();
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            // 启动消费者
            consumer.start();

            // 创建单独的线程处理消息
            new Thread(GroupChatConsumer::processMessages).start();
        } catch (MQClientException e) {
            logger.error("Failed to start consumer", e);
        }
    }

    private static void processMessages() {
        while (true) {
            try {
                MessageExt messageExt = messageQueue.take(); // 从队列中取出一条消息进行处理
                handleMessage(messageExt);
            } catch (InterruptedException e) {
                logger.error("Failed to take message from the queue", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void handleMessage(MessageExt messageExt) {
        // 日志记录消息内容
        logger.info("接收到消息(Id={}): ", messageExt.getMsgId());
        String output = new String(messageExt.getBody());
        logger.info("(Tag={}, msgKey={})\n消息内容:\n\t{}", messageExt.getTags(), messageExt.getKeys(), output);

        // 检查翻译结果是否已经存在于哈希表中
        String translatedText = translationCache.get(output);
        if (translatedText == null) {
            translatedText = BaiduTranslator.translate(output);
            translationCache.put(output, translatedText);
        }

        // 输出原文和翻译结果，原文使用橙色，翻译结果使用蓝色
        printWithColor("原文: ", output, Color.ORANGE); // 输出原文并设置为橙色
        System.out.print(" "); // 添加空格分隔
        printWithColor("翻译结果: ", translatedText, Color.BLUE); // 输出翻译结果并设置为蓝色
        System.out.println(); // 换行
    }

    private static void printWithColor(String prefix, String content, Color color) {
        System.out.print("\u001B[38;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m" + prefix + content + "\u001B[0m");
    }
}
