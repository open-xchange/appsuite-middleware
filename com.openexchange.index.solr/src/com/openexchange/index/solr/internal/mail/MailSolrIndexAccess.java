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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.TriggerType;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.mail.MailUUID;
import com.openexchange.index.solr.mail.SolrMailConstants;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.text.TextFinder;
import com.openexchange.solr.SolrCoreIdentifier;

/**
 * {@link MailSolrIndexAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSolrIndexAccess extends AbstractSolrIndexAccess<MailMessage> implements SolrMailConstants {

    /**
     * The trigger type.
     */
    private final TriggerType triggerType;

    /**
     * The helper instance.
     */
    protected final SolrInputDocumentHelper helper;

    /**
     * Initializes a new {@link MailSolrIndexAccess}.
     * 
     * @param identifier The Solr server identifier
     * @param triggerType The trigger type
     */
    public MailSolrIndexAccess(final SolrCoreIdentifier identifier, final TriggerType triggerType) {
        super(identifier);
        this.triggerType = triggerType;
        helper = SolrInputDocumentHelper.getInstance();
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        return SolrMailField.getIndexedFields();
    }

    @Override
    public void addEnvelopeData(final IndexDocument<MailMessage> document) throws OXException {
        final SolrInputDocument solrDocument = helper.inputDocumentFor(document.getObject(), userId, contextId);
        addDocument(solrDocument);
    }

    @Override
    public void addEnvelopeData(final Collection<IndexDocument<MailMessage>> col) throws OXException, InterruptedException {
        if (col == null || col.isEmpty()) {
            return;
        }
        final List<IndexDocument<MailMessage>> documents;
        if (col instanceof List) {
            documents = (List<IndexDocument<MailMessage>>) col;
        } else {
            documents = new ArrayList<IndexDocument<MailMessage>>(col);
        }

        final int chunkSize = ADD_ROWS;
        final int size = documents.size();
        int off = 0;
        while (off < size) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Thread interrupted while adding Solr input documents.");
            }
            int endIndex = off + chunkSize;
            if (endIndex >= size) {
                endIndex = size;
            }
            final List<IndexDocument<MailMessage>> subList = documents.subList(off, endIndex);
            final List<SolrInputDocument> solrDocuments = helper.inputDocumentsFor(subList, userId, contextId);
            addDocuments(solrDocuments);
            off = endIndex;
        }
    }
    
    private SolrDocument getIndexedDocument(final IndexDocument<MailMessage> document) throws OXException {
        final MailMessage mailMessage = document.getObject();
        final int accountId = mailMessage.getAccountId();
        final MailUUID uuid = new MailUUID(contextId, userId, accountId, mailMessage.getFolder(), mailMessage.getMailId());        
        
        final String uuidField = SolrMailField.UUID.solrName();
        if (uuidField != null) {
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(uuidField).append(":\"").append(uuid.getUUID()).append("\")");
            final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            solrQuery.setStart(Integer.valueOf(0));
            solrQuery.setRows(Integer.valueOf(1));            
            final QueryResponse queryResponse = query(solrQuery);
            final SolrDocumentList results = queryResponse.getResults();
            final long numFound = results.getNumFound();
            if (numFound > 0) {
                return results.get(0);
            }
        }
        
        
        return null;
    }

    @Override
    public void addContent(final IndexDocument<MailMessage> document) throws OXException {
        final MailMessage message = document.getObject();
        final SolrInputDocument inputDocument;
        {
            final SolrDocument solrDocument = getIndexedDocument(document);
            if (null == solrDocument) {
                inputDocument = helper.inputDocumentFor(message, userId, contextId);
            } else {
                final String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
                if (contentFlagField == null) {
                    return;
                }
                
                final Boolean contentFlag = (Boolean) solrDocument.getFieldValue(contentFlagField);
                if (null != contentFlag && contentFlag.booleanValue()) {
                    return;
                }
                inputDocument = new SolrInputDocument();
                for (final Entry<String, Object> entry : solrDocument.entrySet()) {
                    final String name = entry.getKey();
                    final SolrInputField field = new SolrInputField(name);
                    field.setValue(entry.getValue(), 1.0f);
                    inputDocument.put(name, field);
                }
            }
        }

        final TextFinder textFinder = new TextFinder();
        final String text = textFinder.getText(message);
        if (null != text) {
            final String contentField = SolrMailField.CONTENT.solrName();
            if (contentField != null) {
                inputDocument.setField(contentField, text);
            }
//            final Locale locale = detectLocale(text);
        }
        
        final String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
        if (contentFlagField != null) {
            inputDocument.setField(contentFlagField, Boolean.TRUE);    
        }
        addDocument(inputDocument);     
    }
    
    private static final boolean SINGLE_LOADING = true;
    
    @Override
    public void addContent(final Collection<IndexDocument<MailMessage>> documents) throws OXException, InterruptedException {
        if (SINGLE_LOADING) {
            addContentWithSingleLoading(documents);
        } else {
            addContentWithoutSingleLoading(documents);
        }
    }
    
    private void addContentWithSingleLoading(final Collection<IndexDocument<MailMessage>> documents) throws OXException, InterruptedException {
        final Collection<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (final IndexDocument<MailMessage> document : documents) {
            if (Thread.interrupted()) {
                // Clears the thread's interrupted flag
                throw new InterruptedException("Thread interrupted while adding mail contents.");
            }

            final MailMessage message = document.getObject();
            final SolrInputDocument inputDocument;
            {
                final SolrDocument solrDocument = getIndexedDocument(document);
                if (null == solrDocument) {
                    inputDocument = helper.inputDocumentFor(message, userId, contextId);
                } else {
                    inputDocument = new SolrInputDocument();
                    for (final Entry<String, Object> entry : solrDocument.entrySet()) {
                        final String name = entry.getKey();
                        final SolrInputField field = new SolrInputField(name);
                        field.setValue(entry.getValue(), 1.0f);
                        inputDocument.put(name, field);
                    }
                }
            }

            if (message instanceof ContentAwareMailMessage) {
                final ContentAwareMailMessage contentAwareMessage = (ContentAwareMailMessage) message;
                String text = contentAwareMessage.getPrimaryContent();
                if (null == text) {
                    final TextFinder textFinder = new TextFinder();
                    text = textFinder.getText(message);
                }
                if (null != text) {
                    final String contentField = SolrMailField.CONTENT.solrName();
                    if (contentField != null) {
                        inputDocument.setField(contentField, text);
                    }
//                    final Locale locale = detectLocale(text);
                }
            } else {
                final TextFinder textFinder = new TextFinder();
                final String text = textFinder.getText(message);
                if (null != text) {
                    final String contentField = SolrMailField.CONTENT.solrName();
                    if (contentField != null) {
                        inputDocument.setField(contentField, text);
                    }
//                    final Locale locale = detectLocale(text);
                }
            }
            
            final String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
            if (contentFlagField != null) {
                inputDocument.setField(contentFlagField, Boolean.TRUE);    
            }

            inputDocuments.add(inputDocument);
        }
        
        addDocuments(inputDocuments);
    }

    private void addContentWithoutSingleLoading(final Collection<IndexDocument<MailMessage>> documents) throws OXException, InterruptedException {
        final Collection<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>(documents.size());
        final Map<String, IndexDocument<MailMessage>> toAdd = new HashMap<String, IndexDocument<MailMessage>>(documents.size());
        final Map<String, SolrDocument> loadedDocuments = new HashMap<String, SolrDocument>(documents.size());
        
        final StringBuilder queryBuilder = new StringBuilder(128);
        boolean first = true;
        final String uuidField = SolrMailField.UUID.solrName();
        for (final IndexDocument<MailMessage> document : documents) {
            final MailMessage mailMessage = document.getObject();
            final int accountId = mailMessage.getAccountId();
            final MailUUID uuid = new MailUUID(contextId, userId, accountId, mailMessage.getFolder(), mailMessage.getMailId());
            
            if (uuidField != null) {
                if (first) {
                    queryBuilder.append('(').append(uuidField).append(":\"").append(uuid.getUUID()).append("\")");
                    first = false;
                } else {
                    queryBuilder.append(" OR ");
                    queryBuilder.append('(').append(uuidField).append(":\"").append(uuid.getUUID()).append("\")");
                }
                
                toAdd.put(uuid.getUUID(), document);
            }            
        }
        
        final SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
        solrQuery.setStart(Integer.valueOf(0));
        solrQuery.setRows(Integer.valueOf(documents.size()));   
        final QueryResponse queryResponse = query(solrQuery);
        final SolrDocumentList results = queryResponse.getResults();
        if (uuidField != null) {
            for (final SolrDocument solrDocument : results) {
                final String uuid = (String) solrDocument.getFieldValue(uuidField);
                loadedDocuments.put(uuid, solrDocument);
            }
        }
        
        for (final String uuid : toAdd.keySet()) {
            if (Thread.interrupted()) {
                // Clears the thread's interrupted flag
                throw new InterruptedException("Thread interrupted while adding mail contents.");
            }
            
            final IndexDocument<MailMessage> document = toAdd.get(uuid);
            final MailMessage message = document.getObject();
            final SolrDocument solrDocument = loadedDocuments.get(uuid);
            final SolrInputDocument inputDocument;
            if (solrDocument == null) {
                inputDocument = helper.inputDocumentFor(message, userId, contextId);
            } else {
                inputDocument = new SolrInputDocument();
                for (final Entry<String, Object> entry : solrDocument.entrySet()) {
                    final String name = entry.getKey();
                    final SolrInputField field = new SolrInputField(name);
                    field.setValue(entry.getValue(), 1.0f);
                    inputDocument.put(name, field);
                }
            }

            if (message instanceof ContentAwareMailMessage) {
                final ContentAwareMailMessage contentAwareMessage = (ContentAwareMailMessage) message;
                String text = contentAwareMessage.getPrimaryContent();
                if (null == text) {
                    final TextFinder textFinder = new TextFinder();
                    text = textFinder.getText(message);
                }
                if (null != text) {
                    final String contentField = SolrMailField.CONTENT.solrName();
                    if (contentField != null) {
                        inputDocument.setField(contentField, text);
                    }
//                        final Locale locale = detectLocale(text);
                }
            } else {
                final TextFinder textFinder = new TextFinder();
                final String text = textFinder.getText(message);
                if (null != text) {
                    final String contentField = SolrMailField.CONTENT.solrName();
                    if (contentField != null) {
                        inputDocument.setField(contentField, text);
                    }
//                    final Locale locale = detectLocale(text);
                }
            }
            
            final String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
            if (contentFlagField != null) {
                inputDocument.setField(contentFlagField, Boolean.TRUE);    
            }

            inputDocuments.add(inputDocument);
        }
        
        addDocuments(inputDocuments);
    }

    @Override
    public void addAttachments(final IndexDocument<MailMessage> document) throws OXException {
        addContent(document);
    }

    @Override
    public void addAttachments(final Collection<IndexDocument<MailMessage>> documents) throws OXException, InterruptedException {
        addContent(documents);
    }
    
    @Override
    public void change(final IndexDocument<MailMessage> document, final Set<? extends IndexField> fields) throws OXException {
        final Set<SolrMailField> solrFields = convertFields(fields);        
        final SolrInputDocument inputDocument = calculateAndSetChanges(document, solrFields);        
        addDocument(inputDocument, true);
    }

    @Override
    public void change(final Collection<IndexDocument<MailMessage>> documents, final Set<? extends IndexField> fields) throws OXException, InterruptedException {     
        final Set<SolrMailField> solrFields = convertFields(fields);     
        final List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (final IndexDocument<MailMessage> document : documents) {
            if (Thread.interrupted()) {
                // Clears the thread's interrupted flag
                throw new InterruptedException("Thread interrupted while changing mail contents.");
            }
            final SolrInputDocument inputDocument = calculateAndSetChanges(document, solrFields); 
            inputDocuments.add(inputDocument);
        }
        
        addDocuments(inputDocuments);
    }
    
    private SolrInputDocument calculateAndSetChanges(final IndexDocument<MailMessage> document, final Set<SolrMailField> fields) throws OXException {
        final SolrDocument solrDocument = getIndexedDocument(document);
        final MailMessage mailMessage = document.getObject();
        final SolrInputDocument inputDocument;
        if (null == solrDocument) {
            inputDocument = helper.inputDocumentFor(mailMessage, userId, contextId);
        } else {
            inputDocument = new SolrInputDocument();
            for (final Entry<String, Object> entry : solrDocument.entrySet()) {
                final String name = entry.getKey();
                final SolrInputField field = new SolrInputField(name);
                field.setValue(entry.getValue(), 1.0f);
                inputDocument.put(name, field);
            }
            
            /*
             * Write color label and flags
             */
            for (final SolrMailField field : fields) {
                final Object value = field.getValueFromMail(mailMessage);
                if (value != null) {                    
                    SolrInputDocumentHelper.setFieldInDocument(inputDocument, field, value);
                }
            }
        }
        
        return inputDocument;
    }

    @Override
    public void deleteById(final String id) throws OXException {
        deleteDocumentById(id);
    }

    @Override
    public void deleteByQuery(final QueryParameters parameters) throws OXException {
        final SearchHandler searchHandler = checkQueryParametersAndGetSearchHandler(parameters);  
        if (searchHandler.equals(SearchHandler.ALL_REQUEST)) {
            final int accountId = getAccountId(parameters);
            final String folder = parameters.getFolder();
            String queryString = buildQueryString(accountId, folder);
            if (queryString.length() == 0) {
            	queryString = "*:*";
            }
            deleteDocumentsByQuery(queryString);
        } else {
            throw new NotImplementedException("Search handler " + searchHandler.name() + " is not implemented for MailSolrIndexAccess.deleteByQuery().");
        }
    }

    @Override
    public IndexResult<MailMessage> query(final QueryParameters parameters) throws OXException, InterruptedException {
        final SearchHandler searchHandler = checkQueryParametersAndGetSearchHandler(parameters);     
        final List<IndexDocument<MailMessage>> mails = new ArrayList<IndexDocument<MailMessage>>();
        IndexResult<MailMessage> result = Indexes.emptyResult();
        if (searchHandler.equals(SearchHandler.ALL_REQUEST)) {
            final int accountId = getAccountId(parameters);
            final String folder = parameters.getFolder(); 
            final SolrQuery solrQuery;
            if (folder == null) {
                solrQuery = new SolrQuery("*:*");
            } else {
                solrQuery = new SolrQuery(folder);
                solrQuery.setQueryType("allSearch");
            }
            solrQuery.setFilterQueries(buildFilterQueries(accountId, null));
            setSortAndOrder(parameters, solrQuery);

            int off = parameters.getOff();
            final int len = parameters.getLen();
            int maxRows = len;
            if (maxRows > QUERY_ROWS) {
                maxRows = QUERY_ROWS;
            }
            do {                
                solrQuery.setStart(off);
                solrQuery.setRows(maxRows);
                final QueryResponse queryResponse = query(solrQuery);
                final SolrDocumentList results = queryResponse.getResults();                
                final int size = results.size();
                if (results.size() == 0) {
                    break;
                }
                
                for (int i = 0; i < size; i++) {
                    mails.add(helper.readDocument(results.get(i), MailFillers.allFillers()));
                }
                off += size;
            } while (off < len);
            
            final MailIndexResult indexResult = new MailIndexResult(mails.size());
            indexResult.setResults(mails);
            result = indexResult;
        } else if (searchHandler.equals(SearchHandler.SIMPLE)) {
            final SolrQuery solrQuery = new SolrQuery(parameters.getPattern());            
            solrQuery.setQueryType("simpleSearch");
            setSortAndOrder(parameters, solrQuery);
            solrQuery.setStart(parameters.getOff());
            solrQuery.setRows(parameters.getLen());
            solrQuery.setFilterQueries(buildFilterQueries(getAccountId(parameters), parameters.getFolder()));
            
            final QueryResponse queryResponse = query(solrQuery);
            final SolrDocumentList results = queryResponse.getResults();   
            for (int i = 0; i < results.size(); i++) {
                mails.add(helper.readDocument(results.get(i), MailFillers.allFillers()));
            }
            
            final MailIndexResult indexResult = new MailIndexResult(mails.size());
            indexResult.setResults(mails);
            result = indexResult;
        } else if (searchHandler.equals(SearchHandler.GET_REQUEST)) {
            final String[] ids = getIds(parameters);
            final int accountId = getAccountId(parameters);
            final String folder = parameters.getFolder();
            final String queryString = buildQueryString(accountId, folder);
            final StringBuilder sb = new StringBuilder(queryString);
            if (queryString.length() != 0) {
            	sb.append(" AND (");
            } else {
            	sb.append('(');
            }
            boolean first = true;
            for (final String id : ids) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" OR ");
                }
                sb.append('(').append(SolrMailField.UUID.solrName()).append(":\"").append(id).append("\")");
            }
            sb.append(')');
            
            final SolrQuery solrQuery = new SolrQuery(sb.toString());
            setSortAndOrder(parameters, solrQuery);
            solrQuery.setStart(parameters.getOff());
            solrQuery.setRows(parameters.getLen());
            
            final QueryResponse queryResponse = query(solrQuery);
            final SolrDocumentList results = queryResponse.getResults();   
            for (int i = 0; i < results.size(); i++) {
                mails.add(helper.readDocument(results.get(i), MailFillers.allFillers()));
            }
            
            final MailIndexResult indexResult = new MailIndexResult(mails.size());
            indexResult.setResults(mails);
            result = indexResult;  
        } else if (searchHandler.equals(SearchHandler.CUSTOM)) {
            final SearchTerm<?> searchTerm = (SearchTerm<?>) parameters.getSearchTerm();
            final SolrQuery solrQuery = new SolrQuery("*:*");
            solrQuery.setQueryType("customSearch");
            final StringBuilder queryBuilder = SearchTerm2Query.searchTerm2Query(searchTerm);
            solrQuery.add("cq", queryBuilder.toString());
            setSortAndOrder(parameters, solrQuery);
            solrQuery.setStart(parameters.getOff());
            solrQuery.setRows(parameters.getLen());
            solrQuery.setFilterQueries(buildFilterQueries(getAccountId(parameters), parameters.getFolder()));
            
            final QueryResponse queryResponse = query(solrQuery);
            // TODO: analyze svens header for failed query parsing
            final SolrDocumentList results = queryResponse.getResults();   
            for (int i = 0; i < results.size(); i++) {
                mails.add(helper.readDocument(results.get(i), MailFillers.allFillers()));
            }
            
            final MailIndexResult indexResult = new MailIndexResult(mails.size());
            indexResult.setResults(mails);
            result = indexResult;
        } else {
            throw new NotImplementedException("Search handler " + searchHandler.name() + " is not implemented for MailSolrIndexAccess.query().");
        }
        
        return result;
    }

    @Override
    public TriggerType getTriggerType() {
        return triggerType;
    }
    
    private SearchHandler checkQueryParametersAndGetSearchHandler(final QueryParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter `parameters` must not be null!");
        }
        
        final SearchHandler searchHandler = parameters.getHandler();        
        if (searchHandler == null) {
            throw new IllegalArgumentException("Parameter `search handler` must not be null!");
        }
        
        final Type type = parameters.getType();
        if (type == null || type != Type.MAIL) {
            throw new IllegalArgumentException("Parameter `type` must be `mail`!");
        }
        
        switch(searchHandler) {
            case ALL_REQUEST:
                return searchHandler;
                
            case CUSTOM:
                final Object searchTerm = parameters.getSearchTerm();
                if (searchTerm == null) {
                    throw new IllegalArgumentException("Parameter `search term` must not be null!");
                } else if (!(searchTerm instanceof SearchTerm)) {
                    throw new IllegalArgumentException("Parameter `search term` must be an instance of com.openexchange.mail.search.SearchTerm!");
                }
                return searchHandler;
                
            case GET_REQUEST:
                final Map<String, Object> params = parameters.getParameters();
                if (params == null) {
                    throw new IllegalArgumentException("Parameter `parameters.parameters` must not be null!");
                }
                final Object idsObj = params.get("ids");
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
    
    private int getAccountId(final QueryParameters parameters) {
        final Object accountIdObj = parameters.getParameters().get("accountId");
        if (accountIdObj == null || !(accountIdObj instanceof Integer)) {
            return -1;
        }
        
        return ((Integer) accountIdObj).intValue();
    }
    
    private String[] getIds(final QueryParameters parameters) {
        return (String[]) parameters.getParameters().get("ids");
    }
    
    private void setSortAndOrder(final QueryParameters parameters, final SolrQuery query) {
        final IndexField indexField = parameters.getSortField();
        String sortField = null;
        if (indexField != null && indexField instanceof MailIndexField) {
            sortField = SolrMailField.solrMailFieldFor((MailIndexField) indexField).solrName();
        }
        
        final String orderStr = parameters.getOrder();
        if (sortField != null) {
            final ORDER order = orderStr == null ? ORDER.desc : orderStr.equalsIgnoreCase("desc") ? ORDER.desc : ORDER.asc;
            query.setSortField(sortField, order);
        }
    }

    private String buildQueryString(final int accountId, final String folder) {
        final StringBuilder sb = new StringBuilder(128); 
        if (SolrMailField.ACCOUNT.isIndexed() && accountId >= 0) {
            sb.append(" AND ");
            sb.append('(').append(SolrMailField.ACCOUNT.solrName()).append(":\"").append(accountId).append("\")");
        }
            
        if (SolrMailField.FULL_NAME.isIndexed() && folder != null) {
            sb.append(" AND ");
            sb.append('(').append(SolrMailField.FULL_NAME.solrName()).append(":\"").append(folder).append("\")");
        }  
        
        return sb.toString();
    }
    
    private String[] buildFilterQueries(final int accountId, final String folder) {
        final List<String> filters = new ArrayList<String>(2);
        if (SolrMailField.ACCOUNT.isIndexed() && accountId >= 0) {
            filters.add(SolrMailField.ACCOUNT.solrName() + ':' + String.valueOf(accountId));
        }
        if (SolrMailField.FULL_NAME.isIndexed() && folder != null) {
            filters.add(SolrMailField.FULL_NAME.solrName() + ':' + folder);
        }
        
        return filters.toArray(new String[filters.size()]);
    }
    
    private Set<SolrMailField> convertFields(final Set<? extends IndexField> fields) {
        final Set<SolrMailField> solrFields;
        if (fields == null) {
            solrFields = new HashSet<SolrMailField>(Arrays.asList(SolrMailField.values()));
        } else {
            solrFields = new HashSet<SolrMailField>();
            for (final IndexField field : fields) {
                if (field instanceof MailIndexField) {
                    solrFields.add(SolrMailField.solrMailFieldFor((MailIndexField) field));
                }
            }
        }
        
        return solrFields;
    }
}
