package de.kippdata.solrext.queries;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Configuration {
  public List<String> getIndexFields(String key);
  public Set<String> getKeys();
  public Set<String> getKeys(String handlerName);
  public Map<String,String> getRawMapping();
  public Map<String,String> getTranslatorMap();
  public boolean haveTranslatorForHandler(String handler);
  public String getTranslatorForHandler(String handler);
  public Set<String> getHandlers();
}