package pjatk.s24067.subscriber.consumers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

@Component
@ConditionalOnProperty(value = "rabbitmq.pubsub.enabled", havingValue = "true")
public class RabbitMQSubscriber extends GenericConsumer {

    private AppConfig appConfig;
    private ConnectionFactory rabbitConnectionFactory;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String rabbitExchange;
    private String rabbitBindingKey;
    private String rabbitQueue;

    public RabbitMQSubscriber(AppConfig appConfig, ConnectionFactory rabbitConnectionFactory) throws InterruptedException {

        this.appConfig = appConfig;
        this.rabbitConnectionFactory = rabbitConnectionFactory;
        this.rabbitExchange = appConfig.getRabbitmq().getPubsub().getExchangeName();
        this.rabbitBindingKey = appConfig.getRabbitmq().getPubsub().getBindingKey();
        initConsumer();
    }

    private boolean initConsumer() {

        try {
            Connection rabbitConnection = rabbitConnectionFactory.newConnection();
            Channel rabbitChannel = rabbitConnection.createChannel();

            rabbitChannel.exchangeDeclare(rabbitExchange, "topic");
            rabbitQueue = rabbitChannel.queueDeclare().getQueue();

            DeliverCallback messageCallback = (tag, payload) -> {
                String message = new String(payload.getBody(), "UTF-8");
                log.info(message);
                incrementCounter(rabbitExchange);
            };

            rabbitChannel.basicConsume(
                    rabbitQueue,
                    true,
                    messageCallback,
                    (tag) -> {});
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String getConsumerType() {
        return "rabbitmq/pubsub";
    }
}
