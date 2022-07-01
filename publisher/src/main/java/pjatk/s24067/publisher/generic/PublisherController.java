package pjatk.s24067.publisher.generic;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

public abstract class PublisherController {


    @PostMapping("/produce")
    public abstract void produceMessages(@RequestParam("count") Optional<Integer> countOptional,
                                         @RequestParam("message") Optional<String> messageOptional);

}
