package de.flaconi.kafka.connect.formatters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlainPayloadFormatter implements PayloadFormatter {
  private static final Logger LOGGER = LoggerFactory.getLogger(PlainPayloadFormatter.class);
  private final ObjectWriter recordWriter = new ObjectMapper().writerFor(Payload.class);

  public String format(final SinkRecord record) throws PayloadFormattingException {
    final Payload<String, String> payload = new Payload<>(record);
    payload.setKey(record.key() != null ? record.key().toString() : "");
    payload.setValue(record.value() != null ? record.key().toString() : "");
    try {
      return this.recordWriter.writeValueAsString(payload);
    } catch (final JsonProcessingException e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      throw new PayloadFormattingException(e);
    }
  }
}
