package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class KafkaConfig {

    private String bootstrapServer;
    private String outboundTopic;
    private String acks;

}
