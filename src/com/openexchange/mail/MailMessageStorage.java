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

package com.openexchange.mail;

import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.dataobjects.MailContent;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailMessageStorage} - the message strorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface MailMessageStorage {

	/**
	 * Gets the message located in given folder whose UID matches given UID.
	 * <p>
	 * The returned instance of {@link MailMessage} is completely pre-filled
	 * including content references.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param msgUID
	 *            The message UID
	 * @return Corresponding message
	 * @throws MailException
	 *             If message could not be returned
	 */
	public MailMessage getMessage(String folder, long msgUID) throws MailException;

	/**
	 * Gets messages located in given folder. See parameter description to know
	 * which messages are going to be returned
	 * <p>
	 * In contrast to {@link #getMessage(String, long)} the returned instances
	 * of {@link MailMessage} are only pre-filled with the fields specified
	 * through parameter <code>fields</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param fromToIndices
	 *            The indices range specifying the desired sub-list in sorted
	 *            list; may be <code>null</code> or must have a length of
	 *            <code>2</code>
	 * @param sortField
	 *            The sort field
	 * @param order
	 *            Whether ascending or descending sort order
	 * @param searchFields
	 *            The search fields
	 * @param searchPatterns
	 *            The pattern for the search field; therefore this array's
	 *            length must be equal to length of parameter
	 *            <code>searchCols</code>
	 * @param linkSearchTermsWithOR
	 *            <code>true</code> to link search fields with a logical OR;
	 *            <code>false</code> to link with logical AND
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return The desired, pre-filled instances of {@link MailMessage}
	 * @throws MailException
	 */
	public MailMessage[] getMessages(String folder, int[] fromToIndices, MailListField sortField, OrderDirection order,
			MailListField[] searchFields, String[] searchPatterns, boolean linkSearchTermsWithOR, MailListField[] fields)
			throws MailException;

	/**
	 * Gets all unread messages located in given folder; meaning messages that
	 * do not have the \Seen flag set.
	 * 
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param sortField
	 *            The sort field
	 * @param order
	 *            The sort order
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @param limit
	 *            The max. number of returned unread messages
	 * @return Unread messages contained in an array of {@link MailMessage}
	 */
	public MailMessage[] getUnreadMessages(String folder, MailListField sortField, OrderDirection order,
			MailListField[] fields, int limit) throws MailException;

	/**
	 * Searches for certain messages located in given folder. The messages are
	 * sorted by received date by default and pre-filled with the fields
	 * specified through parameter <code>fields</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param searchFields
	 *            The search fields
	 * @param searchPatterns
	 *            The search field's patterns
	 * @param linkWithOR
	 *            <code>true</code> to link the search patterns with a logical
	 *            OR; otherwise to link them with a logical AND
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return The desired, pre-filled instances of {@link MailMessage}
	 * @throws MailException
	 */
	public MailMessage[] searchMessages(String folder, MailListField[] searchFields, String[] searchPatterns,
			boolean linkWithOR, MailListField[] fields) throws MailException;

	/**
	 * Deletes the messages located in given folder identified through given
	 * UIDs
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param msgUIDs
	 *            The message UIDs
	 * @param hardDelete
	 *            <code>true</code> to hard delete the messages, meaning not
	 *            to create a backup copy of each message in default trash
	 *            folder; otherwise <code>false</code>
	 * @return <code>true</code> if delete was successful; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 */
	public boolean deleteMessages(String folder, long[] msgUIDs, boolean hardDelete) throws MailException;

	/**
	 * Copies the messages identifed through given UIDs from source folder to
	 * destination folder
	 * 
	 * @param sourceFolder
	 *            The source folder fullname
	 * @param destFolder
	 *            The destination folder fullname
	 * @param msgUIDs
	 *            The message UIDs in source folder
	 * @param move
	 *            <code>true</code> to perform a move operation, meaning to
	 *            delete the copied messages in source folder afterwards,
	 *            otherwise <code>false</code>
	 * @return The corresponding UIDs if copied messages in destination folder
	 * @throws MailException
	 */
	public long[] copyMessages(String sourceFolder, String destFolder, long[] msgUIDs, boolean move)
			throws MailException;

	/**
	 * Fetches the mail message's attachment identified through given
	 * <code>sequenceId</code>
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param msgUID
	 *            The message UID
	 * @param sequenceId
	 *            The attachment sequence ID
	 * @param displayVersion
	 *            <code>true</code> if returned value is for displaying
	 *            purpose
	 * @return the attachment wrapped by an {@link MailContent} instance
	 * @throws MailException
	 */
	public MailContent getAttachment(String folder, long msgUID, String sequenceId, boolean displayVersion)
			throws MailException;

}
