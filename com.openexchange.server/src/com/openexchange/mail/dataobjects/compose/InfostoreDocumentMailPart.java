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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.dataobjects.compose;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.mail.internet.MimeUtility;
import com.openexchange.exception.OXException;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.MailAttachmentInfo;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.session.Session;

/**
 * {@link InfostoreDocumentMailPart} - A {@link MailPart} implementation that provides the input stream to an infostore document
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class InfostoreDocumentMailPart extends MailPart implements ComposedMailPart {

    /**
     *
     */
    private static final long serialVersionUID = -3158021272821196715L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreDocumentMailPart.class);

    private transient final Session session;
    private transient final String documentId;
    private transient Object cachedContent;

    /**
     * Constructor
     *
     * @param documentId The document's unique ID
     * @param session The session providing needed user data
     * @throws OXException If document cannot be loaded
     */
    public InfostoreDocumentMailPart(final String documentId, final Session session) throws OXException {
        super();
        this.documentId = documentId;
        this.session = session;

        // Get attachment storage
        MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

        // Read document meta data
        MailAttachmentInfo attachment = attachmentStorage.getAttachmentInfo(documentId, session);
        setSize(attachment.getSize());
        setContentType(attachment.getContentType());
        {
            final String fileName = attachment.getName();
            if (!com.openexchange.java.Strings.isEmpty(fileName)) {
                try {
                    setFileName(MimeUtility.encodeText(fileName, MailProperties.getInstance().getDefaultMimeCharset(), "Q"));
                } catch (final UnsupportedEncodingException e) {
                    setFileName(fileName);
                }
            }
        }
    }

    @Override
    public Object getContent() throws OXException {
        if (cachedContent != null) {
            return cachedContent;
        }

        // Get attachment storage
        MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

        Object content = attachmentStorage.getAttachment(documentId, session).getContent();
        cachedContent = content;
        return content;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        // Get attachment storage
        MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

        return attachmentStorage.getAttachment(documentId, session).getDataHandler();
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return null;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        // Get attachment storage
        MailAttachmentStorage attachmentStorage = DefaultMailAttachmentStorageRegistry.getInstance().getMailAttachmentStorage();

        return attachmentStorage.getAttachment(documentId, session).getInputStream();
    }

    @Override
    public void prepareForCaching() {
        // Nope
    }

    @Override
    public void loadContent() {
        // Nope
    }


    @Override
    public ComposedPartType getType() {
        return ComposedMailPart.ComposedPartType.DOCUMENT;
    }

}
