package kafka.learning;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/*
* When a key is assigned, then that key will always go to same partition
* */
public class ProducerDemoKeys {
    private static final Logger LOG = LoggerFactory.getLogger(ProducerDemoKeys.class);

    public static void main(String[] args) {
        LOG.info("This is producer prgm");
        Properties prop = new Properties();
        prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);

//        for (int i = 0; i < 5; i++) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>("firsttopic", "helloworld_");
//asynchronus
        producer.send(record);

//flush data and close producer
        producer.flush();
        producer.close();
//            producer.send(record, (metadata, exception) -> {
//
//                if (exception == null) {
//                    LOG.info("Received new metadata \n" +
//                            "Topic: " + metadata.topic() + "\n" +
//                            "Partition: " + metadata.partition() + "\n" +
//                            "Offset: " + metadata.offset() + "\n" +
//                            "Timestamp: " + metadata.timestamp() + "\n"
//                    );
//                } else {
//                    LOG.error("Error while producing " + exception);
//                }
//            });
//        }


    }
}

