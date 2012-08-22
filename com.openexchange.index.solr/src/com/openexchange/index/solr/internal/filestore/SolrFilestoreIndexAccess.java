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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileFieldSwitcher;
import com.openexchange.file.storage.meta.FileFieldGet;
import com.openexchange.file.storage.meta.FileFieldSet;
import com.openexchange.groupware.Types;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.filestore.FileUUID;
import com.openexchange.index.filestore.FilestoreIndexField;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.attachments.SolrAttachmentField;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;

/**
 * {@link SolrFilestoreIndexAccess}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrFilestoreIndexAccess extends AbstractSolrIndexAccess<File> {

    /**
     * Initializes a new {@link SolrFilestoreIndexAccess}.
     * 
     * @param identifier
     */
    public SolrFilestoreIndexAccess(SolrCoreIdentifier identifier) {
        super(identifier);
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        return super.isIndexed(Types.INFOSTORE, accountId, folderId);
    }

    @Override
    public Set<FilestoreIndexField> getIndexedFields() {
        return SolrFilestoreField.getIndexedFields();
    }

    @Override
    public void addEnvelopeData(IndexDocument<File> document) throws OXException {
        addDocument(convertToDocument(document));        
    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<File>> documents) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<File> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }
        
        addDocuments(inputDocuments);  
    }

    @Override
    public void addContent(IndexDocument<File> document, boolean full) throws OXException {
        addDocument(convertToDocument(document));  
    }

    @Override
    public void addContent(Collection<IndexDocument<File>> documents, boolean full) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<File> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }
        
        addDocuments(inputDocuments);  
    }

    @Override
    public void addAttachments(IndexDocument<File> document, boolean full) throws OXException {
        addDocument(convertToDocument(document));  
    }

    @Override
    public void addAttachments(Collection<IndexDocument<File>> documents, boolean full) throws OXException {
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<File> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }
        
        addDocuments(inputDocuments);
    }
    
    private SolrInputDocument convertToDocument(IndexDocument<File> document) throws OXException {
        return SolrFilestoreDocumentConverter.convertStatic(contextId, userId, document);
    }

    @Override
    public void change(IndexDocument<File> document, Set<? extends IndexField> fields) throws OXException {
        Set<FilestoreIndexField> indexFields;
        if (fields == null) {
            indexFields = EnumSet.allOf(FilestoreIndexField.class);
        } else {
            indexFields = EnumSet.noneOf(FilestoreIndexField.class);
            for (IndexField field : fields) {
                if (field instanceof FilestoreIndexField) {
                    indexFields.add((FilestoreIndexField) field);
                }
            }
        }
        
        File reloaded = loadFromIndex(FileUUID.newUUID(document).toString());
        if (reloaded == null) {
            addAttachments(document, true);
        } else {
            FileFieldSwitcher setter = new FileFieldSet(); 
            FileFieldSwitcher getter = new FileFieldGet();
            for (FilestoreIndexField field : indexFields) {
                Field fileField = field.getFileField();
                if (fileField != null) {
                    Object value = fileField.doSwitch(getter, document.getObject());
                    fileField.doSwitch(setter, reloaded, value);
                }
            }
            
            StandardIndexDocument<File> changed = new StandardIndexDocument<File>(reloaded);
            changed.setProperties(document.getProperties());
            addAttachments(changed, true);
        }
    }

    @Override
    public void change(Collection<IndexDocument<File>> documents, Set<? extends IndexField> fields) throws OXException {
        for (IndexDocument<File> document : documents) {
            change(document, fields);
        }        
    }    

    @Override
    public void deleteById(String id) throws OXException {
        deleteDocumentById(id);      
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        IndexResult<File> indexResult = query(parameters, null);
        List<IndexDocument<File>> documents = indexResult.getResults();
        Set<String> uuids = new HashSet<String>(documents.size());
        for (IndexDocument<File> document : documents) {
            uuids.add(FileUUID.newUUID(document).toString());
        }
        
        String deleteQuery = buildQueryStringWithOr(SolrAttachmentField.UUID.solrName(), uuids);
        deleteDocumentsByQuery(deleteQuery);   
    }
    
    @Override
    public IndexResult<File> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        // TODO: implement me
        return null;
    }

    @Override
    public IndexResult<File> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {        
        SolrQuery solrQuery = buildSolrQuery(parameters);
        setSortAndOrder(parameters, solrQuery);
        Set<SolrFilestoreField> solrFields = convertAndCheckFields(parameters, fields);
        setFieldList(solrQuery, solrFields);        
        List<IndexDocument<File>> results = queryChunkWise(new SolrFilestoreDocumentConverter(), solrQuery, parameters.getOff(), parameters.getLen(), 100);
        if (results.isEmpty()) {
            return Indexes.emptyResult();
        }
        
        return new SolrIndexResult<File>(results.size(), results, null);
    }
    
    private File loadFromIndex(String id) throws OXException {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put(IndexConstants.IDS, new String[] { id });
        QueryParameters query = new QueryParameters.Builder(params).setHandler(SearchHandler.GET_REQUEST).build();
        IndexResult<File> result = query(query, null);
        if (result.getNumFound() == 1) {
            return result.getResults().get(0).getObject();
        }
        
        return null;
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
            
            default:
                throw new IllegalArgumentException("Search handler " + searchHandler.toString() + " is not valid for this action.");
        }        

        return solrQuery;
    }
    
    private void addFilterQueries(QueryParameters parameters, SolrQuery solrQuery) {
        String service = getStringParameter(parameters, IndexConstants.SERVICE);
        String accountId = getStringParameter(parameters, IndexConstants.ACCOUNT);
        addFilterQueryIfNotNull(solrQuery, buildQueryString(SolrAttachmentField.SERVICE.solrName(), service));
        addFilterQueryIfNotNull(solrQuery, buildQueryString(SolrAttachmentField.ACCOUNT.solrName(), accountId));
        addFilterQueryIfNotNull(solrQuery, buildQueryStringWithOr(SolrAttachmentField.FOLDER.solrName(), parameters.getFolders()));
    }
    
    private void setSortAndOrder(QueryParameters parameters, SolrQuery solrQuery) {
        setSortAndOrder(parameters, solrQuery, SolrFilestoreField.class);
    }
    
    private Set<SolrFilestoreField> convertAndCheckFields(QueryParameters parameters, Set<? extends IndexField> fields) {
        Set<SolrFilestoreField> set;
        if (fields == null) {
            set = EnumSet.allOf(SolrFilestoreField.class);
        } else {        
            set = EnumSet.noneOf(SolrFilestoreField.class);
            for (IndexField field : fields) {
                if (field instanceof FilestoreIndexField) {
                    set.add(SolrFilestoreField.getByIndexField((FilestoreIndexField) field));
                }
            }
        }
        
        return set;        
    }
}
