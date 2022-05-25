package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class RabbitMQConfig {

    private boolean enabled = false;
    private ServerConfig server;
    private String inboundQueue;

}
