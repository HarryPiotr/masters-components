package pjatk.s24067.subscriber.extra;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

@Component
@ConditionalOnProperty(value = "kafka.camelEnabled", havingValue = "true")
public class KafkaRouteBuilder extends RouteBuilder {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public void configure() throws Exception {

        String fromString = String.format(
                "kafka:%s?brokers=%s",
                appConfig.getKafka().getInboundTopic(),
                appConfig.getKafka().getBootstrapServer());

        String toString = String.format(
                "kafka:%s?brokers=%s",
                appConfig.getKafka().getOutboundTopic(),
                appConfig.getKafka().getBootstrapServer());

        from(fromString)
                .routeId("SimpleKafkaRoute")
                .log(LoggingLevel.INFO, "Body: ${body}")
                .log(LoggingLevel.INFO, "Headers: ${headers}")
                .to(toString);
    }
}
