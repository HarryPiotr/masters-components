package pjatk.s24067.publisher.config;

import lombok.Data;

@Data
public class ServerConfig {

    private String hostname;
    private int port = 5672;

}
