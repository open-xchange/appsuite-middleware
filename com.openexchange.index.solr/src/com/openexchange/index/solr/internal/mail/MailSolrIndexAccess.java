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
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.mail.MailUUID;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.text.TextFinder;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.SolrProperties;

/**
 * {@link MailSolrIndexAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailSolrIndexAccess extends AbstractSolrIndexAccess<MailMessage> {
    
    /**
     * The max. number of rows fetched for one solr request.
     * If the found number of rows is bigger, the results will be fetched in chunks.
     */
    protected static final int QUERY_ROWS = 2000;
    
    protected static final int ADD_ROWS = 2000;

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
    public MailSolrIndexAccess(SolrCoreIdentifier identifier) {
        super(identifier);
        helper = SolrInputDocumentHelper.getInstance();
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        return SolrMailField.getIndexedFields();
    }

    @Override
    public void addEnvelopeData(IndexDocument<MailMessage> document) throws OXException {
        SolrInputDocument solrDocument = helper.inputDocumentFor(document.getObject(), userId, contextId);
        addDocument(solrDocument);
    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<MailMessage>> col) throws OXException, InterruptedException {
        if (col == null || col.isEmpty()) {
            return;
        }
        List<IndexDocument<MailMessage>> documents;
        if (col instanceof List) {
            documents = (List<IndexDocument<MailMessage>>) col;
        } else {
            documents = new ArrayList<IndexDocument<MailMessage>>(col);
        }

        int chunkSize = ADD_ROWS;
        int size = documents.size();
        int off = 0;
        while (off < size) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Thread interrupted while adding Solr input documents.");
            }
            int endIndex = off + chunkSize;
            if (endIndex >= size) {
                endIndex = size;
            }
            List<IndexDocument<MailMessage>> subList = documents.subList(off, endIndex);
            List<SolrInputDocument> solrDocuments = helper.inputDocumentsFor(subList, userId, contextId);
            addDocuments(solrDocuments);
            off = endIndex;
        }
    }
    
    private SolrDocument getIndexedDocument(IndexDocument<MailMessage> document) throws OXException {
        // TODO: Use Get-Handler
        MailMessage mailMessage = document.getObject();
        int accountId = mailMessage.getAccountId();
        MailUUID uuid = new MailUUID(contextId, userId, accountId, mailMessage.getFolder(), mailMessage.getMailId());        
        
        String uuidField = SolrMailField.UUID.solrName();
        if (uuidField != null) {
            StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(uuidField).append(":\"").append(uuid.getUUID()).append("\")");
            SolrQuery solrQuery = new SolrQuery().setQuery(queryBuilder.toString());
            solrQuery.setStart(Integer.valueOf(0));
            solrQuery.setRows(Integer.valueOf(1));            
            QueryResponse queryResponse = query(solrQuery);
            SolrDocumentList results = queryResponse.getResults();
            long numFound = results.getNumFound();
            if (numFound > 0) {
                return results.get(0);
            }
        }
        
        
        return null;
    }

    @Override
    public void addContent(IndexDocument<MailMessage> document, boolean full) throws OXException {
        MailMessage message = document.getObject();
        SolrInputDocument inputDocument;
        if (full) {
            inputDocument = helper.inputDocumentFor(message, userId, contextId);
        } else {
            SolrDocument solrDocument = getIndexedDocument(document);
            if (solrDocument == null) {
                MailUUID uuid = new MailUUID(contextId, userId, message.getAccountId(), message.getFolder(), message.getMailId());    
                throw SolrExceptionCodes.DOCUMENT_NOT_FOUND.create(uuid.toString());
            }
            
            String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
            if (contentFlagField == null) {
                return;
            }
            
            Boolean contentFlag = (Boolean) solrDocument.getFieldValue(contentFlagField);
            if (null != contentFlag && contentFlag.booleanValue()) {
                return;
            }
            inputDocument = new SolrInputDocument();
            for (Entry<String, Object> entry : solrDocument.entrySet()) {
                String name = entry.getKey();
                SolrInputField field = new SolrInputField(name);
                field.setValue(entry.getValue(), 1.0f);
                inputDocument.put(name, field);
            }
        }
        
        if (message instanceof ContentAwareMailMessage) {
            ContentAwareMailMessage contentAwareMessage = (ContentAwareMailMessage) message;
            String text = contentAwareMessage.getPrimaryContent();
            if (null == text) {
                TextFinder textFinder = new TextFinder();
                text = textFinder.getText(message);
            }
            if (null != text) {
                String contentField = SolrMailField.CONTENT.solrName();
                if (contentField != null) {
                    inputDocument.setField(contentField, text);
                }
            }
        } else {
            TextFinder textFinder = new TextFinder();
            String text = textFinder.getText(message);
            if (null != text) {
                String contentField = SolrMailField.CONTENT.solrName();
                if (contentField != null) {
                    inputDocument.setField(contentField, text);
                }
            }
        }
        
        String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
        if (contentFlagField != null) {
            inputDocument.setField(contentFlagField, Boolean.TRUE);    
        }
        
        addDocument(inputDocument);           
    }
        
    @Override
    public void addContent(Collection<IndexDocument<MailMessage>> documents, boolean full) throws OXException, InterruptedException {
        Collection<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<MailMessage> document : documents) {
            MailMessage message = document.getObject();
            SolrInputDocument inputDocument;
            if (full) {
                inputDocument = helper.inputDocumentFor(message, userId, contextId);
            } else {
                SolrDocument solrDocument = getIndexedDocument(document);
                if (solrDocument == null) {
                    MailUUID uuid = new MailUUID(contextId, userId, message.getAccountId(), message.getFolder(), message.getMailId());    
                    throw SolrExceptionCodes.DOCUMENT_NOT_FOUND.create(uuid.toString());
                }
                
                String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
                if (contentFlagField == null) {
                    return;
                }
                
                Boolean contentFlag = (Boolean) solrDocument.getFieldValue(contentFlagField);
                if (null != contentFlag && contentFlag.booleanValue()) {
                    return;
                }
                inputDocument = new SolrInputDocument();
                for (Entry<String, Object> entry : solrDocument.entrySet()) {
                    String name = entry.getKey();
                    SolrInputField field = new SolrInputField(name);
                    field.setValue(entry.getValue(), 1.0f);
                    inputDocument.put(name, field);
                }
            }

            if (message instanceof ContentAwareMailMessage) {
                ContentAwareMailMessage contentAwareMessage = (ContentAwareMailMessage) message;
                String text = contentAwareMessage.getPrimaryContent();
                if (null == text) {
                    TextFinder textFinder = new TextFinder();
                    text = textFinder.getText(message);
                }
                if (null != text) {
                    String contentField = SolrMailField.CONTENT.solrName();
                    if (contentField != null) {
                        inputDocument.setField(contentField, text);
                    }
                }
            } else {
                TextFinder textFinder = new TextFinder();
                String text = textFinder.getText(message);
                if (null != text) {
                    String contentField = SolrMailField.CONTENT.solrName();
                    if (contentField != null) {
                        inputDocument.setField(contentField, text);
                    }
                }
            }
            
            String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
            if (contentFlagField != null) {
                inputDocument.setField(contentFlagField, Boolean.TRUE);    
            }

            inputDocuments.add(inputDocument);
        }
        
        addDocuments(inputDocuments);
    }

    @Override
    public void addAttachments(IndexDocument<MailMessage> document, boolean full) throws OXException {
        addContent(document, full);
    }

    @Override
    public void addAttachments(Collection<IndexDocument<MailMessage>> documents, boolean full) throws OXException, InterruptedException {
        addContent(documents, full);
    }
    
    @Override
    public void change(IndexDocument<MailMessage> document, Set<? extends IndexField> fields) throws OXException {
        Set<SolrMailField> solrFields = convertFields(fields);        
        SolrInputDocument inputDocument = calculateAndSetChanges(document, solrFields);        
        addDocument(inputDocument, true);
    }

    @Override
    public void change(Collection<IndexDocument<MailMessage>> documents, Set<? extends IndexField> fields) throws OXException, InterruptedException {     
        Set<SolrMailField> solrFields = convertFields(fields);     
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<MailMessage> document : documents) {
            SolrInputDocument inputDocument = calculateAndSetChanges(document, solrFields); 
            inputDocuments.add(inputDocument);
        }
        
        addDocuments(inputDocuments);
    }
    
    private SolrInputDocument calculateAndSetChanges(IndexDocument<MailMessage> document, Set<SolrMailField> fields) throws OXException {
        SolrDocument solrDocument = getIndexedDocument(document);
        MailMessage mailMessage = document.getObject();
        SolrInputDocument inputDocument;
        if (null == solrDocument) {
            inputDocument = helper.inputDocumentFor(mailMessage, userId, contextId);
        } else {
            inputDocument = new SolrInputDocument();
            for (Entry<String, Object> entry : solrDocument.entrySet()) {
                String name = entry.getKey();
                SolrInputField field = new SolrInputField(name);
                field.setValue(entry.getValue(), 1.0f);
                inputDocument.put(name, field);
            }
            
            /*
             * Write color label and flags
             */
            for (SolrMailField field : fields) {
                Object value = field.getValueFromMail(mailMessage);
                if (value != null) {                    
                    SolrInputDocumentHelper.setFieldInDocument(inputDocument, field, value);
                }
            }
        }
        
        return inputDocument;
    }

    @Override
    public void deleteById(String id) throws OXException {
        deleteDocumentById(id);
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        SearchHandler searchHandler = checkQueryParametersAndGetSearchHandler(parameters);  
        if (searchHandler.equals(SearchHandler.ALL_REQUEST)) {
            int accountId = getAccountId(parameters);
            String folder = parameters.getFolder();
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
    public IndexResult<MailMessage> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException, InterruptedException {
        Set<SolrMailField> solrFields = convertFields(fields);
        List<IndexDocument<MailMessage>> mails = new ArrayList<IndexDocument<MailMessage>>();
        SolrQuery solrQuery = buildSolrQuery(parameters);        
        setFieldList(solrQuery, solrFields);
        int off = parameters.getOff();
        int len = parameters.getLen();
        int fetched = 0;
        int maxRows = len;
        if (maxRows > QUERY_ROWS) {
            maxRows = QUERY_ROWS;
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
                mails.add(helper.readDocument(document, MailFillers.allFillers()));
            }

            if (results.size() < maxRows) {
                break;
            }
            
            fetched += maxRows;
            off += maxRows;
        } while (fetched < len);
        
        if (mails.isEmpty()) {
            return Indexes.emptyResult();
        } else {
            MailIndexResult indexResult = new MailIndexResult(mails.size());
            indexResult.setResults(mails);
            return indexResult;
        }        
    }
    
    private void setFieldList(SolrQuery solrQuery, Set<SolrMailField> fields) {
        String[] solrFields = SolrMailField.solrNamesFor(fields);
        solrQuery.setFields(solrFields);
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
                    solrQuery = new SolrQuery(folder);
                    solrQuery.setQueryType(handler);
                }
                solrQuery.setFilterQueries(buildFilterQueries(accountId, null));
                setSortAndOrder(parameters, solrQuery);
                break;
            }                
                
            case SIMPLE:
                {
                    ConfigurationService config = Services.getService(ConfigurationService.class);
                    String handler = config.getProperty(SolrProperties.SIMPLE_HANLDER);
                    solrQuery = new SolrQuery(parameters.getPattern());
                    solrQuery.setQueryType(handler);
                    setSortAndOrder(parameters, solrQuery);
                    solrQuery.setFilterQueries(buildFilterQueries(getAccountId(parameters), parameters.getFolder()));
                    break;
                }
                
            case GET_REQUEST:
                {
                    String[] ids = getIds(parameters);
                    int accountId = getAccountId(parameters);
                    String folder = parameters.getFolder();
                    String queryString = buildQueryString(accountId, folder);
                    StringBuilder sb = new StringBuilder(queryString);
                    if (queryString.length() != 0) {
                        sb.append(" AND (");
                    } else {
                        sb.append('(');
                    }
                    boolean first = true;
                    for (String id : ids) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(" OR ");
                        }
                        sb.append('(').append(SolrMailField.UUID.solrName()).append(":\"").append(id).append("\")");
                    }
                    sb.append(')');
                    solrQuery = new SolrQuery(sb.toString());
                    setSortAndOrder(parameters, solrQuery);
                    break;
                }
                
            case CUSTOM:
            {
                ConfigurationService config = Services.getService(ConfigurationService.class);
                String handler = config.getProperty(SolrProperties.CUSTOM_HANLDER);
                SearchTerm<?> searchTerm = (SearchTerm<?>) parameters.getSearchTerm();
                StringBuilder queryBuilder = SearchTerm2Query.searchTerm2Query(searchTerm);
                solrQuery = new SolrQuery(queryBuilder.toString());
                solrQuery.setQueryType(handler);
                setSortAndOrder(parameters, solrQuery);
                solrQuery.setFilterQueries(buildFilterQueries(getAccountId(parameters), parameters.getFolder()));
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
        if (type == null || type != Type.MAIL) {
            throw new IllegalArgumentException("Parameter `type` must be `mail`!");
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
    
    private void setSortAndOrder(QueryParameters parameters, SolrQuery query) {
        IndexField indexField = parameters.getSortField();
        String sortField = null;
        if (indexField != null && indexField instanceof MailIndexField) {
            sortField = SolrMailField.solrMailFieldFor((MailIndexField) indexField).solrName();
        }
        
        String orderStr = parameters.getOrder();
        if (sortField != null) {
            ORDER order = orderStr == null ? ORDER.desc : orderStr.equalsIgnoreCase("desc") ? ORDER.desc : ORDER.asc;
            query.setSortField(sortField, order);
        }
    }

    private String buildQueryString(int accountId, String folder) {
        StringBuilder sb = new StringBuilder(128); 
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
    
    private String[] buildFilterQueries(int accountId, String folder) {
        List<String> filters = new ArrayList<String>(2);
        if (SolrMailField.ACCOUNT.isIndexed() && accountId >= 0) {
            filters.add(SolrMailField.ACCOUNT.solrName() + ':' + String.valueOf(accountId));
        }
        if (SolrMailField.FULL_NAME.isIndexed() && folder != null) {
            filters.add(SolrMailField.FULL_NAME.solrName() + ':' + folder);
        }
        
        return filters.toArray(new String[filters.size()]);
    }
    
    private Set<SolrMailField> convertFields(Set<? extends IndexField> fields) {
        Set<SolrMailField> solrFields;
        if (fields == null) {
            solrFields = new HashSet<SolrMailField>(Arrays.asList(SolrMailField.values()));
        } else {
            solrFields = new HashSet<SolrMailField>();
            for (IndexField field : fields) {
                if (field instanceof MailIndexField) {
                    solrFields.add(SolrMailField.solrMailFieldFor((MailIndexField) field));
                }
            }
        }
        
        return solrFields;
    }
}
