package de.kippdata.solrext.translators;

import org.apache.log4j.Logger;

import de.kippdata.solrext.queries.Configuration;
import de.kippdata.solrext.queries.QueryTranslator;
import de.kippdata.solrext.queries.TranslationException;

public class IdListTranslator implements QueryTranslator {
  private static final String ID = "id";
  private static final String INIT_ERROR = "Error getting id key";
  private static final String TRANSLATION_ERROR = "Only strings are allowed";
  private String idKey;
  private String handlerName;

  private static final Logger log = Logger.getLogger(IdListTranslator.class);


  @Override
  public void init(String name, Configuration config) throws TranslationException {
    handlerName = name.trim();

    log.info("[init]: initializing configuration for handler \'" + handlerName + "\'");

    for (String key : config.getKeys(handlerName)) {
      String scrubbedKey = key.substring(handlerName.length() + 1);
      idKey = scrubbedKey;
      log.info("[init]: ID key is \'" + idKey + "\'");
      break;
    }
    if (idKey == null) {
      log.error("[init]: No valid id key found.");
      throw new TranslationException(INIT_ERROR);
    }
  }

  @Override
  public String translate(Object o) throws TranslationException {
    log.debug("[translate]: Starting");
    if (o instanceof String) {

      StringBuffer b = new StringBuffer();
      String[] idList = ((String) o).split("\\s+");
      for (String idVal : idList) {
        b.append(idKey + ":" + idVal + " ");
      }

      return b.toString().trim();
    }
    else throw new IllegalArgumentException(TRANSLATION_ERROR);
  }
}
