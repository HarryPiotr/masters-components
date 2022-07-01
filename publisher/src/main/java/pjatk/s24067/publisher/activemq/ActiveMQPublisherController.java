package pjatk.s24067.publisher.activemq;

import lombok.NoArgsConstructor;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pjatk.s24067.publisher.config.AppConfig;
import pjatk.s24067.publisher.generic.PublisherController;

import javax.jms.*;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("activemq/publisher")
@NoArgsConstructor
public class ActiveMQPublisherController extends PublisherController {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ActiveMQConnectionFactory activeConnectionFactory;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        try{
            Connection activeConnection = activeConnectionFactory.createConnection();
            activeConnection.start();
            Session activeSession = activeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination outboundQueue = activeSession.createQueue(appConfig.getActivemq().getOutboundQueue());

            MessageProducer activeProducer = activeSession.createProducer(outboundQueue);
            activeProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

            for(int i = 1; i <= count; i++) {
                activeProducer.send(
                        activeSession.createTextMessage(
                                messageOptional.orElse(UUID.randomUUID().toString())
                        )
                );
            }

            activeSession.close();
            activeConnection.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
