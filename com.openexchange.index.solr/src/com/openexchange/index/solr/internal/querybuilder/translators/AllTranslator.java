package com.openexchange.index.solr.internal.querybuilder.translators;

import com.openexchange.index.solr.internal.querybuilder.Configuration;
import com.openexchange.index.solr.internal.querybuilder.QueryTranslator;
import com.openexchange.index.solr.internal.querybuilder.TranslationException;

public class AllTranslator implements QueryTranslator {

  @Override
  public void init(String name, Configuration config) throws TranslationException {
  }

  @Override
  public String translate(Object o) throws TranslationException {
    return "*:*";
  }
}
