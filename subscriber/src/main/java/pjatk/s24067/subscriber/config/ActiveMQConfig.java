package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class ActiveMQConfig {

    private boolean enabled = false;
    private String server;
    private String groupName;
    private String inboundQueue;

}
