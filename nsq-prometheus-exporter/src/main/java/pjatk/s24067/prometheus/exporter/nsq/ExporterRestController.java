package pjatk.s24067.prometheus.exporter.nsq;

import kotlin.text.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@RestController
public class ExporterRestController {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private StatsdParser parser;

    @GetMapping("/metrics")
    public void fetchAndReturnMetrics(HttpServletRequest request, HttpServletResponse reponse) {

        String nsqStats = new RestTemplate().getForObject(String.format("http://%s/stats", appConfig.getBrokerServer()), String.class);
        String responseBody = parser.parseStats(nsqStats).stream().map(pm -> pm.toString()).collect(Collectors.joining("\n")) + "\n";

        reponse.setContentType(MediaType.TEXT_PLAIN_VALUE);
        reponse.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        reponse.setCharacterEncoding(StandardCharsets.UTF_8.displayName());

        // Prometheus only accepts gzip encoding
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(reponse.getOutputStream())) {
            gzipOutputStream.write(responseBody.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
