package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class RabbitMSQConfig {

    private boolean enabled = false;
    private String inboundQueue;

}
