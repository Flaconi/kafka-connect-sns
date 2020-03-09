package de.flaconi.kafka.connect.formatters;

public class PayloadFormattingException extends Exception {
  public PayloadFormattingException(final Throwable e) {
    super(e);
  }
}
