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

package com.openexchange.mail.api.enhanced;

import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.ImageMessageHandler;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.text.TextFinder;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.tools.exceptions.ExceptionUtils;

/**
 * {@link MailMessageStorageLong} - Enhances {@link MailMessageStorage} to delegate its methods to number-based invocations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailMessageStorageLong extends MailMessageStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageStorageLong.class);

    /**
     * The fields containing {@link MailField#FULL}.
     */
    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private static final SearchTerm<Integer> TERM_FLAG_SEEN = new FlagTerm(MailMessage.FLAG_SEEN, false);

    /**
     * Initializes a new {@link MailMessageStorageLong}.
     */
    protected MailMessageStorageLong() {
        super();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        return longs2uids(appendMessagesLong(destFolder, msgs));
    }

    /**
     * Appends given messages to given folder.
     *
     * @param destFolder The destination folder
     * @param msgs - The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @return The corresponding mail IDs in destination folder
     * @throws OXException If messages cannot be appended.
     */
    public abstract long[] appendMessagesLong(String destFolder, MailMessage[] msgs) throws OXException;

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
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
     * @param sourceFolder The source folder full name
     * @param destFolder The destination folder full name
     * @param mailIds The mail IDs in source folder
     * @param fast <code>true</code> to perform a fast copy operation, meaning the corresponding mail IDs in destination folder are ignored
     *            and an empty array of long is returned; otherwise <code>false</code>
     * @return The corresponding mail IDs if copied messages in destination folder
     * @throws OXException If messages cannot be copied.
     */
    public abstract long[] copyMessagesLong(String sourceFolder, String destFolder, long[] mailIds, boolean fast) throws OXException;

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        deleteMessagesLong(folder, uids2longs(mailIds), hardDelete);
    }

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
    public abstract void deleteMessagesLong(String folder, long[] mailIds, boolean hardDelete) throws OXException;

    /**
     * A convenience method that delivers all messages contained in given folder through invoking
     * {@link #searchMessages(String, IndexRange, MailSortField, OrderDirection, SearchTerm, MailField[]) searchMessages()} without search
     * arguments.
     * <p>
     * Note that sorting needs not to be supported by underlying mailing system. This can be done n application side, too
     * <p>
     * This method may be overridden in implementing subclass if a faster way can be achieved.
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
    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        try {
            return getAttachmentLong(folder, parseUnsignedLong(mailId), sequenceId);
        } catch (final NumberFormatException e) {
            LOG.error("UID cannot be parsed to a number: {}", mailId, e);
            return null;
        }
    }

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
    public MailPart getAttachmentLong(final String folder, final long mailId, final String sequenceId) throws OXException {
        final MailPartHandler handler = new MailPartHandler(sequenceId);
        final MailMessage mail = getMessageLong(folder, mailId, false);
        if (null == mail) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(mailId), folder);
        }
        new MailMessageParser().parseMailMessage(mail, handler);
        if (handler.getMailPart() == null) {
            throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, Long.valueOf(mailId), folder);
        }
        return handler.getMailPart();
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        try {
            return getImageAttachmentLong(folder, parseUnsignedLong(mailId), contentId);
        } catch (final NumberFormatException e) {
            LOG.error("UID cannot be parsed to a number: {}", mailId, e);
            return null;
        }
    }

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
    public MailPart getImageAttachmentLong(final String folder, final long mailId, final String contentId) throws OXException {
        final ImageMessageHandler handler = new ImageMessageHandler(contentId);
        final MailMessage mail = getMessageLong(folder, mailId, false);
        if (null == mail) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(mailId), folder);
        }
        new MailMessageParser().parseMailMessage(mail, handler);
        final MailPart imagePart = handler.getImagePart();
        if (null == imagePart) {
            throw MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND.create(contentId, Long.valueOf(mailId), folder);
        }
        return imagePart;
    }

    @Override
    public String[] getPrimaryContents(final String folder, final String[] mailIds) throws OXException {
        return getPrimaryContentsLong(folder, uids2longs(mailIds));
    }

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
    public String[] getPrimaryContentsLong(final String folder, final long[] mailIds) throws OXException {
        final TextFinder textFinder = new TextFinder();
        final int length = mailIds.length;
        final String[] retval = new String[length];
        for (int i = 0; i < length; i++) {
            String text = null;
            try {
                text = textFinder.getText(getMessageLong(folder, mailIds[i], false));
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.warn("Error while getting primary content for mail ''{}'' in folder ''{}''. Returning null.", mailIds[i], folder, t);
            }

            retval[i] = text;
        }
        return retval;
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        try {
            return getMessageLong(folder, parseUnsignedLong(mailId), markSeen);
        } catch (final NumberFormatException e) {
            LOG.error("UID cannot be parsed to a number: {}", mailId, e);
            return null;
        }
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
     * @param folder The folder full name
     * @param mailId The mail ID
     * @param markSeen <code>true</code> to explicitly mark corresponding mail as seen (setting system flag <i>\Seen</i>); otherwise
     *            <code>false</code> to leave as-is
     * @return Corresponding message
     * @throws OXException If message could not be returned
     */
    public MailMessage getMessageLong(final String folder, final long mailId, final boolean markSeen) throws OXException {
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
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
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
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return Corresponding mails as an array
     * @throws OXException If message could not be returned
     */
    public abstract MailMessage[] getMessagesLong(String folder, long[] mailIds, MailField[] fields) throws OXException;

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
    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        return null;
    }

    /**
     * Gets all unread messages located in given folder; meaning messages that do not have the \Seen flag set. The constant
     * {@link #EMPTY_RETVAL} may be returned if no unseen messages available in specified folder.
     * <p>
     * This is a convenience method that may be overridden if a faster way can be achieved.
     *
     * @param folder The folder full name
     * @param sortField The sort field
     * @param order The sort order
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @param limit The max. number of returned unread messages or <code>-1</code> to request all unread messages in folder
     * @return Unread messages contained in an array of {@link MailMessage}
     * @throws OXException If unread messages cannot be returned.
     */
    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        if (limit == 0) {
            return EMPTY_RETVAL;
        }
        return searchMessages(folder, limit < 0 ? IndexRange.NULL : new IndexRange(0, limit), sortField, order, TERM_FLAG_SEEN, fields);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        return longs2uids(moveMessagesLong(sourceFolder, destFolder, uids2longs(mailIds), fast));
    }

    /**
     * Moves the mails identified through given mail IDs from source folder to destination folder.
     * <p>
     * If no mail could be found for a given mail ID, the corresponding value in returned array of <code>long</code> is <code>-1</code>.
     * <p>
     * This is a convenience method that may be overridden if a faster way can be achieved.
     *
     * @param sourceFolder The source folder full name
     * @param destFolder The destination folder full name
     * @param mailIds The mail IDs in source folder
     * @param fast <code>true</code> to perform a fast move operation, meaning the corresponding mail IDs in destination folder are ignored
     *            and an empty array of String is returned; otherwise <code>false</code>
     * @return The corresponding mail IDs if copied messages in destination folder
     * @throws OXException If messages cannot be copied.
     */
    public long[] moveMessagesLong(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws OXException {
        final long[] ids = copyMessagesLong(sourceFolder, destFolder, mailIds, fast);
        deleteMessagesLong(sourceFolder, mailIds, true);
        return ids;
    }

    /**
     * Releases all resources used by this message storage when closing superior {@link MailAccess}
     *
     * @throws OXException If resources cannot be released
     */
    @Override
    public abstract void releaseResources() throws OXException;

    /**
     * A convenience method that saves given draft mail to default drafts folder and supports deletion of old draft's version (draft-edit
     * operation).
     *
     * @param draftFullName name The full name of default drafts folder
     * @param draftMail The draft mail as a composed mail
     * @return The stored draft mail
     * @throws OXException If saving specified draft message fails
     */
    @Override
    public MailMessage saveDraft(final String draftFullName, final ComposedMailMessage draftMail) throws OXException {
        final String uid;
        try {
            final MailMessage filledMail = MimeMessageConverter.fillComposedMailMessage(draftMail);
            filledMail.setFlag(MailMessage.FLAG_DRAFT, true);
            /*
             * Append message to draft folder
             */
            uid = appendMessages(draftFullName, new MailMessage[] { filledMail })[0];
        } finally {
            draftMail.cleanUp();
        }
        /*
         * Check for draft-edit operation: Delete old version
         */
        final MailPath msgref = draftMail.getMsgref();
        if (msgref != null && draftFullName.equals(msgref.getFolder())) {
            deleteMessages(msgref.getFolder(), new String[] { msgref.getMailID() }, true);
            draftMail.setMsgref(null);
        }
        /*
         * Return draft mail
         */
        return getMessage(draftFullName, uid, true);
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
    @Override
    public abstract MailMessage[] searchMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException;

    @Override
    public abstract int getUnreadCount(String folder, SearchTerm<?> searchTerm) throws OXException;

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
    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
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
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param colorLabel The color label to apply
     * @throws OXException If color label cannot be updated
     */
    public void updateMessageColorLabelLong(final String folder, final long[] mailIds, final int colorLabel) throws OXException {
        // Empty body here
    }

    @Override
    public void updateMessageUserFlags(String folder, String[] mailIds, String[] flags, boolean set) throws OXException {
        updateMessageUserFlagsLong(folder, uids2longs(mailIds), flags, set);
    }

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
    public void updateMessageUserFlagsLong(String folder, long[] mailIds, String[] flags, boolean set) throws OXException {
        // Empty body here
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        updateMessageFlagsLong(folder, uids2longs(mailIds), flags, set);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final String[] userFlags, final boolean set) throws OXException {
        updateMessageFlagsLong(folder, uids2longs(mailIds), flags, userFlags, set);
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
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param flags The bit pattern for the flags to alter
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If system flags cannot be updated
     */
    public abstract void updateMessageFlagsLong(String folder, long[] mailIds, int flags, boolean set) throws OXException;

    /**
     * Like {@link #updateMessageFlagsLong(String, long[], int, boolean)} but also updates user flags
     * 
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param flags The bit pattern for the flags to alter
     * @param userFlags An array of user flags
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If flags cannot be updated
     */
    public abstract void updateMessageFlagsLong(String folder, long[] mailIds, int flags, String[] userFlags, boolean set) throws OXException;

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
            retval[i] = parseUnsignedLong(uids[i]);
        }
        return retval;
    }

    private static final int RADIX = 10;

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

        int len = longs.length;
        if (len <= 0) {
            return new String[0];
        }

        String[] retval = new String[len];
        for (int i = 0; i < len; i++) {
            long l = longs[i];
            retval[i] = (l < 0) ? null : Long.toString(l, RADIX);
        }
        return retval;
    }

    /**
     * Parses the string argument as a signed decimal <code>long</code>. The characters in the string must all be decimal digits.
     * <p>
     * Note that neither the character <code>L</code> (<code>'&#92;u004C'</code>) nor <code>l</code> (<code>'&#92;u006C'</code>) is
     * permitted to appear at the end of the string as a type indicator, as would be permitted in Java programming language source code.
     *
     * @param s A <code>String</code> containing the <code>long</code> representation to be parsed
     * @return The <code>long</code> represented by the argument in decimal or <code>-1</code> if the string does not contain a parsable
     *         <code>long</code>.
     */
    protected static long parseUnsignedLong(final String s) {
        return StorageUtility.parseUnsignedLong(s);
    }

}
