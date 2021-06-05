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

package com.openexchange.mail.json.compose;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ComposeContext} - A compose context; storing necessary state information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface ComposeContext {

    /**
     * Sets the source compose message providing basic information.
     *
     * @param sourceMessage The compose message to set
     */
    void setSourceMessage(ComposedMailMessage sourceMessage);

    /**
     * Gets the source compose message providing basic information.
     *
     * @return The compose message
     */
    ComposedMailMessage getSourceMessage();

    /**
     * Gets the transport provider
     *
     * @return The transport provider
     */
    TransportProvider getProvider();

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    int getAccountId();

    /**
     * Gets the session
     *
     * @return The session
     */
    ServerSession getSession();

    /**
     * Checks if associated text part is explicitly supposed to be sent as plain text
     *
     * @return <code>true</code> for plain text; otherwise <code>false</code>
     */
    boolean isPlainText();

    /**
     * Gets the text part
     *
     * @return The text part
     */
    TextBodyMailPart getTextPart();

    /**
     * Sets the text part
     *
     * @param textPart The text part to set
     */
    void setTextPart(TextBodyMailPart textPart);

    /**
     * Gets a listing of all parts in this context.
     *
     * @return All parts
     */
    List<MailPart> getAllParts();

    /**
     * Checks if this context has any part
     *
     * @return <code>true</code> if there is any part; otherwise <code>false</code>
     */
    boolean hasAnyPart();

    /**
     * Adds specified referenced part
     *
     * @param referencedPart The referenced part
     * @throws OXException If part cannot be added
     */
    void addReferencedPart(ReferencedMailPart referencedPart) throws OXException;

    /**
     * Gets the referenced parts
     *
     * @return The referenced parts or an empty list
     */
    List<ReferencedMailPart> getReferencedParts();

    /**
     * Adds specified data part
     *
     * @param dataPart The data part
     * @throws OXException If part cannot be added
     */
    void addDataPart(DataMailPart dataPart) throws OXException;

    /**
     * Gets the data parts
     *
     * @return The data parts or an empty list
     */
    List<DataMailPart> getDataParts();

    /**
     * Adds specified upload part
     *
     * @param uploadPart The upload part
     * @throws OXException If part cannot be added
     */
    void addUploadPart(MailPart uploadPart) throws OXException;

    /**
     * Gets the upload parts
     *
     * @return The upload parts or an empty list
     */
    List<MailPart> getUploadParts();

    /**
     * Adds specified drive part
     *
     * @param drivePart The drive part
     * @throws OXException If part cannot be added
     */
    void addDrivePart(InfostoreDocumentMailPart drivePart) throws OXException;

    /**
     * Gets the drive parts
     *
     * @return The drive parts or an empty list
     */
    List<InfostoreDocumentMailPart> getDriveParts();

    /**
     * Gets the connected mail access associated with this context.
     *
     * @param accountId The identifier of the target account for which to return a connected mail access
     * @return The connected mail access
     * @throws OXException If mail access cannot be returned
     */
    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getConnectedMailAccess(int accountId) throws OXException;

    /**
     * Reconnects the mail access associated with specified account identifier.
     *
     * @param accountId The identifier of the target account for which to return a connected mail access
     * @return The (reconnected) mail access
     * @throws OXException If mail access cannot be returned
     */
    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> reconnectMailAccess(int accountId) throws OXException;

    /**
     * Disposes this context.
     */
    void dispose();

}
