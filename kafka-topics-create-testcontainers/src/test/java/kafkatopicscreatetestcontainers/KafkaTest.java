package kafkatopicscreatetestcontainers;

import kafkatopicscreatetestcontainers.foo.FooProducer;
import kafkatopicscreatetestcontainers.foo.FooRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public class KafkaTest extends KafkaTestBase {

    @Autowired
    FooProducer fooProducer;

    @Autowired
    FooSyncConsumer fooConsumer;

    @Test
    void test_produce_consume() throws InterruptedException {
        FooRecord foo = new FooRecord();
        foo.setId(1);
        foo.setName("one");
        fooProducer.send(foo);

        FooRecord bar = fooConsumer.awaitPayload();
        Assertions.assertEquals(1, bar.getId());
        Assertions.assertEquals("one", bar.getName());
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }
}
