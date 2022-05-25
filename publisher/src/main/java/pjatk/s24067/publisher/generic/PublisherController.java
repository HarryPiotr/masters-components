package pjatk.s24067.publisher.generic;

import java.util.Optional;

public interface PublisherController {

    void produceMessages(Optional<Integer> i, Optional<String> message);

}
