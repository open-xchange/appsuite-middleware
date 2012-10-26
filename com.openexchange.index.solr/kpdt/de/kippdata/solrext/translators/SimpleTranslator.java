package de.kippdata.solrext.translators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import de.kippdata.cria.solrext.utils.FormalFieldParser;
import de.kippdata.solrext.queries.Configuration;
import de.kippdata.solrext.queries.QueryTranslator;
import de.kippdata.solrext.queries.TranslationException;

public class SimpleTranslator implements QueryTranslator {
  private Map<String,List<String>> translationDict;
  private String handlerName;
  private FormalFieldParser parser;

  private static Log log = com.openexchange.log.Log.loggerFor(SimpleTranslator.class);


  @Override
  public void init(String name, Configuration config) throws TranslationException {
    handlerName = name.trim();
    translationDict = new HashMap<String,List<String>>();

    log.info("[init]: initializing configuration for handler \'" + handlerName + "\'");

    for (String key : config.getKeys(handlerName)) {
      String scrubbedKey = key.substring(handlerName.length() + 1);

      translationDict.put(scrubbedKey, config.getIndexFields(key));
      log.info("[init]: Added translation for \'" + scrubbedKey + "\'");
    }
    parser = new FormalFieldParser(translationDict,true);
  }

  @Override
  public String translate(Object o) throws TranslationException {
    log.debug("[translate]: Starting");
    if (o instanceof String) {
      String parsedQueryString = parser.parse((String) o);
      return parsedQueryString;
    }
    else throw new IllegalArgumentException("Only strings are allowed");
  }
}
