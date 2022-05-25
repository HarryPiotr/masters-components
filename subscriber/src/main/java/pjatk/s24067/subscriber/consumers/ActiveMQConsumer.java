package pjatk.s24067.subscriber.consumers;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pjatk.s24067.subscriber.config.AppConfig;

import javax.jms.*;

@Component
@ConditionalOnProperty(value = "activemq.enabled", havingValue = "true")
public class ActiveMQConsumer {

    private AppConfig appConfig;
    private ActiveMQConnectionFactory activeConnectionFactory;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public ActiveMQConsumer(AppConfig appConfig, ActiveMQConnectionFactory factory) {
        this.appConfig = appConfig;
        this.activeConnectionFactory = factory;
        initConsumer();
    }

    private void initConsumer() {

        try {

            Connection activeConnection = activeConnectionFactory.createConnection();
            activeConnection.start();

            Session activeSession = activeConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            MessageConsumer activeConsumer = activeSession.createConsumer(
                    activeSession.createQueue(appConfig.getActivemq().getInboundQueue())
            );

            activeConsumer.setMessageListener(message -> {
                try {
                    log.info(((TextMessage) message).getText());
                    message.acknowledge();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
