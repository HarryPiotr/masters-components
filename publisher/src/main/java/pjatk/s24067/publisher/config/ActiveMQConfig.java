package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class ActiveMQConfig {

    private String server;
    private String outboundQueue;

}
