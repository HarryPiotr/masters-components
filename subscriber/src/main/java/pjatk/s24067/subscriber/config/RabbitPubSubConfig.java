package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class RabbitPubSubConfig {

    private boolean enabled = false;
    private String bindingKey;
    private String exchangeName;
    private String queuePrefix;
    private boolean namedQueue;
    private boolean persistentMessages;
    private boolean exclusiveQueue;

}
