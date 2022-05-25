package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class KafkaConfig {

    private boolean enabled = false;
    private String bootstrapServer;
    private String consumerGroup;
    private String inboundTopic;

}
