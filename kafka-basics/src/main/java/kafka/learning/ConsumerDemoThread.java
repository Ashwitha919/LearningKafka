package kafka.learning;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoThread {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemoThread.class);
    private static String server = "localhost:9092";
    private static String GROUP_ID = "my_first_application";
    private static String OFFSETCONFIG = "earliest";
    private static String TOPIC = "firsttopic";

    public static void main(String[] args) {
        new ConsumerDemoThread().run();
    }


    private void run() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        //create a consumer runnable
        logger.info("creating a consumer thread");
        Runnable myConsumeRunnable = new ConsumerThread(countDownLatch);
        //start a thread
        Thread myThread = new Thread(myConsumeRunnable);
        myThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    logger.info("caught shutdown hook");
                    ((ConsumerThread) myConsumeRunnable).shutdown();
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        logger.info("application has exited");
                    }

                }
        ));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("application is interrupted");
        } finally {
            logger.info("application is closing");
        }
    }


    public class ConsumerThread implements Runnable {

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;

        public ConsumerThread(CountDownLatch latch) {

            Properties prop = new Properties();
            prop.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
            prop.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            prop.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            prop.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
            prop.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSETCONFIG);

            //create consumer
            this.consumer = new KafkaConsumer<String, String>(prop);
            //subscribe topics
            consumer.subscribe(Collections.singleton(TOPIC));

            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                while (true) {

//            consumer.poll(100); //new kafka 2.0.0 poll is deprecated
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord record : records) {
                        logger.info("key is " + record.key() + " Value " + record.value()
                                + " offsets " + record.offset() + " partition " + record.partition());
                    }
                }
            } catch (WakeupException e) {
                logger.info("received shut down signal!");
            } finally {
                consumer.close();
                //to tell main method to done with consumer
                latch.countDown();
            }
        }

        //is a special method to interrupt consumer.poll
        public void shutdown() {
            //it will throw a wake exception
            consumer.wakeup();
        }
    }
}
