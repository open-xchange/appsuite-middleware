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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * {@link MessagingAccess} - Provides access to message storage.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingAccess {

    /**
     * Gets the message associated with specified identifier.
     * 
     * @param id The identifier
     * @param folder The folder identifier
     * @param peek <code>true</code> to peek message content (meaning any mechanisms to mark content as read disabled); otherwise
     *            <code>false</code>
     * @return The message associated with specified identifier
     * @throws MessagingException If message cannot be returned
     */
    public MessagingMessage getMessage(String id, String folder, boolean peek) throws MessagingException;

    /**
     * Appends specified messages to given folder.
     * 
     * @param folder The folder to append to
     * @param messages The messages to append.
     * @throws MessagingException If appending messages fails
     */
    public void appendMessages(String folder, MessagingMessage[] messages) throws MessagingException;

    /**
     * Copies specified messages from source folder to destination folder.
     * 
     * @param sourceFolder The source folder identifier
     * @param destFolder The destination folder identifier
     * @param messageIds The message identifiers
     * @param fast <code>true</code> for fast copy (meaning no identifiers of corresponding messages in destination folder); otherwise
     *            <code>false</code>
     * @return The identifiers of corresponding messages in destination folder. Unless parameter <tt>fast</tt> is set to <code>true</code>;
     *         then <code>null</code> is returned.
     * @throws MessagingException If copy operation fails
     */
    public String[] copyMessages(String sourceFolder, String destFolder, String[] messageIds, boolean fast) throws MessagingException;

    /**
     * Deletes specified messages in folder.
     * 
     * @param folder The folder to delete in
     * @param messageIds The message identifiers
     * @param hardDelete <code>true</code> to perform a hard-delete; otherwise <code>false</code> to backup in default location
     * @throws MessagingException If delete operation fails
     */
    public void deleteMessages(String folder, String[] messageIds, boolean hardDelete) throws MessagingException;

    /**
     * A convenience method to get all messages located in given folder.
     * 
     * @param folder The folder identifier
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field
     * @param order Whether ascending or descending sort order
     * @param fields The fields to pre-fill in returned instances of {@link MessagingMessage}
     * @return The desired, pre-filled instances of {@link MessagingMessage}
     * @throws MessagingException If returning all messages fails
     */
    public List<MessagingMessage> getAllMessages(String folder, IndexRange indexRange, MessagingField sortField, OrderDirection order, MessagingField... fields) throws MessagingException;

    public MessagingMessage perform(String action, String id, String folder) throws MessagingException;

    public void send(MessagingMessage message) throws MessagingException;

}
