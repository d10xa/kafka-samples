package kafkanotypeheaders.foobar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class FooProducerImpl implements FooProducer {

    private final KafkaTemplate<String, FooRecord> template;
    private final String topicName;

    public FooProducerImpl(
            KafkaTemplate<String, FooRecord> template,
            @Value("${topic.myTopic.name}") String topicName) {
        this.template = template;
        this.topicName = topicName;
    }

    @Override
    public ListenableFuture<SendResult<String, FooRecord>> send(FooRecord record) {
        return template.send(topicName, record);
    }
}
