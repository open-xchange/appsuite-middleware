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
    public final boolean equals(final Object obj) {
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
    void setDeprecated(final boolean deprecated) {
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
    public final boolean supportsProtocol(final String protocol) {
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
