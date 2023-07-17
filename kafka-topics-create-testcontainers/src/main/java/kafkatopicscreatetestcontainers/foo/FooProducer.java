package kafkatopicscreatetestcontainers.foo;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

public interface FooProducer {
    ListenableFuture<SendResult<String, FooRecord>> send(FooRecord record);
}
