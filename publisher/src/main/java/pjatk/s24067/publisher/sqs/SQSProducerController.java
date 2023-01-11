package pjatk.s24067.publisher.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pjatk.s24067.publisher.config.AppConfig;
import pjatk.s24067.publisher.generic.ProducerController;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("sqs")
@NoArgsConstructor
@ConditionalOnExpression("${sqs.enabled}")
public class SQSProducerController extends ProducerController {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private AmazonSQS sqs;
    private String queueName;
    private String queueUrl;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private static final byte[] ALLOWED_BYTES = {
            32,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
            65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
            97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122
    };
    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        if(queueName == null) {
            queueName = appConfig.getSqs().getQueueName();
            queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        }

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
    @PostMapping("/produce-continuously")
    public void produceMessagesContinuously(@RequestParam("length") Optional<Integer> messageLengthOptional,
                                            @RequestParam("threads") Optional<Integer> threadCountOptional) {

        int threadCount = threadCountOptional.orElse(1);
        if(threadCount <= 0 || threadCount > 100) threadCount = 1;
        int messageLength = messageLengthOptional.orElse(10);

        for(int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                byte[] randomBytes = new byte[messageLength];
                for(int j = 0; j < messageLength; j++) {
                    byte b = (byte)Math.abs(random.nextInt() % 128);
                    randomBytes[j] = ALLOWED_BYTES[b % ALLOWED_BYTES.length];
                }
                String message = new String(randomBytes, Charset.forName("UTF-8"));
                while(true) {
                    produceMessages(Optional.of(1), Optional.of(message));
                }
            }).start();
        }
    }

    @Override
    public String getPublisherType() {
        return "sqs";
    }
}
