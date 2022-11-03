package pjatk.s24067.subscriber;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.monitoring.MonitoringEvent;
import com.amazonaws.monitoring.MonitoringListener;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import pjatk.s24067.subscriber.config.AppConfig;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@SpringBootApplication
public class SubscriberApplication {

	@Autowired
	AppConfig appConfig;

	public static void main(String[] args) {
		SpringApplication.run(SubscriberApplication.class, args);
	}

	@Bean
	public ConsumerFactory<String, String> kafkaConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getKafka().getBootstrapServer());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, appConfig.getKafka().getConsumerGroup());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(kafkaConsumerFactory());
		return factory;
	}

	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		ConnectionFactory rabbitFactory = new ConnectionFactory();
		rabbitFactory.setHost(appConfig.getRabbitmq().getServer().getHostname());
		rabbitFactory.setPort(appConfig.getRabbitmq().getServer().getPort());
		return rabbitFactory;
	}

	@Bean
	public ActiveMQConnectionFactory activeConnectionFactory() {
		return new ActiveMQConnectionFactory(
				String.format("tcp://%s", appConfig.getActivemq().getServer())
		);
	}

	@Bean
	@ConditionalOnProperty(value = "sqs.enabled", havingValue = "true")
	public AmazonSQS amazonSqsClient() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(appConfig.getSqs().getAccessKeyId(), appConfig.getSqs().getAccessKeySecret());
		return AmazonSQSClientBuilder
				.standard()
				.withRegion(appConfig.getSqs().getRegion())
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withClientConfiguration(new ClientConfiguration().withConnectionTimeout(30000))
				.build();
	}

}
