package pjatk.s24067.subscriber.consumers;

import java.util.HashMap;
import java.util.Map;

public abstract class GenericConsumer {


    private long messagesReceivedTotal = 0;
    private Map<String, Long> messagesReceived = new HashMap<>();


    protected synchronized void incrementCounter(String topic) {
        if(messagesReceived.get(topic) == null)
            messagesReceived.put(topic, new Long(1));
        else
            messagesReceived.put(topic, messagesReceived.get(topic) + 1);
        this.messagesReceivedTotal++;
    }

    public long getMessagesReceivedTotal() {
        return messagesReceivedTotal;
    }

    public Map<String, Long> getMessagesReceived() {
        return messagesReceived;
    }

    public String getConsumerType() {
        return "generic";
    }


}
