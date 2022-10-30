package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class RabbitMQConfig {

    private ServerConfig server;
    private RabbitMSQConfig msq;
    private RabbitPubSubConfig pubsub;

}
