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

package com.openexchange.mail.attachment.storage;

import java.io.InputStream;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.session.Session;

/**
 * {@link MailAttachmentStorage} - Storage for mail attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public interface MailAttachmentStorage {

    /**
     * Performs required preparations in order to store attachments
     *
     * @param folderName The name of the folder holding the stored attachment (only applicable if storage supports folders visible to user)
     * @param checkForExpiredAttachments <code>true</code> to check for expired mail attachments; otherwise <code>false</code>
     * @param timeToLive The time-to-live for stored attachments
     * @param session The associated session
     * @throws OXException If preparations fail
     */
    void prepareStorage(String folderName, boolean checkForExpiredAttachments, long timeToLive, Session session) throws OXException;

    /**
     * Stores specified mail attachment into storage
     *
     * @param attachment The attachment to store
     * @param op The store operation
     * @param storeProps Additional properties for the store operation
     * @param session The associated session
     * @return The identifier of the stored attachment
     * @throws OXException If store operation fails
     */
    String storeAttachment(MailPart attachment, StoreOperation op, Map<String, Object> storeProps, Session session) throws OXException;

    /**
     * Gets the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The mail attachment
     * @throws OXException If mail attachment cannot be returned
     */
    MailPart getAttachment(String id, Session session) throws OXException;

    /**
     * Gets the information for the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The mail attachment information
     * @throws OXException If mail attachment cannot be returned
     */
    MailAttachmentInfo getAttachmentInfo(String id, Session session) throws OXException;

    /**
     * Gets the input stream of the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The mail attachment stream
     * @throws OXException If mail attachment cannot be returned
     */
    InputStream getAttachmentStream(String id, Session session) throws OXException;

    /**
     * Removes the mail attachment denoted by given identifier from storage
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @throws OXException If mail attachment cannot be removed
     */
    void removeAttachment(String id, Session session) throws OXException;

    /**
     * Gets the download URI for the mail attachment denoted by given identifier
     *
     * @param id The identifier of the attachment held in storage
     * @param session The associated session
     * @return The download URI
     * @throws OXException If download URI cannot be returned
     */
    DownloadUri getDownloadUri(String id, Session session) throws OXException;

    /**
     * Discards the the mail attachment denoted by given identifier and associated URI's resource as well
     *
     * @param id The identifier of the attachment held in storage
     * @param uriInformation The optional URI information
     * @param session The associated session
     * @throws OXException If discard operation fails
     */
    void discard(String id, DownloadUri uriInformation, Session session) throws OXException;

}
