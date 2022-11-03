package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class RabbitMSQConfig {

    private String outboundQueue;
    private boolean persistentMessages;
    private boolean exclusiveQueue;

}
