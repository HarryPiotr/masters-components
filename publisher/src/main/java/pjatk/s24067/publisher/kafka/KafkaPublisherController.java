package pjatk.s24067.publisher.kafka;

import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pjatk.s24067.publisher.config.AppConfig;
import pjatk.s24067.publisher.generic.PublisherController;

import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@RestController
@RequestMapping("kafka/publisher")
@NoArgsConstructor
public class KafkaPublisherController extends PublisherController {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private KafkaProducer<Long, String> publisher;

    public Producer<Long, String> getPublisher() {
        if(publisher == null)
            publisher = new KafkaProducer<>(createKafkaConfigMap());
        return publisher;
    }

    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;
        Producer<Long, String> publisher = getPublisher();

        try {
            for(int i = 1; i <= count; i++) {
                ProducerRecord<Long, String> record = new ProducerRecord<>(
                        appConfig.getKafka().getOutboundTopic(),
                        Instant.now().toEpochMilli(),
                        messageOptional.isPresent() ? messageOptional.get() : UUID.randomUUID().toString());
                RecordMetadata metadata = publisher.send(record).get();
                log.info("Sent message to partition {} offset {}", metadata.partition(), metadata.offset());
            }
        } catch(Exception e) {
            e.printStackTrace();
            publisher.close();
            publisher = null;
        } finally {
            publisher.flush();
        }
    }

    private Properties createKafkaConfigMap() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getKafka().getBootstrapServer());
        properties.put(ProducerConfig.ACKS_CONFIG, appConfig.getKafka().getAcks());
        // Serializers
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }
}
