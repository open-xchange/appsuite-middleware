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

import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;

/**
 * {@link MailMessageStorage} - Offers basic access methods to mail message(s)
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface MailMessageStorage {

	/**
	 * Gets the mail located in given folder whose mail ID matches specified ID.
	 * <p>
	 * The returned instance of {@link MailMessage} is completely pre-filled
	 * including content references.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailId
	 *            The mail ID
	 * @param markSeen
	 *            <code>true</code> to explicitly mark corresponding mail as
	 *            seen (setting system flag <i>\Seen</i>); otherwise
	 *            <code>false</code> to leave as-is
	 * @return Corresponding message
	 * @throws MailException
	 *             If message could not be returned
	 */
	public MailMessage getMessage(String folder, long mailId, boolean markSeen) throws MailException;

	/**
	 * A convenience method that delivers all messages contained in given
	 * folder. See parameter description to know which messages are going to be
	 * returned
	 * <p>
	 * Note that sorting needs not to be supported by underlying mailing system.
	 * This can be done n application side, too
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param indexRange
	 *            The indices range specifying the desired sub-list in sorted
	 *            list; may be <code>null</code> to obtain complete list
	 * @param sortField
	 *            The sort field
	 * @param order
	 *            Whether ascending or descending sort order
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return The desired, pre-filled instances of {@link MailMessage}
	 * @throws MailException
	 */
	public MailMessage[] getAllMessages(String folder, IndexRange indexRange, MailListField sortField,
			OrderDirection order, MailListField[] fields) throws MailException;

	/**
	 * Gets messages located in given folder. See parameter description to know
	 * which messages are going to be returned
	 * <p>
	 * In contrast to {@link #getMessage(String, long)} the returned instances
	 * of {@link MailMessage} are only pre-filled with the fields specified
	 * through parameter <code>fields</code>.
	 * <p>
	 * <b>Note</b> that sorting needs not to be supported by underlying mailing
	 * system. This can be done on application side, too.<br>
	 * Same is for search but in most cases it's faster to search on mailing
	 * system, but this heavily depends on how mails are accessed.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param indexRange
	 *            The indices range specifying the desired sub-list in sorted
	 *            list; may be <code>null</code> to obtain complete list
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
	public MailMessage[] getMessages(String folder, IndexRange indexRange, MailListField sortField,
			OrderDirection order, MailListField[] searchFields, String[] searchPatterns, boolean linkSearchTermsWithOR,
			MailListField[] fields) throws MailException;

	/**
	 * An <b>optional</b> method that gets the messages located in given folder
	 * sorted by message thread reference.
	 * <p>
	 * If underlying mailing system is IMAP, this method requires the IMAPv4
	 * SORT extension or in detail the IMAP <code>CAPABILITY</code> command
	 * should contain "SORT THREAD=ORDEREDSUBJECT THREAD=REFERENCES". This
	 * method may be omitted if mailing system does not support message thread
	 * reference.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param indexRange
	 *            The indices range specifying the desired sub-list in sorted
	 *            list; may be <code>null</code> to obtain complete list
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
	 * @return The thread-sorted messages or an empty array of
	 *         {@link MailMessage} if SORT is not supported by mail server
	 * @throws MailException
	 */
	public MailMessage[] getThreadSortedMessages(String folder, IndexRange indexRange, MailListField[] searchFields,
			String[] searchPatterns, boolean linkSearchTermsWithOR, MailListField[] fields) throws MailException;

	/**
	 * Gets messages located in given folder by specified mail IDs
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailIds
	 *            The mail IDs
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @param fromCache
	 *            <code>true</code> to try to fetch result from cache;
	 *            otherwise <code>false</code>
	 * @return The desired, pre-filled instances of {@link MailMessage}
	 * @throws MailException
	 *             If messages cannot be returned.
	 */
	public MailMessage[] getMessagesByUID(String folder, long[] mailIds, MailListField[] fields, boolean fromCache)
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
	 * @throws MailException
	 *             If unread messages cannot be returned.
	 */
	public MailMessage[] getUnreadMessages(String folder, MailListField sortField, OrderDirection order,
			MailListField[] fields, int limit) throws MailException;

	/**
	 * Deletes the messages located in given folder identified through given
	 * mail IDs
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailIds
	 *            The mail IDs
	 * @param hardDelete
	 *            <code>true</code> to hard delete the messages, meaning not
	 *            to create a backup copy of each message in default trash
	 *            folder; otherwise <code>false</code>
	 * @return <code>true</code> if delete was successful; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 *             If messages cannot be deleted.
	 */
	public boolean deleteMessages(String folder, long[] mailIds, boolean hardDelete) throws MailException;

	/**
	 * Copies the messages identified through given mail IDs from source folder
	 * to destination folder
	 * 
	 * @param sourceFolder
	 *            The source folder fullname
	 * @param destFolder
	 *            The destination folder fullname
	 * @param mailIds
	 *            The mail IDs in source folder
	 * @param move
	 *            <code>true</code> to perform a move operation, meaning to
	 *            delete the copied messages in source folder afterwards,
	 *            otherwise <code>false</code>
	 * @param fast
	 *            <code>true</code> to perform a fast copy operation, meaning
	 *            the corresponding UIDs in destination folder are ignored and
	 *            an empty array of long is returned; otherwise
	 *            <code>false</code>
	 * @return The corresponding UIDs if copied messages in destination folder
	 * @throws MailException
	 *             If messages cannot be copied.
	 */
	public long[] copyMessages(String sourceFolder, String destFolder, long[] mailIds, boolean move, boolean fast)
			throws MailException;

	/**
	 * Appends given messages to given folder.
	 * 
	 * @param destFolder
	 *            The destination folder
	 * @param msgs -
	 *            The messages to append (<b>must</b> be completely pre-filled
	 *            incl. content references)
	 * @return The corresponding mail IDs in destination folder
	 * @throws MailException
	 *             If messages cannot be appended.
	 */
	public long[] appendMessages(String destFolder, MailMessage[] msgs) throws MailException;

	/**
	 * Updates the system flags of the messages specified by given mail IDs
	 * located in given folder.
	 * <p>
	 * System flags are:
	 * <ul>
	 * <li>ANSWERED - This flag is set by clients to indicate that this message
	 * has been answered to.</li>
	 * <li>DELETED - Clients set this flag to mark a message as deleted. The
	 * expunge operation on a folder removes all messages in that folder that
	 * are marked for deletion.</li>
	 * <li>DRAFT - This flag is set by clients to indicate that the message is
	 * a draft message.</li>
	 * <li>FLAGGED - No semantic is defined for this flag. Clients alter this
	 * flag.</li>
	 * <li>RECENT - Folder implementations set this flag to indicate that this
	 * message is new to this folder, that is, it has arrived since the last
	 * time this folder was opened. </li>
	 * <li>SEEN - This flag is implicitly set by the implementation when the
	 * this Message's content is returned to the client in some form.Clients can
	 * alter this flag.</li>
	 * <li>USER - A special flag that indicates that this folder supports user
	 * defined flags. </li>
	 * </ul>
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailIds
	 *            The mail IDs
	 * @param flags
	 *            The bit pattern for the flags to alter
	 * @param set
	 *            <code>true</code> to enable the flags; otherwise
	 *            <code>false</code>
	 * @throws MailException
	 *             If system flags cannot be updated
	 */
	public void updateMessageFlags(String folder, long[] mailIds, int flags, boolean set) throws MailException;

	/**
	 * An <b>optional</b> method that updates the color label of the messages
	 * specified by given mail IDs located in given folder.
	 * <p>
	 * The underlying mailing system needs to support some kind of
	 * user-definable flags.
	 * <p>
	 * The color labels are user flags with the common prefix <code>"cl_"</code>
	 * and its numeric color code appended (currently numbers 0 through 10).
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailIds
	 *            The mail IDs
	 * @param colorLabel
	 *            The color label to apply
	 * @throws MailException
	 *             If color label cannot be updated
	 */
	public void updateMessageColorLabel(String folder, long[] mailIds, int colorLabel) throws MailException;

	/**
	 * Fetches the mail message's attachment identified through given
	 * <code>sequenceId</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailId
	 *            The mail ID
	 * @param sequenceId
	 *            The attachment sequence ID
	 * @param displayVersion
	 *            <code>true</code> if returned value is for displaying
	 *            purpose
	 * @return The attachment wrapped by an {@link MailPart} instance
	 * @throws MailException
	 *             If no attachment can be found whose sequence ID matches given
	 *             <code>sequenceId</code>.
	 */
	public MailPart getAttachment(String folder, long mailId, String sequenceId, boolean displayVersion)
			throws MailException;

	/**
	 * Fetches the mail message's image attachment identified through given
	 * <code>cid</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailId
	 *            The mail ID
	 * @param cid
	 *            The value of header <code>Content-ID</code>
	 * @return The image attachment wrapped by an {@link MailPart} instance
	 * @throws MailException
	 *             If no image can be found whose "Content-ID" header matches
	 *             given <code>cid</code>.
	 */
	public MailPart getImageAttachment(String folder, long mailId, String cid) throws MailException;

	/**
	 * Saves given draft mail to default drafts folder.
	 * 
	 * @param draftMail
	 *            The draft mail as a composed mail
	 * @return The stored draft mail
	 * @throws MailException
	 */
	public MailMessage saveDraft(ComposedMailMessage draftMail) throws MailException;

	/**
	 * Releases all resources used by this message storage when closing parental
	 * {@link MailConnection}
	 * 
	 * @throws MailException
	 *             If resources cannot be released
	 */
	public void releaseResources() throws MailException;

}
