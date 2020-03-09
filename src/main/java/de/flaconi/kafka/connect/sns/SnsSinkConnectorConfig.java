package de.flaconi.kafka.connect.sns;

import com.amazonaws.auth.AWSCredentialsProvider;
import de.flaconi.kafka.connect.formatters.PayloadFormatterConfig;
import java.util.Map;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnsSinkConnectorConfig extends AbstractConfig {
  private static final Logger LOG = LoggerFactory.getLogger(SnsSinkConnectorConfig.class);

  private final String topicArn;
  private final String topics;
  private final PayloadFormatterConfig payloadFormatterConfig;

  public SnsSinkConnectorConfig(Map<String, String> originals) {
    super(configDef(), originals);
    topicArn = getString(SnsConnectorConfigKeys.SNS_TOPIC_ARN.getValue());
    topics = getString(SnsConnectorConfigKeys.TOPICS.getValue());
    payloadFormatterConfig = new PayloadFormatterConfig(originals);
  }

  public String getTopics() {
    return topics;
  }

  public String getTopicArn() {
    return topicArn;
  }

  public PayloadFormatterConfig getPayloadFormatterConfig() {
    return payloadFormatterConfig;
  }

  public static ConfigDef configDef() {
    ConfigDef configDef =
        new ConfigDef()
            .define(
                SnsConnectorConfigKeys.TOPICS.getValue(),
                Type.STRING,
                Importance.HIGH,
                "Kafka topic to be read from.")
            .define(
                SnsConnectorConfigKeys.SNS_TOPIC_ARN.getValue(),
                Type.STRING,
                Importance.HIGH,
                "ARN of the SNS topic to be written to.")
            .define(
                SnsConnectorConfigKeys.CREDENTIALS_PROVIDER_CLASS_CONFIG.getValue(),
                Type.CLASS,
                SnsConnectorConfigKeys.CREDENTIALS_PROVIDER_CLASS_DEFAULT.getValue(),
                new CredentialsProviderValidator(),
                Importance.LOW,
                "Credentials provider or provider chain to use for authentication to AWS. By default the connector uses 'DefaultAWSCredentialsProviderChain'.");
    configDef = PayloadFormatterConfig.configDef(configDef);
    return configDef;
  }

  private static class CredentialsProviderValidator implements ConfigDef.Validator {
    @Override
    public void ensureValid(String name, Object provider) {
      LOG.warn(".validator:name={}, provider={}", name, provider);
      if (provider instanceof Class
          && AWSCredentialsProvider.class.isAssignableFrom((Class<?>) provider)) {
        return;
      }
      throw new ConfigException(
          name, provider, "Class must extend: " + AWSCredentialsProvider.class);
    }

    @Override
    public String toString() {
      return "Any class implementing: " + AWSCredentialsProvider.class;
    }
  }
}
