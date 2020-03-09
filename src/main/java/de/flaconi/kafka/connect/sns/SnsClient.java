package de.flaconi.kafka.connect.sns;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnsClient {

  public static final Class<? extends AWSCredentialsProvider> CREDENTIALS_PROVIDER_CLASS_DEFAULT =
      com.amazonaws.auth.DefaultAWSCredentialsProviderChain.class;
  private static final Logger LOG = LoggerFactory.getLogger(SnsClient.class);

  private final AmazonSNS client;

  public SnsClient(Map<String, ?> configs) {
    LOG.warn(".ctor:configs={}", configs);
    AWSCredentialsProvider provider = null;
    try {
      provider = getCredentialsProvider(configs);
    } catch (Exception e) {
      LOG.error("Problem initializing provider", e);
    }
    final AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
    builder.setCredentials(provider);

    client = builder.build();
  }

  public String publish(final String topicArn, final String subject, final String message,
      final Map<String, Object> attributes) {
    LOG.debug(".send: topicArn={}, subject={}, attributes={}", topicArn, subject, attributes);

    if (!isValidState()) {
      throw new IllegalStateException("AmazonSNS client is not initialized");
    }

    final PublishRequest request = new PublishRequest()
        .withTopicArn(topicArn)
        .withMessage(message)
        .withSubject(subject);

    attributes.entrySet().stream()
        .filter(entry -> Objects.nonNull(entry.getValue()))
        .filter(entry -> !(entry.getValue() instanceof String) || !((String) entry.getValue())
            .isEmpty())
        .forEach(entry ->
          request.addMessageAttributesEntry(
            entry.getKey(),
            new MessageAttributeValue()
                .withDataType(mapJavaTypeToSnsDataType(entry.getValue().getClass()))
                .withStringValue(entry.getValue().toString())));

    LOG.debug(".publish-message: request={}", request);

    final PublishResult result = client.publish(request);

    LOG.debug(".publish-message.OK: topic={}, result={}", topicArn, result);

    return result.getMessageId();
  }

  public AWSCredentialsProvider getCredentialsProvider(Map<String, ?> configs) {
    LOG.warn(".get-credentials-provider:configs={}", configs);

    try {
      Object providerField = configs.get("class");
      String providerClass = SnsConnectorConfigKeys.CREDENTIALS_PROVIDER_CLASS_DEFAULT.getValue();
      if (null != providerField) {
        providerClass = providerField.toString();
      }
      LOG.warn(".get-credentials-provider:field={}, class={}", providerField, providerClass);
      AWSCredentialsProvider provider = ((Class<? extends AWSCredentialsProvider>)
          getClass(providerClass)).newInstance();

      if (provider instanceof Configurable) {
        ((Configurable) provider).configure(configs);
      }

      LOG.warn(".get-credentials-provider:provider={}", provider);
      return provider;
    } catch (IllegalAccessException | InstantiationException e) {
      throw new ConnectException(
          "Invalid class for: " + SnsConnectorConfigKeys.CREDENTIALS_PROVIDER_CLASS_CONFIG,
          e
      );
    }
  }

  private Class<?> getClass(String className) {
    LOG.warn(".get-class:class={}", className);
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      LOG.error("Provider class not found: {}", className, e);
    }
    return null;
  }

  private boolean isValidState() {
    return !Objects.isNull(client);
  }

  private String mapJavaTypeToSnsDataType(Class<?> javaType) {
    if (Integer.class.equals(javaType) || Long.class
        .equals(javaType) || Float.class
        .equals(javaType)) {
      return "Number";
    }

    if (Boolean.class.equals(javaType)) {
      return "Binary";
    }

    return "String";
  }
}
