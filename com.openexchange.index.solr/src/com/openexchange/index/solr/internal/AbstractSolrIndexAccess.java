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

package com.openexchange.index.solr.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.index.solr.internal.mail.SolrMailField;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;

/**
 * {@link AbstractSolrIndexAccess}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractSolrIndexAccess<V> implements IndexAccess<V> {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(AbstractSolrIndexAccess.class);
    
    private final Lock folderCacheLock = new ReentrantLock();

    protected final int contextId;

    protected final int userId;

    protected final int module;
    
    private final SolrCoreIdentifier identifier;

    private final AtomicInteger retainCount;

    private long lastAccess;
    
    private Map<Integer, Map<String, Set<String>>> indexedFolders;
        

    /**
     * Initializes a new {@link AbstractSolrIndexAccess}.
     * 
     * @param identifier The Solr index identifier
     */
    protected AbstractSolrIndexAccess(final SolrCoreIdentifier identifier) {
        super();
        this.identifier = identifier;
        this.contextId = identifier.getContextId();
        this.userId = identifier.getUserId();
        this.module = identifier.getModule();
        lastAccess = System.currentTimeMillis();
        retainCount = new AtomicInteger(0);
        indexedFolders = new HashMap<Integer, Map<String, Set<String>>>();
    }
    
    /*
     * Implemented methods
     */
    protected boolean isIndexed(int module, String accountId, String folderId) throws OXException {
        Map<String, Set<String>> accounts = indexedFolders.get(module);
        folderCacheLock.lock();        
        try {
            Set<String> folders;
            if (accounts == null) {
                accounts = new HashMap<String, Set<String>>();
                folders = new HashSet<String>();
                accounts.put(accountId, folders);
                indexedFolders.put(module, accounts);
            } else {
                folders = accounts.get(accountId);
                if (folders == null) {
                    folders = new HashSet<String>();
                    accounts.put(accountId, folders);                    
                }              
            }
            
            if (folders.contains(folderId)) {
                return true;
            }
            
            if (IndexFolderManager.isIndexed(contextId, userId, module, accountId, folderId)) {
                folders.add(folderId);
                return true;
            }

            return false;
        } finally {
            folderCacheLock.unlock();
        }
    }
    
    /*
     * Public methods
     */
    public void releaseCore() {
        indexedFolders.clear();
        indexedFolders = null;
        indexedFolders = new HashMap<Integer, Map<String, Set<String>>>();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);
        accessService.freeResources(identifier);
    }

    public SolrCoreIdentifier getIdentifier() {
        return identifier;
    }

    public int incrementRetainCount() {
        return retainCount.incrementAndGet();
    }

    public int decrementRetainCount() {
        return retainCount.decrementAndGet();
    }
    
    public boolean isRetained() {
    	return retainCount.get() != 0;
    }

    public long getLastAccess() {
        return lastAccess;
    }
    
    /*
     * Protected methods
     */
    protected UpdateResponse addDocument(final SolrInputDocument document) throws OXException {        
        return addDocument(document, true);
    }
    
    protected UpdateResponse addDocuments(final Collection<SolrInputDocument> documents) throws OXException {
        return addDocuments(documents, true);
    }
    
    protected UpdateResponse addDocument(final SolrInputDocument document, final boolean commit) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.add(identifier, document, commit);        
        return response;
    }
    
    protected UpdateResponse addDocuments(final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);
        final UpdateResponse response = accessService.add(identifier, documents, commit);
        return response;
    }
    
    protected UpdateResponse commit() throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.commit(identifier);        
        return response;
    }
    
    protected UpdateResponse optimize() throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.optimize(identifier);        
        return response;
    }
    
    protected SolrResponse deleteDocumentById(final String id) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.deleteById(identifier, id, true);
        return response;
    }
    
    protected SolrResponse deleteDocumentsByQuery(final String query) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final UpdateResponse response = accessService.deleteByQuery(identifier, query, true);        
        return response;
    }
    
    protected QueryResponse query(final SolrParams query) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);        
        final QueryResponse response = accessService.query(identifier, query);
        return response;
    }
    
    protected List<IndexDocument<V>> queryChunkWise(SolrResultConverter<V> converter, SolrQuery solrQuery, int off, int len, int chunkSize) throws OXException {
        List<IndexDocument<V>> indexDocuments = new ArrayList<IndexDocument<V>>();
        int fetched = 0;
        int maxRows = len;
        if (maxRows > chunkSize) {
            maxRows = chunkSize;
        }
        do {
            solrQuery.setStart(off);
            if ((fetched + maxRows) > len) {
                maxRows = (len - fetched);
            }
            solrQuery.setRows(maxRows);
            QueryResponse queryResponse = query(solrQuery);
            SolrDocumentList results = queryResponse.getResults();
            for (SolrDocument document : results) {
                indexDocuments.add(converter.convert(document));
            }

            if (results.size() < maxRows) {
                break;
            }
            
            fetched += maxRows;
            off += maxRows;
        } while (fetched < len);
        
        return indexDocuments;
    }
    
    protected IndexResult<V> queryChunkWise1(SolrResultConverter<V> converter, SolrQuery solrQuery, int off, int len, int chunkSize) throws OXException {
        List<IndexDocument<V>> indexDocuments = new ArrayList<IndexDocument<V>>();
        Map<IndexField, Map<String, Long>> facetCountsMap = null;
        int fetched = 0;
        int maxRows = len;
        if (maxRows > chunkSize) {
            maxRows = chunkSize;
        }
        do {
            solrQuery.setStart(off);
            if ((fetched + maxRows) > len) {
                maxRows = (len - fetched);
            }
            solrQuery.setRows(maxRows);
            QueryResponse queryResponse = query(solrQuery);
            SolrDocumentList results = queryResponse.getResults();
            for (SolrDocument document : results) {
                indexDocuments.add(converter.convert(document));
            }
            
            List<FacetField> facetFields = queryResponse.getFacetFields();
            if (null != facetFields) {
                if (null == facetCountsMap) {
                    // Initialize map
                    facetCountsMap = new HashMap<IndexField, Map<String,Long>>(facetFields.size());
                }
                for (final FacetField facetField : facetFields) {
                    final List<Count> counts = facetField.getValues();
                    if (null != counts) {
                        final MailIndexField field = SolrMailField.fieldFor(facetField.getName());
                        if (null != field) {
                            Map<String, Long> map = facetCountsMap.get(field);
                            if (null == map) {
                                map = new HashMap<String, Long>(counts.size());
                                facetCountsMap.put(field, map);
                            }
                            for (final Count count : counts) {
                                final String countName = count.getName();
                                final Long l = map.get(countName);
                                if (null == l) {
                                    map.put(countName, Long.valueOf(count.getCount()));
                                } else {
                                    map.put(countName, Long.valueOf(count.getCount() + l.longValue()));
                                }
                            }
                        }
                    }
                }
            }

            if (results.size() < maxRows) {
                break;
            }
            
            fetched += maxRows;
            off += maxRows;
        } while (fetched < len);
        
        return converter.createIndexResult(indexDocuments, facetCountsMap);
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
    
    protected void setSortAndOrder(QueryParameters parameters, SolrQuery solrQuery, Class<? extends SolrField> fieldEnum) {
        IndexField sortField = parameters.getSortField();
        if (sortField == null) {
            return;
        }
        
        SolrField[] enumConstants = fieldEnum.getEnumConstants();
        if (enumConstants != null) {
            for (SolrField field : enumConstants) {
                if (field.indexField() != null && field.indexField().equals(sortField)) {
                    Order order = parameters.getOrder();
                    solrQuery.setSortField(field.solrName(), order == null ? ORDER.desc : order.equals(Order.DESC) ? ORDER.desc : ORDER.asc);
                    return;
                }
            }
            
            LOG.warn("Did not find a SolrField for IndexField " + sortField.toString());
        }
        
        LOG.warn("Parameter fieldEnum seems not to be a valid enum.");
    }
    
    protected void setFieldList(SolrQuery solrQuery, Set<? extends SolrField> solrFields) {
        for (SolrField field : solrFields) {
            String solrName = field.solrName();
            if (solrName != null) {
                solrQuery.addField(solrName);
            }            
        }
    }
    
    protected String getStringParameter(QueryParameters parameters, String name) {
        if (parameters.getParameters() == null) {
            return null;
        }
        
        Object value = parameters.getParameters().get(name);
        if (value != null && value instanceof String) {
            return (String) value;
        }
        
        return null;
    }
    
    protected String[] getStringArrayParameter(QueryParameters parameters, String name) {
        if (parameters.getParameters() == null) {
            return null;
        }
        
        Object value = parameters.getParameters().get(name);
        if (value != null && value instanceof String[]) {
            return (String[]) value;
        }
        
        return null;
    }
    
    protected Integer getIntParameter(QueryParameters parameters, String name) {
        if (parameters.getParameters() == null) {
            return null;
        }
        
        Object value = parameters.getParameters().get(name);
        if (value != null && value instanceof Integer) {
            return (Integer) value;
        }
        
        return null;
    }
    
    protected String stringArrayToQuery(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(' ');
        }
        
        return sb.toString();
    }
}