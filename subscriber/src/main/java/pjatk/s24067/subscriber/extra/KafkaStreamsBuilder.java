package pjatk.s24067.subscriber.extra;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

import java.util.Properties;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "kafka.streamsEnabled", havingValue = "true")
public class KafkaStreamsBuilder {

    public KafkaStreamsBuilder(AppConfig appConfig) {

        // Konfiguracja właściwości klienta KafkaStreams
        Properties streamsProperties = new Properties();
        // Obowiązkowy identyfikator aplikacji
        streamsProperties.put(
                StreamsConfig.APPLICATION_ID_CONFIG,
                "Subscriber-Application-" + UUID.randomUUID());
        // Podanie adresu brokera Kafki
        streamsProperties.put(
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                appConfig.getKafka().getBootstrapServer()); // "locahost:9094"
        // Zadeklarowanie klas obiektów klucza i wiadomości
        streamsProperties.put(
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.Long().getClass().getName());
        streamsProperties.put(
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        String inboundTopic =
                appConfig.getKafka().getInboundTopic(); // "inbound-topic"
        String outboundTopic =
                appConfig.getKafka().getOutboundTopic(); // "outbound-topic"

        // Utworzenie nowego obiektu klasy StreamsBuilder,
        // służącego do generowania topologii
        StreamsBuilder builder = new StreamsBuilder();

        // Rozpoczęcie generowanie topologii na podstawie topica wejściowego
        builder
                .stream(inboundTopic, Consumed.with(Serdes.Long(), Serdes.String()))
                // Zdefiniowanie odgałęzienia,
                // do którego trafiają wiadomości zaczynające się od cyfry
                .split()
                .branch((k, v) -> v.matches("^[0-9].+$"),
                        Branched.withConsumer(branchStream1 ->
                                branchStream1
                                        // Usunięcie wszystkich liter
                                        // z wiadomości w tym odgałęzieniu
                                        .mapValues(val ->
                                                val.replaceAll("[a-zA-Z]]", ""))
                                        // Wysłanie zmodyfikowanych wiadomości na topic
                                        // outbound-topic-1
                                        .to(outboundTopic + "-1")))
                // Zdefiniowanie odgałęzienia,
                // do którego trafiają wiadomości zaczynające się od litery
                .branch((k, v) -> v.matches("^[a-zA-Z].+$"),
                        Branched.withConsumer(branchStream2 ->
                                branchStream2
                                        // Usunięcie wszystkich cyfr
                                        // z wiadomości w tym odgałęzieniu
                                        .mapValues(val ->
                                                val.replaceAll("[0-9]]", ""))
                                        // Wysłanie zmodyfikowanych wiadomości na topic
                                        // outbound-topic-2
                                        .to(outboundTopic + "-2")));
        // Utworzenie i wystartowanie KafkaStreams na podstawie
        // zdefiniowanej topologii i właściwości
        KafkaStreams streams = new KafkaStreams(builder.build(), streamsProperties);
        streams.start();
    }
}
