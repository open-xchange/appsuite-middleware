/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.dataobjects.compose;

import java.io.InputStream;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.attachment.storage.DefaultMailAttachmentStorageRegistry;
import com.openexchange.mail.attachment.storage.MailAttachmentInfo;
import com.openexchange.mail.attachment.storage.MailAttachmentStorage;
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
    public InfostoreDocumentMailPart(String documentId, Session session) throws OXException {
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
                setFileName(fileName);
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
    public MailPart getEnclosedMailPart(int index) throws OXException {
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
