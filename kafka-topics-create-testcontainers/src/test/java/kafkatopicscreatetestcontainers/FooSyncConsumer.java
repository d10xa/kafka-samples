package kafkatopicscreatetestcontainers;

import kafkatopicscreatetestcontainers.foo.FooRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class FooSyncConsumer {
    private CountDownLatch latch = new CountDownLatch(1);
    private FooRecord payload;

    @KafkaListener(
            topics = "${topic.myTopic.name}",
            groupId = "FooSyncConsumer",
            properties = "spring.json.value.default.type=kafkatopicscreatetestcontainers.foo.FooRecord"
    )
    public void receive(ConsumerRecord<String, FooRecord> consumerRecord) {
        this.payload = consumerRecord.value();
        latch.countDown();
    }

    public FooRecord awaitPayload() throws InterruptedException {
        boolean messageConsumed = this.latch.await(500, TimeUnit.MILLISECONDS);
        if (!messageConsumed) {
            throw new RuntimeException("message not consumed");
        } else {
            this.latch = new CountDownLatch(1);
            return this.payload;
        }
    }

}
