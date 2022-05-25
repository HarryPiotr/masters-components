package pjatk.s24067.publisher.rocketmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
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
@RequestMapping("rocketmq/publisher")
@NoArgsConstructor
public class RocketMQPublisherController {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("tag") Optional<String> tagOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.orElse(1);
        String tag = tagOptional.orElse("<no-tag>");
        DefaultMQProducer rocketProducer = new DefaultMQProducer(appConfig.getRocketmq().getGroupName());


        try{
            rocketProducer.setNamesrvAddr(appConfig.getRocketmq().getNameserver());
            rocketProducer.start();

            for(int i = 1; i <= count; i++) {
                SendResult response = rocketProducer.send(
                        new Message(
                                appConfig.getRocketmq().getOutboundTopic(),
                                tag,
                                messageOptional.orElse(UUID.randomUUID().toString()).getBytes()
                        )
                );
                log.info("Sent: {}", response.toString());
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally{
            rocketProducer.shutdown();
        }
    }
}
