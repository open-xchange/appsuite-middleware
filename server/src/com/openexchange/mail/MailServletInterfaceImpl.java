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

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;

import com.openexchange.cache.OXCachingException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SearchUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link MailServletInterfaceImpl} - The mail servlet interface implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
final class MailServletInterfaceImpl extends MailServletInterface {

	private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

	private static final MailField[] FIELDS_ID_INFO = new MailField[] { MailField.ID, MailField.FOLDER_ID };

	private static final String INBOX_ID = "INBOX";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailServletInterfaceImpl.class);

	/*
	 * ++++++++++++++ Fields ++++++++++++++
	 */

	private Context ctx;

	private boolean init;

	private MailConfig mailConfig;

	private MailAccess<?, ?> mailAccess;

	private final Session session;

	private final UserSettingMail usm;

	/**
	 * No direct instantiation
	 * 
	 * @throws MailException
	 *             If user has no mail access or properties cannot be
	 *             successfully loaded
	 */
	protected MailServletInterfaceImpl(final Session session) throws MailException {
		super();
		try {
			this.ctx = ContextStorage.getStorageContext(session.getContextId());
		} catch (final ContextException e) {
			throw new MailException(e);
		}
		if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx).hasWebMail()) {
			throw new MailException(MailException.Code.NO_MAIL_ACCESS);
		} else if (/* IMAPProperties.noAdminMailbox() && */session.getUserId() == ctx.getMailadmin()) {
			throw new MailException(MailException.Code.ACCOUNT_DOES_NOT_EXIST, Integer.valueOf(ctx.getContextId()));
		}
		this.session = session;
		usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
	}

	@Override
	public void checkDefaultFolders(final String[] defaultFolderNames) throws MailException {
		initConnection();
		mailAccess.getFolderStorage().checkDefaultFolders();
	}

	@Override
	public boolean clearFolder(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		mailAccess.getFolderStorage().clearFolder(fullname);
		try {
			/*
			 * Update message cache
			 */
			MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return true;
	}

	@Override
	public void close(final boolean putIntoCache) throws MailException {
		try {
			if (mailAccess != null) {
				mailAccess.close(putIntoCache);
			}
		} finally {
			mailAccess = null;
			init = false;
		}
	}

	private static final int SPAM_HAM = -1;

	private static final int SPAM_NOOP = 0;

	private static final int SPAM_SPAM = 1;

	@Override
	public long[] copyMessages(final String sourceFolder, final String destFolder, final long[] msgUIDs,
			final boolean move) throws MailException {
		initConnection();
		final String sourceFullname = prepareMailFolderParam(sourceFolder);
		final String destFullname = prepareMailFolderParam(destFolder);
		/*
		 * Check for spam action; meaning a move/copy from/to spam folder
		 */
		final String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
		final int spamAction;
		if (usm.isSpamEnabled()) {
			spamAction = spamFullname.equals(sourceFullname) ? SPAM_HAM
					: (spamFullname.equals(destFullname) ? SPAM_SPAM : SPAM_NOOP);
		} else {
			spamAction = SPAM_NOOP;
		}
		if (spamAction != SPAM_NOOP) {
			final boolean locatedInSpamFolder = SPAM_HAM == spamAction || spamFullname.equals(sourceFullname);
			if (spamAction == SPAM_SPAM) {
				if (!locatedInSpamFolder) {
					/*
					 * Handle spam
					 */
					SpamHandlerRegistry.getSpamHandlerBySession(session).handleSpam(sourceFullname, msgUIDs, false,
							mailAccess);
				}
			} else {
				if (locatedInSpamFolder) {
					/*
					 * Handle ham.
					 */
					SpamHandlerRegistry.getSpamHandlerBySession(session).handleHam(sourceFullname, msgUIDs, false,
							mailAccess);
				}
			}
		}
		final long[] maildIds;
		if (move) {
			maildIds = mailAccess.getMessageStorage().moveMessages(sourceFullname, destFullname, msgUIDs, false);
		} else {
			maildIds = mailAccess.getMessageStorage().copyMessages(sourceFullname, destFullname, msgUIDs, false);
		}
		try {
			/*
			 * Update message cache
			 */
			MailMessageCache.getInstance().removeFolderMessages(sourceFullname, session.getUserId(), ctx);
			MailMessageCache.getInstance().removeFolderMessages(destFullname, session.getUserId(), ctx);
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return maildIds;
	}

	@Override
	public String deleteFolder(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return prepareFullname(mailAccess.getFolderStorage().deleteFolder(fullname, false), getSeparator());
	}

	@Override
	public boolean deleteMessages(final String folder, final long[] msgUIDs, final boolean hardDelete)
			throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final boolean retval = mailAccess.getMessageStorage().deleteMessages(fullname, msgUIDs, hardDelete);
		try {
			/*
			 * Update message cache
			 */
			MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return retval;
	}

	@Override
	public int[] getAllMessageCount(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final MailFolder f = mailAccess.getFolderStorage().getFolder(fullname);
		return new int[] { f.getMessageCount(), f.getNewMessageCount(), f.getUnreadMessageCount(),
				f.getDeletedMessageCount() };
	}

	@Override
	public SearchIterator<?> getAllMessages(final String folder, final int sortCol, final int order, final int[] fields)
			throws MailException {
		return getMessages(folder, null, sortCol, order, null, null, false, fields);
	}

	@Override
	public SearchIterator<?> getAllThreadedMessages(final String folder, final int[] fields) throws MailException {
		return getThreadedMessages(folder, null, null, null, false, fields);
	}

	@Override
	public SearchIterator<?> getChildFolders(final String parentFolder, final boolean all) throws MailException {
		initConnection();
		final String parentFullname = prepareMailFolderParam(parentFolder);
		return SearchIteratorAdapter.createArrayIterator(mailAccess.getFolderStorage().getSubfolders(parentFullname,
				all));
	}

	@Override
	public String getConfirmedHamFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_HAM), getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getConfirmedHamFolder(), getSeparator());
	}

	@Override
	public String getConfirmedSpamFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_SPAM), getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getConfirmedSpamFolder(), getSeparator());
	}

	private String getDefaultMailFolder(final int index) {
		final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
		return arr == null ? null : arr[index];
	}

	@Override
	public int getDeletedMessageCount(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getFolderStorage().getFolder(fullname).getDeletedMessageCount();
	}

	@Override
	public String getDraftsFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_DRAFTS), getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getDraftsFolder(), getSeparator());
	}

	@Override
	public MailFolder getFolder(final String folder, final boolean checkFolder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getFolderStorage().getFolder(fullname);
	}

	@Override
	public MailMessage getForwardMessageForDisplay(final String folder, final long[] fowardMsgUIDs)
			throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final MailMessage[] originalMails = mailAccess.getMessageStorage().getMessages(fullname, fowardMsgUIDs,
				FIELDS_FULL);
		return mailAccess.getLogicTools().getFowardMessage(originalMails);
	}

	@Override
	public String getInboxFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(INBOX_ID, getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getFolder(INBOX_ID).getFullname(), getSeparator());
	}

	@Override
	public MailConfig getMailConfig() throws MailException {
		return mailConfig;
	}

	private static final MailListField[] FIELDS_FLAGS = new MailListField[] { MailListField.FLAGS };

	private static final transient Object[] ARGS_FLAG_SEEN_SET = new Object[] { Integer.valueOf(MailMessage.FLAG_SEEN) };

	private static final transient Object[] ARGS_FLAG_SEEN_UNSET = new Object[] { Integer.valueOf(-1
			* MailMessage.FLAG_SEEN) };

	@Override
	public MailMessage getMessage(final String folder, final long msgUID) throws MailException {
		initConnection();
		if (MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
			throw new MailException(MailException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, MailFolder.DEFAULT_FOLDER_ID);
		}
		final String fullname = prepareMailFolderParam(folder);
		final MailMessage mail = mailAccess.getMessageStorage().getMessage(fullname, msgUID, true);
		/*
		 * Update cache since \Seen flag is possibly changed
		 */
		try {
			if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
				/*
				 * Update cache entry
				 */
				MailMessageCache.getInstance().updateCachedMessages(new long[] { mail.getMailId() }, fullname,
						session.getUserId(), ctx, FIELDS_FLAGS,
						mail.isSeen() ? ARGS_FLAG_SEEN_SET : ARGS_FLAG_SEEN_UNSET);

			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return mail;
	}

	@Override
	public MailPart getMessageAttachment(final String folder, final long msgUID, final String attachmentPosition,
			final boolean displayVersion) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getMessageStorage().getAttachment(fullname, msgUID, attachmentPosition);
	}

	@Override
	public int getMessageCount(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getFolderStorage().getFolder(fullname).getMessageCount();
	}

	@Override
	public MailPart getMessageImage(final String folder, final long msgUID, final String cid) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getMessageStorage().getImageAttachment(fullname, msgUID, cid);
	}

	@Override
	public MailMessage[] getMessageList(final String folder, final long[] uids, final int[] fields)
			throws MailException {
		/*
		 * Although message cache is only used within mail implementation, we
		 * have to examine if cache already holds desired messages. If the cache
		 * holds the desired messages no connection has to be
		 * fetched/established. This avoids a lot of overhead.
		 */
		final String fullname = prepareMailFolderParam(folder);
		try {
			final MailMessage[] mails = MailMessageCache.getInstance().getMessages(uids, fullname, session.getUserId(),
					ctx);
			if (null != mails) {
				return mails;
			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		initConnection();
		final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fullname, uids,
				MailField.toFields(MailListField.getFields(fields)));
		try {
			if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
				MailMessageCache.getInstance().putMessages(mails, session.getUserId(), ctx);
			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return mails;
	}

	@Override
	public SearchIterator<?> getMessages(final String folder, final int[] fromToIndices, final int sortCol,
			final int order, final int[] searchCols, final String[] searchPatterns,
			final boolean linkSearchTermsWithOR, final int[] fields) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final SearchTerm<?> searchTerm = searchCols == null || searchCols.length == 0 ? null : SearchUtility
				.parseFields(searchCols, searchPatterns, linkSearchTermsWithOR);
		/*
		 * Identify and sort messages according to search term and sort criteria
		 * while only fetching their IDs
		 */
		MailMessage[] mails = mailAccess.getMessageStorage().searchMessages(fullname,
				null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
				MailListField.getField(sortCol), OrderDirection.getOrderDirection(order), searchTerm, FIELDS_ID_INFO);
		if ((mails == null) || (mails.length == 0)) {
			return SearchIterator.EMPTY_ITERATOR;
		}
		final MailField[] useFields;
		final boolean onlyFolderAndID;
		if (mails.length < MailProperties.getInstance().getMailFetchLimit()) {
			/*
			 * Selection fits into cache: Prepare for caching
			 */
			useFields = com.openexchange.mail.mime.utils.MIMEStorageUtility.getCacheFieldsArray();
			onlyFolderAndID = false;
		} else {
			useFields = MailField.toFields(MailListField.getFields(fields));
			onlyFolderAndID = onlyFolderAndID(useFields);
		}
		if (!onlyFolderAndID) {
			/*
			 * Extract IDs
			 */
			final long[] mailIds = new long[mails.length];
			for (int i = 0; i < mailIds.length; i++) {
				mailIds[i] = mails[i].getMailId();
			}
			/*
			 * Fetch identified messages by their IDs and pre-fill them
			 * according to specified fields
			 */
			mails = mailAccess.getMessageStorage().getMessages(fullname, mailIds, useFields);
		}
		try {
			/*
			 * Remove old user cache entries
			 */
			MailMessageCache.getInstance().removeUserMessages(session.getUserId(), ctx);
			if ((mails != null) && (mails.length > 0)
					&& (mails.length < MailProperties.getInstance().getMailFetchLimit())) {
				/*
				 * ... and put new ones
				 */
				MailMessageCache.getInstance().putMessages(mails, session.getUserId(), ctx);
			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return SearchIteratorAdapter.createArrayIterator(mails);
	}

	/**
	 * Checks if specified fields only consist of mail ID and folder ID
	 * 
	 * @param fields
	 *            The fields to check
	 * @return <code>true</code> if specified fields only consist of mail ID
	 *         and folder ID; otherwise <code>false</code>
	 */
	private static boolean onlyFolderAndID(final MailField[] fields) {
		if (fields.length != 2) {
			return false;
		}
		int i = 0;
		for (final MailField field : fields) {
			if (MailField.ID.equals(field)) {
				i |= 1;
			} else if (MailField.FOLDER_ID.equals(field)) {
				i |= 2;
			}
		}
		return (i == 3);
	}

	@Override
	public int getNewMessageCount(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getFolderStorage().getFolder(fullname).getNewMessageCount();
	}

	@Override
	public SearchIterator<?> getNewMessages(final String folder, final int sortCol, final int order,
			final int[] fields, final int limit) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return SearchIteratorAdapter.createArrayIterator(mailAccess.getMessageStorage().getUnreadMessages(fullname,
				MailListField.getField(sortCol), OrderDirection.getOrderDirection(order),
				MailField.toFields(MailListField.getFields(fields)), limit));
	}

	@Override
	public SearchIterator<?> getPathToDefaultFolder(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return SearchIteratorAdapter.createArrayIterator(mailAccess.getFolderStorage().getPath2DefaultFolder(fullname));
	}

	@Override
	public long[] getQuota() throws MailException {
		initConnection();
		return mailAccess.getFolderStorage().getQuota(INBOX_ID).toLongArray();
	}

	@Override
	public long getQuotaLimit() throws MailException {
		initConnection();
		return mailAccess.getFolderStorage().getQuota(INBOX_ID).limit;
	}

	@Override
	public long getQuotaUsage() throws MailException {
		initConnection();
		return mailAccess.getFolderStorage().getQuota(INBOX_ID).usage;
	}

	@Override
	public MailMessage getReplyMessageForDisplay(final String folder, final long replyMsgUID, final boolean replyToAll)
			throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final MailMessage originalMail = mailAccess.getMessageStorage().getMessage(fullname, replyMsgUID, false);
		return mailAccess.getLogicTools().getReplyMessage(originalMail, replyToAll);
	}

	@Override
	public SearchIterator<?> getRootFolders() throws MailException {
		initConnection();
		return SearchIteratorAdapter.createArrayIterator(new MailFolder[] { mailAccess.getFolderStorage()
				.getRootFolder() });
	}

	@Override
	public String getSentFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_SENT), getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getSentFolder(), getSeparator());
	}

	private char getSeparator() {
		final Object c = session.getParameter(MailSessionParameterNames.PARAM_SEPARATOR);
		if (null == c) {
			return MailConfig.getDefaultSeparator();
		}
		return ((Character) c).charValue();
	}

	@Override
	public String getSpamFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_SPAM), getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getSpamFolder(), getSeparator());
	}

	@Override
	public SearchIterator<?> getThreadedMessages(final String folder, final int[] fromToIndices,
			final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR,
			final int[] fields) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final SearchTerm<?> searchTerm = searchCols == null || searchCols.length == 0 ? null : SearchUtility
				.parseFields(searchCols, searchPatterns, linkSearchTermsWithOR);
		/*
		 * Identify and thread-sort messages according to search term while only
		 * fetching their IDs
		 */
		MailMessage[] mails = mailAccess.getMessageStorage().getThreadSortedMessages(fullname,
				fromToIndices == null ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
				searchTerm, FIELDS_ID_INFO);
		if ((mails == null) || (mails.length == 0)) {
			return SearchIterator.EMPTY_ITERATOR;
		}
		final MailField[] useFields;
		final boolean onlyFolderAndID;
		if (mails.length < MailProperties.getInstance().getMailFetchLimit()) {
			/*
			 * Selection fits into cache: Prepare for caching
			 */
			useFields = com.openexchange.mail.mime.utils.MIMEStorageUtility.getCacheFieldsArray();
			onlyFolderAndID = false;
		} else {
			useFields = MailField.toFields(MailListField.getFields(fields));
			onlyFolderAndID = onlyFolderAndID(useFields);
		}
		if (!onlyFolderAndID) {
			/*
			 * Extract IDs
			 */
			final long[] mailIds = new long[mails.length];
			for (int i = 0; i < mailIds.length; i++) {
				mailIds[i] = mails[i].getMailId();
			}
			/*
			 * Fetch identified messages by their IDs and pre-fill them
			 * according to specified fields
			 */
			mails = mailAccess.getMessageStorage().getMessages(fullname, mailIds, useFields);
		}
		try {
			/*
			 * Remove old user cache entries
			 */
			MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
			if ((mails != null) && (mails.length > 0)
					&& (mails.length < MailProperties.getInstance().getMailFetchLimit())) {
				/*
				 * ... and put new ones
				 */
				MailMessageCache.getInstance().putMessages(mails, session.getUserId(), ctx);
			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return SearchIteratorAdapter.createArrayIterator(mails);
	}

	@Override
	public String getTrashFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_TRASH), getSeparator());
		}
		initConnection();
		return prepareFullname(mailAccess.getFolderStorage().getTrashFolder(), getSeparator());
	}

	@Override
	public int getUnreadMessageCount(final String folder) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		return mailAccess.getFolderStorage().getFolder(fullname).getUnreadMessageCount();
	}

	private void initConnection() throws MailException {
		if (init) {
			return;
		}
		/*
		 * Fetch a mail connection (either from cache or a new instance)
		 */
		mailAccess = MailAccess.getInstance(session);
		if (!mailAccess.isConnected()) {
			/*
			 * Get new mail configuration
			 */
			final long start = System.currentTimeMillis();
			try {
				mailAccess.connect();
				MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				MailServletInterface.mailInterfaceMonitor.changeNumSuccessfulLogins(true);
			} catch (final MailException e) {
				if (e.getDetailNumber() == 2) {
					MailServletInterface.mailInterfaceMonitor.changeNumFailedLogins(true);
				}
				throw e;
			}
		}
		mailConfig = mailAccess.getMailConfig();
		mailAccess.getFolderStorage().checkDefaultFolders();
		init = true;
	}

	private boolean isDefaultFoldersChecked() {
		final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
		return (b != null) && b.booleanValue();
	}

	@Override
	public String saveDraft(final ComposedMailMessage draftMail) throws MailException {
		initConnection();
		if (draftMail.getMsgref() != null) {
			final MailPath path = new MailPath(draftMail.getMsgref());
			draftMail.setReferencedMail(mailAccess.getMessageStorage().getMessage(path.getFolder(), path.getUid(),
					false));
		}
		final String draftsFullname = prepareMailFolderParam(mailAccess.getFolderStorage().getDraftsFolder());
		return mailAccess.getMessageStorage().saveDraft(draftsFullname, draftMail).getMailPath().toString();
	}

	@Override
	public String saveFolder(final MailFolderDescription mailFolder) throws MailException {
		initConnection();
		if (!mailFolder.containsExists() && !mailFolder.containsFullname()) {
			throw new MailException(MailException.Code.INSUFFICIENT_FOLDER_ATTR);
		}
		if ((mailFolder.containsExists() && mailFolder.exists())
				|| ((mailFolder.getFullname() != null) && mailAccess.getFolderStorage()
						.exists(mailFolder.getFullname()))) {
			/*
			 * Update
			 */
			String fullname = prepareMailFolderParam(mailFolder.getFullname());
			final char separator = mailFolder.getSeparator();
			final String oldParent;
			final String oldName;
			{
				final int pos = fullname.lastIndexOf(separator);
				if (pos == -1) {
					oldParent = "";
					oldName = fullname;
				} else {
					oldParent = fullname.substring(0, pos);
					oldName = fullname.substring(pos + 1);
				}
			}
			boolean movePerformed = false;
			/*
			 * Check if a move shall be performed
			 */
			if (mailFolder.containsParentFullname()) {
				final String newParent = prepareMailFolderParam(mailFolder.getParentFullname());
				final StringBuilder newFullname = new StringBuilder(newParent).append(mailFolder.getSeparator());
				if (mailFolder.containsName()) {
					newFullname.append(mailFolder.getName());
				} else {
					newFullname.append(oldName);
				}
				if (!newParent.equals(oldParent)) { // move & rename
					fullname = mailAccess.getFolderStorage().moveFolder(fullname, newFullname.toString());
					movePerformed = true;
				}
			}
			/*
			 * Check if a rename shall be performed
			 */
			if (!movePerformed && mailFolder.containsName()) {
				final String newName = mailFolder.getName();
				if (!newName.equals(oldName)) { // rename
					fullname = mailAccess.getFolderStorage().renameFolder(fullname, newName);
				}
			}
			/*
			 * Handle update of permission or subscription
			 */
			return prepareFullname(mailAccess.getFolderStorage().updateFolder(fullname, mailFolder), getSeparator());
		}
		/*
		 * Insert
		 */
		return prepareFullname(mailAccess.getFolderStorage().createFolder(mailFolder), getSeparator());
	}

	@Override
	public String sendMessage(final ComposedMailMessage composedMail, final ComposeType type) throws MailException {
		initConnection();
		final MailTransport transport = MailTransport.getInstance(session);
		final MailMessage sentMail;
		try {
			final MailPath[] paths;
			if (composedMail.getMsgref() != null) {
				paths = MailPath.getMailPaths(composedMail.getMsgref());
				if (ComposeType.REPLY.equals(type) && (paths.length > 1)) {
					/*
					 * No reply on multiple messages
					 */
					throw new MailException(MailException.Code.NO_MULTIPLE_REPLY);
				}
				final MailMessage[] referencedMails = new MailMessage[paths.length];
				for (int i = 0; i < paths.length; i++) {
					referencedMails[i] = mailAccess.getMessageStorage().getMessage(paths[i].getFolder(),
							paths[i].getUid(), false);
				}
				composedMail.setReferencedMails(referencedMails);
			} else {
				paths = null;
			}
			sentMail = transport.sendMailMessage(composedMail, type);
			if ((paths != null) && ComposeType.REPLY.equals(type)) {
				/*
				 * Mark referenced mail as answered
				 */
				mailAccess.getMessageStorage().updateMessageFlags(paths[0].getFolder(),
						new long[] { paths[0].getUid() }, MailMessage.FLAG_ANSWERED, true);
			}
			if (UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx)
					.isNoCopyIntoStandardSentFolder()) {
				/*
				 * No copy in sent folder
				 */
				return null;
			}
			/*
			 * Append to Sent folder
			 */
			final long start = System.currentTimeMillis();
			final String sentFullname = prepareMailFolderParam(mailAccess.getFolderStorage().getSentFolder());
			final long[] uidArr;
			try {
				uidArr = mailAccess.getMessageStorage().appendMessages(sentFullname, new MailMessage[] { sentMail });
				try {
					/*
					 * Update cache
					 */
					MailMessageCache.getInstance().removeFolderMessages(sentFullname, session.getUserId(), ctx);
				} catch (final OXCachingException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			} catch (final MailException e) {
				if (e.getMessage().indexOf("quota") != -1) {
					throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED_QUOTA, e, new Object[0]);
				}
				throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED, e, new Object[0]);
			}
			if ((uidArr != null) && (uidArr[0] != -1)) {
				/*
				 * Mark appended sent mail as seen
				 */
				mailAccess.getMessageStorage().updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
			}
			final MailPath retval = new MailPath(sentFullname, uidArr[0]);
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(128).append("Mail copy (").append(retval.toString()).append(
						") appended in ").append(System.currentTimeMillis() - start).append("msec").toString());
			}
			return retval.toString();
		} finally {
			transport.close();
		}
	}

	@Override
	public void sendReceiptAck(final String folder, final long msgUID, final String fromAddr) throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		final MailTransport transport = MailTransport.getInstance(session);
		try {
			transport.sendReceiptAck(mailAccess.getMessageStorage().getMessage(fullname, msgUID, false), fromAddr);
		} finally {
			transport.close();
		}
	}

	private static final MailListField[] FIELDS_COLOR_LABEL = new MailListField[] { MailListField.COLOR_LABEL };

	@Override
	public void updateMessageColorLabel(final String folder, final long[] msgUID, final int newColorLabel)
			throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		mailAccess.getMessageStorage().updateMessageColorLabel(fullname, msgUID, newColorLabel);
		try {
			if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
				/*
				 * Update cache entries
				 */
				MailMessageCache.getInstance().updateCachedMessages(msgUID, fullname, session.getUserId(), ctx,
						FIELDS_COLOR_LABEL, new Object[] { Integer.valueOf(newColorLabel) });
			}
		} catch (final OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void updateMessageFlags(final String folder, final long[] msgUID, final int flagBits, final boolean flagVal)
			throws MailException {
		initConnection();
		final String fullname = prepareMailFolderParam(folder);
		mailAccess.getMessageStorage().updateMessageFlags(fullname, msgUID, flagBits, flagVal);
		if (usm.isSpamEnabled() && ((flagBits & MailMessage.FLAG_SPAM) > 0)) {
			/*
			 * Remove from cache
			 */
			try {
				if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
					MailMessageCache.getInstance().removeMessages(msgUID, fullname, session.getUserId(), ctx);

				}
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		} else {
			try {
				if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
					/*
					 * Update cache entries
					 */
					MailMessageCache.getInstance().updateCachedMessages(msgUID, fullname, session.getUserId(), ctx,
							FIELDS_FLAGS, new Object[] { Integer.valueOf(flagVal ? flagBits : (flagBits * -1)) });
				}
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}
}
