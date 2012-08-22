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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileFieldSwitcher;
import com.openexchange.file.storage.meta.FileFieldGet;
import com.openexchange.file.storage.meta.FileFieldSet;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.filestore.FileUUID;
import com.openexchange.index.filestore.FilestoreIndexField;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.SolrResultConverter;


/**
 * {@link SolrFilestoreDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrFilestoreDocumentConverter implements SolrResultConverter<File> {
    
    public static SolrInputDocument convertStatic(int contextId, int userId, IndexDocument<File> indexDocument) throws OXException {
        File file = indexDocument.getObject();
        SolrInputDocument document = new SolrInputDocument();
        FileFieldSwitcher getter = new FileFieldGet();
        for (SolrFilestoreField field : SolrFilestoreField.values()) {
            FilestoreIndexField indexField = field.indexField();
            Field fileField = indexField.getFileField();
            if (fileField != null && fileField != Field.CONTENT) {
                Object value = fileField.doSwitch(getter, file);
                if (value != null) {
                    if (fileField == Field.CREATED || fileField == Field.LAST_MODIFIED) {
                        document.setField(field.solrName(), ((Date) value).getTime());
                    } else {
                        document.setField(field.solrName(), value);
                    }
                }
            }
        }
        
        // Special fields: uuid, account, content
        Map<String, Object> properties = indexDocument.getProperties();
        String service = (String) properties.get(IndexConstants.SERVICE);
        String accountId = (String) properties.get(IndexConstants.ACCOUNT);        
        document.setField(SolrFilestoreField.UUID.solrName(), FileUUID.newUUID(indexDocument));
        document.setField(SolrFilestoreField.SERVICE.solrName(), service);
        document.setField(SolrFilestoreField.ACCOUNT.solrName(), accountId);
        
        return document;
    }

    @Override
    public IndexDocument<File> convert(SolrDocument document) throws OXException {
        File converted = convertStatic(document);
        IndexDocument<File> indexDocument = new StandardIndexDocument<File>(converted);
        
        return indexDocument;
    }
    
    public static File convertStatic(SolrDocument document) {
        File file = new DefaultFile();
        FileFieldSwitcher setter = new FileFieldSet();
        for (Entry<String, Object> field : document) {
            String name = field.getKey();
            Object value = field.getValue();
            SolrFilestoreField solrField = SolrFilestoreField.getBySolrName(name);
            if (solrField != null && value != null) {
                FilestoreIndexField indexField = solrField.indexField();
                Field fileField = indexField.getFileField();
                if (fileField != null) {
                    fileField.doSwitch(setter, file, value);
                }
            }
        }
        
        return file;
    }

    @Override
    public IndexResult<File> createIndexResult(List<IndexDocument<File>> documents, Map<IndexField, Map<String, Long>> facetCounts) throws OXException {
        return new SolrIndexResult<File>(documents.size(), documents, facetCounts);
    }

}
