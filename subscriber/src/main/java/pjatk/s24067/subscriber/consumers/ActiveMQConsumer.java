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
public class ActiveMQConsumer extends GenericConsumer {

    private AppConfig appConfig;
    private ActiveMQConnectionFactory activeConnectionFactory;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public ActiveMQConsumer(AppConfig appConfig, ActiveMQConnectionFactory factory) throws InterruptedException {
        this.appConfig = appConfig;
        this.activeConnectionFactory = factory;
        while(!initConsumer()) Thread.sleep(5000);
    }

    private boolean initConsumer() {

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
                    incrementCounter(appConfig.getActivemq().getInboundQueue());
                    message.acknowledge();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getConsumerType() {
        return "activemq";
    }
}
