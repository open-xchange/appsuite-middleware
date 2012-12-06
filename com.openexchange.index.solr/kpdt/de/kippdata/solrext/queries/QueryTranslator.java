package de.kippdata.solrext.queries;

public interface QueryTranslator {
  public void init(String name, Configuration config) throws TranslationException;
  public String translate(Object o) throws TranslationException;
}
