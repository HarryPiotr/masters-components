package pjatk.s24067.publisher.generic;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pjatk.s24067.prometheus.PrometheusMetric;

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

    @GetMapping("/metrics")
    public void gatherMetrics(HttpServletRequest request, HttpServletResponse response) {

        String responseBody = Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> name.endsWith("ProducerController"))
                .flatMap(name -> {
                    List<PrometheusMetric> metrics = new ArrayList<>();
                    ProducerController controller = (ProducerController) context.getBean(name);
                    metrics.add(
                            new PrometheusMetric()
                                    .withName("producer_messages_sent_total")
                                    .withLabel("type", controller.getPublisherType())
                                    .withValue(controller.getMessagesSentTotal())
                    );
                    controller
                            .getMessagesSent()
                            .entrySet()
                            .stream()
                            .forEach(entry -> metrics.add(
                                    new PrometheusMetric()
                                            .withName("producer_messages_sent")
                                            .withLabel("type", controller.getPublisherType())
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
