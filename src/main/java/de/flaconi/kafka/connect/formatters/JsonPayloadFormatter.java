package de.flaconi.kafka.connect.formatters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Collections;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.sink.SinkRecord;

public class JsonPayloadFormatter implements PayloadFormatter {
  private final ObjectWriter recordWriter = new ObjectMapper().writerFor(Payload.class);
  private final JsonConverter converter = new JsonConverter();
  private final JsonDeserializer deserializer = new JsonDeserializer();

  public JsonPayloadFormatter() {
    converter.configure(Collections.emptyMap(), false);
    deserializer.configure(Collections.emptyMap(), false);
  }

  public String format(final SinkRecord record) throws PayloadFormattingException {
    try {
      return recordWriter.writeValueAsString(recordToPayload(record));
    } catch (JsonProcessingException e) {
      throw new PayloadFormattingException(e);
    }
  }

  private Payload<Object, Object> recordToPayload(final SinkRecord record) {
    final Object deserializedKey;
    final Object deserializedValue;

    if (record.keySchema() == null) {
      deserializedKey = record.key();
    } else {
      deserializedKey = deserialize(record.topic(), record.keySchema(), record.key());
    }

    if (record.valueSchema() == null) {
      deserializedValue = record.value();
    } else {
      deserializedValue = deserialize(record.topic(), record.valueSchema(), record.value());
    }

    final Payload<Object, Object> payload = new Payload<>(record);
    payload.setKey(deserializedKey);
    payload.setValue(deserializedValue);

    return payload;
  }

  private JsonNode deserialize(final String topic, final Schema schema, final Object value) {
    return deserializer.deserialize(topic, converter.fromConnectData(topic, schema, value));
  }
}
