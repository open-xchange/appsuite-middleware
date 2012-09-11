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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.index.InfostoreIndexField;
import com.openexchange.groupware.infostore.index.InfostoreUUID;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.SolrResultConverter;


/**
 * {@link SolrInfostoreDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrInfostoreDocumentConverter implements SolrResultConverter<DocumentMetadata> {
    
    public static SolrInputDocument convertStatic(int contextId, int userId, IndexDocument<DocumentMetadata> indexDocument) throws OXException {
        DocumentMetadata file = indexDocument.getObject();
        SolrInputDocument document = new SolrInputDocument();
        GetSwitch getter = new GetSwitch(file);
        for (SolrInfostoreField field : SolrInfostoreField.values()) {
            InfostoreIndexField indexField = field.indexField();
            Metadata metadataField = indexField.getMetadataField();
            if (metadataField != null) {
                Object value = metadataField.doSwitch(getter);
                if (value != null) {
                    if (metadataField.equals(Metadata.CREATION_DATE_LITERAL) || metadataField.equals(Metadata.LAST_MODIFIED_LITERAL)) {
                        document.setField(field.solrName(), ((Date) value).getTime());
                    } else {
                        document.setField(field.solrName(), value);
                    }
                }
            }
        }

        document.setField(SolrInfostoreField.UUID.solrName(), InfostoreUUID.newUUID(contextId, userId, indexDocument));        
        return document;
    }

    @Override
    public IndexDocument<DocumentMetadata> convert(SolrDocument document) throws OXException {
        DocumentMetadata converted = convertStatic(document);
        IndexDocument<DocumentMetadata> indexDocument = new StandardIndexDocument<DocumentMetadata>(converted);
        
        return indexDocument;
    }
    
    public static DocumentMetadata convertStatic(SolrDocument document) {
        DocumentMetadata file = new SolrDocumentMetadata();
        SetSwitch setter = new SetSwitch(file);
        for (Entry<String, Object> field : document) {
            String name = field.getKey();
            Object value = field.getValue();
            SolrInfostoreField solrField = SolrInfostoreField.getBySolrName(name);
            if (solrField != null && value != null) {
                InfostoreIndexField indexField = solrField.indexField();
                Metadata metadataField = indexField.getMetadataField();
                if (metadataField != null) {
                    setter.setValue(value);
                    metadataField.doSwitch(setter);
                    setter.setValue(null);
                }
            }
        }
        
        return file;
    }

    @Override
    public IndexResult<DocumentMetadata> createIndexResult(List<IndexDocument<DocumentMetadata>> documents, Map<IndexField, Map<String, Long>> facetCounts) throws OXException {
        return new SolrIndexResult<DocumentMetadata>(documents.size(), documents, facetCounts);
    }

}
