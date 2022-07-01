package pjatk.s24067.prometheus.exporter.nsq;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pjatk.s24067.prometheus.exporter.PrometheusMetric;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsdParser {

    @Autowired
    private AppConfig appConfig;

    public List<PrometheusMetric> parseStats(String statsResponse) {

        List<PrometheusMetric> result = new ArrayList<>();
        String[] lines = statsResponse.split("\n");
        String basePrefix = "nsq_broker";
        String currentPrefix = "";
        String currentTopic = "";
        String currentChannel = "";
        String currentConsumer = "";
        String currentProducer = "";

        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if(line.matches("^[a-zA-Z]+: *$")) {
                currentPrefix = line.replace(":", "").trim().toLowerCase();
                continue;
            }
            if(line.matches("^start_time .+")) {
                result.add(
                        new PrometheusMetric()
                                .withName("nsq_broker_start_time_ms")
                                .withLabel("broker", appConfig.getBrokerServer())
                                .withValue(new DateTime(line.replace("start_time ", "").trim()).getMillis())

                );
            }
            if(line.matches("^uptime .+")) {
                result.add(
                        new PrometheusMetric()
                                .withName("nsq_broker_uptime_ms")
                                .withLabel("broker", appConfig.getBrokerServer())
                                .withValue(parsePeriodStringToMillis(line.replace("uptime ", "").trim()))

                );
            }
            if(line.matches("^Health: .+")) {
                result.add(
                        new PrometheusMetric()
                                .withName("nsq_broker_health")
                                .withLabel("broker", appConfig.getBrokerServer())
                                .withValue(line.replace("Health: ", "").trim().equals("OK") ? 1.0 : 0.0)

                );
            }
            if(line.matches("^   [a-z_]+\\s+[0-9.]+")) {
                List<String> tokens = Arrays.stream(line.split("\\s")).filter(t -> !t.isEmpty()).collect(Collectors.toList());
                String name = String.format("%s_%s_%s", basePrefix, currentPrefix, tokens.get(0));
                result.add(
                        new PrometheusMetric()
                                .withName(name)
                                .withLabel("broker", appConfig.getBrokerServer())
                                .withValue(Double.parseDouble(tokens.get(1)))
                );
            }
            if(currentPrefix.equals("topics")) {
                // parse Topic
                if(line.startsWith("   [")) {
                    currentTopic = line.split("\\[")[1].split("\\]")[0].trim();
                    Map<String, Double> kv = parseKVLine(line.split("\\]")[1].trim());
                    for(Map.Entry<String, Double> entry : kv.entrySet())
                        result.add(
                                new PrometheusMetric()
                                        .withName(String.format("%s_%s_%s", basePrefix, "topic", entry.getKey()))
                                        .withLabel("broker", appConfig.getBrokerServer())
                                        .withLabel("topic", currentTopic)
                                        .withValue(entry.getValue())
                        );
                }
                // parse Channel
                if(line.startsWith("      [")) {
                    currentChannel = line.split("\\[")[1].split("\\]")[0].trim();
                    Map<String, Double> kv = parseKVLine(line.split("\\]")[1].trim());
                    for(Map.Entry<String, Double> entry : kv.entrySet())
                        result.add(
                                new PrometheusMetric()
                                        .withName(String.format("%s_%s_%s", basePrefix, "channel", entry.getKey()))
                                        .withLabel("broker", appConfig.getBrokerServer())
                                        .withLabel("topic", currentTopic)
                                        .withLabel("channel", currentChannel)
                                        .withValue(entry.getValue())
                        );
                }
                // parse Consumers
                if(line.startsWith("        [")) {
                    currentConsumer = line.split("\\[")[1].split("\\]")[0].trim();
                    Map<String, Double> kv = parseKVLine(line.split("\\]")[1].trim());
                    for(Map.Entry<String, Double> entry : kv.entrySet())
                        result.add(
                                new PrometheusMetric()
                                        .withName(String.format("%s_%s_%s", basePrefix, "consumer", entry.getKey()))
                                        .withLabel("broker", appConfig.getBrokerServer())
                                        .withLabel("topic", currentTopic)
                                        .withLabel("channel", currentChannel)
                                        .withLabel("consumer", currentConsumer)
                                        .withValue(entry.getValue())
                        );
                }
            }
            if(currentPrefix.equals("producers")) {
                // parse Producers
                if (line.startsWith("   [")) {
                    currentProducer = line.split("\\[")[1].split("\\]")[0].trim();
                    Map<String, Double> kv = parseProducerLine(line.split("\\]")[1].trim());
                    for (Map.Entry<String, Double> entry : kv.entrySet()) {

                        if (entry.getKey().startsWith("TOPICS/"))
                            result.add(
                                    new PrometheusMetric()
                                            .withName(String.format("%s_%s_%s", basePrefix, "producer", "msgs_topic"))
                                            .withLabel("producer", currentProducer)
                                            .withLabel("topic", entry.getKey().replace("TOPICS/", ""))
                                            .withValue(entry.getValue()));
                        else
                            result.add(
                                    new PrometheusMetric()
                                            .withName(String.format("%s_%s_%s", basePrefix, "producer", entry.getKey()))
                                            .withLabel("producer", currentProducer)
                                            .withValue(entry.getValue()));
                    }
                }
            }
        }
        return result;
    }

    private Double parsePeriodStringToMillis(String s) {

        Period period = new Period();
        String[] firstSplit = s.split("h");
        if(firstSplit.length > 1)
            period = period.plusMillis(Integer.parseInt(firstSplit[0]) * 3600000);
        String[] secondSplit = firstSplit[firstSplit.length - 1].split("m");
        if(secondSplit.length > 1)
            period = period.plusMillis(Integer.parseInt(secondSplit[0]) * 60000);
        String[] thirdSplit = secondSplit[secondSplit.length - 1].split("s");
        period = period.plusMillis((int) (Double.parseDouble(thirdSplit[0]) * 1000.0));

        return Double.valueOf(period.getMillis());
    }

    private Map<String, Double> parseKVLine(String s) {
        Map<String, Double> result = new HashMap<>();
        Arrays.stream(s.replace(": ", ":").split("\\s+"))
                .filter( t -> t.matches("^[a-z_\\-%]+:[0-9.hms]+$"))
                .forEach( t -> result.put(
                        t.split(":")[0]
                                .trim()
                                .replace("%", "")
                                .replace("-", "")
                                .replace("_", ""),
                        parseValue(t.split(":")[1].trim())));
        return result;
    }

    private Map<String, Double> parseProducerLine(String s) {

        Map<String, Double> result = new HashMap<>();

        Arrays.stream(s
                        .replaceAll("([a-zA-Z0-9_\\-]+=[0-9]+)", "TOPICS/$1")
                        .replace("msgs: ", "msgs=")
                        .replace("connected: ", "connected=")
                        .replace("topics: ", "")
                        .split("\\s+"))
                .forEach(t ->
                        result.put(
                                t.split("=")[0].trim(),
                                parseValue(t.split("=")[1].trim())
                        )
                );

        return result;
    }



    private Double parseValue(String s) {
        if(s.matches("^[0-9.]+$"))
            return Double.parseDouble(s);
        if(s.matches("^[0-9]+h[0-9]+m[0-9.]+s$") || s.matches("^[0-9]+m[0-9.]+s$") || s.matches("^[0-9.]+s$"))
            return parsePeriodStringToMillis(s);
        return null;
    }
}
