package pjatk.s24067.publisher.nsq;

import com.sproutsocial.nsq.Publisher;
import lombok.NoArgsConstructor;
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
@RequestMapping("nsq/publisher")
@NoArgsConstructor
public class NSQPublisherController extends PublisherController {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Publisher publisher;

    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        try {

            Publisher publisher = getPublisher();

            for(int i = 1; i <= count; i++) {
                publisher.publish(
                        appConfig.getNsq().getOutboundTopic(),
                        messageOptional.isPresent() ? messageOptional.get().getBytes() : UUID.randomUUID().toString().getBytes()
                );
                super.incrementCounter(appConfig.getNsq().getOutboundTopic());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/produce-buffered")
    public void produceMessagesBuffered(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        try {
            Publisher publisher = new Publisher(appConfig.getNsq().getServer());

            for(int i = 1; i <= count; i++) {
                publisher.publishBuffered(
                        appConfig.getNsq().getOutboundTopic(),
                        messageOptional.isPresent() ? messageOptional.get().getBytes() : UUID.randomUUID().toString().getBytes()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized Publisher getPublisher() {
        if(this.publisher == null)
            this.publisher = new Publisher(appConfig.getNsq().getServer());
        return this.publisher;
    }

    @Override
    public String getPublisherType() {
        return "nsq";
    }
}
