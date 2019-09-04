package kafka.learning;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/*
* To read from particular partition and from particular offset. Also can set how many messages to read for example,
* read from offset 4 to offset 8
* */
public class ConsumerDemoAssignNdSeek {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignNdSeek.class);
    private static String server = "localhost:9092";
    private static String OFFSETCONFIG = "earliest";
    private static String TOPIC = "firsttopic";

    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        prop.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSETCONFIG);

        //create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

        //subscribe topics
//        consumer.subscribe(Collections.singleton(TOPIC));

        TopicPartition partitionToReadFrom=new TopicPartition(TOPIC,0);
        //assign and seek are mostly used to replay data and fetch specific message
        Long offsetToReadFrom=2l;
        consumer.assign(Arrays.asList(partitionToReadFrom));

        //seek
        consumer.seek(partitionToReadFrom,offsetToReadFrom);

        int numOfMsgToRead=5;
        boolean keepOnReading=true;
        int numOfMsgReadSofar=0;
        //poll for new data

        while (keepOnReading) {

//            consumer.poll(100); //new kafka 2.0.0 poll is deprecated
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord record : records) {
                logger.info("key is " + record.key() + " Value " + record.value()
                        + " offsets " + record.offset() + " partition " + record.partition());
                if (numOfMsgReadSofar>=numOfMsgToRead){
                    keepOnReading=false;
                    break;
                }
            }
        }
        logger.info("exiting the application");
    }
}
