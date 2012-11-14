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

package com.openexchange.index.solr.internal.attachments;

import java.io.File;
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
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentIndexField;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.index.FacetParameters;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.Indexes;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.querybuilder.BuilderException;
import com.openexchange.index.solr.internal.querybuilder.SimpleQueryBuilder;
import com.openexchange.index.solr.internal.querybuilder.SolrQueryBuilder;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;

/**
 * {@link SolrAttachmentIndexAccess}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentIndexAccess extends AbstractSolrIndexAccess<Attachment> {
    
    private final SolrQueryBuilder queryBuilder;
    

    /**
     * Initializes a new {@link SolrAttachmentIndexAccess}.
     * 
     * @param identifier
     */
    public SolrAttachmentIndexAccess(SolrCoreIdentifier identifier) {
        super(identifier);
        try {
            ConfigurationService config = Services.getService(ConfigurationService.class);
            String configDir = config.getProperty(SolrProperties.CONFIG_DIR);
            queryBuilder = new SimpleQueryBuilder(
                configDir + File.separatorChar + "attachment-querybuilder.properties",
                SolrAttachmentField.MODULE,
                SolrAttachmentField.ACCOUNT,
                SolrAttachmentField.FOLDER,
                AttachmentFieldMapper.getInstance());
        } catch (BuilderException e) {
            throw new IllegalStateException("Could not initialize query builder." + e);
        }
    }

    @Override
    public boolean isIndexed(String accountId, String folderId) throws OXException {
        return isIndexed(Types.ATTACHMENT, accountId, folderId);
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        return SolrAttachmentField.getIndexedFields();
    }

    @Override
    public void addEnvelopeData(IndexDocument<Attachment> document) throws OXException {
        checkIfIndexIsLocked();
        addDocument(convertToDocument(document));
    }

    @Override
    public void addEnvelopeData(Collection<IndexDocument<Attachment>> documents) throws OXException {
        checkIfIndexIsLocked();
        if (documents.isEmpty()) {
            return;
        }
        
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<Attachment> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addDocuments(inputDocuments);
    }

    @Override
    public void addContent(IndexDocument<Attachment> document, boolean full) throws OXException {
        checkIfIndexIsLocked();
        addDocument(convertToDocument(document));
    }

    @Override
    public void addContent(Collection<IndexDocument<Attachment>> documents, boolean full) throws OXException {
        checkIfIndexIsLocked();
        if (documents.isEmpty()) {
            return;
        }
        
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<Attachment> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addDocuments(inputDocuments);
    }

    @Override
    public void addAttachments(IndexDocument<Attachment> document, boolean full) throws OXException {
        checkIfIndexIsLocked();
        addDocument(convertToDocument(document));
    }

    @Override
    public void addAttachments(Collection<IndexDocument<Attachment>> documents, boolean full) throws OXException {
        checkIfIndexIsLocked();
        if (documents.isEmpty()) {
            return;
        }
        
        List<SolrInputDocument> inputDocuments = new ArrayList<SolrInputDocument>();
        for (IndexDocument<Attachment> document : documents) {
            inputDocuments.add(convertToDocument(document));
        }

        addDocuments(inputDocuments);
    }

    @Override
    public void deleteById(String id) throws OXException {
        checkIfIndexIsLocked();
        deleteDocumentById(id);
    }

    @Override
    public void deleteByQuery(QueryParameters parameters) throws OXException {
        checkIfIndexIsLocked();
        Set<AttachmentIndexField> fields = EnumSet.noneOf(AttachmentIndexField.class);
        fields.add(AttachmentIndexField.MODULE);
        fields.add(AttachmentIndexField.SERVICE);
        fields.add(AttachmentIndexField.ACCOUNT);
        fields.add(AttachmentIndexField.FOLDER);
        fields.add(AttachmentIndexField.OBJECT_ID);
        fields.add(AttachmentIndexField.ATTACHMENT_ID);
        
        IndexResult<Attachment> indexResult = query(parameters, fields);
        List<IndexDocument<Attachment>> documents = indexResult.getResults();
        Set<String> uuids = new HashSet<String>(documents.size());
        for (IndexDocument<Attachment> document : documents) {
            uuids.add(AttachmentUUID.newUUID(contextId, userId, document.getObject()).toString());
        }

        String deleteQuery = buildQueryStringWithOr(SolrAttachmentField.UUID.solrName(), uuids);
        if (deleteQuery != null) {
            deleteDocumentsByQuery(deleteQuery);
        }
    }

    @Override
    public IndexResult<Attachment> query(QueryParameters parameters, Set<? extends IndexField> fields) throws OXException {
        checkIfIndexIsLocked();
        SolrQuery solrQuery = queryBuilder.buildQuery(parameters);
        Set<SolrAttachmentField> solrFields = checkAndConvert(fields);
        setFieldList(solrQuery, solrFields);
        List<IndexDocument<Attachment>> results = queryChunkWise(new SolrAttachmentDocumentConverter(), solrQuery, parameters.getOff(), parameters.getLen(), 100);
        if (results.isEmpty()) {
            return Indexes.emptyResult();
        }
        
        return new SolrIndexResult<Attachment>(results.size(), results, null);
    }
    
    @Override
    public IndexResult<Attachment> query(QueryParameters parameters, FacetParameters facetParameters, Set<? extends IndexField> fields) throws OXException {
        checkIfIndexIsLocked();
        // TODO Auto-generated method stub
        return null;
    }
    
    private Set<SolrAttachmentField> checkAndConvert(Set<? extends IndexField> fields) {
        Set<SolrAttachmentField> set;
        if (fields == null) {
            set = EnumSet.allOf(SolrAttachmentField.class);
        } else {
            set = EnumSet.noneOf(SolrAttachmentField.class);
            for (IndexField indexField : fields) {
                if (indexField instanceof AttachmentIndexField) {
                    set.add(SolrAttachmentField.solrFieldFor((AttachmentIndexField) indexField));
                }
            }
        }
        
        return set;
    }

    private SolrInputDocument convertToDocument(IndexDocument<Attachment> document) throws OXException {
        return SolrAttachmentDocumentConverter.convertStatic(contextId, userId, document);
    }

}
