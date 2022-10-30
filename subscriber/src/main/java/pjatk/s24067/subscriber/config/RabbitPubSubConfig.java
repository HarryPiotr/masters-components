package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class RabbitPubSubConfig {

    private boolean enabled = false;
    private String bindingKey;
    private String exchangeName;

}
