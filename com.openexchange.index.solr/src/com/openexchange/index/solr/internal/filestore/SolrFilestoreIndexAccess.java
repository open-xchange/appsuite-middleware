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

package com.openexchange.index.solr.internal.filestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.filestore.FilestoreIndexField;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.mail.SearchTerm2Query;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;


/**
 * {@link SolrFilestoreIndexAccess}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrFilestoreIndexAccess extends AbstractSolrIndexAccess<File> {
    
    private static final Set<FilestoreIndexField> INDEXED_FIELDS = new HashSet<FilestoreIndexField>();
    
    static {
        for (SolrFilestoreField field : SolrFilestoreField.getIndexedFields()) {
            INDEXED_FIELDS.add(field.getIndexField());
        }
    }

    /**
     * Initializes a new {@link SolrFilestoreIndexAccess}.
     * @param identifier
     */
    protected SolrFilestoreIndexAccess(SolrCoreIdentifier identifier) {
        super(identifier);
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        return super.isIndexed(Types.INFOSTORE, accountId, folderId);
    }

    @Override
    public Set<FilestoreIndexField> getIndexedFields() {
        return INDEXED_FIELDS;
    }

    @Override
    public void addEnvelopeData(IndexDocument<File> document) throws OXException {
        addDocument(SolrFilestoreDocumentConverter.convert(document.getObject()));        
    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<File>> documents) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<File> document : documents) {
            inputDocuments.add(SolrFilestoreDocumentConverter.convert(document.getObject()));
        }
        
        addDocuments(inputDocuments);  
    }

    @Override
    public void addContent(IndexDocument<File> document, boolean full) throws OXException {
        addDocument(SolrFilestoreDocumentConverter.convert(document.getObject()));  
    }

    @Override
    public void addContent(Collection<IndexDocument<File>> documents, boolean full) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<File> document : documents) {
            inputDocuments.add(SolrFilestoreDocumentConverter.convert(document.getObject()));
        }
        
        addDocuments(inputDocuments);  
    }

    @Override
    public void addAttachments(IndexDocument<File> document, boolean full) throws OXException {
        addDocument(SolrFilestoreDocumentConverter.convert(document.getObject()));  
    }

    @Override
    public void addAttachments(Collection<IndexDocument<File>> documents, boolean full) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<File> document : documents) {
            inputDocuments.add(SolrFilestoreDocumentConverter.convert(document.getObject()));
        }
        
        addDocuments(inputDocuments);
    }

    @Override
    public void change(IndexDocument<File> document, Set<? extends IndexField> fields) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void change(Collection<IndexDocument<File>> documents, Set<? extends IndexField> fields) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteById(String id) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IndexResult<File> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {
        SolrQuery solrQuery = buildSolrQuery(parameters);
        List<IndexDocument<File>> results = queryChunkWise(new SolrFilestoreDocumentConverter(), solrQuery, parameters.getOff(), parameters.getLen(), 100);
        
        return new SolrFilestoreIndexResult(results.size(), results);
    }
    
    private SolrQuery buildSolrQuery(QueryParameters parameters) throws OXException {
        SearchHandler searchHandler = checkQueryParametersAndGetSearchHandler(parameters);
        SolrQuery solrQuery;
        switch (searchHandler) {
            case ALL_REQUEST:
            {
                int accountId = getAccountId(parameters);
                String folder = parameters.getFolder();
                if (folder == null) {
                    solrQuery = new SolrQuery("*:*");
                } else {
                    ConfigurationService config = Services.getService(ConfigurationService.class);
                    String handler = config.getProperty(SolrProperties.ALL_HANLDER);
                    solrQuery = new SolrQuery("\"" + folder + "\"");
                    solrQuery.setQueryType(handler);
                }
//                solrQuery.setFilterQueries(buildFilterQueries(accountId, null));                
//                solrQuery.set("sortManually", setSortAndOrder(parameters, solrQuery));
                break;
            }                
                
            case SIMPLE:
                {
                    ConfigurationService config = Services.getService(ConfigurationService.class);
                    String handler = config.getProperty(SolrProperties.SIMPLE_HANLDER);
                    solrQuery = new SolrQuery(parameters.getPattern());
                    solrQuery.setQueryType(handler);
//                    solrQuery.set("sortManually", setSortAndOrder(parameters, solrQuery));
//                    solrQuery.setFilterQueries(buildFilterQueries(getAccountId(parameters), parameters.getFolder()));
                    break;
                }
//                
//            case GET_REQUEST:
//                {
//                    String[] ids = getIds(parameters);
//                    int accountId = getAccountId(parameters);
//                    String folder = parameters.getFolder();
//                    String queryString = buildQueryString(accountId, folder);
//                    StringBuilder sb = new StringBuilder(queryString);
//                    if (queryString.length() != 0) {
//                        sb.append(" AND (");
//                    } else {
//                        sb.append('(');
//                    }
//                    boolean first = true;
//                    for (String id : ids) {
//                        if (first) {
//                            first = false;
//                        } else {
//                            sb.append(" OR ");
//                        }
//                        sb.append('(').append(SolrMailField.UUID.solrName()).append(":\"").append(id).append("\")");
//                    }
//                    sb.append(')');
//                    solrQuery = new SolrQuery(sb.toString());
//                    solrQuery.set("sortManually", setSortAndOrder(parameters, solrQuery));
//                    break;
//                }
                
            case CUSTOM:
            {
                ConfigurationService config = Services.getService(ConfigurationService.class);
                String handler = config.getProperty(SolrProperties.CUSTOM_HANLDER);
                SearchTerm<?> searchTerm = (SearchTerm<?>) parameters.getSearchTerm();
                StringBuilder queryBuilder = SearchTerm2Query.searchTerm2Query(searchTerm);
                solrQuery = new SolrQuery(queryBuilder.toString());
                solrQuery.setQueryType(handler);
//                solrQuery.set("sortManually", setSortAndOrder(parameters, solrQuery));
//                solrQuery.setFilterQueries(buildFilterQueries(getAccountId(parameters), parameters.getFolder()));
                break;
            }
            
            default:
                throw new NotImplementedException("Search handler " + searchHandler.name() + " is not implemented for MailSolrIndexAccess.query().");
        }

        return solrQuery;
    }
    
    private SearchHandler checkQueryParametersAndGetSearchHandler(QueryParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter `parameters` must not be null!");
        }
        
        SearchHandler searchHandler = parameters.getHandler();        
        if (searchHandler == null) {
            throw new IllegalArgumentException("Parameter `search handler` must not be null!");
        }
        
        Type type = parameters.getType();
        if (type == null || type != Type.INFOSTORE_DOCUMENT) {
            throw new IllegalArgumentException("Parameter `type` must be `infostore`!");
        }
        
        switch(searchHandler) {
            case ALL_REQUEST:
                return searchHandler;
                
            case CUSTOM:
                Object searchTerm = parameters.getSearchTerm();
                if (searchTerm == null) {
                    throw new IllegalArgumentException("Parameter `search term` must not be null!");
                } else if (!(searchTerm instanceof SearchTerm)) {
                    throw new IllegalArgumentException("Parameter `search term` must be an instance of com.openexchange.mail.search.SearchTerm!");
                }
                return searchHandler;
                
            case GET_REQUEST:
                Map<String, Object> params = parameters.getParameters();
                if (params == null) {
                    throw new IllegalArgumentException("Parameter `parameters.parameters` must not be null!");
                }
                Object idsObj = params.get("ids");
                if (idsObj == null) {
                    throw new IllegalArgumentException("Parameter `parameters.parameters.ids` must not be null!");
                } else if (!(idsObj instanceof String[])) {
                    throw new IllegalArgumentException("Parameter `parameters.parameters.ids` must not be an instance of String[]!");
                }
                return searchHandler;
                
            case SIMPLE:
                if (parameters.getPattern() == null) {
                    throw new IllegalArgumentException("Parameter `pattern` must not be null!");
                }
                return searchHandler;                
                
            default:
                throw new NotImplementedException("Search handler " + searchHandler.name() + " is not implemented for this action.");
        }
    }
    
    private int getAccountId(QueryParameters parameters) {
        Object accountIdObj = parameters.getParameters().get("accountId");
        if (accountIdObj == null || !(accountIdObj instanceof Integer)) {
            return -1;
        }
        
        return ((Integer) accountIdObj).intValue();
    }
    
    private String[] getIds(QueryParameters parameters) {
        return (String[]) parameters.getParameters().get("ids");
    }
}
