package com.openexchange.index.solr.internal.querybuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import com.openexchange.exception.OXException;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexField;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.solr.internal.FieldMapper;
import com.openexchange.index.solr.internal.SolrField;


public class SimpleQueryBuilder implements SolrQueryBuilder {
  private Configuration config;
  private Map<String,QueryTranslator> translators;
  private SolrField accountField;
  private SolrField folderField;
  private FieldMapper fieldMapper;

  private static Log log = com.openexchange.log.Log.loggerFor(SimpleQueryBuilder.class);


  public SimpleQueryBuilder(String configPath, SolrField accountField, SolrField folderField, FieldMapper fieldMapper) throws BuilderException {
    config = new SimpleConfiguration(configPath);
    translators = new HashMap<String,QueryTranslator>();

    for (String handler : config.getHandlers()) {
      translators.put(handler.trim(), this.initTranslatorForHandler(handler, config));
    }
    
    this.accountField = accountField;
    this.folderField = folderField;
    this.fieldMapper = fieldMapper;
  }


  @Override
  public SolrQuery buildQuery(QueryParameters parameters) throws OXException {
    try {
        SearchHandler searchHandler = parameters.getHandler();
        if (searchHandler == null) {
            throw new IllegalArgumentException("Parameter 'search handler' must not be null!");
        }
        log.debug("[buildQuery]: Handler is \'" + searchHandler.toString() + "\'");        
        
        Object searchTerm = parameters.getSearchTerm();
        QueryTranslator translator = translators.get(searchHandler.toString().toLowerCase());        
        SolrQuery solrQuery = new SolrQuery();
        // FIXME: set query type based on search handler
        switch (searchHandler) { 
            case SIMPLE: 
            {
                if (searchTerm == null || !(searchTerm instanceof String)) {
                    throw new IllegalArgumentException("Parameter 'search term' must not be null and of type java.lang.String!");
                }
                solrQuery.setQuery((String) searchTerm);
                break;
            }
            
            case CUSTOM:
            {
                if (searchTerm == null) {
                    throw new IllegalArgumentException("Parameter 'search term' must not be null!");
                }
                if (translator == null) {
                    throw new IllegalStateException("Could not find a translator for search handler '" + searchHandler.toString() + "'.");
                }
                solrQuery.setQuery(translator.translate(searchTerm));
                break;
            }
            
            case ALL_REQUEST:
            {
                solrQuery.setQuery("*:*");
                break;
            }
            
            case GET_REQUEST:
            {
                Set<String> indexIds = parameters.getIndexIds();
                if (indexIds == null) {
                    throw new IllegalArgumentException("Parameter 'index ids' must not be null!");
                }
                if (translator == null) {
                    throw new IllegalStateException("Could not find a translator for search handler '" + searchHandler.toString() + "'.");
                }
                solrQuery.setQuery(translator.translate(indexIds));
                break;
            }
            
            default:
                throw new IllegalArgumentException("Search handler '" + searchHandler.toString() + "' is not valid for this action.");
        }
        
        log.debug("[buildQuery]: Search term is \'" + solrQuery.getQuery() + "\'");
//        solrQuery.setQueryType(handlerName);
        setSortAndOrder(parameters, solrQuery);
        addFilterQueries(parameters, solrQuery);
        return solrQuery;
    }
    catch (Exception e) {
      log.warn("[buildQuery]: Exception occurred: " + e.getMessage());
      throw new OXException(e);
    } finally {
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
  
  protected void setSortAndOrder(QueryParameters parameters, SolrQuery query) {
      IndexField sortField = parameters.getSortField();
      if (sortField == null) {
          return;
      }
      
      SolrField solrSortField = fieldMapper.solrFieldFor(sortField);
      if (solrSortField != null) {
          Order orderParam = parameters.getOrder();
          ORDER order = orderParam == null ? ORDER.desc : orderParam.equals(Order.DESC) ? ORDER.desc : ORDER.asc;
          query.setSortField(solrSortField.solrName(), order);
      }
  }
  
  protected void addFilterQueries(QueryParameters parameters, SolrQuery solrQuery) {
      if (accountField == null && folderField == null) {
          return;
      }

      Set<AccountFolders> all = parameters.getAccountFolders();
      Set<String> queries = new HashSet<String>();
      if (all != null && all.size() > 0) {
          if (accountField == null) {
              AccountFolders accountFolders = all.iterator().next();
              Set<String> folders = accountFolders.getFolders();
              String stringWithOr = buildQueryStringWithOr(folderField.solrName(), folders);
              if (stringWithOr != null) {
                  queries.add(stringWithOr);
              }
          } else {
              if (folderField == null) {
                  AccountFolders accountFolders = all.iterator().next();
                  String account = accountFolders.getAccount();
                  String queryString = buildQueryString(accountField.solrName(), account);
                  if (queryString != null) {
                      queries.add(queryString);
                  }
              } else {
                  for (AccountFolders accountFolders : all) {
                      String account = accountFolders.getAccount();
                      Set<String> folders = accountFolders.getFolders();
                      if (folders.isEmpty()) {
                          String queryString = buildQueryString(accountField.solrName(), account);
                          if (queryString != null) {
                              queries.add(queryString);
                          }
                      } else {
                          String folderQuery = buildQueryStringWithOr(folderField.solrName(), folders);
                          String accountQuery = buildQueryString(accountField.solrName(), account);
                          String finalQuery = catenateQueriesWithAnd(folderQuery, accountQuery);
                          if (finalQuery != null) {
                              queries.add(finalQuery);
                          }
                      }
                  }
              }
          }
      }

      if (!queries.isEmpty()) {
          String filterQuery = catenateQueriesWithOr(queries);
          solrQuery.addFilterQuery(filterQuery);
      }
  }

  protected String buildQueryString(String fieldName, Object value) {
      if (fieldName == null || value == null) {
          return null;
      }
      
      StringBuilder sb = new StringBuilder(); 
      sb.append('(').append(fieldName).append(":\"").append(value.toString()).append("\")");
      return sb.toString();
  }
  
  protected String buildQueryStringWithOr(String fieldName, Set<String> values) {
      if (fieldName == null || values == null || values.isEmpty()) {
          return null;
      }
      
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      boolean first = true;
      for (String value : values) {
          if (first) {
              sb.append('(').append(fieldName).append(":\"").append(value).append("\")");
              first = false;
          } else {
              sb.append(" OR (").append(fieldName).append(":\"").append(value).append("\")");
          }
      }
      
      sb.append(')');
      return sb.toString();
  }
  
  protected String catenateQueriesWithAnd(String... queries) {
      if (queries == null || queries.length == 0) {
          return null;
      }
      
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      boolean first = true;
      for (String query : queries) {
          if (query != null) {
              if (first) {
                  sb.append(query);
                  first = false; 
              } else {
                  sb.append(" AND ").append(query);
              }
          }
      }
      
      if (sb.length() == 1) {
          return null;
      }
      
      sb.append(')');
      return sb.toString();
  }
  
  protected String catenateQueriesWithOr(Set<String> queries) {
      if (queries == null || queries.size() == 0) {
          return null;
      }
      
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      boolean first = true;
      for (String query : queries) {
          if (query != null) {
              if (first) {
                  sb.append(query);
                  first = false; 
              } else {
                  sb.append(" OR ").append(query);
              }
          }
      }
      
      if (sb.length() == 1) {
          return null;
      }
      
      sb.append(')');
      return sb.toString();
  }
}
