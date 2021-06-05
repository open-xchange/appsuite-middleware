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

package com.openexchange.mail.transport;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.session.Session;

/**
 * {@link TransportProvider} - Provider for mail transport
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class TransportProvider {

    private final int hashCode;

    private boolean deprecated;

    /**
     * Initializes a new {@link TransportProvider}
     */
    protected TransportProvider() {
        super();
        hashCode = getProtocol().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof TransportProvider)) {
            return false;
        }
        final TransportProvider other = (TransportProvider) obj;
        if (getProtocol() == null) {
            if (other.getProtocol() != null) {
                return false;
            }
        } else if (!getProtocol().equals(other.getProtocol())) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    /**
     * Checks if this provider is deprecated; any cached references should be discarded
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Sets the deprecated flag
     *
     * @param deprecated <code>true</code> if deprecated; otherwise <code>false</code>
     */
    void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * Performs provider's start-up
     *
     * @throws OXException If start-up fails
     */
    protected void startUp() throws OXException {
        getProtocolProperties().loadProperties();
    }

    /**
     * Performs provider's shut-down
     *
     * @throws OXException if shut-down fails
     */
    protected void shutDown() throws OXException {
        getProtocolProperties().resetProperties();
    }

    /**
     * Gets this transport provider's protocol
     *
     * @return The protocol
     */
    public abstract Protocol getProtocol();

    /**
     * Checks if this transport provider supports the given protocol (which is either in secure or non-secure notation).
     * <p>
     * This is a convenience method that invokes {@link Protocol#isSupported(String)}
     *
     * @param protocol The protocol
     * @return <code>true</code> if supported; otherwise <code>false</code>
     */
    public final boolean supportsProtocol(String protocol) {
        return getProtocol().isSupported(protocol);
    }

    /**
     * Gets a newly created {@link MailTransport mail transport}
     *
     * @param session The session providing needed user data
     * @return A newly created {@link MailTransport mail transport}
     * @throws OXException If instantiation fails
     */
    public abstract MailTransport createNewMailTransport(Session session) throws OXException;

    /**
     * Gets a newly created {@link MailTransport mail transport}
     *
     * @param session The session providing needed user data
     * @param accountId The account ID
     * @return A newly created {@link MailTransport mail transport}
     * @throws OXException If instantiation fails
     */
    public abstract MailTransport createNewMailTransport(Session session, int accountId) throws OXException;

    /**
     * Gets a newly created {@link MailTransport} instance, that can be used to
     * send mails via the configured no-reply address.
     *
     * @param contextId The context ID
     * @return The transport
     * @throws OXException
     */
    public abstract MailTransport createNewNoReplyTransport(int contextId) throws OXException;

    /**
     * Gets a newly created {@link MailTransport} instance, that can be used to
     * send mails either via the configured no-reply address or using existing
     * <code>"From"</code> header (if any)
     *
     * @param contextId The context ID
     * @param useNoReplyAddress <code>true</code> to use configured no-reply address; otherwise <code>false</code> to keep existing <code>"From"</code> header (if any)
     * @return The transport
     * @throws OXException
     */
    public abstract MailTransport createNewNoReplyTransport(int contextId, boolean useNoReplyAddress) throws OXException;

    /**
     * Gets the protocol properties
     *
     * @return The protocol properties
     */
    protected abstract AbstractProtocolProperties getProtocolProperties();

    /**
     * Gets a new instance of {@link ComposedMailMessage}
     *
     * @param session The session for handling temporary uploaded files which shall be added to composed mail
     * @param ctx The context to load session-related data
     * @return A new instance of {@link ComposedMailMessage}
     * @throws OXException If a new instance of {@link ComposedMailMessage} cannot be created
     */
    public abstract ComposedMailMessage getNewComposedMailMessage(Session session, Context ctx) throws OXException;

    /**
     * Gets a new instance of {@link UploadFileMailPart}
     *
     * @param uploadFile The upload file
     * @return A new instance of {@link UploadFileMailPart}
     * @throws OXException If a new instance of {@link UploadFileMailPart} cannot be created
     */
    public abstract UploadFileMailPart getNewFilePart(UploadFile uploadFile) throws OXException;

    /**
     * Gets a new instance of {@link InfostoreDocumentMailPart}
     *
     * @param documentId The infostore document's unique ID
     * @param session The session providing needed user data
     * @return A new instance of {@link InfostoreDocumentMailPart}
     * @throws OXException If a new instance of {@link InfostoreDocumentMailPart} cannot be created
     */
    public abstract InfostoreDocumentMailPart getNewDocumentPart(String documentId, Session session) throws OXException;

    /**
     * Gets a new instance of {@link DataMailPart}
     *
     * @param data The data obtained by a data source
     * @param dataProperties The data properties
     * @param session The session providing needed user data
     * @return A new instance of {@link DataMailPart}
     * @throws OXException If a new instance of {@link DataMailPart} cannot be created
     */
    public abstract DataMailPart getNewDataPart(Object data, Map<String, String> dataProperties, Session session) throws OXException;

    /**
     * Gets a new instance of {@link TextBodyMailPart}
     *
     * @param textBody The text body
     * @return A new instance of {@link TextBodyMailPart}
     * @throws OXException If a new instance of {@link TextBodyMailPart} cannot be created
     */
    public abstract TextBodyMailPart getNewTextBodyPart(String textBody) throws OXException;

    /**
     * Gets a new instance of {@link ReferencedMailPart}
     *
     * @param referencedPart The referenced part
     * @param session The session providing user data
     * @return A new instance of {@link ReferencedMailPart}
     * @throws OXException If a new instance of {@link ReferencedMailPart} cannot be created
     */
    public abstract ReferencedMailPart getNewReferencedPart(MailPart referencedPart, Session session) throws OXException;

    /**
     * Gets a new instance of {@link ReferencedMailPart}
     *
     * @param referencedMail The referenced mail
     * @param session The session providing user data
     * @return A new instance of {@link ReferencedMailPart}
     * @throws OXException If a new instance of {@link ReferencedMailPart} cannot be created
     */
    public abstract ReferencedMailPart getNewReferencedMail(MailMessage referencedMail, Session session) throws OXException;
}
