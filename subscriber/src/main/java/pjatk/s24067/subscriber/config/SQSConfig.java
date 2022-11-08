package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class SQSConfig {

    private boolean enabled = false;
    private String queueName;
    private String region;
    private String accessKeyId;
    private String accessKeySecret;
    private int consumerCount;

}
