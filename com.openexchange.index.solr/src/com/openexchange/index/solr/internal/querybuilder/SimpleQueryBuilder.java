/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.index.solr.internal.querybuilder;

import static com.openexchange.index.solr.internal.LuceneQueryTools.buildQueryString;
import static com.openexchange.index.solr.internal.LuceneQueryTools.buildQueryStringWithOr;
import static com.openexchange.index.solr.internal.LuceneQueryTools.catenateQueriesWithAnd;
import static com.openexchange.index.solr.internal.LuceneQueryTools.catenateQueriesWithOr;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import com.openexchange.exception.OXException;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexField;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.solr.internal.config.FieldConfiguration;

/**
 * {@link SimpleQueryBuilder}
 *
 * @author Sven Maurmann
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SimpleQueryBuilder implements SolrQueryBuilder {

    private Configuration config;

    private Map<String, QueryTranslator> translators;

    private String moduleField;

    private String accountField;

    private String folderField;

    private FieldConfiguration fieldConfig;

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleQueryBuilder.class);

    public SimpleQueryBuilder(String configPath, String moduleField, String accountField, String folderField, FieldConfiguration fieldConfig) throws BuilderException {
        config = new SimpleConfiguration(configPath);
        translators = new HashMap<String, QueryTranslator>();
        this.moduleField = moduleField;
        this.accountField = accountField;
        this.folderField = folderField;
        this.fieldConfig = fieldConfig;

        for (String handler : config.getHandlers()) {
            translators.put(handler.trim(), this.initTranslatorForHandler(handler));
        }
    }

    /*
     * For testing purposes only
     */
    protected SimpleQueryBuilder() {
        super();
    }

    @Override
    public SolrQuery buildQuery(QueryParameters parameters) throws OXException {
        try {
            SearchHandler searchHandler = parameters.getHandler();
            if (searchHandler == null) {
                throw new IllegalArgumentException("Parameter 'search handler' must not be null!");
            }
            log.debug("[buildQuery]: Handler is \'{}\'", searchHandler);

            String handlerId = searchHandler.getHandlerName();
            String handlerName = config.getRawMapping().get(Configuration.HANDLER + '.' + handlerId);
            QueryTranslator translator = translators.get(handlerId);
            Object searchTerm = parameters.getSearchTerm();
            SolrQuery solrQuery = new SolrQuery();
            if (handlerName == null) {
                // TODO: also check handler existence in solrconfig
                throw new IllegalArgumentException("No solr search handler is configured for '" + handlerId + "'");
            }
            
            if (SearchHandlers.SIMPLE.equals(searchHandler)) {
                if (searchTerm == null || !(searchTerm instanceof String)) {
                    throw new IllegalArgumentException("Parameter 'search term' must not be null and of type java.lang.String!");
                }
                solrQuery.setQuery((String) searchTerm);
                solrQuery.setQueryType(handlerName);
            } else if (SearchHandlers.CUSTOM.equals(searchHandler)) {
                if (searchTerm == null) {
                    throw new IllegalArgumentException("Parameter 'search term' must not be null!");
                }
                if (translator == null) {
                    throw new IllegalStateException(
                        "Could not find a translator for search handler '" + searchHandler.toString() + "'.");
                }
                solrQuery.setQuery(translator.translate(searchTerm));
                solrQuery.setQueryType(handlerName);
            } else if (SearchHandlers.ALL_REQUEST.equals(searchHandler)) {
                solrQuery.setQuery(translator.translate(searchTerm));
                solrQuery.setQueryType(handlerName);
            } else if (SearchHandlers.GET_REQUEST.equals(searchHandler)) {
                Set<String> indexIds = parameters.getIndexIds();
                if (indexIds == null) {
                    throw new IllegalArgumentException("Parameter 'index ids' must not be null!");
                }
                if (translator == null) {
                    throw new IllegalStateException(
                        "Could not find a translator for search handler '" + searchHandler.toString() + "'.");
                }
                solrQuery.setQuery(translator.translate(indexIds));
                solrQuery.setQueryType(handlerName);
            } else {
                if (searchTerm == null) {
                    throw new IllegalArgumentException("Parameter 'search term' must not be null!");
                }

                String finalTerm;
                if (translator == null) {
                    if (!(searchTerm instanceof String)) {
                        throw new IllegalArgumentException("Parameter 'search term' must be of type java.lang.String!");
                    }
                    finalTerm = (String) searchTerm;
                } else {
                    finalTerm = translator.translate(searchTerm);
                }

                solrQuery.setQuery(finalTerm);
                solrQuery.setQueryType(handlerName);
            }

            log.debug("[buildQuery]: Search term is \'{}\'", solrQuery.getQuery());
            setSortAndOrder(parameters, solrQuery);
            addFilterQueries(parameters, solrQuery);
            return solrQuery;
        } catch (Exception e) {
            log.warn("[buildQuery]: Exception occurred: {}", e.getMessage());
            throw new OXException(e);
        }
    }

    // -------------------------- private methods below ----------------------------------- //

    private QueryTranslator initTranslatorForHandler(String handler) throws BuilderException {
        try {
            Class<?> cls = Class.forName(config.getTranslatorForHandler(handler).trim());
            QueryTranslator qt = (QueryTranslator) cls.newInstance();
            qt.init(handler, config, fieldConfig);
            return qt;
        } catch (ClassNotFoundException e) {
            log.warn("[SimpleQueryBuilder]: Could not find class for handler \'{}\': {}", handler, e.getMessage());
            throw new BuilderException(e);
        } catch (InstantiationException e) {
            log.warn("[SimpleQueryBuilder]: Could not instantiate translator: {}", e.getMessage());
            throw new BuilderException(e);
        } catch (IllegalAccessException e) {
            log.warn("[SimpleQueryBuilder]: Could not instantiate translator: {}", e.getMessage());
            throw new BuilderException(e);
        } catch (TranslationException e) {
            log.warn("[SimpleQueryBuilder]: Could not initialize translator: {}", e.getMessage());
            throw new BuilderException(e);
        }
    }

    protected void setSortAndOrder(QueryParameters parameters, SolrQuery query) {
        IndexField sortField = parameters.getSortField();
        if (sortField == null) {
            return;
        }

        Set<String> sortFields = fieldConfig.getSolrFields(sortField);
        if (sortFields != null && !sortFields.isEmpty()) {
            Order orderParam = parameters.getOrder();
            ORDER order = orderParam == null ? ORDER.desc : orderParam.equals(Order.DESC) ? ORDER.desc : ORDER.asc;
            for (String field : sortFields) {
                query.addSortField(field, order);
            }
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
                String stringWithOr = buildQueryStringWithOr(folderField, folders);
                if (stringWithOr != null) {
                    queries.add(stringWithOr);
                }
            } else {
                if (folderField == null) {
                    AccountFolders accountFolders = all.iterator().next();
                    String account = accountFolders.getAccount();
                    String queryString = buildQueryString(accountField, account);
                    if (queryString != null) {
                        queries.add(queryString);
                    }
                } else {
                    for (AccountFolders accountFolders : all) {
                        String account = accountFolders.getAccount();
                        Set<String> folders = accountFolders.getFolders();
                        if (folders.isEmpty()) {
                            String queryString = buildQueryString(accountField, account);
                            if (queryString != null) {
                                queries.add(queryString);
                            }
                        } else {
                            String folderQuery = buildQueryStringWithOr(folderField, folders);
                            String accountQuery = buildQueryString(accountField, account);
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

        if (moduleField != null) {
            Integer module = parameters.getModule() < 0 ? null : new Integer(parameters.getModule());
            String moduleQuery = buildQueryString(moduleField, module);
            if (moduleQuery != null) {
                solrQuery.addFilterQuery(moduleQuery);
            }
        }
    }

    protected void addFilterQueryIfNotNull(SolrQuery solrQuery, String filterQuery) {
        if (filterQuery != null) {
            solrQuery.addFilterQuery(filterQuery);
        }
    }
}
