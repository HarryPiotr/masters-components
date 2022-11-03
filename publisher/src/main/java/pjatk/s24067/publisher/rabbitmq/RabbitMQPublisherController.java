package pjatk.s24067.publisher.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
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
import pjatk.s24067.publisher.generic.PublisherController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("rabbitmq/publisher/msq")
@NoArgsConstructor
public class RabbitMQPublisherController extends PublisherController {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ConnectionFactory rabbitFactory;

    private Connection rabbitConnection;
    private Channel rabbitChannel;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private boolean persistentMessages;
    private boolean exclusiveQueue;

    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        try{
            if(rabbitConnection == null || !rabbitConnection.isOpen()) rabbitConnection = rabbitFactory.newConnection();
            if(rabbitChannel == null || !rabbitChannel.isOpen()) {
                rabbitChannel = rabbitConnection.createChannel();
                persistentMessages = appConfig.getRabbitmq().getMsq().isPersistentMessages();
                exclusiveQueue = appConfig.getRabbitmq().getMsq().isExclusiveQueue();
                rabbitChannel.queueDeclare(appConfig.getRabbitmq().getMsq().getOutboundQueue(), persistentMessages, exclusiveQueue, !persistentMessages, null);
            }

            for(int i = 1; i <= count; i++) {
                rabbitChannel.basicPublish(
                        Strings.EMPTY,
                        appConfig.getRabbitmq().getMsq().getOutboundQueue(),
                        persistentMessages ? MessageProperties.MINIMAL_PERSISTENT_BASIC : null,
                        messageOptional.isPresent() ? messageOptional.get().getBytes() : UUID.randomUUID().toString().getBytes()
                );
                super.incrementCounter(appConfig.getRabbitmq().getMsq().getOutboundQueue());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPublisherType() {
        return "rabbitmq/msq";
    }
}
