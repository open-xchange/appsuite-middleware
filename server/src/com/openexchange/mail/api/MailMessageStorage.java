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

package com.openexchange.mail.api;

import java.util.List;

import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.ImageMessageHandler;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link MailMessageStorage} - Offers basic access methods to mail message
 * storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailMessageStorage {

	/**
	 * The fields containing {@link MailField#FULL}.
	 */
	private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

	private static final SearchTerm<Integer> TERM_FLAG_SEEN = new FlagTerm(MailMessage.FLAG_SEEN, false);

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
	public abstract long[] appendMessages(String destFolder, MailMessage[] msgs) throws MailException;

	/**
	 * Copies the mails identified through given mail IDs from source folder to
	 * destination folder.
	 * <p>
	 * If no mail could be found for a given mail ID, the corresponding value in
	 * returned array of <code>long</code> is <code>-1</code>.
	 * <p>
	 * Moreover the implementation should take care if a copy operation from or
	 * to default drafts folder is performed. If so, this method should ensure
	 * that system flag <tt>DRAFT</tt> is enabled or disabled.
	 * 
	 * @param sourceFolder
	 *            The source folder fullname
	 * @param destFolder
	 *            The destination folder fullname
	 * @param mailIds
	 *            The mail IDs in source folder
	 * @param fast
	 *            <code>true</code> to perform a fast copy operation, meaning
	 *            the corresponding mail IDs in destination folder are ignored
	 *            and an empty array of long is returned; otherwise
	 *            <code>false</code>
	 * @return The corresponding mail IDs if copied messages in destination
	 *         folder
	 * @throws MailException
	 *             If messages cannot be copied.
	 */
	public abstract long[] copyMessages(String sourceFolder, String destFolder, long[] mailIds, boolean fast)
			throws MailException;

	/**
	 * Deletes the messages located in given folder identified through given
	 * mail IDs.
	 * <p>
	 * If no mail could be found for a given mail ID, it is treated as a no-op.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailIds
	 *            The mail IDs
	 * @param hardDelete
	 *            <code>true</code> to hard delete the messages, meaning not
	 *            to create a backup copy of each message in default trash
	 *            folder; otherwise <code>false</code>
	 * @throws MailException
	 *             If messages cannot be deleted.
	 */
	public abstract void deleteMessages(String folder, long[] mailIds, boolean hardDelete) throws MailException;

	/**
	 * A convenience method that delivers all messages contained in given folder
	 * through invoking
	 * {@link #searchMessages(String, IndexRange, MailListField, OrderDirection, SearchTerm, MailField[])}
	 * without search arguments.
	 * <p>
	 * Note that sorting needs not to be supported by underlying mailing system.
	 * This can be done n application side, too
	 * <p>
	 * This method may be overridden in implementing subclass if a faster way
	 * can be achieved.
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
	public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange,
			final MailListField sortField, final OrderDirection order, final MailField[] fields) throws MailException {
		return searchMessages(folder, indexRange, sortField, order, null, fields);
	}

	/**
	 * A convenience method that fetches the mail message's attachment
	 * identified through given <code>sequenceId</code>.
	 * <p>
	 * If no mail could be found for given mail ID, returned mail part is
	 * <code>null</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailId
	 *            The mail ID
	 * @param sequenceId
	 *            The attachment sequence ID
	 * @return The attachment wrapped by an {@link MailPart} instance
	 * @throws MailException
	 *             If no attachment can be found whose sequence ID matches given
	 *             <code>sequenceId</code>.
	 */
	public MailPart getAttachment(final String folder, final long mailId, final String sequenceId) throws MailException {
		final MailPartHandler handler = new MailPartHandler(sequenceId);
		new MailMessageParser().parseMailMessage(getMessage(folder, mailId, false), handler);
		if (handler.getMailPart() == null) {
			throw new MailException(MailException.Code.ATTACHMENT_NOT_FOUND, sequenceId, Long.valueOf(mailId), folder);
		}
		return handler.getMailPart();
	}

	/**
	 * A convenience method that fetches the mail message's image attachment
	 * identified by its <code>Content-Id</code> header given through argument
	 * <code>contentId</code>.
	 * <p>
	 * If no mail could be found for given mail ID, returned mail part is
	 * <code>null</code>.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailId
	 *            The mail ID
	 * @param contentId
	 *            The value of header <code>Content-Id</code>
	 * @return The image attachment wrapped by an {@link MailPart} instance
	 * @throws MailException
	 *             If no image can be found whose <code>Content-Id</code>
	 *             header matches given <code>contentId</code>.
	 */
	public MailPart getImageAttachment(final String folder, final long mailId, final String contentId)
			throws MailException {
		final ImageMessageHandler handler = new ImageMessageHandler(contentId);
		new MailMessageParser().parseMailMessage(getMessage(folder, mailId, false), handler);
		if (handler.getImagePart() == null) {
			throw new MailException(MailException.Code.ATTACHMENT_NOT_FOUND, contentId, Long.valueOf(mailId), folder);
		}
		return handler.getImagePart();
	}

	/**
	 * Gets the mail located in given folder whose mail ID matches specified ID.
	 * <p>
	 * This is a convenience method that invokes
	 * {@link #getMessages(String, long[], MailField[])} with specified mail ID
	 * and {@link MailField#FULL}. Thus the returned instance of
	 * {@link MailMessage} is completely pre-filled including content
	 * references.
	 * <p>
	 * If no mail could be found for given mail ID, <code>null</code> is
	 * returned.
	 * <p>
	 * This method may be overridden in implementing subclass if a faster way
	 * can be achieved.
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
	public MailMessage getMessage(final String folder, final long mailId, final boolean markSeen) throws MailException {
		final MailMessage[] mails = getMessages(folder, new long[] { mailId }, FIELDS_FULL);
		if ((mails == null) || (mails.length == 0) || (mails[0] == null)) {
			return null;
		}
		final MailMessage mail = mails[0];
		if (!mail.isSeen() && markSeen) {
			mail.setPrevSeen(false);
			updateMessageFlags(folder, new long[] { mailId }, MailMessage.FLAG_SEEN, true);
			mail.setFlag(MailMessage.FLAG_SEEN, true);
			mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
		}
		return mail;
	}

	/**
	 * Gets the mails located in given folder whose mail ID matches specified
	 * ID.
	 * <p>
	 * The returned instances of {@link MailMessage} are pre-filled with
	 * specified fields through argument <code>fields</code>.
	 * <p>
	 * If any mail ID is invalid, <code>null</code> is returned for that
	 * entry.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param mailIds
	 *            The mail IDs
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return Corresponding mails as an array
	 * @throws MailException
	 *             If message could not be returned
	 */
	public abstract MailMessage[] getMessages(String folder, long[] mailIds, MailField[] fields) throws MailException;

	/**
	 * An <b>optional</b> convenience method that gets the messages located in
	 * given folder sorted by message thread reference. By default
	 * <code>null</code> is returned assuming that mailing system does not
	 * support message thread reference, but may be overridden if it does.
	 * <p>
	 * If underlying mailing system is IMAP, this method requires the IMAPv4
	 * SORT extension or in detail the IMAP <code>CAPABILITY</code> command
	 * should contain "SORT THREAD=ORDEREDSUBJECT THREAD=REFERENCES".
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param indexRange
	 *            The indices range specifying the desired sub-list in sorted
	 *            list; may be <code>null</code> to obtain complete list
	 * @param searchTerm
	 *            The search term
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return The thread-sorted messages or <code>null</code> if SORT is not
	 *         supported by mail server
	 * @throws MailException
	 */
	public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange,
			final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
		return null;
	}

	/**
	 * Gets all unread messages located in given folder; meaning messages that
	 * do not have the \Seen flag set.
	 * <p>
	 * This is a convenience method that may be overridden if a faster way can
	 * be achieved.
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
	public MailMessage[] getUnreadMessages(final String folder, final MailListField sortField,
			final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
		return searchMessages(folder, IndexRange.NULL, sortField, order, TERM_FLAG_SEEN, fields);
	}

	/**
	 * Moves the mails identified through given mail IDs from source folder to
	 * destination folder.
	 * <p>
	 * If no mail could be found for a given mail ID, the corresponding value in
	 * returned array of <code>long</code> is <code>-1</code>.
	 * <p>
	 * This is a convenience method that may be overridden if a faster way can
	 * be achieved.
	 * 
	 * @param sourceFolder
	 *            The source folder fullname
	 * @param destFolder
	 *            The destination folder fullname
	 * @param mailIds
	 *            The mail IDs in source folder
	 * @param fast
	 *            <code>true</code> to perform a fast move operation, meaning
	 *            the corresponding mail IDs in destination folder are ignored
	 *            and an empty array of long is returned; otherwise
	 *            <code>false</code>
	 * @return The corresponding mail IDs if copied messages in destination
	 *         folder
	 * @throws MailException
	 *             If messages cannot be copied.
	 */
	public long[] moveMessages(final String sourceFolder, final String destFolder, final long[] mailIds,
			final boolean fast) throws MailException {
		final long[] ids = copyMessages(sourceFolder, destFolder, mailIds, fast);
		deleteMessages(sourceFolder, mailIds, true);
		return ids;
	}

	/**
	 * Releases all resources used by this message storage when closing superior
	 * {@link MailAccess}
	 * 
	 * @throws MailException
	 *             If resources cannot be released
	 */
	public abstract void releaseResources() throws MailException;

	/**
	 * A convenience method that saves given draft mail to default drafts folder
	 * and supports deletion of old draft's version (draft-edit operation).
	 * 
	 * @param draftFullname
	 *            The fullname of default drafts folder
	 * @param draftMail
	 *            The draft mail as a composed mail
	 * @return The stored draft mail
	 * @throws MailException
	 */
	public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws MailException {
		if (!draftMail.isDraft()) {
			draftMail.setFlag(MailMessage.FLAG_DRAFT, true);
		}
		final List<String> tempIds;
		if (draftMail.getMsgref() != null) {
			/*
			 * Load referenced mail parts from original message
			 */
			tempIds = ReferencedMailPart.loadReferencedParts(draftMail, draftMail.getSession());
		} else {
			tempIds = null;
		}
		final long uid;
		try {
			final MailMessage filledMail = MIMEMessageConverter.fillComposedMailMessage(draftMail);
			filledMail.setFlag(MailMessage.FLAG_DRAFT, true);
			/*
			 * Append message to draft folder
			 */
			uid = appendMessages(draftFullname, new MailMessage[] { filledMail })[0];
		} finally {
			draftMail.release();
			if (null != tempIds) {
				for (final String id : tempIds) {
					draftMail.getSession().removeUploadedFile(id);
				}
			}
		}
		/*
		 * Check for draft-edit operation: Delete old version
		 */
		if (draftMail.getReferencedMail() != null) {
			if (draftMail.getReferencedMail().isDraft()) {
				deleteMessages(draftMail.getReferencedMail().getFolder(), new long[] { draftMail.getReferencedMail()
						.getMailId() }, true);
			}
			draftMail.setMsgref(null);
		}
		/*
		 * Return draft mail
		 */
		return getMessage(draftFullname, uid, true);
	}

	/**
	 * Searches mails located in given folder. This method's purpose is to
	 * return filtered mails' headers for a <b>fast</b> list view. Therefore
	 * this method's <code>fields</code> parameter should only contain
	 * instances of {@link MailField} which are marked as <b>[low cost]</b>.
	 * Otherwise pre-filling of returned messages may take a long time and does
	 * no more fit to generate a fast list view.
	 * <p>
	 * <b>Note</b> that sorting needs not to be supported by underlying mailing
	 * system. This can be done on application side, too.<br>
	 * Same is for search, but in most cases it's faster to search on mailing
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
	 * @param searchTerm
	 *            The search term to filter messages
	 * @param fields
	 *            The fields to pre-fill in returned instances of
	 *            {@link MailMessage}
	 * @return The desired, pre-filled instances of {@link MailMessage}
	 * @throws MailException
	 *             If mails cannot be returned
	 */
	public abstract MailMessage[] searchMessages(String folder, IndexRange indexRange, MailListField sortField,
			OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws MailException;

	/**
	 * An <b>optional</b> method that updates the color label of the messages
	 * specified by given mail IDs located in given folder.
	 * <p>
	 * If no mail could be found for a given mail ID, it is treated as a no-op.
	 * <p>
	 * The underlying mailing system needs to support some kind of
	 * user-definable flags to support this method. Otherwise this method should
	 * be left unchanged with an empty body.
	 * <p>
	 * The color labels are user flags with the common prefix <code>"cl_"</code>
	 * and its numeric color code appended (currently numbers 0 to 10).
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
	public void updateMessageColorLabel(final String folder, final long[] mailIds, final int colorLabel)
			throws MailException {
	}

	/**
	 * Updates the system flags of the messages specified by given mail IDs
	 * located in given folder. If parameter <code>set</code> is
	 * <code>true</code> the affected flags denoted by <code>flags</code>
	 * are added; otherwise removed.
	 * <p>
	 * If no mail could be found for a given mail ID, it is treated as a no-op.
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
	 * <p>
	 * Moreover this routine checks for any spam related actions; meaning the
	 * {@link MailMessage#FLAG_SPAM} shall be enabled/disabled. Thus the
	 * {@link SpamHandler#handleSpam(String, long[], boolean, MailAccess)}/{@link SpamHandler#handleHam(String, long[], boolean, MailAccess)}
	 * methods needs to be executed.
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
	public abstract void updateMessageFlags(String folder, long[] mailIds, int flags, boolean set) throws MailException;

}
