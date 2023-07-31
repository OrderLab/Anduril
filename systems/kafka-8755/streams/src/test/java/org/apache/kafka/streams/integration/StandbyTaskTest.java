/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.integration;

import kafka.utils.MockTime;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.processor.StateRestoreListener;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.test.IntegrationTest;
import org.apache.kafka.test.TestUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@Category({IntegrationTest.class})
public class StandbyTaskTest {

    private static final int NUM_BROKERS = 1;

    @ClassRule
    public static final EmbeddedKafkaCluster CLUSTER = new EmbeddedKafkaCluster(NUM_BROKERS);

    private static final String INPUT_TOPIC = "input-topic";
    private static final String OUTPUT_TOPIC = "output-topic";

    private static final Integer INPUT_SIZE = 1000;

    private final MockTime mockTime = CLUSTER.time;

    private KafkaStreams client1;
    private KafkaStreams client2;

    private long start = -1;

    @BeforeClass
    public static void createTopics() throws InterruptedException {
        CLUSTER.createTopic(INPUT_TOPIC, 2, 1);
        CLUSTER.createTopic(OUTPUT_TOPIC, 1, 1);
    }

    @After
    public void after() {
        //client1.close();
        //client2.close();
    }

    private Properties streamsConfiguration() {
        final String applicationId = "streamsApp";
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory(applicationId).getPath());
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        streamsConfiguration.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 1);
        streamsConfiguration.put(StreamsConfig.TOPOLOGY_OPTIMIZATION, StreamsConfig.OPTIMIZE);
        streamsConfiguration.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
        return streamsConfiguration;
    }

    @Test
    public void shouldTestStandbyTask() throws Exception {
        final Properties streamsConfiguration1 = streamsConfiguration();
        final Properties streamsConfiguration2 = streamsConfiguration();

        final String stateName = "source-table";

        final StreamsBuilder builder = new StreamsBuilder();
        builder
            .table(INPUT_TOPIC, Consumed.with(Serdes.Integer(), Serdes.Integer()), Materialized.<Integer, Integer, KeyValueStore<Bytes, byte[]>>as(stateName).withCachingDisabled())
            .toStream()
            .peek((key, value) -> {System.out.println("(" + key + ", " + value + ")"); try {Thread.sleep(100);} catch (InterruptedException e) {}})
            .to(OUTPUT_TOPIC);

        createClients(
            builder.build(streamsConfiguration1),
            streamsConfiguration1,
            builder.build(streamsConfiguration2),
            streamsConfiguration2
        );

        // get start offset for restoring with callback
        client1.setGlobalStateRestoreListener(new StateRestoreListener() {
            @Override
            public void onRestoreStart(final TopicPartition topicPartition, final String storeName,
                                       final long startingOffset, final long endingOffset) {
                if (storeName.equals(stateName)) {
                    start = startingOffset;
                }
            }

            @Override
            public void onBatchRestored(final TopicPartition topicPartition, final String storeName,
                                        final long batchEndOffset, final long numRestored) {

            }

            @Override
            public void onRestoreEnd(final TopicPartition topicPartition, final String storeName,
                                     final long totalRestored) {
            }
        });
        client2.setGlobalStateRestoreListener(new StateRestoreListener() {
            @Override
            public void onRestoreStart(final TopicPartition topicPartition, final String storeName,
                                       final long startingOffset, final long endingOffset) {

                if (storeName.equals(stateName)) {
                    start = startingOffset;
                }
            }

            @Override
            public void onBatchRestored(final TopicPartition topicPartition, final String storeName,
                                        final long batchEndOffset, final long numRestored) {

            }

            @Override
            public void onRestoreEnd(final TopicPartition topicPartition, final String storeName,
                                     final long totalRestored) {
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::after));

        produceData();

        startClients();

        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(
            TestUtils.consumerConfig(
                CLUSTER.bootstrapServers(),
                "consumerApp",
                IntegerDeserializer.class,
                IntegerDeserializer.class
            ),
            OUTPUT_TOPIC,
            INPUT_SIZE / 3,
            60000
        );

        final ReadOnlyKeyValueStore<Integer, Integer> store1 = client1.store(stateName, QueryableStoreTypes.keyValueStore());

        // one client is closed to simulate failure
        final boolean closeClient1 = (store1.get(1) != null);
        if (closeClient1) {
            //client1.close(Duration.ofSeconds(10));
        } else {
            //client2.close(Duration.ofSeconds(10));
        }


        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(
            TestUtils.consumerConfig(
                CLUSTER.bootstrapServers(),
                "consumerApp",
                IntegerDeserializer.class,
                IntegerDeserializer.class
            ),
            OUTPUT_TOPIC,
            INPUT_SIZE / 3,
            60000
        );

        // the start offset for restoring is always zero
        assertThat(start, lessThan(0L));
    }


    private void createClients(final Topology topology1,
                               final Properties streamsConfiguration1,
                               final Topology topology2,
                               final Properties streamsConfiguration2) {

        System.out.println(topology1.describe());
        client1 = new KafkaStreams(topology1, streamsConfiguration1);
        client2 = new KafkaStreams(topology2, streamsConfiguration2);
    }

    private void startClients() {
        client1.cleanUp();
        client2.cleanUp();
        client1.start();
        client2.start();
    }

    private void produceData() throws Exception {
        final Properties producerProp = new Properties();
        producerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
        producerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        producerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);

        final List<KeyValue<Integer, Integer>> inputKeyValuePairs = new ArrayList<>();
        for (int i = 0; i < INPUT_SIZE; ++i) {
            inputKeyValuePairs.add(KeyValue.pair(1, i));
        }
        IntegrationTestUtils.produceKeyValuesSynchronously(INPUT_TOPIC, inputKeyValuePairs, producerProp, mockTime);
    }
}
