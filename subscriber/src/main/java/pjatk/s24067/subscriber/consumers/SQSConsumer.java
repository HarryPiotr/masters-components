package pjatk.s24067.subscriber.consumers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.sproutsocial.nsq.Subscriber;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "sqs.enabled", havingValue = "true")
public class SQSConsumer extends GenericConsumer {

    private static AppConfig appConfig;
    private AmazonSQS sqs;
    private Logger log;
    private String queueName;
    private String queueUrl;

    public SQSConsumer(AppConfig appConfig, AmazonSQS sqs) throws InterruptedException {
        this.appConfig = appConfig;
        this.sqs = sqs;
        this.log = LoggerFactory.getLogger(this.getClass().getName());

        this.queueName = appConfig.getSqs().getQueueName();
        this.queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();

        initConsumer();
    }

    private void handleData(String data) {
        log.info("Received: {}", data);
        incrementCounter(queueName);
    }

    private void initConsumer() throws InterruptedException {

        log.info("Initiating {} SQS Consumers", appConfig.getSqs().getConsumerCount());

        for(int i = 0; i < appConfig.getSqs().getConsumerCount(); i++) {
            log.info("Setting up the Long-Polling Consumer");

            new Thread(() -> {
                ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withWaitTimeSeconds(20);

                while(true) {
                    log.info("Polling...");
                    List<DeleteMessageBatchRequestEntry> messagesReceipts = sqs.receiveMessage(receiveRequest)
                            .getMessages()
                            .stream()
                            .peek(message -> handleData(message.getBody()))
                            .map(message -> new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()))
                            .collect(Collectors.toList());
                    if(!messagesReceipts.isEmpty())
                        sqs.deleteMessageBatch(
                                new DeleteMessageBatchRequest(
                                        queueUrl,
                                        messagesReceipts
                                )
                        );
                }
            }).start();
        }
    }

    @Override
    public String getConsumerType() {
        return "sqs";
    }

}
