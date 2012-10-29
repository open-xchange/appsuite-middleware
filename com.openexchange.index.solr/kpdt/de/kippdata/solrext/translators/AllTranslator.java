package de.kippdata.solrext.translators;

import de.kippdata.solrext.queries.Configuration;
import de.kippdata.solrext.queries.QueryTranslator;
import de.kippdata.solrext.queries.TranslationException;

public class AllTranslator implements QueryTranslator {

  @Override
  public void init(String name, Configuration config) throws TranslationException {
  }

  @Override
  public String translate(Object o) throws TranslationException {
    return "*:*";
  }
}
