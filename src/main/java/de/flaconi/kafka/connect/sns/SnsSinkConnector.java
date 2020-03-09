package de.flaconi.kafka.connect.sns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnsSinkConnector extends SinkConnector {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private Map<String, String> configProps;

  @Override
  public void start(Map<String, String> props) {
    configProps = props;
    log.info("connector.start:OK");
  }

  @Override
  public Class<? extends Task> taskClass() {
    return SnsSinkConnectorTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    final List<Map<String, String>> taskConfigs = new ArrayList<>(maxTasks);
    final Map<String, String> taskProps = new HashMap<>(configProps);
    for (int i = 0; i < maxTasks; i++) {
      taskConfigs.add(taskProps);
    }
    return taskConfigs;
  }

  @Override
  public void stop() {
    log.info("connector.stop:OK");
  }

  @Override
  public ConfigDef config() {
    return SnsSinkConnectorConfig.configDef();
  }

  @Override
  public String version() {
    return "0.0.1";
  }
}
