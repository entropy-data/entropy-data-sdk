package entropydata.sdk;

import java.util.Map;

public interface EntropyDataStateRepository {

  Map<String, Object> getState();
  void saveState(Map<String, Object> state);

}
