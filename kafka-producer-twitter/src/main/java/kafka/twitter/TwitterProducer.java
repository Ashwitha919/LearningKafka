package kafka.twitter;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterProducer.class);

    private static final String CONSUMER_KEY = "";
    private static final String CONSUMER_SECRET = "";
    private static final String TOKEN = "";
    private static final String SECRET = "";

    public TwitterProducer() {
    }

    public static void main(String[] args) {

        new TwitterProducer().run();
    }

    public void run() {
        LOGGER.info("Set up");
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(2);

        /*create twitter client
         Attempts to establish a connection.*/
        Client client = createTwitterClient(msgQueue);
        client.connect();

        //create kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        //add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Stopping application...");
            LOGGER.info("shutting down client from twitter...");
            client.stop();
            LOGGER.info("Closing Producer...");
            producer.close();
            LOGGER.info("Done!!!");
        }));


        /*loop to send tweets to kafka
         on a different thread, or multiple different threads....*/
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if (msg != null) {
                LOGGER.info("" + msg);
                producer.send(new ProducerRecord<>("twitterTweets", null, msg), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception != null)
                            LOGGER.error("something bad happened", exception);
                    }
                });
            }
        }
        LOGGER.info("End of application");

    }

    public Client createTwitterClient(BlockingQueue msgQueue) {

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        // Optional: set up some followings and track terms
        List<String> terms = Lists.newArrayList("target","soccer","politics","sports");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, SECRET);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));
//                .eventMessageQueue(msgQueue);                          // optional: use this if you want to process client events

        Client hosebirdClient = builder.build();
        return hosebirdClient;
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        Properties prop = new Properties();
        prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        /*create a safe producer meaning idempotent*/
        prop.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,"true");
        prop.setProperty(ProducerConfig.ACKS_CONFIG,"all");
        prop.setProperty(ProducerConfig.RETRIES_CONFIG,Integer.toString((Integer.MAX_VALUE)));
        prop.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,"5"); //as kafka 2.0>=1.1

        // high throughput producer at the expense of latency
        prop.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy");
        prop.setProperty(ProducerConfig.LINGER_MS_CONFIG,"20");
        prop.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,Integer.toString(32*1024)); //32KB



        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);

        return producer;
    }

}





























