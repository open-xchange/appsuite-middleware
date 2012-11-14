package com.openexchange.index.solr.internal.querybuilder.translators;

import java.util.Set;

import org.apache.commons.logging.Log;

import com.openexchange.index.solr.internal.querybuilder.Configuration;
import com.openexchange.index.solr.internal.querybuilder.QueryTranslator;
import com.openexchange.index.solr.internal.querybuilder.TranslationException;

public class IdListTranslator implements QueryTranslator {
  private static final String ID_FIELD = "id_field";
  
private static final String INIT_ERROR = "Error getting id key";
  private static final String TRANSLATION_ERROR = "Only sets of strings are allowed";
  private String idKey;
  private String handlerName;

  //private static final Logger log = Logger.getLogger(IdListTranslator.class);
  private static Log log = com.openexchange.log.Log.loggerFor(IdListTranslator.class);


  @Override
  public void init(String name, Configuration config) throws TranslationException {
    handlerName = name.trim();

    log.info("[init]: initializing configuration for handler \'" + handlerName + "\'");

    Set<String> keys = config.getKeys(handlerName);
    String key = handlerName + '.' + ID_FIELD;
    if (keys.contains(key)) {
        idKey = config.getRawMapping().get(key);
        log.info("[init]: ID key is \'" + idKey + "\'");
        return;
    }

      log.error("[init]: No valid id key found.");
      throw new TranslationException(INIT_ERROR);
  }

  @Override
  public String translate(Object o) throws TranslationException {
    log.debug("[translate]: Starting");
    if (o instanceof Set<?>) {
      StringBuffer b = new StringBuffer();
      Set<?> idList = (Set<?>) o;

      for (Object idVal : idList) {
        if (idVal instanceof String) b.append(idKey + ":" + idVal + " ");
        else log.warn("[translate]: Wrong type in list");
      }

      return b.toString().trim();
    }
    else throw new IllegalArgumentException(TRANSLATION_ERROR);
  }
}
