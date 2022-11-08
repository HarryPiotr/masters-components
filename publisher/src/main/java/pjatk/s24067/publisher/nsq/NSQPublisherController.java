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

import java.nio.charset.Charset;
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
                produceMessage(
                        publisher,
                        messageOptional.isPresent() ? messageOptional.get() : UUID.randomUUID().toString(),
                        appConfig.getNsq().getOutboundTopic());
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
            Publisher publisher = getPublisher();

            for(int i = 1; i <= count; i++) {
                produceMessageBuffered(
                        publisher,
                        messageOptional.isPresent() ? messageOptional.get() : UUID.randomUUID().toString(),
                        appConfig.getNsq().getOutboundTopic());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/produce-continuously")
    public void produceMessagesContinuously(@RequestParam("length") Optional<Integer> messageLength,
                                            @RequestParam("threads") Optional<Integer> threadCountOptional) {

        int threadCount = threadCountOptional.orElse(1);
        if(threadCount <= 0 || threadCount > 100) threadCount = 1;

        for(int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                Publisher tPublisher = new Publisher(appConfig.getNsq().getServer());
                while(true) {
                    byte[] randomBytes = new byte[messageLength.orElse(10)];
                    random.nextBytes(randomBytes);
                    String randomMessage = new String(randomBytes, Charset.forName("UTF-8"));
                    produceMessage(tPublisher, randomMessage, appConfig.getNsq().getOutboundTopic());
                }
            }).start();
        }
    }

    @PostMapping("/produce-buffered-continuously")
    public void produceMessagesContinuouslyBuffered(@RequestParam("length") Optional<Integer> messageLength,
                                                    @RequestParam("threads") Optional<Integer> threadCountOptional) {

        int threadCount = threadCountOptional.orElse(1);
        if(threadCount <= 0 || threadCount > 100) threadCount = 1;

        for(int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                Publisher tPublisher = new Publisher(appConfig.getNsq().getServer());
                while(true) {
                    byte[] randomBytes = new byte[messageLength.orElse(10)];
                    random.nextBytes(randomBytes);
                    String randomMessage = new String(randomBytes, Charset.forName("UTF-8"));
                    produceMessageBuffered(tPublisher, randomMessage, appConfig.getNsq().getOutboundTopic());
                }
            }).start();
        }
    }

    private void produceMessage(Publisher publisher, String message, String topic) {
        publisher.publish(
                topic,
                message.getBytes()
        );
        super.incrementCounter(topic);
    }

    private void produceMessageBuffered(Publisher publisher, String message, String topic) {
        publisher.publishBuffered(
                topic,
                message.getBytes()
        );
        super.incrementCounter(topic);
    }

    private Publisher getPublisher() {
        if(this.publisher == null)
            this.publisher = new Publisher(appConfig.getNsq().getServer());
        return this.publisher;
    }

    @Override
    public String getPublisherType() {
        return "nsq";
    }
}
