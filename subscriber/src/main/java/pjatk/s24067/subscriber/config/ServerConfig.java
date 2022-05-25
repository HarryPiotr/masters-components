package pjatk.s24067.subscriber.config;

import lombok.Data;

@Data
public class ServerConfig {

    private String hostname;
    private int port = 5672;

}
