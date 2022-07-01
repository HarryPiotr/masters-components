package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class NSQConfig {

    private String server;
    private String outboundTopic;

}
