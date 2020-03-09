package de.flaconi.kafka.connect.formatters;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.ConnectException;

public class PayloadFormatterConfig extends AbstractConfig {
  static final String FORMATTER_CLASS_KEY = "payload.formatter.class";
  static final String FORMATTER_CLASS_DOC = "Implementation class that formats the invocation payload";

  public PayloadFormatterConfig(final Map<String, String> parsedConfig) {
    super(configDef(), parsedConfig);
  }

  @SuppressWarnings("unchecked")
  public PayloadFormatter getPayloadFormatter() {
    try {
      return ((Class<? extends PayloadFormatter>)
          getClass(FORMATTER_CLASS_KEY)).getDeclaredConstructor().newInstance();
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      throw new ConnectException("Unable to create " + FORMATTER_CLASS_KEY, e);
    }
  }

  public static ConfigDef configDef() {
    return configDef(new ConfigDef());
  }

  public static ConfigDef configDef(ConfigDef base) {
    return new ConfigDef(base)
        .define(FORMATTER_CLASS_KEY,
            ConfigDef.Type.CLASS,
            PlainPayloadFormatter.class,
            new FormatterClassValidator(),
            ConfigDef.Importance.LOW,
            FORMATTER_CLASS_DOC);
  }

  static class FormatterClassValidator implements ConfigDef.Validator {
    @Override
    public void ensureValid(String name, Object formatter) {
      if (formatter instanceof Class && PayloadFormatter.class.isAssignableFrom((Class<?>)formatter)) {
        return;
      }

      throw new ConfigException(name, formatter, "Class must extend: " + PayloadFormatter.class);
    }

    @Override
    public String toString() {
      return "Any class implementing: " + PayloadFormatter.class;
    }
  }
}
