package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class SQSConfig {

    private String queueName;
    private String region;
    private String accessKeyId;
    private String accessKeySecret;

}
