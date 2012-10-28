package de.kippdata.solrext.queries;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrQuery;
import com.openexchange.exception.OXException;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.SolrQueryBuilder;


public class SimpleQueryBuilder implements SolrQueryBuilder {
  private Configuration config;
  private Map<String,QueryTranslator> translators;

  private static Log log = com.openexchange.log.Log.loggerFor(SimpleQueryBuilder.class);


  public SimpleQueryBuilder(String configPath) throws BuilderException {
    config = new SimpleConfiguration(configPath);
    translators = new HashMap<String,QueryTranslator>();

    for (String handler : config.getHandlers()) {
      translators.put(handler.trim(), this.initTranslatorForHandler(handler, config));
    }
  }


  @Override
  public SolrQuery buildQuery(QueryParameters params) throws OXException {
    try {
      log.debug("[buildQuery]: Handler is \'" + params.getHandler().toString() + "\'");
      log.debug("[buildQuery]: Search term is \'" + params.getSearchTerm().toString() + "\'");

      if (translators.containsKey(params.getHandler().toString().toLowerCase())) {
        String queryString = translators.get(params.getHandler().toString().toLowerCase()).translate(params.getSearchTerm());
        SolrQuery solrQuery = new SolrQuery(queryString);
        log.debug("[buildQuery]: Solr query string is \'" + solrQuery.getQuery() + "\'");
        return solrQuery;
      }
      else throw new TranslationException("No translator for handler \'" + params.getHandler().toString() + "\'");
    }
    catch (Exception e) {
      log.warn("[buildQuery]: Exception occurred: " + e.getMessage());
      throw new OXException(e);
    }
  }


// -------------------------- private methods below ----------------------------------- //

  private QueryTranslator initTranslatorForHandler(String handler, Configuration conf) throws BuilderException {
    try {
      Class<?> cls = Class.forName(conf.getTranslatorForHandler(handler).trim());
      QueryTranslator qt = (QueryTranslator) cls.newInstance();
      qt.init(handler, conf);
      return qt;
    }
    catch (ClassNotFoundException e) {
      log.warn("[SimpleQueryBuilder]: Could not find class for handler \'" + handler + "\': " + e.getMessage());
      throw new BuilderException(e);
    }
    catch (InstantiationException e) {
      log.warn("[SimpleQueryBuilder]: Could not instantiate translator: " + e.getMessage());
      throw new BuilderException(e);
    }
    catch (IllegalAccessException e) {
      log.warn("[SimpleQueryBuilder]: Could not instantiate translator: " + e.getMessage());
      throw new BuilderException(e);
    } catch (TranslationException e) {
      log.warn("[SimpleQueryBuilder]: Could not initialize translator: " + e.getMessage());
      throw new BuilderException(e);
    }
  }
}
