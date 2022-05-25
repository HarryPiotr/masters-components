package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class RabbitMQConfig {

    private ServerConfig server;
    private String outboundQueue;

}
