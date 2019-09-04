package kafka.learning;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);
    private static String server = "localhost:9092";
    private static String GROUP_ID = "my_first_application";
    private static String OFFSETCONFIG = "earliest";
    private static String TOPIC = "firsttopic";

    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        prop.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        prop.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSETCONFIG);

        //create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

        //subscribe topics
        consumer.subscribe(Collections.singleton(TOPIC));

        //poll for new data

        while (true) {

//            consumer.poll(100); //new kafka 2.0.0 poll is deprecated
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord record : records) {
                logger.info("key is " + record.key() + " Value " + record.value()
                        + " offsets " + record.offset() + " partition " + record.partition());
            }
        }
    }
}
