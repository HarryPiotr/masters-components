package pjatk.s24067.subscriber.rest;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pjatk.s24067.prometheus.PrometheusMetric;
import pjatk.s24067.subscriber.consumers.GenericConsumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@RestController
@NoArgsConstructor
public class ServiceRestController {

    @Autowired
    private ApplicationContext context;
    public static final List<String> CONTAINED_CONSUMERS = Arrays.asList(
            "ActiveMQConsumer",
            "KafkaConsumer",
            "NSQConsumer",
            "RabbitMQConsumer",
            "RocketMQConsumer"
    );

    @GetMapping("/metrics")
    public void gatherMetrics(HttpServletRequest request, HttpServletResponse response) {

        String responseBody = Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> CONTAINED_CONSUMERS.contains(name))
                .flatMap(name -> {
                    List<PrometheusMetric> metrics = new ArrayList<>();
                    GenericConsumer consumer = (GenericConsumer) context.getBean(name);
                    metrics.add(
                            new PrometheusMetric()
                                    .withName("consumer_messages_received_total")
                                    .withLabel("type", consumer.getConsumerType())
                                    .withValue(consumer.getMessagesReceivedTotal())
                    );
                    consumer
                            .getMessagesReceived()
                            .entrySet()
                            .stream()
                            .forEach(entry -> metrics.add(
                                    new PrometheusMetric()
                                            .withName("consumer_messages_received")
                                            .withLabel("type", consumer.getConsumerType())
                                            .withLabel("topic", entry.getKey())
                                            .withValue(entry.getValue())
                            ));
                    return metrics.stream();
                })
                .map(metric -> metric.toString())
                .collect(Collectors.joining("\n"));

        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());

        // Prometheus only accepts gzip encoding
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())) {
            gzipOutputStream.write(responseBody.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
