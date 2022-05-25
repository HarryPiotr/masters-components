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
@ConditionalOnProperty(value = "rabbitmq.enabled", havingValue = "true")
public class RabbitMQConsumer {

    private AppConfig appConfig;
    private ConnectionFactory rabbitConnectionFactory;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public RabbitMQConsumer(AppConfig appConfig, ConnectionFactory rabbitConnectionFactory) {

        this.appConfig = appConfig;
        this.rabbitConnectionFactory = rabbitConnectionFactory;

        if(!initConsumer()) throw new RuntimeException("Rabbit Connection setup failed");
    }

    private boolean initConsumer() {

        try {
            Connection rabbitConnection = rabbitConnectionFactory.newConnection();
            Channel rabbitChannel = rabbitConnection.createChannel();

            rabbitChannel.queueDeclare(appConfig.getRabbitmq().getInboundQueue(), false, false, false, null);

            DeliverCallback messageCallback = (tag, payload) -> {
                String message = new String(payload.getBody(), "UTF-8");
                log.info(message);
            };

            rabbitChannel.basicConsume(
                    appConfig.getRabbitmq().getInboundQueue(),
                    true,
                    messageCallback,
                    (tag) -> {
                    });
        }
        catch(Exception e) {
            return false;
        }

        return true;
    }
}
