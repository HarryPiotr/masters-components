package pjatk.s24067.publisher.kafka;

import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.*;
import pjatk.s24067.publisher.config.AppConfig;
import pjatk.s24067.publisher.generic.ProducerController;

import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@RestController
@RequestMapping("kafka")
@NoArgsConstructor
@ConditionalOnExpression("${kafka.enabled}")
public class KafkaProducerController extends ProducerController {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private KafkaProducer<Long, String> producer;

    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        try(Producer<Long, String> producer = getProducer()) {
            for(int i = 1; i <= count; i++) {
                ProducerRecord<Long, String> record = new ProducerRecord<>(
                        appConfig.getKafka().getOutboundTopic(),
                        Instant.now().toEpochMilli(),
                        messageOptional.isPresent()
                                ? messageOptional.get()
                                : UUID.randomUUID().toString());
                RecordMetadata metadata = producer.send(record).get();
                log.debug("Sent message to partition {} offset {}",
                        metadata.partition(),
                        metadata.offset());
                super.incrementCounter(appConfig.getKafka().getOutboundTopic());
            }
            producer.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Producer<Long, String> getProducer() {
        return new KafkaProducer<>(createKafkaConfigMap());
    }

    private Properties createKafkaConfigMap() {
        Properties properties = new Properties();
        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                appConfig.getKafka().getBootstrapServer());
        properties.put(
                ProducerConfig.ACKS_CONFIG,
                appConfig.getKafka().getAcks());
        // Serializers
        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        return properties;
    }

    @Override
    public String getPublisherType() {
        return "kafka";
    }
}
