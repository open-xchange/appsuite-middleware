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
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
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
import com.openexchange.tools.exceptions.ExceptionUtils;

/**
 * {@link MailMessageStorage} - Abstract implementation of {@link IMailMessageStorage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailMessageStorage implements IMailMessageStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageStorage.class);

    /**
     * The fields containing {@link MailField#FULL}.
     */
    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private static final SearchTerm<Integer> TERM_FLAG_SEEN = new FlagTerm(MailMessage.FLAG_SEEN, false);

    @Override
    public abstract String[] appendMessages(String destFolder, MailMessage[] msgs) throws OXException;

    @Override
    public abstract String[] copyMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException;

    @Override
    public abstract void deleteMessages(String folder, String[] mailIds, boolean hardDelete) throws OXException;

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
        final MailMessage mail = getMessage(folder, mailId, false);
        if (null == mail) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(mailId, folder);
        }
        final MailPartHandler handler = new MailPartHandler(sequenceId);
        new MailMessageParser().parseMailMessage(mail, handler);
        final MailPart ret = handler.getMailPart();
        if (ret == null) {
            throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, mailId, folder);
        }
        return ret;
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        final MailMessage mail = getMessage(folder, mailId, false);
        if (null == mail) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(mailId, folder);
        }
        final ImageMessageHandler handler = new ImageMessageHandler(contentId);
        new MailMessageParser().parseMailMessage(mail, handler);
        final MailPart ret = handler.getImagePart();
        if (ret == null) {
            throw MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND.create(contentId, mailId, folder);
        }
        return ret;
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
    @Override
    public String[] getPrimaryContents(final String folder, final String[] mailIds) throws OXException {
        final TextFinder textFinder = new TextFinder();
        final int length = mailIds.length;
        final String[] retval = new String[length];
        for (int i = 0; i < length; i++) {
            String text = null;
            try {
               text = textFinder.getText(getMessage(folder, mailIds[i], false));
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.warn("Error while getting primary content for mail ''{}'' in folder ''{}''. Returning null.", mailIds[i], folder, t);
            }

            retval[i] = text;
        }
        return retval;
    }

    /**
     * Gets the mail located in given folder whose mail ID matches specified ID.
     * <p>
     * This is a convenience method that invokes {@link #getMessages(String, String[], MailField[])} with specified mail ID and
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
    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        final MailMessage[] mails = getMessages(folder, new String[] { mailId }, FIELDS_FULL);
        if ((mails == null) || (mails.length == 0) || (mails[0] == null)) {
            return null;
        }
        final MailMessage mail = mails[0];
        if (!mail.isSeen() && markSeen) {
            mail.setPrevSeen(false);
            updateMessageFlags(folder, new String[] { mailId }, MailMessage.FLAG_SEEN, true);
            mail.setFlag(MailMessage.FLAG_SEEN, true);
            mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
        }
        return mail;
    }

    @Override
    public abstract MailMessage[] getMessages(String folder, String[] mailIds, MailField[] fields) throws OXException;

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

    /**
     * Moves the mails identified through given mail IDs from source folder to destination folder.
     * <p>
     * If no mail could be found for a given mail ID, the corresponding value in returned array of <code>String</code> is <code>null</code>.
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
    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] ids = copyMessages(sourceFolder, destFolder, mailIds, fast);
        deleteMessages(sourceFolder, mailIds, true);
        return ids;
    }

    @Override
    public abstract void releaseResources() throws OXException;

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        final String uid;
        try {
            final MailMessage filledMail = MimeMessageConverter.fillComposedMailMessage(draftMail);
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
        if (msgref != null && draftFullname.equals(msgref.getFolder())) {
            deleteMessages(msgref.getFolder(), new String[] { msgref.getMailID() }, true);
            draftMail.setMsgref(null);
        }
        /*
         * Return draft mail
         */
        return getMessage(draftFullname, uid, true);
    }

    @Override
    public abstract MailMessage[] searchMessages(String folder, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException;

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        // Empty body here
    }

    @Override
    public void updateMessageUserFlags(String folder, String[] mailIds, String[] flags, boolean set) throws OXException {
        // Empty body here
    }

    @Override
    public abstract void updateMessageFlags(String folder, String[] mailIds, int flags, boolean set) throws OXException;

    /**
     * Gets all new and modified messages in specified folder. By default the constant {@link #EMPTY_RETVAL} is returned.
     *
     * @param folder The folder full name
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return All new and modified messages in specified folder
     * @throws OXException If mails cannot be returned
     */
    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        return EMPTY_RETVAL;
    }

    /**
     * Gets all deleted messages in specified folder. By default the constant {@link #EMPTY_RETVAL} is returned.
     *
     * @param folder The folder full name
     * @param fields The fields to pre-fill in returned instances of {@link MailMessage}
     * @return All deleted messages in specified folder
     * @throws OXException If mails cannot be returned
     */
    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
        return EMPTY_RETVAL;
    }

    @Override
    public abstract int getUnreadCount(String folder, SearchTerm<?> searchTerm) throws OXException;
}
