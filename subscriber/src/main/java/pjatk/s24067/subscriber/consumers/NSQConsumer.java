package pjatk.s24067.subscriber.consumers;

import com.sproutsocial.nsq.Subscriber;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

@Component
@ConditionalOnProperty(value = "nsq.enabled", havingValue = "true")
public class NSQConsumer extends GenericConsumer {

    private static AppConfig appConfig;
    private static Logger log;
    private static NSQConsumer instance;

    public NSQConsumer(AppConfig appConfig) {
        instance = this;
        this.appConfig = appConfig;
        log = LoggerFactory.getLogger(this.getClass().getName());
        for(int i = 0; i < appConfig.getNsq().getConsumerCount(); i++) {
            new Thread(() -> initConsumer()).start();
        }
    }

    private static void handleData(byte[] data) {

        log.info(new String(data));
        getInstance().incrementCounter(appConfig.getNsq().getInboundTopic());

    }

    private void initConsumer() {

        log.info("Starting consumer: {}", appConfig.getNsq());
        new Subscriber(appConfig.getNsq().getServer()).subscribe(appConfig.getNsq().getInboundTopic(), appConfig.getNsq().getChannel(), NSQConsumer::handleData);
        log.info("Subscribed!");

    }

    @Override
    public String getConsumerType() {
        return "nsq";
    }

    private static NSQConsumer getInstance() {
        return instance;
    }

}
