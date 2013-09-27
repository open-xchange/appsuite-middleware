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
import com.openexchange.groupware.attach.index.AttachmentIndexField;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.converter.AbstractDocumentConverter;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.textxtraction.TextXtractService;

/**
 * {@link SolrAttachmentDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentDocumentConverter extends AbstractDocumentConverter<Attachment> {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(SolrAttachmentDocumentConverter.class);


    /**
     * Initializes a new {@link SolrAttachmentDocumentConverter}.
     */
    public SolrAttachmentDocumentConverter(FieldConfiguration fieldConfig) {
        super(fieldConfig);
    }

    @Override
    public IndexDocument<Attachment> convert(SolrDocument document) throws OXException {
        return convertInternal(document);
    }

    @Override
    public IndexDocument<Attachment> convert(SolrDocument document, Map<String, List<String>> highlightedFields) throws OXException {
        StandardIndexDocument<Attachment> indexDocument = convertInternal(document);
        addHighlighting(indexDocument, highlightedFields);

        return indexDocument;
    }

    @Override
    public IndexResult<Attachment> createIndexResult(long numFound, List<IndexDocument<Attachment>> documents, Map<IndexField, Map<String, Long>> facetCounts) throws OXException {
        return new SolrIndexResult<Attachment>(numFound, documents, facetCounts);
    }

    private StandardIndexDocument<Attachment> convertInternal(SolrDocument document) throws OXException {
        Attachment attachment = new Attachment();
        Integer module = getFieldValue(AttachmentIndexField.MODULE, document);
        if (module != null) {
            attachment.setModule(module.intValue());
        }
        String account = getFieldValue(AttachmentIndexField.ACCOUNT, document);
        if (account != null) {
            attachment.setAccount(account);
        }
        String folder = getFieldValue(AttachmentIndexField.FOLDER, document);
        if (folder != null) {
            attachment.setFolder(folder);
        }
        String objectId = getFieldValue(AttachmentIndexField.OBJECT_ID, document);
        if (objectId != null) {
            attachment.setObjectId(objectId);
        }
        String fileName = getFieldValue(AttachmentIndexField.FILE_NAME, document);
        if (fileName != null) {
            attachment.setFileName(fileName);
        }
        Long fileSize = getFieldValue(AttachmentIndexField.FILE_SIZE, document);
        if (fileSize != null) {
            attachment.setFileSize(fileSize.longValue());
        }
        String mimeType = getFieldValue(AttachmentIndexField.MIME_TYPE, document);
        if (mimeType != null) {
            attachment.setMimeType(mimeType);
        }
        String md5Sum = getFieldValue(AttachmentIndexField.MD5_SUM, document);
        if (md5Sum != null) {
            attachment.setMd5Sum(md5Sum);
        }
        String attachmentId = getFieldValue(AttachmentIndexField.ATTACHMENT_ID, document);
        if (attachmentId != null) {
            attachment.setAttachmentId(attachmentId);
        }

        String documentId = String.valueOf(document.getFieldValue(fieldConfig.getUUIDField()));
        return new StandardIndexDocument<Attachment>(documentId, attachment);
    }

    public SolrInputDocument convert(int contextId, int userId, IndexDocument<Attachment> document) throws OXException {
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

        setFieldInDocument(inputDocument, AttachmentIndexField.UUID, uuid);
        setFieldInDocument(inputDocument, AttachmentIndexField.MODULE, new Integer(module));
        setFieldInDocument(inputDocument, AttachmentIndexField.FOLDER, folder);
        setFieldInDocument(inputDocument, AttachmentIndexField.OBJECT_ID, objectId);
        setFieldInDocument(inputDocument, AttachmentIndexField.FILE_SIZE, new Long(fileSize));
        setFieldInDocument(inputDocument, AttachmentIndexField.ATTACHMENT_ID, attachmentId);

        setFieldInDocument(inputDocument, AttachmentIndexField.ACCOUNT, account);
        setFieldInDocument(inputDocument, AttachmentIndexField.FILE_NAME, fileName);
        setFieldInDocument(inputDocument, AttachmentIndexField.MIME_TYPE, mimeType);
        setFieldInDocument(inputDocument, AttachmentIndexField.MD5_SUM, md5Sum);

        if (file != null) {
            TextXtractService xtractService = Services.getService(TextXtractService.class);
            try {
                String extractedText = xtractService.extractFrom(file, mimeType);
                setFieldInDocument(inputDocument, AttachmentIndexField.CONTENT, extractedText);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Error during text extraction. Skipping attachments content.\nCause: " + t.getMessage());
                }
            }
        }

        return inputDocument;
    }

}
