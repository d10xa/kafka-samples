package kafkanotypeheaders;

import kafkanotypeheaders.foobar.BarRecord;
import kafkanotypeheaders.foobar.FooProducer;
import kafkanotypeheaders.foobar.FooRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public class KafkaTest {
    @Container
    public static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

    @Autowired
    FooProducer fooProducer;

    @Autowired
    BarSyncConsumer barConsumer;

    @Test
    void test_produce_consume() throws InterruptedException {
        FooRecord foo = new FooRecord();
        foo.setId(2);
        foo.setName("two");
        fooProducer.send(foo);

        BarRecord bar = barConsumer.awaitPayload();
        Assertions.assertEquals(2, bar.getId());
        Assertions.assertEquals("two", bar.getName());
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }
}
