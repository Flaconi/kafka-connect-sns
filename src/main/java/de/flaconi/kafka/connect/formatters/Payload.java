package de.flaconi.kafka.connect.formatters;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.connect.sink.SinkRecord;

@Getter
@Setter
public class Payload<K, V> {
  private K key;
  private String keySchemaName;
  private String keySchemaVersion;
  private V value;
  private String valueSchemaName;
  private String valueSchemaVersion;
  private String topic;
  private int partition;
  private long offset;
  private long timestamp;
  private String timestampTypeName;

  public Payload(final SinkRecord record) {
    if (record.keySchema() != null) {
      this.keySchemaName = record.keySchema().name();
      if (record.keySchema().version() != null) {
        this.keySchemaVersion = record.keySchema().version().toString();
      }
    }

    if (record.valueSchema() != null) {
      this.valueSchemaName = record.valueSchema().name();
      if (record.valueSchema().version() != null) {
        this.valueSchemaVersion = record.valueSchema().version().toString();
      }
    }

    this.topic = record.topic();
    this.partition = record.kafkaPartition();
    this.offset = record.kafkaOffset();

    if (record.timestamp() != null) {
      this.timestamp = record.timestamp();
    }

    if (record.timestampType() != null) {
      this.timestampTypeName = record.timestampType().name;
    }
  }
}
