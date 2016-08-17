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


package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link IMailMessageStorage} - Offers basic access methods to mail message storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorage {

    /**
     * The empty return value; e.g. may be used to indicate no result on
     * {@link #searchMessages(String, IndexRange, MailSortField, OrderDirection, SearchTerm, MailField[])} .
     */
    public static final MailMessage[] EMPTY_RETVAL = new MailMessage[0];

    /**
     * Appends given messages to given folder.
     *
     * @param destFolder The destination folder
     * @param msgs - The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @return The corresponding mail IDs in destination folder
     * @throws OXException If messages cannot be appended.
     */
    public String[] appendMessages(String destFolder, MailMessage[] msgs) throws OXException;

    /**
     * Copies the mails identified through given mail IDs from source folder to destination folder.
     * <p>
     * If no mail could be found for a given mail ID, the corresponding value in returned array of <code>String</code> is <code>null</code>.
     * <p>
     * Moreover the implementation should take care if a copy operation from or to default drafts folder is performed. If so, this method
     * should ensure that system flag <tt>DRAFT</tt> is enabled or disabled.
     *
     * @param sourceFolder The source folder full name
     * @param destFolder The destination folder full name
     * @param mailIds The mail IDs in source folder
     * @param fast <code>true</code> to perform a fast copy operation, meaning the corresponding mail IDs in destination folder are ignored
     *            and an empty array of String is returned; otherwise <code>false</code>
     * @return The corresponding mail IDs if copied messages in destination folder
     * @throws OXException If messages cannot be copied.
     */
    public String[] copyMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException;

    /**
     * Deletes the messages located in given folder identified through given mail IDs.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param hardDelete <code>true</code> to hard delete the messages, meaning not to create a backup copy of each message in default trash
     *            folder; otherwise <code>false</code>
     * @throws OXException If messages cannot be deleted.
     */
    public void deleteMessages(String folder, String[] mailIds, boolean hardDelete) throws OXException;

    /**
     * A convenience method that delivers all messages contained in given folder through invoking
     * {@link #searchMessages(String, IndexRange, MailSortField, OrderDirection, SearchTerm, MailField[]) searchMessages()} without search
     * arguments.
     * <p>
     * Note that sorting needs not to be supported by underlying mailing system. This can be done n application side, too
     *
     * @param folder The folder full name
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field
     * @param order Whether ascending or descending sort order
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return The desired, pre-filled instances of {@link MailMessage}
     * @throws OXException
     */
    public MailMessage[] getAllMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, MailField[] fields) throws OXException;

    /**
     * A convenience method that fetches the mail message's attachment identified through given <code>sequenceId</code>.
     * <p>
     * If no mail could be found for given mail ID, returned mail part is <code>null</code>.
     *
     * @param folder The folder full name
     * @param mailId The mail ID
     * @param sequenceId The attachment sequence ID
     * @return The attachment wrapped by a {@link MailPart} instance
     * @throws OXException If no attachment can be found whose sequence ID matches given <code>sequenceId</code>.
     */
    public MailPart getAttachment(String folder, String mailId, String sequenceId) throws OXException;

    /**
     * A convenience method that fetches the mail message's image attachment identified by its <code>Content-Id</code> header given through
     * argument <code>contentId</code>.
     * <p>
     * If no mail could be found for given mail ID, returned mail part is <code>null</code>.
     *
     * @param folder The folder full name
     * @param mailId The mail ID
     * @param contentId The value of header <code>Content-Id</code>
     * @return The image attachment wrapped by an {@link MailPart} instance
     * @throws OXException If no image can be found whose <code>Content-Id</code> header matches given <code>contentId</code>.
     */
    public MailPart getImageAttachment(String folder, String mailId, String contentId) throws OXException;

    /**
     * Gets the plain-text versions of the parts considered as primary mails' content.
     * <p>
     * If plain text for a single mail cannot be determined, <code>null</code> is inserted at corresponding position in returned array.
     *
     * @param folder The folder identifier
     * @param mailIds The mail identifiers
     * @return The plain-text versions of primary content
     * @throws OXException If plain texts cannot be returned
     */
    public String[] getPrimaryContents(String folder, String[] mailIds) throws OXException;

    /**
     * Gets the mail located in given folder whose mail ID matches specified ID.
     * <p>
     * This is a convenience method that invokes {@link #getMessages(String, String[], MailField[])} with specified mail ID and
     * {@link MailField#FULL}. Thus the returned instance of {@link MailMessage} is completely pre-filled including content references.
     * <p>
     * If no mail could be found for given mail ID, <code>null</code> is returned.
     *
     * @param folder The folder full name
     * @param mailId The mail ID
     * @param markSeen <code>true</code> to explicitly mark corresponding mail as seen (setting system flag <i>\Seen</i>); otherwise
     *            <code>false</code> to leave as-is
     * @return Corresponding message
     * @throws OXException If message could not be returned
     */
    public MailMessage getMessage(String folder, String mailId, boolean markSeen) throws OXException;

    /**
     * Gets the mails located in given folder whose mail ID matches specified ID. The constant {@link #EMPTY_RETVAL} may be returned, if
     * folder contains no messages.
     * <p>
     * The returned instances of {@link MailMessage} are pre-filled with specified fields through argument <code>fields</code>.
     * <p>
     * If any mail ID is invalid, <code>null</code> is returned for that entry.
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return Corresponding mails as an array
     * @throws OXException If message could not be returned
     */
    public MailMessage[] getMessages(String folder, String[] mailIds, MailField[] fields) throws OXException;

    /**
     * An <b>optional</b> convenience method that gets the messages located in given folder sorted by message thread reference. By default
     * <code>null</code> is returned assuming that mailing system does not support message thread reference, but may be overridden if it
     * does.
     * <p>
     * If underlying mailing system is IMAP, this method requires the IMAPv4 SORT extension or in detail the IMAP <code>CAPABILITY</code>
     * command should contain "SORT THREAD=ORDEREDSUBJECT THREAD=REFERENCES".
     *
     * @param folder The folder full name
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field applied to thread root elements
     * @param order Whether ascending or descending sort order
     * @param searchTerm The search term
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return The thread-sorted messages or <code>null</code> if SORT is not supported by mail server
     * @throws OXException If messages cannot be returned
     */
    public MailMessage[] getThreadSortedMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException;

    /**
     * Gets all unread messages located in given folder; meaning messages that do not have the \Seen flag set. The constant
     * {@link #EMPTY_RETVAL} may be returned if no unseen messages available in specified folder.
     *
     * @param folder The folder full name
     * @param sortField The sort field
     * @param order The sort order
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @param limit The max. number of returned unread messages or <code>-1</code> to request all unread messages in folder
     * @return Unread messages contained in an array of {@link MailMessage}
     * @throws OXException If unread messages cannot be returned.
     */
    public MailMessage[] getUnreadMessages(String folder, MailSortField sortField, OrderDirection order, MailField[] fields, int limit) throws OXException;

    /**
     * Moves the mails identified through given mail IDs from source folder to destination folder.
     * <p>
     * If no mail could be found for a given mail ID, the corresponding value in returned array of <code>String</code> is <code>null</code>.
     *
     * @param sourceFolder The source folder full name
     * @param destFolder The destination folder full name
     * @param mailIds The mail IDs in source folder
     * @param fast <code>true</code> to perform a fast move operation, meaning the corresponding mail IDs in destination folder are ignored
     *            and an empty array of String is returned; otherwise <code>false</code>
     * @return The corresponding mail IDs if copied messages in destination folder
     * @throws OXException If messages cannot be copied.
     */
    public String[] moveMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException;

    /**
     * Releases all resources used by this message storage when closing superior {@link MailAccess}
     *
     * @throws OXException If resources cannot be released
     */
    public void releaseResources() throws OXException;

    /**
     * A convenience method that saves given draft mail to default drafts folder and supports deletion of old draft's version (draft-edit
     * operation).
     *
     * @param draftFullname The full name of default drafts folder
     * @param draftMail The draft mail as a composed mail
     * @return The stored draft mail
     * @throws OXException If saving specified draft message fails
     */
    public MailMessage saveDraft(String draftFullname, ComposedMailMessage draftMail) throws OXException;

    /**
     * Searches mails located in given folder. If the search yields no results, the constant {@link #EMPTY_RETVAL} may be returned. This
     * method's purpose is to return filtered mails' headers for a <b>fast</b> list view. Therefore this method's <code>fields</code>
     * parameter should only contain instances of {@link MailField} which are marked as <b>[low cost]</b>. Otherwise pre-filling of returned
     * messages may take a long time and does no more fit to generate a fast list view.
     * <p>
     * <b>Note</b> that sorting needs not to be supported by underlying mailing system. This can be done on application side, too.<br>
     * Same is for search, but in most cases it's faster to search on mailing system, but this heavily depends on how mails are accessed.
     *
     * @param folder The folder full name
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field
     * @param order Whether ascending or descending sort order
     * @param searchTerm The search term to filter messages; may be <code>null</code> to obtain all messages
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return The desired, pre-filled instances of {@link MailMessage}
     * @throws OXException If mails cannot be returned
     */
    public MailMessage[] searchMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException;

    /**
     * An <b>optional</b> method that updates the color label of the messages specified by given mail IDs located in given folder.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     * <p>
     * The underlying mailing system needs to support some kind of user-definable flags to support this method. Otherwise this method should
     * be left unchanged with an empty body.
     * <p>
     * The color labels are user flags with the common prefix <code>"cl_"</code> and its numeric color code appended (currently numbers 0 to
     * 10).
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param colorLabel The color label to apply
     * @throws OXException If color label cannot be updated
     */
    public void updateMessageColorLabel(String folder, String[] mailIds, int colorLabel) throws OXException;

    /**
     * Updates the user flags of the messages specified by given mail IDs located in given folder. If parameter <code>set</code> is
     * <code>true</code> the affected flags denoted by <code>flags</code> are added; otherwise removed.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     * <p>
     * Mail folder in question requires to support user flags (storing individual strings per message)
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param flags The user flags
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If user flags cannot be updated
     */
    public void updateMessageUserFlags(String folder, String[] mailIds, String[] flags, boolean set) throws OXException;

    /**
     * Updates the flags of the messages specified by given mail IDs located in given folder. If parameter <code>set</code> is
     * <code>true</code> the affected flags denoted by <code>flags</code> are added; otherwise removed.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     * <p>
     * System flags are:
     * <ul>
     * <li>ANSWERED - This flag is set by clients to indicate that this message has been answered to.</li>
     * <li>DELETED - Clients set this flag to mark a message as deleted. The expunge operation on a folder removes all messages in that
     * folder that are marked for deletion.</li>
     * <li>DRAFT - This flag is set by clients to indicate that the message is a draft message.</li>
     * <li>FLAGGED - No semantic is defined for this flag. Clients alter this flag.</li>
     * <li>RECENT - Folder implementations set this flag to indicate that this message is new to this folder, that is, it has arrived since
     * the last time this folder was opened.</li>
     * <li>SEEN - This flag is implicitly set by the implementation when the this Message's content is returned to the client in some
     * form.Clients can alter this flag.</li>
     * <li>USER - A special flag that indicates that this folder supports user defined flags.</li>
     * </ul>
     * <p>
     * If mail folder in question supports user flags (storing individual strings per message) the virtual flags can also be updated through
     * this routine; e.g. {@link MailMessage#FLAG_FORWARDED}.
     * <p>
     * Moreover this routine checks for any spam related actions; meaning the {@link MailMessage#FLAG_SPAM} shall be enabled/disabled. Thus
     * the {@link SpamHandler#handleSpam(String, String[], boolean, MailAccess)}/
     * {@link SpamHandler#handleHam(String, String[], boolean, MailAccess)} methods needs to be executed.
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param flags The bit pattern for the flags to alter
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If system flags cannot be updated
     */
    public void updateMessageFlags(String folder, String[] mailIds, int flags, boolean set) throws OXException;
    
    /**
     * 
     * Like {@link #updateMessageFlags(String, String[], int, boolean)} but also updates user flags
     * 
     * @param folder The folder full name
     * @param mailIds The mail ids
     * @param flags The bit pattern for the system flags to alter
     * @param userFlags An array of user flags
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If flags cannot be updated
     */
    public void updateMessageFlags(String folder, String[] mailIds, int flags, String[] userFlags, boolean set) throws OXException;

    /**
     * Gets all new and modified messages in specified folder.
     *
     * @param folder The folder full name
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return All new and modified messages in specified folder
     * @throws OXException If mails cannot be returned
     */
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException;

    /**
     * Gets all deleted messages in specified folder.
     *
     * @param folder The folder full name
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return All deleted messages in specified folder
     * @throws OXException If mails cannot be returned
     */
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException;

    /**
     * Gets the number of unread messages in the given folder which match the given search term.
     * 
     * @param folder The folder full name
     * @param searchTerm The search term to filter messages; may be <code>null</code> to obtain all messages
     * @return The number of unread messages.
     * @throws OXException If unread count cannot be returned
     */
    int getUnreadCount(String folder, SearchTerm<?> searchTerm) throws OXException;
}
