package pjatk.s24067.publisher.dryrun;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pjatk.s24067.publisher.config.AppConfig;
import pjatk.s24067.publisher.generic.ProducerController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("dry-run")
@NoArgsConstructor
@ConditionalOnExpression("${dryrun.enabled}")
public class DryRunProducerController extends ProducerController {

    @Autowired
    private AppConfig appConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    @PostMapping("/produce")
    public void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                @RequestParam("message") Optional<String> messageOptional) {

        int count = countOptional.isPresent() ? countOptional.get() : 1;

        for(int i = 1; i <= count; i++) {
            String s = messageOptional.isPresent() ? messageOptional.get() : UUID.randomUUID().toString();
            log.debug("Sent dry message");
            super.incrementCounter("none");
        }
    }

    @Override
    public String getPublisherType() {
        return "dry-run";
    }
}
