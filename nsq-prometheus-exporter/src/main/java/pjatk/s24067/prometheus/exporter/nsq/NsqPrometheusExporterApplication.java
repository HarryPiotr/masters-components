package pjatk.s24067.prometheus.exporter.nsq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NsqPrometheusExporterApplication {

	public static void main(String[] args) {
		SpringApplication.run(NsqPrometheusExporterApplication.class, args);
	}

}
