package pjatk.s24067.publisher;

import com.rabbitmq.client.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pjatk.s24067.publisher.activemq.ActiveMQPublisherController;
import pjatk.s24067.publisher.config.AppConfig;
import pjatk.s24067.publisher.generic.PublisherController;
import pjatk.s24067.publisher.kafka.KafkaPublisherController;

@SpringBootApplication
public class PublisherApplication {

	@Autowired
	AppConfig appConfig;

	public static void main(String[] args) {
		SpringApplication.run(PublisherApplication.class, args);
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

}
