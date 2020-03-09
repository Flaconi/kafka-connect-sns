package de.flaconi.kafka.connect.formatters;

import org.apache.kafka.connect.sink.SinkRecord;

public interface PayloadFormatter {
  String format(final SinkRecord record) throws PayloadFormattingException;
}
