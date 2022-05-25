package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class RocketMQConfig {

    private String nameserver;
    private String groupName;
    private String outboundTopic;

}
