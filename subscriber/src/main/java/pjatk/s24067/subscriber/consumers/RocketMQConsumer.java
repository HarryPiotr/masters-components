package pjatk.s24067.subscriber.consumers;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

import java.util.List;

@Component
@ConditionalOnProperty(value = "rocketmq.enabled", havingValue = "true")
public class RocketMQConsumer {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public RocketMQConsumer(AppConfig appConfig) {

        this.appConfig = appConfig;

        initConsumer();
    }

    private void initConsumer() {

        try {
            DefaultMQPushConsumer rocketConsumer = new DefaultMQPushConsumer(appConfig.getRocketmq().getGroupName());
            rocketConsumer.setNamesrvAddr(appConfig.getRocketmq().getNameserver());
            rocketConsumer.subscribe(appConfig.getRocketmq().getInboundTopic(), "*");

            rocketConsumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
                messages.stream()
                        .forEach(msg -> log.info("Received message: {}", new String(msg.getBody())));
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            rocketConsumer.start();
            log.info("Consumer started");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
