package pjatk.s24067.subscriber.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Data
public class AppConfig {

    private KafkaConfig kafka = new KafkaConfig();
    private RabbitMQConfig rabbitmq = new RabbitMQConfig();
    private RocketMQConfig rocketmq = new RocketMQConfig();
    private ActiveMQConfig activemq = new ActiveMQConfig();
    private NSQConfig nsq = new NSQConfig();
    private SQSConfig sqs = new SQSConfig();

}
