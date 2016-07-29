// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package com.google.pubsub.kafka.source;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.google.pubsub.kafka.common.ConnectorUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.pubsub.kafka.sink.CloudPubSubSinkConnector;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CloudPubSubSourceConnector}. */
public class CloudPubSubSourceConnectorTest {

  private static final int NUM_TASKS = 10;
  private static final String CPS_PROJECT = "hello";
  private static final String CPS_SUBSCRIPTION = "big";
  private static final String KAFKA_TOPIC = "world";
  private static final String INVALID_CPS_MAX_BATCH_SIZE = "Not an int";
  private static final String INVALID_KAFKA_PARTITION_SCHEME = "Not a scheme";
  private static final String INVALID_KAFKA_PARTITION_COUNT = "0";

  private CloudPubSubSourceConnector connector;
  private Map<String, String> props;

  @Before
  public void setup() {
    connector = spy(new CloudPubSubSourceConnector());
    props = new HashMap<>();
    props.put(CloudPubSubSourceConnector.CPS_SUBSCRIPTION_CONFIG, CPS_SUBSCRIPTION);
    props.put(ConnectorUtils.CPS_PROJECT_CONFIG, CPS_PROJECT);
    props.put(CloudPubSubSourceConnector.KAFKA_TOPIC_CONFIG, KAFKA_TOPIC);
  }

  @Test(expected = ConnectException.class)
  public void testStartWhenSubscriptionNonexistant() {
    doThrow(new ConnectException("")).when(connector).verifySubscription(anyString(), anyString());
    connector.start(props);
  }

  @Test(expected = ConfigException.class)
  public void testStartWhenRequiredConfigMissing() {
    connector.start(new HashMap<>());
  }

  @Test(expected = ConfigException.class)
  public void testStartWhenConfigHasInvalidMaxBatchSize() {
    props.put(CloudPubSubSourceConnector.CPS_MAX_BATCH_SIZE_CONFIG, INVALID_CPS_MAX_BATCH_SIZE);
    connector.start(props);
  }

  @Test(expected = ConfigException.class)
  public void testStartWhenConfigHasInvalidPartitionScheme() {
    props.put(CloudPubSubSourceConnector.KAFKA_PARTITION_SCHEME_CONFIG,
        INVALID_KAFKA_PARTITION_SCHEME);
    connector.start(props);
  }

  @Test(expected = ConfigException.class)
  public void testStartWhenConfigHasInvalidPartitionCount() {
    props.put(CloudPubSubSourceConnector.KAFKA_PARTITIONS_CONFIG, INVALID_KAFKA_PARTITION_COUNT);
    connector.start(props);
  }

  @Test
  public void testTaskConfigs() {
    doNothing().when(connector).verifySubscription(anyString(), anyString());
    connector.start(props);
    List<Map<String, String>> taskConfigs = connector.taskConfigs(NUM_TASKS);
    assertEquals(taskConfigs.size(), NUM_TASKS);
    for (int i = 0; i < taskConfigs.size(); ++i) {
      assertEquals(taskConfigs.get(i), props);
    }
  }

  @Test
  public void testSourceConnectorTaskClass() {
    assertEquals(CloudPubSubSourceTask.class, connector.taskClass());
  }
}