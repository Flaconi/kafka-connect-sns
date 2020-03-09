package de.flaconi.kafka.connect.sns;

public enum SnsConnectorConfigKeys {
  SNS_TOPIC_ARN("sns.topic.arn"),
  TOPICS("topics"),
  CREDENTIALS_PROVIDER_CLASS_CONFIG("sns.credentials.provider.class"),
  CREDENTIALS_PROVIDER_CLASS_DEFAULT("com.amazonaws.auth.DefaultAWSCredentialsProviderChain"),
  CREDENTIALS_PROVIDER_CONFIG_PREFIX("sns.credentials.provider.");

  private final String value;

  SnsConnectorConfigKeys(String  value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
