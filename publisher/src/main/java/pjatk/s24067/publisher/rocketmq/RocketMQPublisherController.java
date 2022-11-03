package pjatk.s24067.publisher.rocketmq;

import lombok.NoArgsConstructor;
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
import pjatk.s24067.publisher.generic.PublisherController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("rocketmq/publisher")
@NoArgsConstructor
public class RocketMQPublisherController extends PublisherController {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private DefaultMQProducer rocketProducer;


    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {
        produceMessages(countOptional, Optional.of("<no-tag>"), messageOptional);
    }

    @PostMapping("/produce-with-tag")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("tag") Optional<String> tagOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.orElse(1);
        String tag = tagOptional.orElse("<no-tag>");


        try{

            if(rocketProducer == null) {
                rocketProducer = new DefaultMQProducer(appConfig.getRocketmq().getGroupName());
                rocketProducer.setNamesrvAddr(appConfig.getRocketmq().getNameserver());
                rocketProducer.start();
            }

            for(int i = 1; i <= count; i++) {
                SendResult response = rocketProducer.send(
                        new Message(
                                appConfig.getRocketmq().getOutboundTopic(),
                                tag,
                                messageOptional.orElse(UUID.randomUUID().toString()).getBytes()
                        )
                );
                super.incrementCounter(appConfig.getRocketmq().getOutboundTopic());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPublisherType() {
        return "rocketmq";
    }
}
