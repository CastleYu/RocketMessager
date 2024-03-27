package groupchat;

import constants.Constants;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * 接收消息（群聊）
 * 消费者
 */

public class OutGroupChatConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OutGroupChatConsumer.class);

    public static void main(String[] args) throws ClientException, IOException, InterruptedException, MQClientException {
        // 服务获取
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        // 配置消费者
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNamesrvAddr(Constants.NAME_SERVER_ADDRESS_PORT);
        Group group = new Group("10001", "TestGroup");
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
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt messageExt : msgs) {
                    // 日志记录消息内容
                    logger.info("接收到消息(Id={}): ", messageExt.getMsgId());
                    logger.info("(Tag={}, msgKey={})\n消息内容:\n\t{}", messageExt.getTags(), messageExt.getKeys(), new String(messageExt.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动消费者
        consumer.start();

        System.out.printf("Consumer Started.%n");
        // pushConsumer.close();
    }
}