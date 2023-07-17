package kafkatopicscreatetestcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Testcontainers
public abstract class KafkaTestBase {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(KafkaTestBase.class);
    public static Network network = Network.newNetwork();
    public static KafkaContainer kafkaContainer =
            new KafkaContainer(
                    DockerImageName
                            .parse("confluentinc/cp-kafka")
            );

    public static GenericContainer<?> kafkaTopicsCreate = new GenericContainer<>(
            DockerImageName.parse("confluentinc/cp-kafka")
    );

    // Uncomment `kafkaUi.start();` to enable KAFKA UI
    public static GenericContainer<?> kafkaUi = new GenericContainer<>(
            DockerImageName.parse("provectuslabs/kafka-ui:v0.7.1")
    );

    @BeforeAll
    static void setUp() {
        kafkaContainer.withReuse(true);
        kafkaContainer.withNetwork(network);
        kafkaContainer.withEnv("TOPIC_AUTO_CREATE", "false");
        kafkaContainer.setNetworkAliases(Collections.singletonList("kafka"));
        kafkaContainer.start();

        int kafkaUiPort=8080;
        kafkaUi.withNetwork(network);
        kafkaUi.addExposedPort(kafkaUiPort);
        kafkaUi.withEnv("KAFKA_CLUSTERS_0_BOOTSTRAP_SERVERS", "kafka:9092");
        kafkaUi.withEnv("KAFKA_CLUSTERS_0_NAME", "kraft");
//        kafkaUi.start();
//        log.info("KAFKA UI: localhost:"+kafkaUi.getMappedPort(kafkaUiPort));

        String topicsStr = String.join(
                " ",
                "kafkatopicscreatetestcontainers.foo",
                "kafkatopicscreatetestcontainers.bar"
        );
        kafkaTopicsCreate.dependsOn(kafkaContainer);
        kafkaTopicsCreate.withEnv("KAFKA_HOST", "kafka");
        kafkaTopicsCreate.withEnv("KAFKA_PORT", "9092");
        kafkaTopicsCreate.withNetwork(network);
        kafkaTopicsCreate.withCommand(
                "/bin/sh", "-c",
                "set -e\n" +
                "set -x\n" +
                "topics_to_create='" + topicsStr + "'\n" +
                "for topic in $topics_to_create\n" +
                "do\n" +
                "  echo create $topic\n" +
                "  kafka-topics \\\n" +
                "  --bootstrap-server $KAFKA_HOST:$KAFKA_PORT \\\n" +
                "  --create \\\n" +
                "  --if-not-exists \\\n" +
                "  --topic $topic \\\n" +
                "  --replication-factor 1 \\\n" +
                "  --partitions 1\n" +
                "done\n" +
                "echo 'kafka topics successfully created'\n"
        );
        kafkaTopicsCreate.waitingFor(new LogMessageWaitStrategy()
                .withRegEx(".*kafka topics successfully created.*\\n")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS)));
        kafkaTopicsCreate.withLogConsumer(new Slf4jLogConsumer(log).withPrefix("kafkaTopicsCreate"));
        kafkaTopicsCreate.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }
}
