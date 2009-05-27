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

package com.openexchange.mail.api.enhanced;

import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.ImageMessageHandler;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link MailMessageStorageLong} - Enhances {@link MailMessageStorage} to delegate its methods to number-based invocations.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailMessageStorageLong extends MailMessageStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailMessageStorageLong.class);

    /**
     * The fields containing {@link MailField#FULL}.
     */
    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private static final SearchTerm<Integer> TERM_FLAG_SEEN = new FlagTerm(MailMessage.FLAG_SEEN, false);

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws MailException {
        return longs2uids(appendMessagesLong(destFolder, msgs));
    }

    /**
     * Appends given messages to given folder.
     * 
     * @param destFolder The destination folder
     * @param msgs - The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @return The corresponding mail IDs in destination folder
     * @throws MailException If messages cannot be appended.
     */
    public abstract long[] appendMessagesLong(String destFolder, MailMessage[] msgs) throws MailException;

    @Override
    public final String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        return longs2uids(copyMessagesLong(sourceFolder, destFolder, uids2longs(mailIds), fast));
    }

    /**
     * Copies the mails identified through given mail IDs from source folder to destination folder.
     * <p>
     * If no mail could be found for a given mail ID, the corresponding value in returned array of <code>long</code> is <code>-1</code>.
     * <p>
     * Moreover the implementation should take care if a copy operation from or to default drafts folder is performed. If so, this method
     * should ensure that system flag <tt>DRAFT</tt> is enabled or disabled.
     * 
     * @param sourceFolder The source folder fullname
     * @param destFolder The destination folder fullname
     * @param mailIds The mail IDs in source folder
     * @param fast <code>true</code> to perform a fast copy operation, meaning the corresponding mail IDs in destination folder are ignored
     *            and an empty array of long is returned; otherwise <code>false</code>
     * @return The corresponding mail IDs if copied messages in destination folder
     * @throws MailException If messages cannot be copied.
     */
    public abstract long[] copyMessagesLong(String sourceFolder, String destFolder, long[] mailIds, boolean fast) throws MailException;

    @Override
    public final void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws MailException {
        deleteMessagesLong(folder, uids2longs(mailIds), hardDelete);
    }

    /**
     * Deletes the messages located in given folder identified through given mail IDs.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     * 
     * @param folder The folder fullname
     * @param mailIds The mail IDs
     * @param hardDelete <code>true</code> to hard delete the messages, meaning not to create a backup copy of each message in default trash
     *            folder; otherwise <code>false</code>
     * @throws MailException If messages cannot be deleted.
     */
    public abstract void deleteMessagesLong(String folder, long[] mailIds, boolean hardDelete) throws MailException;

    /**
     * A convenience method that delivers all messages contained in given folder through invoking
     * {@link #searchMessages(String, IndexRange, MailSortField, OrderDirection, SearchTerm, MailField[]) searchMessages()} without search
     * arguments.
     * <p>
     * Note that sorting needs not to be supported by underlying mailing system. This can be done n application side, too
     * <p>
     * This method may be overridden in implementing subclass if a faster way can be achieved.
     * 
     * @param folder The folder fullname
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field
     * @param order Whether ascending or descending sort order
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return The desired, pre-filled instances of {@link MailMessage}
     * @throws MailException
     */
    @Override
    public final MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws MailException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public final MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws MailException {
        return getAttachmentLong(folder, Long.parseLong(mailId), sequenceId);
    }

    /**
     * A convenience method that fetches the mail message's attachment identified through given <code>sequenceId</code>.
     * <p>
     * If no mail could be found for given mail ID, returned mail part is <code>null</code>.
     * 
     * @param folder The folder fullname
     * @param mailId The mail ID
     * @param sequenceId The attachment sequence ID
     * @return The attachment wrapped by a {@link MailPart} instance
     * @throws MailException If no attachment can be found whose sequence ID matches given <code>sequenceId</code>.
     */
    public MailPart getAttachmentLong(final String folder, final long mailId, final String sequenceId) throws MailException {
        final MailPartHandler handler = new MailPartHandler(sequenceId);
        new MailMessageParser().parseMailMessage(getMessageLong(folder, mailId, false), handler);
        if (handler.getMailPart() == null) {
            throw new MailException(MailException.Code.ATTACHMENT_NOT_FOUND, sequenceId, Long.valueOf(mailId), folder);
        }
        return handler.getMailPart();
    }

    @Override
    public final MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws MailException {
        return getImageAttachmentLong(folder, Long.parseLong(mailId), contentId);
    }

    /**
     * A convenience method that fetches the mail message's image attachment identified by its <code>Content-Id</code> header given through
     * argument <code>contentId</code>.
     * <p>
     * If no mail could be found for given mail ID, returned mail part is <code>null</code>.
     * 
     * @param folder The folder fullname
     * @param mailId The mail ID
     * @param contentId The value of header <code>Content-Id</code>
     * @return The image attachment wrapped by an {@link MailPart} instance
     * @throws MailException If no image can be found whose <code>Content-Id</code> header matches given <code>contentId</code>.
     */
    public MailPart getImageAttachmentLong(final String folder, final long mailId, final String contentId) throws MailException {
        final ImageMessageHandler handler = new ImageMessageHandler(contentId);
        new MailMessageParser().parseMailMessage(getMessageLong(folder, mailId, false), handler);
        if (handler.getImagePart() == null) {
            throw new MailException(MailException.Code.IMAGE_ATTACHMENT_NOT_FOUND, contentId, Long.valueOf(mailId), folder);
        }
        return handler.getImagePart();
    }

    @Override
    public final MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws MailException {
        return getMessageLong(folder, Long.parseLong(mailId), markSeen);
    }

    /**
     * Gets the mail located in given folder whose mail ID matches specified ID.
     * <p>
     * This is a convenience method that invokes {@link #getMessagesLong(String, long[], MailField[])} with specified mail ID and
     * {@link MailField#FULL}. Thus the returned instance of {@link MailMessage} is completely pre-filled including content references.
     * <p>
     * If no mail could be found for given mail ID, <code>null</code> is returned.
     * <p>
     * This method may be overridden in implementing subclass if a faster way can be achieved.
     * 
     * @param folder The folder fullname
     * @param mailId The mail ID
     * @param markSeen <code>true</code> to explicitly mark corresponding mail as seen (setting system flag <i>\Seen</i>); otherwise
     *            <code>false</code> to leave as-is
     * @return Corresponding message
     * @throws MailException If message could not be returned
     */
    public MailMessage getMessageLong(final String folder, final long mailId, final boolean markSeen) throws MailException {
        final MailMessage[] mails = getMessagesLong(folder, new long[] { mailId }, FIELDS_FULL);
        if ((mails == null) || (mails.length == 0) || (mails[0] == null)) {
            return null;
        }
        final MailMessage mail = mails[0];
        if (!mail.isSeen() && markSeen) {
            mail.setPrevSeen(false);
            updateMessageFlagsLong(folder, new long[] { mailId }, MailMessage.FLAG_SEEN, true);
            mail.setFlag(MailMessage.FLAG_SEEN, true);
            mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
        }
        return mail;
    }

    @Override
    public final MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws MailException {
        return getMessagesLong(folder, uids2longs(mailIds), fields);
    }

    /**
     * Gets the mails located in given folder whose mail ID matches specified ID. The constant {@link #EMPTY_RETVAL} may be returned, if
     * folder contains no messages.
     * <p>
     * The returned instances of {@link MailMessage} are pre-filled with specified fields through argument <code>fields</code>.
     * <p>
     * If any mail ID is invalid, <code>null</code> is returned for that entry.
     * 
     * @param folder The folder fullname
     * @param mailIds The mail IDs
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return Corresponding mails as an array
     * @throws MailException If message could not be returned
     */
    public abstract MailMessage[] getMessagesLong(String folder, long[] mailIds, MailField[] fields) throws MailException;

    /**
     * An <b>optional</b> convenience method that gets the messages located in given folder sorted by message thread reference. By default
     * <code>null</code> is returned assuming that mailing system does not support message thread reference, but may be overridden if it
     * does.
     * <p>
     * If underlying mailing system is IMAP, this method requires the IMAPv4 SORT extension or in detail the IMAP <code>CAPABILITY</code>
     * command should contain "SORT THREAD=ORDEREDSUBJECT THREAD=REFERENCES".
     * 
     * @param folder The folder fullname
     * @param indexRange The index range specifying the desired sub-list in sorted list; may be <code>null</code> to obtain complete list.
     *            Range begins at the specified start index and extends to the message at index <code>end - 1</code>. Thus the length of the
     *            range is <code>end - start</code>.
     * @param sortField The sort field applied to thread root elements
     * @param order Whether ascending or descending sort order
     * @param searchTerm The search term
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return The thread-sorted messages or <code>null</code> if SORT is not supported by mail server
     * @throws MailException If messages cannot be returned
     */
    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        return null;
    }

    /**
     * Gets all unread messages located in given folder; meaning messages that do not have the \Seen flag set. The constant
     * {@link #EMPTY_RETVAL} may be returned if no unseen messages available in specified folder.
     * <p>
     * This is a convenience method that may be overridden if a faster way can be achieved.
     * 
     * @param folder The folder fullname
     * @param sortField The sort field
     * @param order The sort order
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @param limit The max. number of returned unread messages or <code>-1</code> to request all unread messages in folder
     * @return Unread messages contained in an array of {@link MailMessage}
     * @throws MailException If unread messages cannot be returned.
     */
    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        if (limit == 0) {
            return EMPTY_RETVAL;
        }
        return searchMessages(folder, limit < 0 ? IndexRange.NULL : new IndexRange(0, limit), sortField, order, TERM_FLAG_SEEN, fields);
    }

    @Override
    public final String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        return longs2uids(moveMessagesLong(sourceFolder, destFolder, uids2longs(mailIds), fast));
    }

    /**
     * Moves the mails identified through given mail IDs from source folder to destination folder.
     * <p>
     * If no mail could be found for a given mail ID, the corresponding value in returned array of <code>long</code> is <code>-1</code>.
     * <p>
     * This is a convenience method that may be overridden if a faster way can be achieved.
     * 
     * @param sourceFolder The source folder fullname
     * @param destFolder The destination folder fullname
     * @param mailIds The mail IDs in source folder
     * @param fast <code>true</code> to perform a fast move operation, meaning the corresponding mail IDs in destination folder are ignored
     *            and an empty array of String is returned; otherwise <code>false</code>
     * @return The corresponding mail IDs if copied messages in destination folder
     * @throws MailException If messages cannot be copied.
     */
    public long[] moveMessagesLong(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws MailException {
        final long[] ids = copyMessagesLong(sourceFolder, destFolder, mailIds, fast);
        deleteMessagesLong(sourceFolder, mailIds, true);
        return ids;
    }

    /**
     * Releases all resources used by this message storage when closing superior {@link MailAccess}
     * 
     * @throws MailException If resources cannot be released
     */
    @Override
    public abstract void releaseResources() throws MailException;

    /**
     * A convenience method that saves given draft mail to default drafts folder and supports deletion of old draft's version (draft-edit
     * operation).
     * 
     * @param draftFullname The fullname of default drafts folder
     * @param draftMail The draft mail as a composed mail
     * @return The stored draft mail
     * @throws MailException If saving specified draft message fails
     */
    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws MailException {
        final String uid;
        try {
            final MailMessage filledMail = MIMEMessageConverter.fillComposedMailMessage(draftMail);
            filledMail.setFlag(MailMessage.FLAG_DRAFT, true);
            /*
             * Append message to draft folder
             */
            uid = appendMessages(draftFullname, new MailMessage[] { filledMail })[0];
        } finally {
            draftMail.cleanUp();
        }
        /*
         * Check for draft-edit operation: Delete old version
         */
        final MailPath msgref = draftMail.getMsgref();
        if (msgref != null) {
            deleteMessages(msgref.getFolder(), new String[] { msgref.getMailID() }, true);
            draftMail.setMsgref(null);
        }
        /*
         * Return draft mail
         */
        return getMessage(draftFullname, uid, true);
    }

    /**
     * Searches mails located in given folder. If the search yields no results, the constant {@link #EMPTY_RETVAL} may be returned. This
     * method's purpose is to return filtered mails' headers for a <b>fast</b> list view. Therefore this method's <code>fields</code>
     * parameter should only contain instances of {@link MailField} which are marked as <b>[low cost]</b>. Otherwise pre-filling of returned
     * messages may take a long time and does no more fit to generate a fast list view.
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
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return The desired, pre-filled instances of {@link MailMessage}
     * @throws MailException If mails cannot be returned
     */
    @Override
    public abstract MailMessage[] searchMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws MailException;

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
     * @param folder The folder fullname
     * @param mailIds The mail IDs
     * @param colorLabel The color label to apply
     * @throws MailException If color label cannot be updated
     */
    @Override
    public final void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws MailException {
        updateMessageColorLabelLong(folder, uids2longs(mailIds), colorLabel);
    }

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
     * @param folder The folder fullname
     * @param mailIds The mail IDs
     * @param colorLabel The color label to apply
     * @throws MailException If color label cannot be updated
     */
    public void updateMessageColorLabelLong(final String folder, final long[] mailIds, final int colorLabel) throws MailException {
        // Empty body here
    }

    @Override
    public final void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws MailException {
        updateMessageFlagsLong(folder, uids2longs(mailIds), flags, set);
    }

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
     * @param folder The folder fullname
     * @param mailIds The mail IDs
     * @param flags The bit pattern for the flags to alter
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws MailException If system flags cannot be updated
     */
    public abstract void updateMessageFlagsLong(String folder, long[] mailIds, int flags, boolean set) throws MailException;

    /**
     * Converts specified UID strings to an array of <code>long</code>.
     * 
     * @param uids The UID strings
     * @return An array of <code>long</code>
     */
    protected static final long[] uids2longs(final String[] uids) {
        if (null == uids) {
            return null;
        }
        final long[] retval = new long[uids.length];
        for (int i = 0; i < retval.length; i++) {
            final String s = uids[i];
            if (null == s) {
                retval[i] = -1L;
            } else {
                try {
                    retval[i] = Long.parseLong(s);
                } catch (final NumberFormatException e) {
                    LOG.error("UID cannot be parsed to a number: " + s, e);
                    retval[i] = -1L;
                }
            }
        }
        return retval;
    }

    /**
     * Converts specified UID numbers to an array of <code>String</code>.
     * 
     * @param longs The UID numbers
     * @return An array of <code>String</code>
     */
    protected static final String[] longs2uids(final long[] longs) {
        if (null == longs) {
            return null;
        }
        final String[] retval = new String[longs.length];
        for (int i = 0; i < retval.length; i++) {
            final long l = longs[i];
            if (-1 == l) {
                retval[i] = null;
            } else {
                retval[i] = String.valueOf(longs[i]);
            }
        }
        return retval;
    }

}
