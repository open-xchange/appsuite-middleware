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

package com.openexchange.chronos.storage;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attachment;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AttachmentStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface AttachmentStorage {

    /**
     * Inserts new attachments for a specific event.
     * <p/>
     * Binary attachments (where {@link Attachment#getData()} is not <code>null</code>) are uploaded and stored, while existing
     * <i>managed</i> attachments (where {@link Attachment#getManagedId()} is not <code>0</code>) attached to other groupware objects are
     * copied over.
     * <p/>
     * Permissions may be re-checked under the hood, therefore a valid session and parent folder needs to passed.
     *
     * @param session The session of the acting user
     * @param folderID The identifier of the parent folder where the event appears for the acting user
     * @param eventID The identifier of the event to add the attachments for
     * @param attachments The attachments to add
     */
    void insertAttachments(Session session, String folderID, String eventID, List<Attachment> attachments) throws OXException;

    /**
     * Loads metadata for all attachments of specific events.
     *
     * @param eventIDs The identifiers of the event to get the attachment metadata for
     * @return The metadata for all attachments of each event, mapped to the corresponding event's identifier
     */
    Map<String, List<Attachment>> loadAttachments(String[] eventIDs) throws OXException;

    /**
     * Loads information about which events have at least one attachment in the storage.
     *
     * @param eventIds The identifiers of the event to get the attachment information for
     * @return A map that associates the identifiers of those events where at least one attachment stored to {@link Boolean#TRUE}
     */
    Map<String, Boolean> hasAttachments(String[] eventIds) throws OXException;

    /**
     * Loads metadata for all attachments of a specific event.
     *
     * @param eventID The identifier of the event to get the attachment metadata for
     * @return The metadata for all attachments of the event, or <code>null</code> if there are none
     */
    List<Attachment> loadAttachments(String eventID) throws OXException;

    /**
     * Deletes all attachments of a specific event.
     * <p/>
     * Permissions may be re-checked under the hood, therefore a valid session and parent folder needs to passed.
     *
     * @param session The session of the acting user
     * @param folderID The identifier of the parent folder where the event appears for the acting user
     * @param eventID The identifier of the event to delete the attachments for
     */
    void deleteAttachments(Session session, String folderID, String eventID) throws OXException;

    /**
     * Deletes certain attachments of a specific event.
     * <p/>
     * Permissions may be re-checked under the hood, therefore a valid session and parent folder needs to passed.
     *
     * @param session The session of the acting user
     * @param folderID The identifier of the parent folder where the event appears for the acting user
     * @param attachments The attachments to delete
     * @param eventID The identifier of the event to delete the attachments for
     */
    void deleteAttachments(Session session, String folderID, String eventID, List<Attachment> attachments) throws OXException;

    /**
     * Deletes certain attachments of a multiple events.
     * <p/>
     * Permissions may be re-checked under the hood, therefore a valid session and parent folder needs to passed.
     *
     * @param session The session of the acting user
     * @param attachmentsByEventPerFolderId The attachments to delete for each event, mapped to the folder identifiers where an event
     *            appears for the acting user
     */
    void deleteAttachments(Session session, Map<String, Map<String, List<Attachment>>> attachmentsByEventPerFolderId) throws OXException;

    /**
     * Loads the actual data of the {@link Attachment} with the specified managed identifier
     *
     * @param managedId The managed identifier of the {@link Attachment}
     * @return The actual data of the {@link Attachment} as an {@link InputStream}
     * @throws OXException if an error is occurred
     */
    InputStream loadAttachmentData(int managedId) throws OXException;

}
