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
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.exception.OXException;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexManagementService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.converter.SolrDocumentConverter;
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

    protected final FieldConfiguration fieldConfig;

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
    protected AbstractSolrIndexAccess(final SolrCoreIdentifier identifier, final FieldConfiguration fieldConfig) {
        super();
        this.identifier = identifier;
        this.contextId = identifier.getContextId();
        this.userId = identifier.getUserId();
        this.module = identifier.getModule();
        this.fieldConfig = fieldConfig;
        lastAccess = System.currentTimeMillis();
        retainCount = new AtomicInteger(0);
        indexedFolders = new HashMap<Integer, Map<String, Set<String>>>();
    }

    /*
     * Implemented methods
     */
    protected boolean isIndexed(int module, String accountId, String folderId) throws OXException {
        checkIfIndexIsLocked();
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

    @Override
    public void addDocument(IndexDocument<V> document) throws OXException {
        checkIfIndexIsLocked();
        addDocument0(document);
    }

    @Override
    public void addDocuments(Collection<IndexDocument<V>> documents) throws OXException {
        checkIfIndexIsLocked();
        addDocuments0(documents);
    }

    @Override
    public void deleteById(String id) throws OXException {
        checkIfIndexIsLocked();
        deleteById0(id);
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        checkIfIndexIsLocked();
        deleteByQuery0(parameters);
    }

    @Override
    public IndexResult<V> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {
        checkIfIndexIsLocked();
        return query0(parameters, fields);
    }

    @Override
    public IndexResult<V> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        checkIfIndexIsLocked();
        return query0(parameters, facetParameters, fields);
    }

    /*
     * Abstract methods
     */
    public abstract void addDocument0(IndexDocument<V> document) throws OXException;

    public abstract void addDocuments0(Collection<IndexDocument<V>> documents) throws OXException;

    public abstract void deleteById0(String id) throws OXException;

    public abstract void deleteByQuery0(QueryParameters parameters) throws OXException;

    public abstract IndexResult<V> query0(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException;

    public abstract IndexResult<V> query0(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException;

    /*
     * Public methods
     */
    public void releaseCore() {
        indexedFolders.clear();
        indexedFolders = null;
        indexedFolders = new HashMap<Integer, Map<String, Set<String>>>();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);
        if (accessService != null) {
            accessService.freeResources(identifier);
        }
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
    protected UpdateResponse addSolrDocument(final SolrInputDocument document) throws OXException {
        return addSolrDocument(document, true);
    }

    protected UpdateResponse addSolrDocuments(final Collection<SolrInputDocument> documents) throws OXException {
        return addSolrDocuments(documents, true);
    }

    protected UpdateResponse addSolrDocument(final SolrInputDocument document, final boolean commit) throws OXException {
        lastAccess = System.currentTimeMillis();
        final SolrAccessService accessService = Services.getService(SolrAccessService.class);
        final UpdateResponse response = accessService.add(identifier, document, commit);
        return response;
    }

    protected UpdateResponse addSolrDocuments(final Collection<SolrInputDocument> documents, final boolean commit) throws OXException {
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

    protected void checkIfIndexIsLocked() throws OXException {
        IndexManagementService managementService = Services.getService(IndexManagementService.class);
        if (managementService.isLocked(contextId, userId, module)) {
            throw IndexExceptionCodes.INDEX_LOCKED.create(module, userId, contextId);
        }
    }

    protected IndexResult<V> queryChunkWise(String uuidField, SolrDocumentConverter<V> converter, SolrQuery solrQuery, int off, int len, int chunkSize) throws OXException {
        List<IndexDocument<V>> indexDocuments = new ArrayList<IndexDocument<V>>();
        int fetched = 0;
        int maxRows = len;
        if (maxRows > chunkSize) {
            maxRows = chunkSize;
        }

        long numFound = -1L;
        do {
            solrQuery.setStart(off);
            if ((fetched + maxRows) > len) {
                maxRows = (len - fetched);
            }

            solrQuery.setRows(maxRows);
            QueryResponse queryResponse = query(solrQuery);
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
            SolrDocumentList results = queryResponse.getResults();
            if (results == null) {
                return Indexes.emptyResult();
            }

            if (numFound < 0) {
                numFound = results.getNumFound();
            }

            for (SolrDocument document : results) {
                if (highlighting != null && document.containsKey(uuidField)) {
                    String uuid = (String) document.getFieldValue(uuidField);
                    if (uuid != null) {
                        Map<String, List<String>> highlightFields = highlighting.get(uuid);
                        if (highlightFields != null) {
                            indexDocuments.add(converter.convert(document, highlightFields));
                            continue;
                        }
                    }
                }

                indexDocuments.add(converter.convert(document));
            }

            if (results.size() < maxRows) {
                break;
            }

            fetched += maxRows;
            off += maxRows;
        } while (fetched < len);

        if (numFound == 0L && indexDocuments.isEmpty()) {
            return Indexes.emptyResult();
        }

        return converter.createIndexResult(numFound, indexDocuments, null);
    }

    protected Set<String> collectFields(Set<? extends IndexField> fields) {
        if (fields == null) {
            return null;
        }

        Set<String> allFields = new HashSet<String>();
        for (IndexField indexField : fields) {
            Set<String> solrFields = fieldConfig.getSolrFields(indexField);
            if (solrFields != null) {
                allFields.addAll(solrFields);
            }
        }

        return allFields;
    }

    protected void setFieldList(SolrQuery solrQuery, Set<? extends IndexField> fields) {
        if (fields == null) {
            return;
        }

        Set<String> allFields = collectFields(fields);
        for (String field : allFields) {
            solrQuery.addField(field);
        }
    }

    // protected IndexResult<V> queryChunkWise1(SolrResultConverter<V> converter, SolrQuery solrQuery, int off, int len, int chunkSize)
    // throws OXException {
    // List<IndexDocument<V>> indexDocuments = new ArrayList<IndexDocument<V>>();
    // Map<IndexField, Map<String, Long>> facetCountsMap = null;
    // int fetched = 0;
    // int maxRows = len;
    // if (maxRows > chunkSize) {
    // maxRows = chunkSize;
    // }
    // do {
    // solrQuery.setStart(off);
    // if ((fetched + maxRows) > len) {
    // maxRows = (len - fetched);
    // }
    // solrQuery.setRows(maxRows);
    // QueryResponse queryResponse = query(solrQuery);
    // SolrDocumentList results = queryResponse.getResults();
    // for (SolrDocument document : results) {
    // indexDocuments.add(converter.convert(document));
    // }
    //
    // List<FacetField> facetFields = queryResponse.getFacetFields();
    // if (null != facetFields) {
    // if (null == facetCountsMap) {
    // // Initialize map
    // facetCountsMap = new HashMap<IndexField, Map<String,Long>>(facetFields.size());
    // }
    // for (final FacetField facetField : facetFields) {
    // final List<Count> counts = facetField.getValues();
    // if (null != counts) {
    // final MailIndexField field = SolrMailField.fieldFor(facetField.getName());
    // if (null != field) {
    // Map<String, Long> map = facetCountsMap.get(field);
    // if (null == map) {
    // map = new HashMap<String, Long>(counts.size());
    // facetCountsMap.put(field, map);
    // }
    // for (final Count count : counts) {
    // final String countName = count.getName();
    // final Long l = map.get(countName);
    // if (null == l) {
    // map.put(countName, Long.valueOf(count.getCount()));
    // } else {
    // map.put(countName, Long.valueOf(count.getCount() + l.longValue()));
    // }
    // }
    // }
    // }
    // }
    // }
    //
    // if (results.size() < maxRows) {
    // break;
    // }
    //
    // fetched += maxRows;
    // off += maxRows;
    // } while (fetched < len);
    //
    // return converter.createIndexResult(indexDocuments, facetCountsMap);
    // }

    // protected String buildQueryString(String fieldName, Object value) {
    // if (fieldName == null || value == null) {
    // return null;
    // }
    //
    // StringBuilder sb = new StringBuilder();
    // sb.append('(').append(fieldName).append(":\"").append(value.toString()).append("\")");
    // return sb.toString();
    // }
    //
}
