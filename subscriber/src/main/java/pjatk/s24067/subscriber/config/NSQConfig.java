package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class NSQConfig {

    private boolean enabled = false;
    private String server;
    private String inboundTopic;
    private String channel;
    private int consumerCount;

}
