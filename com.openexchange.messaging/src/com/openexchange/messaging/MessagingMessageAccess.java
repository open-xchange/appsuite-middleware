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

package com.openexchange.messaging;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link MessagingMessageAccess} - Provides access to message storage.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingMessageAccess {

    /**
     * Gets the message associated with specified identifier.
     *
     * @param folder The folder identifier
     * @param id The identifier
     * @param peek <code>true</code> to peek message content (meaning any mechanisms to mark content as read disabled); otherwise
     *            <code>false</code>
     * @return The message associated with specified identifier
     * @throws OXException If message cannot be returned
     */
    public MessagingMessage getMessage(String folder, String id, boolean peek) throws OXException;

    /**
     * Gets the messages located in given folder whose identifier matches specified identifier.
     * <p>
     * The returned instances of {@link MailMessage} are pre-filled with specified fields through argument <code>fields</code>.
     * <p>
     * If any mail ID is invalid, <code>null</code> is returned for that entry.
     *
     * @param folder The folder identifier
     * @param messageIds The message identifiers
     * @param fields The fields to pre-fill in returned instances of {@link MessagingMessage}
     * @return The corresponding messages
     * @throws OXException If message could not be returned
     */
    public List<MessagingMessage> getMessages(String folder, String[] messageIds, MessagingField[] fields) throws OXException;

    /**
     * Searches for messages located in given folder. If the search yields no results, an empty {@link List} is returned. This method's
     * purpose is to return filtered messages' information for a <b>fast</b> list view. Therefore this method's <code>fields</code>
     * parameter should only contain instances of {@link MessagingField} which are marked as <b>[low cost]</b>. Otherwise pre-filling of
     * returned messages may take a long time and does no more fit to generate a fast list view.
     * <p>
     * <b>Note</b> that sorting needs not to be supported by underlying mailing system. This can be done on application side, too.<br>
     * Same is for search, but in most cases it's faster to search on mailing system, but this heavily depends on how mails are accessed.
     *
     * @param folder The folder fullname
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field
     * @param order Whether ascending or descending sort order
     * @param searchTerm The search term to filter messages; may be <code>null</code> to obtain all messages
     * @param fields The fields to pre-fill in returned instances of {@link MessagingMessage}
     * @return The desired, pre-filled instances of {@link MessagingMessage}
     * @throws OXException If messages cannot be returned
     */
    public List<MessagingMessage> searchMessages(String folder, IndexRange indexRange, MessagingField sortField, OrderDirection order, SearchTerm<?> searchTerm, MessagingField[] fields) throws OXException;

    /**
     * Gets the message's attachment identified through given section identifier.
     *
     * @param folder The folder fullname
     * @param messageId The message identifier
     * @param sectionId The attachment's section identifier
     * @return The attachment wrapped by a {@link MessagingPart} instance
     * @throws OXException If no attachment can be found whose sequence ID matches given section identifier.
     */
    public MessagingPart getAttachment(String folder, String messageId, String sectionId) throws OXException;

    /**
     * Updates specified fields of given message.
     *
     * @param message The message
     * @param fields The fields to update
     * @throws OXException If update operation fails
     */
    public void updateMessage(MessagingMessage message, MessagingField[] fields) throws OXException;

    /**
     * Appends specified messages to given folder.
     *
     * @param folder The folder to append to
     * @param messages The messages to append.
     * @throws OXException If appending messages fails
     */
    public void appendMessages(String folder, MessagingMessage[] messages) throws OXException;

    /**
     * Copies specified messages from source folder to destination folder.
     * <p>
     * If no mail could be found for a given message identifier, the corresponding value in returned array of <code>String</code> is
     * <code>null</code>.
     *
     * @param sourceFolder The source folder identifier
     * @param destFolder The destination folder identifier
     * @param messageIds The message identifiers
     * @param fast <code>true</code> for fast copy (meaning no identifiers of corresponding messages in destination folder); otherwise
     *            <code>false</code>
     * @return The identifiers of corresponding messages in destination folder. Unless parameter <tt>fast</tt> is set to <code>true</code>;
     *         then <code>null</code> is returned.
     * @throws OXException If copy operation fails
     */
    public List<String> copyMessages(String sourceFolder, String destFolder, String[] messageIds, boolean fast) throws OXException;

    /**
     * Moves specified messages from source folder to destination folder.
     * <p>
     * If no mail could be found for a given message identifier, the corresponding value in returned array of <code>String</code> is
     * <code>null</code>.
     *
     * @param sourceFolder The source folder identifier
     * @param destFolder The destination folder identifier
     * @param messageIds The message identifiers
     * @param fast <code>true</code> for fast move (meaning no identifiers of corresponding messages in destination folder); otherwise
     *            <code>false</code>
     * @return The identifiers of corresponding messages in destination folder. Unless parameter <tt>fast</tt> is set to <code>true</code>;
     *         then <code>null</code> is returned.
     * @throws OXException If move operation fails
     */
    public List<String> moveMessages(String sourceFolder, String destFolder, String[] messageIds, boolean fast) throws OXException;

    /**
     * Deletes specified messages in folder.
     *
     * @param folder The folder to delete in
     * @param messageIds The message identifiers
     * @param hardDelete <code>true</code> to perform a hard-delete; otherwise <code>false</code> to backup in default location
     * @throws OXException If delete operation fails
     */
    public void deleteMessages(String folder, String[] messageIds, boolean hardDelete) throws OXException;

    /**
     * A convenience method to get all messages located in given folder.
     * <p>
     * If any messaging ID is invalid, <code>null</code> is returned for that entry.
     *
     * @param folder The folder identifier
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field
     * @param order Whether ascending or descending sort order
     * @param fields The fields to pre-fill in returned instances of {@link MessagingMessage}
     * @return The desired, pre-filled instances of {@link MessagingMessage}
     * @throws OXException If returning all messages fails
     */
    public List<MessagingMessage> getAllMessages(String folder, IndexRange indexRange, MessagingField sortField, OrderDirection order, MessagingField... fields) throws OXException;

    /**
     * Performs specified action to the message identified by given arguments and either returns resulting message or <code>null</code> if
     * no further user interaction is required.
     *
     * @param folder The folder identifier
     * @param id The message identifier
     * @param action The action to perform
     * @return The resulting message or <code>null</code> if requested action yields no resulting message (meaning no further user
     *         interaction required)
     * @throws OXException If performing specified action fails or action is not applicable for this perform() method
     */
    public MessagingMessage perform(String folder, String id, String action) throws OXException;

    /**
     * Performs specified action and either returns resulting message or <code>null</code> if no further user interaction is required.
     *
     * @param action The action to perform
     * @return The resulting message or <code>null</code> if requested action yields no resulting message (meaning no further user
     *         interaction required)
     * @throws OXException If performing specified action fails or action is not applicable for this perform() method
     */
    public MessagingMessage perform(String action) throws OXException;

    /**
     * Performs specified action to given message and either returns resulting message or <code>null</code> if no further user interaction
     * is required.
     *
     * @param message The message to process
     * @param action The action to perform
     * @return The resulting message or <code>null</code> if requested action yields no resulting message (meaning no further user
     *         interaction required)
     * @throws OXException If performing specified action fails or action is not applicable for this perform() method
     */
    public MessagingMessage perform(MessagingMessage message, String action) throws OXException;

    /**
     * This method resolves a @see {@link ReferenceContent} id.
     *
     * @param folder The folder identifier
     * @param id The message identifier
     * @param referenceId the reference identifier
     * @return
     * @throws OXException
     */
    public MessagingContent resolveContent(String folder, String id, String referenceId) throws OXException;
}
