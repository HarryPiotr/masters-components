package pjatk.s24067.publisher.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
@RequestMapping("sqs/publisher")
@NoArgsConstructor
public class SQSPublisherController extends PublisherController {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private AmazonSQS sqs;
    private String queueName;
    private String queueUrl;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        if(queueName == null)
            queueName = appConfig.getSqs().getQueueName();

        if(queueUrl == null)
            queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();

        try {
            for(int i = 1; i <= count; i++) {
                sqs.sendMessage(
                        new SendMessageRequest(
                                queueUrl,
                                messageOptional.isPresent() ? messageOptional.get() : UUID.randomUUID().toString()
                        )
                );
                super.incrementCounter(queueName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPublisherType() {
        return "sqs";
    }
}
