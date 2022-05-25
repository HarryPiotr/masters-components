package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class RocketMQConfig {

    private boolean enabled = false;
    private String nameserver;
    private String groupName;
    private String inboundTopic;

}
