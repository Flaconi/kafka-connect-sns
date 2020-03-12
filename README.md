# kafka-connect-sns
The SNS connector plugin provides the ability to use AWS SNS topics as a sink (out of a Kafka topic into an SNS topic).

Based on https://github.com/Nordstrom/kafka-connect-lambda/.

# Building
You can build the connector with Maven using the standard lifecycle goals:
```
gradle build
gradle jar
```

## Sink Connector

A sink connector reads from a Kafka topic and publishes to an AWS SNS topic.

A sink connector configuration has two required fields:
 * `topics`: The Kafka topic to be read from.
 * `sns.topic.arn`: The ARN of the SNS topic to be written to.
 * `payload.formatter.class`: Implementation class that formats the invocation payload
 
If topic is using Avro, you may need to provide config fields:
 * `key.converter`: The Kafka topic key converter, e.g `org.apache.kafka.connect.storage.StringConverter`
 * `value.converter`: The Kafka topic value converter, e.g. `io.confluent.connect.avro.AvroConverter`
 * `value.converter.schema.registry.url`: The schema registry URL

### AWS Assume Role Support options
 The connector can assume a cross-account role to enable such features as Server Side Encryption of a queue:
 * `sqs.credentials.provider.class=de.flaconi.kafka.connect.auth.AWSAssumeRoleCredentialsProvider`: REQUIRED Class providing cross-account role assumption.
 * `sqs.credentials.provider.role.arn`: REQUIRED AWS Role ARN providing the access.
 * `sqs.credentials.provider.session.name`: REQUIRED Session name
 * `sqs.credentials.provider.external.id`: OPTIONAL (but recommended) External identifier used by the `kafka-connect-sqs` when assuming the role.

### Sample Configuration
```json
{
  "name": "chirps-t-sink",
  "connector.class": "de.flaconi.kafka.connect.sns.SnsSinkConnector",
  "topics": [
    "chirps-t"
  ],
  "sns.topic.arn": "arn:aws:sns:<REGION>:<ACCOUNT_NUMBER>:test-kafka",
  "payload.formatter.class": "de.flaconi.kafka.connect.formatters.JsonPayloadFormatter"
}

```

### Sample Configuration with Avro
```json
{
  "name": "chirps-t-avro-sink",
  "connector.class": "de.flaconi.kafka.connect.sns.SnsSinkConnector",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "io.confluent.connect.avro.AvroConverter",
  "topics": [
    "chirps-t"
  ],
  "sns.topic.arn": "arn:aws:sns:<REGION>:<ACCOUNT_NUMBER>:test-kafka",
  "payload.formatter.class": "de.flaconi.kafka.connect.formatters.JsonPayloadFormatter",
  "value.converter.schema.registry.url": "http://<SCHEMA_REGISTRY_URL>:8081",
  "schema.compatibility": "NONE"
}
```

## AWS IAM Policies

The IAM Role that Kafka Connect is running under must have policies set for SNS resources in order
to read from or write to the target queues.

For a `sink` connector, the minimum actions required are:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "kafka-connect-sns-sink-policy",
    "Effect": "Allow",
    "Action": [
      "sns:PublishMessage"
    ],
    "Resource": "arn:aws:sns:*:*:*"
  }]
}
```

### AWS Assume Role Support
* Define the AWS IAM Role that `kafka-connect-sns` will assume when writing to the queue (e.g., `kafka-connect-sns-role`) with a Trust Relationship where `xxxxxxxxxxxx` is the AWS Account in which Kafka Connect executes:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::xxxxxxxxxxxx:root"
      },
      "Action": "sts:AssumeRole",
      "Condition": {
        "StringEquals": {
          "sts:ExternalId": "my-sns-external-id"
        }
      }
    }
  ]
}
```

* Define an SNS Policy Document for the queue to allow `PublishMessage`. An example policy is:

```json
{
  "Version": "2012-10-17",
  "Id": "arn:aws:sns:<SOME_REGION>:<SOME_ACCOUNTN>:my-queue/SQSDefaultPolicy",
  "Statement": [
    {
      "Sid": "kafka-connect-sns-sendmessage",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::<SOME_ACCOUNT>:role/kafka-connect-sns-role"
      },
      "Action": "sns:PublishMessage",
      "Resource": "arn:aws:sns:<SOME_REGION>:<SOME_ACCOUNT>:my-topic"
    }
  ]
}
```

The sink connector configuration would then include the additional fields if not provided via environment variables:

```
  sns.credentials.provider.class=de.flaconi.kafka.connect.auth.AWSAssumeRoleCredentialsProvider
  sns.credentials.provider.role.arn=arn:aws:iam::<SOME_ACCOUNT>:role/kafka-connect-sns-role
  sns.credentials.provider.session.name=my-topic-session
  sns.credentials.provider.external.id=my-topic-external-id
```
