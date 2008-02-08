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

import com.openexchange.api2.MailInterfaceMonitor;
import com.openexchange.cache.OXCachingException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link MailInterfaceImpl} - The mail interface implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailInterfaceImpl extends MailInterface {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailInterfaceImpl.class);

	public static final MailInterfaceMonitor mailInterfaceMonitor = new MailInterfaceMonitor();

	/*
	 * Fields
	 */
	private MailConnection<?, ?, ?> mailConnection;

	private MailConfig mailConfig;

	private Session session;

	private Context ctx;

	private boolean init;

	/**
	 * No direct instantiation
	 * 
	 * @throws MailException
	 *             If user has no mail access or properties cannot be
	 *             successfully loaded
	 */
	protected MailInterfaceImpl(final Session session) throws MailException {
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
	}

	private void initConnection() throws MailException {
		if (init) {
			return;
		}
		/*
		 * Fetch a mail connection (either from cache or a new instance)
		 */
		mailConnection = MailConnection.getInstance(session);
		if (!mailConnection.isConnected()) {
			/*
			 * Get new mail configuration
			 */
			getMailConfig(true);
			if (mailConfig.getError() != null) {
				throw new MailException(mailConfig.getError());
			}
			final long start = System.currentTimeMillis();
			try {
				mailConnection.connect(mailConfig);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				mailInterfaceMonitor.changeNumSuccessfulLogins(true);
			} catch (final MailException e) {
				if (e.getDetailNumber() == 2) {
					mailInterfaceMonitor.changeNumFailedLogins(true);
				}
				throw e;
			}
		} else {
			/*
			 * Get mail configuration
			 */
			mailConfig = mailConnection.getMailConfig();
		}
		mailConnection.getFolderStorage().checkDefaultFolders();
		init = true;
	}

	@Override
	public void checkDefaultFolders(final String[] defaultFolderNames) throws MailException {
		initConnection();
		mailConnection.getFolderStorage().checkDefaultFolders();
	}

	@Override
	public boolean clearFolder(final String folder) throws MailException {
		initConnection();
		mailConnection.getFolderStorage().clearFolder(folder);
		return true;
	}

	@Override
	public void close(final boolean putIntoCache) throws MailException {
		try {
			if (mailConnection != null) {
				mailConnection.close(putIntoCache);
			}
		} finally {
			mailConnection = null;
			init = false;
		}
	}

	@Override
	public long[] copyMessages(final String sourceFolder, final String destFolder, final long[] msgUIDs,
			final boolean move) throws MailException {
		initConnection();
		return mailConnection.getMessageStorage().copyMessages(sourceFolder, destFolder, msgUIDs, move, false);
	}

	@Override
	public String deleteFolder(final String folder) throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().deleteFolder(folder);
	}

	@Override
	public boolean deleteMessages(final String folder, final long[] msgUIDs, final boolean hardDelete)
			throws MailException {
		initConnection();
		return mailConnection.getMessageStorage().deleteMessages(folder, msgUIDs, hardDelete);
	}

	@Override
	public int[] getAllMessageCount(final String folder) throws MailException {
		initConnection();
		final MailFolder f = mailConnection.getFolderStorage().getFolder(folder);
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
		return SearchIteratorAdapter.createArrayIterator(mailConnection.getFolderStorage().getSubfolders(parentFolder,
				all));
	}

	private String getDefaultMailFolder(final int index) {
		final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
		return arr == null ? null : arr[index];
	}

	private boolean isDefaultFoldersChecked() {
		final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
		return b != null && b.booleanValue();
	}

	@Override
	public String getConfirmedHamFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_HAM);
		}
		initConnection();
		return mailConnection.getFolderStorage().getConfirmedHamFolder();
	}

	@Override
	public String getConfirmedSpamFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
		}
		initConnection();
		return mailConnection.getFolderStorage().getConfirmedSpamFolder();
	}

	@Override
	public int getDeletedMessageCount(final String folder) throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getFolder(folder).getDeletedMessageCount();
	}

	@Override
	public String getDraftsFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(StorageUtility.INDEX_DRAFTS);
		}
		initConnection();
		return mailConnection.getFolderStorage().getDraftsFolder();
	}

	@Override
	public MailFolder getFolder(final String folder, final boolean checkFolder) throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getFolder(folder);
	}

	@Override
	public MailMessage getForwardMessageForDisplay(final String folder, final long fowardMsgUID) throws MailException {
		initConnection();
		return mailConnection.getLogicTools().getFowardMessage(fowardMsgUID, folder);
	}

	private static final String INBOX_ID = "INBOX";

	@Override
	public String getInboxFolder() throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getFolder(INBOX_ID).getFullname();
	}

	@Override
	public MailMessage getMessage(final String folder, final long msgUID) throws MailException {
		initConnection();
		return mailConnection.getMessageStorage().getMessage(folder, msgUID);
	}

	@Override
	public MailPart getMessageAttachment(final String folder, final long msgUID, final String attachmentPosition,
			final boolean displayVersion) throws MailException {
		initConnection();
		return mailConnection.getMessageStorage().getAttachment(folder, msgUID, attachmentPosition, displayVersion);
	}

	@Override
	public int getMessageCount(final String folder) throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getFolder(folder).getMessageCount();
	}

	@Override
	public MailPart getMessageImage(final String folder, final long msgUID, final String cid) throws MailException {
		initConnection();
		return mailConnection.getMessageStorage().getImageAttachment(folder, msgUID, cid);
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
		final String fullname = StorageUtility.prepareMailFolderParam(folder);
		try {
			final int userId = session.getUserId();
			if (MailMessageCache.getInstance().containsFolderMessages(fullname, userId, ctx)) {
				return MailMessageCache.getInstance().getMessages(uids, fullname, userId, ctx);
			}
		} catch (OXCachingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		initConnection();
		return mailConnection.getMessageStorage()
				.getMessagesByUID(folder, uids, MailListField.getFields(fields), false);
	}

	@Override
	public SearchIterator<?> getMessages(final String folder, final int[] fromToIndices, final int sortCol,
			final int order, final int[] searchCols, final String[] searchPatterns,
			final boolean linkSearchTermsWithOR, final int[] fields) throws MailException {
		initConnection();
		return SearchIteratorAdapter.createArrayIterator(mailConnection.getMessageStorage().getMessages(folder,
				fromToIndices, MailListField.getField(sortCol), OrderDirection.getOrderDirection(order),
				MailListField.getFields(searchCols), searchPatterns, linkSearchTermsWithOR,
				MailListField.getFields(fields)));
	}

	@Override
	public int getNewMessageCount(final String folder) throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getFolder(folder).getNewMessageCount();
	}

	@Override
	public SearchIterator<?> getNewMessages(final String folder, final int sortCol, final int order,
			final int[] fields, final int limit) throws MailException {
		initConnection();
		return SearchIteratorAdapter.createArrayIterator(mailConnection.getMessageStorage().getUnreadMessages(folder,
				MailListField.getField(sortCol), OrderDirection.getOrderDirection(order),
				MailListField.getFields(fields), limit));
	}

	@Override
	public SearchIterator<?> getPathToDefaultFolder(final String folder) throws MailException {
		initConnection();
		return SearchIteratorAdapter.createArrayIterator(mailConnection.getFolderStorage()
				.getPath2DefaultFolder(folder));
	}

	@Override
	public long[] getQuota() throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getQuota(INBOX_ID);
	}

	@Override
	public long getQuotaLimit() throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getQuota(INBOX_ID)[0];
	}

	@Override
	public long getQuotaUsage() throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getQuota(INBOX_ID)[1];
	}

	@Override
	public MailMessage getReplyMessageForDisplay(final String folder, final long replyMsgUID, final boolean replyToAll)
			throws MailException {
		initConnection();
		return mailConnection.getLogicTools().getReplyMessage(replyMsgUID, folder, replyToAll);
	}

	@Override
	public SearchIterator<?> getRootFolders() throws MailException {
		initConnection();
		return SearchIteratorAdapter.createArrayIterator(new MailFolder[] { mailConnection.getFolderStorage()
				.getRootFolder() });
	}

	@Override
	public String getSentFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(StorageUtility.INDEX_SENT);
		}
		initConnection();
		return mailConnection.getFolderStorage().getSentFolder();
	}

	@Override
	public String getSpamFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(StorageUtility.INDEX_SPAM);
		}
		initConnection();
		return mailConnection.getFolderStorage().getSpamFolder();
	}

	@Override
	public SearchIterator<?> getThreadedMessages(final String folder, final int[] fromToIndices,
			final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR,
			final int[] fields) throws MailException {
		initConnection();
		return SearchIteratorAdapter.createArrayIterator(mailConnection.getMessageStorage().getThreadSortedMessages(
				folder, fromToIndices, MailListField.getFields(searchCols), searchPatterns, linkSearchTermsWithOR,
				MailListField.getFields(fields)));
	}

	@Override
	public String getTrashFolder() throws MailException {
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(StorageUtility.INDEX_TRASH);
		}
		initConnection();
		return mailConnection.getFolderStorage().getTrashFolder();
	}

	@Override
	public int getUnreadMessageCount(final String folder) throws MailException {
		initConnection();
		return mailConnection.getFolderStorage().getFolder(folder).getUnreadMessageCount();
	}

	@Override
	public String saveFolder(final MailFolder mailFolder) throws MailException {
		initConnection();
		if (mailFolder.exists()
				|| (mailFolder.getFullname() != null && mailConnection.getFolderStorage().exists(
						mailFolder.getFullname()))) {
			/*
			 * Update
			 */
			return mailConnection.getFolderStorage().updateFolder(mailFolder.getFullname(), mailFolder);
		}
		/*
		 * Insert
		 */
		return mailConnection.getFolderStorage().createFolder(mailFolder);
	}

	@Override
	public String saveDraft(final ComposedMailMessage draftMail) throws MailException {
		initConnection();
		if (draftMail.getMsgref() != null) {
			final MailPath path = new MailPath(draftMail.getMsgref());
			draftMail.setReferencedMail(mailConnection.getMessageStorage().getMessage(path.getFolder(),
					path.getUid()));
		}
		return mailConnection.getMessageStorage().saveDraft(draftMail).getMailPath().toString();
	}

	@Override
	public String sendMessage(final ComposedMailMessage composedMail, final ComposeType type) throws MailException {
		initConnection();
		final MailTransport transport = MailTransport.getInstance(session);
		final MailMessage sentMail;
		try {
			final MailPath path;
			if (composedMail.getMsgref() != null) {
				path = new MailPath(composedMail.getMsgref());
				composedMail.setReferencedMail(mailConnection.getMessageStorage().getMessage(path.getFolder(),
						path.getUid()));
			} else {
				path = null;
			}
			sentMail = transport.sendMailMessage(composedMail, type);
			if (path != null && ComposeType.REPLY.equals(type)) {
				/*
				 * Mark referenced mail as answered
				 */
				mailConnection.getMessageStorage().updateMessageFlags(path.getFolder(), new long[] { path.getUid() },
						MailMessage.FLAG_ANSWERED, true);
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
			final String sentFullname = mailConnection.getFolderStorage().getSentFolder();
			final long[] uidArr;
			try {
				uidArr = mailConnection.getMessageStorage()
						.appendMessages(sentFullname, new MailMessage[] { sentMail });
			} catch (final MailException e) {
				if (e.getMessage().indexOf("quota") != -1) {
					throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED_QUOTA, e, new Object[0]);
				}
				throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED, e, new Object[0]);
			}
			if (uidArr != null && uidArr[0] != -1) {
				/*
				 * Mark appended sent mail as seen
				 */
				mailConnection.getMessageStorage()
						.updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
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
		final MailTransport transport = MailTransport.getInstance(session);
		try {
			transport.sendReceiptAck(mailConnection.getMessageStorage().getMessage(folder, msgUID), fromAddr);
		} finally {
			transport.close();
		}
	}

	@Override
	public void updateMessageColorLabel(final String folder, final long[] msgUID, final int newColorLabel)
			throws MailException {
		initConnection();
		mailConnection.getMessageStorage().updateMessageColorLabel(folder, msgUID, newColorLabel);
	}

	@Override
	public void updateMessageFlags(final String folder, final long[] msgUID, final int flagBits, final boolean flagVal)
			throws MailException {
		initConnection();
		mailConnection.getMessageStorage().updateMessageFlags(folder, msgUID, flagBits, flagVal);
	}

	@Override
	public MailConfig getMailConfig() throws MailException {
		return getMailConfig(false);
		// initConnection();
		// return mailConnection.getMailConfig();
	}

	private MailConfig getMailConfig(final boolean create) throws MailException {
		if (create) {
			mailConfig = MailProvider.getInstance().getMailConfig(session);
		}
		return mailConfig;
	}

}
