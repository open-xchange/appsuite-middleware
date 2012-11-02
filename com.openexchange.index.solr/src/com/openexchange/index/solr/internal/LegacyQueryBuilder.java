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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal;

import java.util.HashSet;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexField;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.solr.internal.mail.MailFieldMapper;
import com.openexchange.index.solr.internal.mail.SearchTerm2Query;
import com.openexchange.index.solr.internal.mail.SolrMailField;
import com.openexchange.index.solr.internal.querybuilder.SolrQueryBuilder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.solr.SolrProperties;


/**
 * {@link LegacyQueryBuilder}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class LegacyQueryBuilder implements SolrQueryBuilder {
    
    private final AbstractSolrIndexAccess<MailMessage> access;
    
    
    public LegacyQueryBuilder(AbstractSolrIndexAccess<MailMessage> access) {
        super();
        this.access = access;
    }

    @Override
    public SolrQuery buildQuery(QueryParameters parameters) throws OXException {
        SearchHandler searchHandler = parameters.getHandler();
        if (searchHandler == null) {
            throw new IllegalArgumentException("Parameter `search handler` must not be null!");
        }
        
        ConfigurationService config = Services.getService(ConfigurationService.class);
        SolrQuery solrQuery;
        switch (searchHandler) { 
            case SIMPLE:
            {
                Object searchTerm = parameters.getSearchTerm();
                if (searchTerm == null || !(searchTerm instanceof String)) {
                    throw new IllegalArgumentException("Parameter `searchTerm` must not be null and of type java.lang.String!");
                }
                
                solrQuery = new SolrQuery((String) searchTerm);
                solrQuery.setQueryType(config.getProperty(SolrProperties.SIMPLE_HANLDER));
                addFilterQueries(parameters, solrQuery);
                break;
            }
            
            case ALL_REQUEST:
            {                
                solrQuery = new SolrQuery("*:*");
                solrQuery.setQueryType(config.getProperty(SolrProperties.ALL_HANLDER));                
                addFilterQueries(parameters, solrQuery);             
                break;
            }
            
            case GET_REQUEST:
            {
                Set<String> ids = parameters.getIndexIds();
                if (ids == null) {
                    throw new IllegalArgumentException("Parameter `indexIds` must not be null!");
                }
                
                solrQuery = new SolrQuery(access.stringSetToQuery(ids));
                solrQuery.setQueryType(config.getProperty(SolrProperties.GET_HANDLER));
                addFilterQueries(parameters, solrQuery);
                break;
            }
            
            case CUSTOM:
            {
                Object termObject = parameters.getSearchTerm();
                if (termObject == null || !(termObject instanceof SearchTerm<?>)) {
                    throw new IllegalArgumentException("Parameter `searchTerm` must not be null and must be instance of com.openexchange.index.attachments.SearchTerm<?>!");
                }
                
                String queryString = SearchTerm2Query.searchTerm2Query((SearchTerm<?>) termObject).toString();
                solrQuery = new SolrQuery(queryString);
                solrQuery.setQueryType(config.getProperty(SolrProperties.CUSTOM_HANLDER));
                addFilterQueries(parameters, solrQuery);
                break;
            }
            
            default:
                throw new IllegalArgumentException("Search handler " + searchHandler.name() + " is not implemented for MailSolrIndexAccess.query().");
        }
        
        setSortAndOrder(parameters, solrQuery);
        return solrQuery;
    }
    
    protected void setSortAndOrder(QueryParameters parameters, SolrQuery query) {
        IndexField sortField = parameters.getSortField();
        if (sortField == null) {
            return;
        }
        
        SolrField solrSortField = MailFieldMapper.getInstance().solrFieldFor(sortField);
        if (solrSortField != null) {
            Order orderParam = parameters.getOrder();
            ORDER order = orderParam == null ? ORDER.desc : orderParam.equals(Order.DESC) ? ORDER.desc : ORDER.asc;
            query.setSortField(solrSortField.solrName(), order);
        }
    }
    
    private void addFilterQueries(QueryParameters parameters, SolrQuery solrQuery) {
        Set<AccountFolders> all = parameters.getAccountFolders();
        Set<String> queries = new HashSet<String>();
        if (all != null) {
            for (AccountFolders accountFolders : all) {
                String account = accountFolders.getAccount();
                Set<String> folders = accountFolders.getFolders();
                if (folders.isEmpty()) {
                    queries.add(access.buildQueryString(SolrMailField.ACCOUNT.solrName(), account));
                } else {
                    queries.add(access.catenateQueriesWithAnd(access.buildQueryString(SolrMailField.ACCOUNT.solrName(), account), access.buildQueryStringWithOr(SolrMailField.FULL_NAME.solrName(), folders)));
                }
            }
        }
        
        access.addFilterQueryIfNotNull(solrQuery, access.catenateQueriesWithOr(queries.toArray(new String[queries.size()])));   
    }

}
