package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class RabbitPubSubConfig {

    private String routingKey;
    private String exchangeName;
    private boolean persistentMessages;

}
