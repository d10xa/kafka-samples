package kafkanotypeheaders;

import kafkanotypeheaders.foobar.BarRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class BarSyncConsumer {
    private CountDownLatch latch = new CountDownLatch(1);
    private BarRecord payload;

    @KafkaListener(
            topics = "${topic.myTopic.name}",
            groupId = "BarSyncConsumer",
            properties = "spring.json.value.default.type=kafkanotypeheaders.foobar.BarRecord"
    )
    public void receive(ConsumerRecord<String, BarRecord> consumerRecord) {
        this.payload = consumerRecord.value();
        latch.countDown();
    }

    public BarRecord awaitPayload() throws InterruptedException {
        boolean messageConsumed = this.latch.await(500, TimeUnit.MILLISECONDS);
        if (!messageConsumed) {
            throw new RuntimeException("message not consumed");
        } else {
            this.latch = new CountDownLatch(1);
            return this.payload;
        }
    }

}
