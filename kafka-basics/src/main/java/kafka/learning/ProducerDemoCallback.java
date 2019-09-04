package kafka.learning;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoCallback {
    private static final Logger LOG = LoggerFactory.getLogger(ProducerDemoCallback.class);
    private static String topic = "firsttopic";


    public static void main(String[] args) {
        LOG.info("This is producer prgm");
        Properties prop = new Properties();
        prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);

        for (int i = 0; i < 6; i++) {
            String value = "helloworld_"+Integer.toString(i);
            String key = "id_" + Integer.toString(i);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, key, value );
            LOG.info("Key is " + key);

            //asynchronus
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception == null) {
                        LOG.info("Received new metadata \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp() + "\n"
                        );
                    } else {
                        LOG.error("Error while producing " + exception);
                    }
                }
            });
        }
        //flush data and close producer
        producer.flush();
        producer.close();


    }
}

