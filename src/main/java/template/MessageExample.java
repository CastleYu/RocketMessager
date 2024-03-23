package template;

import constants.Constants;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class MessageExample {
    private static final Logger logger = LoggerFactory.getLogger(MessageExample.class);

    public static void main(String[] args) throws MQClientException, ClientException {
        // 1. 创建消费者
        String consumerGroup = "YourConsumerGroup";
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("YourConsumerGroup");
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        // 2. 设置NameServer的地址
        String endpoints = Constants.SERVER_ADDRESS_PORT;
        consumer.setNamesrvAddr(Constants.SERVER_ADDRESS_PORT);
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                .setEndpoints(endpoints)
                .build();

        // 3. 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
        String topic = "TestTopic";
        consumer.subscribe("TestTopic", "*");
        String tag = "*";
        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
        // 4. 注册回调实现类来处理从broker拉取回来的消息  
        consumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> {
            System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
            // 遍历消息列表  
            for (MessageExt msg : msgs) {
                // 提取消息体并转换为字符串  
                String msgBody = new String(msg.getBody());
                // 打印消息体内容  
                System.out.println("Message Body: " + msgBody);
            }
            // 标记该消息已经被成功消费  
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                // 设置消费者分组。
                .setConsumerGroup(consumerGroup)
                // 设置预绑定的订阅关系。
                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
                // 设置消费监听器。
                .setMessageListener(messageView -> {
                    // 处理消息并返回消费结果。
                    logger.info("Consume message successfully, messagebody={}", messageView.getBody());
                    return ConsumeResult.SUCCESS;
                })
                .build();

        // 5. 启动消费者实例  
        consumer.start();

        System.out.println("Consumer Started.");
    }
}