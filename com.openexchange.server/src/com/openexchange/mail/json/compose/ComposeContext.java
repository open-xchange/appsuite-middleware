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
 *    trademarks of the OX Software GmbH. group of companies.
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
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
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
    void addUploadPart(UploadFileMailPart uploadPart) throws OXException;

    /**
     * Gets the upload parts
     *
     * @return The upload parts or an empty list
     */
    List<UploadFileMailPart> getUploadParts();

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
