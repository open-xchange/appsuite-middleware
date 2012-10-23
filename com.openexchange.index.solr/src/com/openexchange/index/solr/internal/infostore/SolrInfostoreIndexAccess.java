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

package com.openexchange.index.solr.internal.infostore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.index.InfostoreIndexField;
import com.openexchange.groupware.infostore.index.InfostoreUUID;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.attachments.SolrAttachmentField;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;

/**
 * {@link SolrInfostoreIndexAccess}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrInfostoreIndexAccess extends AbstractSolrIndexAccess<DocumentMetadata> {

    /**
     * Initializes a new {@link SolrInfostoreIndexAccess}.
     * 
     * @param identifier
     */
    public SolrInfostoreIndexAccess(SolrCoreIdentifier identifier) {
        super(identifier);
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        return super.isIndexed(Types.INFOSTORE, accountId, folderId);
    }

    @Override
    public Set<InfostoreIndexField> getIndexedFields() {
        return SolrInfostoreField.getIndexedFields();
    }

    @Override
    public void addEnvelopeData(IndexDocument<DocumentMetadata> document) throws OXException {
        addDocument(convertToDocument(document));        
    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<DocumentMetadata>> documents) throws OXException {
        if (documents.isEmpty()) {
            return;
        }
        
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<DocumentMetadata> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }
        
        addDocuments(inputDocuments);  
    }

    @Override
    public void addContent(IndexDocument<DocumentMetadata> document, boolean full) throws OXException {
        addDocument(convertToDocument(document));  
    }

    @Override
    public void addContent(Collection<IndexDocument<DocumentMetadata>> documents, boolean full) throws OXException {
        if (documents.isEmpty()) {
            return;
        }
        
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<DocumentMetadata> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }
        
        addDocuments(inputDocuments);  
    }

    @Override
    public void addAttachments(IndexDocument<DocumentMetadata> document, boolean full) throws OXException {
        addDocument(convertToDocument(document));  
    }

    @Override
    public void addAttachments(Collection<IndexDocument<DocumentMetadata>> documents, boolean full) throws OXException {
        if (documents.isEmpty()) {
            return;
        }
        
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<DocumentMetadata> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }
        
        addDocuments(inputDocuments);
    }
    
    private SolrInputDocument convertToDocument(IndexDocument<DocumentMetadata> document) throws OXException {
        return SolrInfostoreDocumentConverter.convertStatic(contextId, userId, document);
    }

    @Override
    public void deleteById(String id) throws OXException {
        deleteDocumentById(id);      
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        IndexResult<DocumentMetadata> indexResult = query(parameters, null);
        List<IndexDocument<DocumentMetadata>> documents = indexResult.getResults();
        Set<String> uuids = new HashSet<String>(documents.size());
        for (IndexDocument<DocumentMetadata> document : documents) {
            uuids.add(InfostoreUUID.newUUID(contextId, userId, document).toString());
        }
        
        String deleteQuery = buildQueryStringWithOr(SolrAttachmentField.UUID.solrName(), uuids);
        if (deleteQuery != null) {
            deleteDocumentsByQuery(deleteQuery);
        }
    }
    
    @Override
    public IndexResult<DocumentMetadata> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        // TODO: implement me
        return null;
    }

    @Override
    public IndexResult<DocumentMetadata> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {        
        SolrQuery solrQuery = buildSolrQuery(parameters);
        setSortAndOrder(parameters, solrQuery);
        Set<SolrInfostoreField> solrFields = convertAndCheckFields(parameters, fields);
        setFieldList(solrQuery, solrFields);        
        List<IndexDocument<DocumentMetadata>> results = queryChunkWise(new SolrInfostoreDocumentConverter(), solrQuery, parameters.getOff(), parameters.getLen(), 100);
        if (results.isEmpty()) {
            return Indexes.emptyResult();
        }
        
        return new SolrIndexResult<DocumentMetadata>(results.size(), results, null);
    }
    
    private SolrQuery buildSolrQuery(QueryParameters parameters) throws OXException {
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
            
            default:
                throw new IllegalArgumentException("Search handler " + searchHandler.toString() + " is not valid for this action.");
        }        

        return solrQuery;
    }
    
    private void addFilterQueries(QueryParameters parameters, SolrQuery solrQuery) {
        addFilterQueryIfNotNull(solrQuery, buildQueryStringWithOr(SolrAttachmentField.FOLDER.solrName(), parameters.getFolders()));
    }
    
    private void setSortAndOrder(QueryParameters parameters, SolrQuery solrQuery) {
        setSortAndOrder(parameters, solrQuery, SolrInfostoreField.class);
    }
    
    private Set<SolrInfostoreField> convertAndCheckFields(QueryParameters parameters, Set<? extends IndexField> fields) {
        Set<SolrInfostoreField> set;
        if (fields == null) {
            set = EnumSet.allOf(SolrInfostoreField.class);
        } else {        
            set = EnumSet.noneOf(SolrInfostoreField.class);
            for (IndexField field : fields) {
                if (field instanceof InfostoreIndexField) {
                    set.add(SolrInfostoreField.getByIndexField((InfostoreIndexField) field));
                }
            }
        }
        
        return set;        
    }
}
