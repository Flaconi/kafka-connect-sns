package de.flaconi.kafka.connect.sns;

import de.flaconi.kafka.connect.formatters.PayloadFormatter;
import de.flaconi.kafka.connect.formatters.PayloadFormattingException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnsSinkConnectorTask extends SinkTask {
  private static final Logger LOG = LoggerFactory.getLogger(SnsSinkConnectorTask.class);
  private SnsSinkConnectorConfig config;
  private SnsClient client;
  private PayloadFormatter payloadFormatter;

  @Override
  public String version() {
    return new SnsSinkConnector().version();
  }

  @Override
  public void start(Map<String, String> props) {
    LOG.info("task.start");
    Objects.requireNonNull(props, "Task properties should not be null");

    config = new SnsSinkConnectorConfig(props);
    payloadFormatter = config.getPayloadFormatterConfig().getPayloadFormatter();
    client = new SnsClient(config.originalsWithPrefix(SnsConnectorConfigKeys.CREDENTIALS_PROVIDER_CONFIG_PREFIX.getValue()));

    LOG.info("task.start:OK, sns.topic.arn={}, topics={}", config.getTopicArn(), config.getTopics());
  }

  @Override
  public void put(Collection<SinkRecord> records) {
    if (records.isEmpty()) {
      return;
    }

    if (!isValidState()) {
      throw new IllegalStateException("Task is not properly initialized");
    }

    LOG.debug(".put:record_count={}", records.size());

    for (final SinkRecord record : records) {
      LOG.debug(".put:record_class={}", record.value().getClass());

      final String key = MessageFormat
          .format("{0}-{1}-{2}", record.topic(), record.kafkaPartition().longValue(),
          record.kafkaOffset()) ;
      final String body = getPayload(record);

      Map<String, Object> attributes = new HashMap<>();
      attributes.put("key", record.key());
      attributes.put("topic", record.topic());
      attributes.put("kafkaOffset", record.kafkaOffset());
      attributes.put("kafkaPartition", record.kafkaPartition());
      attributes.put("timestamp", record.timestamp());

      if (Objects.isNull(body) || body.isEmpty()) {
        LOG.warn("Skipping empty message: key={}", key);
        continue;
      }

      try {
        final String sid = client.publish(config.getTopicArn(), key, body, attributes);
        LOG.debug(".publish.OK:topic.arn={}, sns-subject={}, sns-message-id={}", config.getTopicArn(), key, sid);
      } catch (final RuntimeException e) {
        LOG.error("An Exception ocurred while publishing message {} to target arn {}:", key, config.getTopicArn(), e);
      }
    }
  }

  @Override
  public void stop() {
    LOG.info("task.stop:OK");
  }

  private String getPayload(final SinkRecord record) {
    try {
      final String formatted = payloadFormatter.format(record);
      LOG.debug("formatted-payload:|{}|", formatted);
      return formatted;
    } catch (final PayloadFormattingException e) {
      throw new DataException("Record could not be formatted.", e);
    }
  }

  private boolean isValidState() {
    return Objects.nonNull(config) && Objects.nonNull(client);
  }
}
