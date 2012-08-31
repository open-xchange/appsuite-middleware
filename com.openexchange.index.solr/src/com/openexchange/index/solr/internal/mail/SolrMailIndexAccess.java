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

package com.openexchange.index.solr.internal.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.mail.MailUUID;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;

/**
 * {@link SolrMailIndexAccess}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMailIndexAccess extends AbstractSolrIndexAccess<MailMessage> {

    /**
     * Initializes a new {@link SolrMailIndexAccess}.
     * 
     * @param identifier
     */
    public SolrMailIndexAccess(SolrCoreIdentifier identifier) {
        super(identifier);
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        return isIndexed(Types.EMAIL, accountId, folderId);
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        return SolrMailField.getIndexedFields();
    }

    @Override
    public void addEnvelopeData(IndexDocument<MailMessage> document) throws OXException {
        addDocument(convertToDocument(document));
    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<MailMessage>> documents) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<MailMessage> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addDocuments(inputDocuments);        
    }

    @Override
    public void addContent(IndexDocument<MailMessage> document, boolean full) throws OXException {
        addDocument(convertToDocument(document));
    }

    @Override
    public void addContent(Collection<IndexDocument<MailMessage>> documents, boolean full) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<MailMessage> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addDocuments(inputDocuments);  
    }

    @Override
    public void addAttachments(IndexDocument<MailMessage> document, boolean full) throws OXException {
        addDocument(convertToDocument(document));
    }

    @Override
    public void addAttachments(Collection<IndexDocument<MailMessage>> documents, boolean full) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<MailMessage> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addDocuments(inputDocuments);  
    }

    @Override
    public void change(final IndexDocument<MailMessage> document, final Set<? extends IndexField> fields) throws OXException {
        throw new UnsupportedOperationException("change is not implemented.");
    }

    @Override
    public void change(final Collection<IndexDocument<MailMessage>> documents, final Set<? extends IndexField> fields) throws OXException {     
        throw new UnsupportedOperationException("change is not implemented.");
    }

    @Override
    public void deleteById(String id) throws OXException {
        deleteDocumentById(id);
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        Set<MailIndexField> fields = EnumSet.noneOf(MailIndexField.class);
        fields.add(MailIndexField.ACCOUNT);
        fields.add(MailIndexField.FULL_NAME);
        fields.add(MailIndexField.ID);
        
        IndexResult<MailMessage> indexResult = query(parameters, fields);
        List<IndexDocument<MailMessage>> documents = indexResult.getResults();
        Set<String> uuids = new HashSet<String>(documents.size());
        for (IndexDocument<MailMessage> document : documents) {
            uuids.add(MailUUID.newUUID(contextId, userId, document.getObject()).toString());
        }
        
        String deleteQuery = buildQueryStringWithOr(SolrMailField.UUID.solrName(), uuids);
        deleteDocumentsByQuery(deleteQuery);
    }

    @Override
    public IndexResult<MailMessage> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {
        SolrQuery solrQuery = buildSolrQuery(parameters);
        boolean sortManually = setSortAndOrder(parameters, solrQuery);
        Set<SolrMailField> solrFields = checkAndConvert(fields);
        setFieldList(solrQuery, solrFields);
        List<IndexDocument<MailMessage>> results = queryChunkWise(new SolrMailDocumentConverter(), solrQuery, parameters.getOff(), parameters.getLen(), 100);
        if (results.isEmpty()) {
            return Indexes.emptyResult();
        }
        
        if (sortManually) {
            Collections.sort(results, new AddressComparator(parameters.getSortField(), parameters.getOrder()));
        }
        return new SolrIndexResult<MailMessage>(results.size(), results, null);
    }
    
    @Override
    public IndexResult<MailMessage> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }
    
    private boolean setSortAndOrder(QueryParameters parameters, final SolrQuery query) {
        final IndexField indexField = parameters.getSortField();
        String sortField = null;
        boolean retval = false;
        if (indexField != null && indexField instanceof MailIndexField) {
            final SolrMailField solrField = SolrMailField.solrMailFieldFor((MailIndexField) indexField);
            if (solrField.equals(SolrMailField.FROM) || solrField.equals(SolrMailField.TO) || solrField.equals(SolrMailField.CC) || solrField.equals(SolrMailField.BCC)) {
                retval = true;
            } else {
                sortField = solrField.solrName();
            }
        }
        
        final Order orderParam = parameters.getOrder();
        if (sortField != null) {
            final ORDER order = orderParam == null ? ORDER.desc : orderParam.equals(Order.DESC) ? ORDER.desc : ORDER.asc;
            query.setSortField(sortField, order);
        }
        
        return retval;
    }

    private Set<SolrMailField> checkAndConvert(Set<? extends IndexField> fields) {
        Set<SolrMailField> set;
        if (fields == null) {
            set = EnumSet.allOf(SolrMailField.class);
        } else {
            set = EnumSet.noneOf(SolrMailField.class);
            for (IndexField indexField : fields) {
                if (indexField instanceof MailIndexField) {
                    set.add(SolrMailField.solrMailFieldFor((MailIndexField) indexField));
                }
            }
        }
        
        return set;
    }

    private SolrQuery buildSolrQuery(QueryParameters parameters) {
        SearchHandler searchHandler = parameters.getHandler();
        if (searchHandler == null) {
            throw new IllegalArgumentException("Parameter `search handler` must not be null!");
        }
        
        ConfigurationService config = Services.getService(ConfigurationService.class);
        SolrQuery solrQuery;
        switch (searchHandler) { 
            case SIMPLE:
            {
                String pattern = parameters.getPattern();
                if (pattern == null) {
                    throw new IllegalArgumentException("Parameter `pattern` must not be null!");
                }
                
                solrQuery = new SolrQuery(pattern);
                solrQuery.setQueryType(config.getProperty(SolrProperties.SIMPLE_HANLDER));
                addFilterQueries(parameters, solrQuery);
                break;
            }
            
            case ALL_REQUEST:
            {
                if (parameters.getFolders() == null) {
                    throw new IllegalArgumentException("Parameter `folders` must not be null!");
                }
                
                solrQuery = new SolrQuery("*:*");
                solrQuery.setQueryType(config.getProperty(SolrProperties.ALL_HANLDER));                
                addFilterQueries(parameters, solrQuery);             
                break;
            }
            
            case GET_REQUEST:
            {
                String[] ids = getStringArrayParameter(parameters, IndexConstants.IDS);
                if (ids == null) {
                    throw new IllegalArgumentException("Parameter `ids` must not be null!");
                }
                
                solrQuery = new SolrQuery(stringArrayToQuery(ids));
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
        
        return solrQuery;
    }
    
    private void addFilterQueries(QueryParameters parameters, SolrQuery solrQuery) {
        String account = getStringParameter(parameters, IndexConstants.ACCOUNT);
        addFilterQueryIfNotNull(solrQuery, buildQueryString(SolrMailField.ACCOUNT.solrName(), account));
        addFilterQueryIfNotNull(solrQuery, buildQueryStringWithOr(SolrMailField.FULL_NAME.solrName(), parameters.getFolders()));        
    }
    
    private SolrInputDocument convertToDocument(IndexDocument<MailMessage> document) throws OXException {
        return SolrMailDocumentConverter.convertStatic(contextId, userId, document);
    }

}
