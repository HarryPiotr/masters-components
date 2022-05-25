package pjatk.s24067.publisher.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pjatk.s24067.publisher.config.AppConfig;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("rabbitmq/publisher")
@NoArgsConstructor
public class RabbitMQPublisherController {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ConnectionFactory rabbitFactory;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        try{
            Connection rabbitConnection = rabbitFactory.newConnection();
            Channel rabbitChannel = rabbitConnection.createChannel();
            rabbitChannel.queueDeclare(appConfig.getRabbitmq().getOutboundQueue(), false, false, false, null);

            for(int i = 1; i <= count; i++) {
                rabbitChannel.basicPublish(
                        Strings.EMPTY,
                        appConfig.getRabbitmq().getOutboundQueue(),
                        null,
                        messageOptional.isPresent() ? messageOptional.get().getBytes() : UUID.randomUUID().toString().getBytes()
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
