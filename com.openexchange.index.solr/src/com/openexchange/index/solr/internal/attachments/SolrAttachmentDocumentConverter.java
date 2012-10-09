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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.SolrResultConverter;
import com.openexchange.textxtraction.TextXtractService;


/**
 * {@link SolrAttachmentDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentDocumentConverter implements SolrResultConverter<Attachment> {    
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(SolrAttachmentDocumentConverter.class);

    @Override
    public IndexDocument<Attachment> convert(SolrDocument document) throws OXException {
        return convertStatic(document);
    }

    @Override
    public IndexResult<Attachment> createIndexResult(List<IndexDocument<Attachment>> documents, Map<IndexField, Map<String, Long>> facetCounts) throws OXException {
        return new SolrIndexResult<Attachment>(documents.size(), documents, null);
    }
    
    public static IndexDocument<Attachment> convertStatic(SolrDocument document) {
        Attachment attachment = new Attachment();
        if (document.containsKey(SolrAttachmentField.MODULE.solrName())) {
            attachment.setModule(((Integer) document.get(SolrAttachmentField.MODULE.solrName())).intValue());
        }
        if (document.containsKey(SolrAttachmentField.ACCOUNT.solrName())) {
            attachment.setAccount((String) document.get(SolrAttachmentField.ACCOUNT.solrName()));
        }
        if (document.containsKey(SolrAttachmentField.FOLDER.solrName())) {
            attachment.setFolder((String) document.get(SolrAttachmentField.FOLDER.solrName()));
        }
        if (document.containsKey(SolrAttachmentField.OBJECT_ID.solrName())) {
            attachment.setObjectId((String) document.get(SolrAttachmentField.OBJECT_ID.solrName()));
        }
        if (document.containsKey(SolrAttachmentField.FILE_NAME.solrName())) {
            attachment.setFileName((String) document.get(SolrAttachmentField.FILE_NAME.solrName()));
        }
        if (document.containsKey(SolrAttachmentField.FILE_SIZE.solrName())) {
            attachment.setFileSize(((Long) document.get(SolrAttachmentField.FILE_SIZE.solrName())).longValue());
        }
        if (document.containsKey(SolrAttachmentField.MIME_TYPE.solrName())) {
            attachment.setMimeType((String) document.get(SolrAttachmentField.MIME_TYPE.solrName()));
        }
        if (document.containsKey(SolrAttachmentField.MD5_SUM.solrName())) {
            attachment.setMd5Sum((String) document.get(SolrAttachmentField.MD5_SUM.solrName()));
        }
        if (document.containsKey(SolrAttachmentField.ATTACHMENT_ID.solrName())) {
            attachment.setAttachmentId((String) document.get(SolrAttachmentField.ATTACHMENT_ID.solrName()));
        }
        
        return new StandardIndexDocument<Attachment>(attachment);
    }
    
    public static SolrInputDocument convertStatic(int contextId, int userId, IndexDocument<Attachment> document) throws OXException {
        Attachment attachment = document.getObject();
        SolrInputDocument inputDocument = new SolrInputDocument();        
        int module = attachment.getModule();
        String account = attachment.getAccount();
        String folder = attachment.getFolder();
        String objectId = attachment.getObjectId();
        String attachmentId = attachment.getAttachmentId();
        String fileName = attachment.getFileName();
        long fileSize = attachment.getFileSize();
        String mimeType = attachment.getMimeType();
        String md5Sum = attachment.getMd5Sum();
        InputStream file = attachment.getContent();
        String uuid = AttachmentUUID.newUUID(contextId, userId, attachment).toString();   
        if (folder == null) {
            throw new IllegalArgumentException("Folder id must not be null!");
        }
        if (objectId == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }
        
        inputDocument.setField(SolrAttachmentField.UUID.solrName(), uuid);
        inputDocument.setField(SolrAttachmentField.MODULE.solrName(), new Integer(module));
        inputDocument.setField(SolrAttachmentField.FOLDER.solrName(), folder);
        inputDocument.setField(SolrAttachmentField.OBJECT_ID.solrName(), objectId);
        inputDocument.setField(SolrAttachmentField.FILE_SIZE.solrName(), new Long(fileSize));    
        inputDocument.setField(SolrAttachmentField.ATTACHMENT_ID.solrName(), attachmentId);    
        
        setFieldIfNotNull(inputDocument, SolrAttachmentField.ACCOUNT.solrName(), account);
        setFieldIfNotNull(inputDocument, SolrAttachmentField.FILE_NAME.solrName(), fileName);
        setFieldIfNotNull(inputDocument, SolrAttachmentField.MIME_TYPE.solrName(), mimeType);
        setFieldIfNotNull(inputDocument, SolrAttachmentField.MD5_SUM.solrName(), md5Sum);
        TextXtractService xtractService = Services.getService(TextXtractService.class);
        try {
            String extractedText = xtractService.extractFrom(file, mimeType);
            inputDocument.setField(SolrAttachmentField.CONTENT.solrName(), extractedText);
        } catch (Exception e) {
            LOG.warn("Exception during text extraction. Skipping attachments content...", e);
        }
        
        return inputDocument;        
    }
    
    private static void setFieldIfNotNull(SolrInputDocument inputDocument, String solrName, String value) {
        if (value != null) {
            inputDocument.setField(solrName, value);
        }
    }
}
