package pjatk.s24067.publisher.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public abstract class PublisherController {

    private long messagesSentTotal = 0;
    protected Random random = new Random();
    private Map<String, Long> messagesSent = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @PostMapping("/produce")
    public abstract void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                         @RequestParam("message") Optional<String> messageOptional);

    @PostMapping("/produce-continuously")
    public void produceMessagesContinuously(@RequestParam("length") Optional<Integer> messageLength,
                                            @RequestParam("threads") Optional<Integer> threadCountOptional) {

        int threadCount = threadCountOptional.orElse(1);
        if(threadCount <= 0 || threadCount > 100) threadCount = 1;

        for(int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                while(true) {
                    byte[] randomBytes = new byte[messageLength.orElse(10)];
                    random.nextBytes(randomBytes);
                    String randomMessage = new String(randomBytes, Charset.forName("UTF-8"));
                    produceMessages(Optional.of(1), Optional.of(randomMessage));
                }
            }).start();
        }
    }

    protected synchronized void incrementCounter(String topic) {
        if(messagesSent.get(topic) == null)
            messagesSent.put(topic, new Long(1));
        else
            messagesSent.put(topic, messagesSent.get(topic) + 1);
        this.messagesSentTotal++;
    }

    public long getMessagesSentTotal() {
        return messagesSentTotal;
    }

    public Map<String, Long> getMessagesSent() {
        return messagesSent;
    }

    public String getPublisherType() {
        return "generic";
    }
}
