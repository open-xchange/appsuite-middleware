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

package com.openexchange.imap;

import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;
import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Quota.Resource;
import javax.mail.internet.MimeMessage;

import com.openexchange.groupware.container.CommonObject;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailLogicTools;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.mime.processing.MimeReply;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.versit.VersitUtility;
import com.openexchange.sessiond.impl.SessionObject;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPLogicTools}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPLogicTools extends IMAPFolderWorker implements MailLogicTools {

	/*
	 * Static constants
	 */
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPLogicTools.class);

	/**
	 * Constructor
	 * 
	 * @throws MailException
	 */
	public IMAPLogicTools(final IMAPStore imapStore, final IMAPConnection imapMailConnection,
			final SessionObject session) throws MailException {
		super(imapStore, imapMailConnection, session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailTools#getFowardMessage(long,
	 *      java.lang.String)
	 */
	public MailMessage getFowardMessage(final long originalUID, final String folder) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			/*
			 * Fetch original message
			 */
			final MailMessage forwardMail = MimeForward.getFowardMail((MimeMessage) imapFolder
					.getMessageByUID(originalUID), session);
			forwardMail.setMsgref(MailPath.getMailPath(fullname, originalUID));
			return forwardMail;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		} catch (final IMAPException e) {
			throw e;
		} catch (final MailException e) {
			throw new IMAPException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailTools#getReplyMessage(long,
	 *      java.lang.String, boolean)
	 */
	public MailMessage getReplyMessage(final long originalUID, final String folder, final boolean replyAll)
			throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			/*
			 * Fetch original message
			 */
			final MailMessage replyMail = MimeReply.getReplyMail((MimeMessage) imapFolder.getMessageByUID(originalUID),
					replyAll, session, imapConnection.getSession());
			replyMail.setMsgref(MailPath.getMailPath(fullname, originalUID));
			return replyMail;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		} catch (final IMAPException e) {
			throw e;
		} catch (final MailException e) {
			throw new IMAPException(e);
		}
	}

	private static final String QUOTA_RES_STORAGE = "STORAGE";

	public long[] getQuota(final String folder) throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, folder == null ? STR_INBOX : prepareMailFolderParam(folder),
					Folder.READ_ONLY);
			if (!imapConfig.getImapCapabilities().hasQuota()) {
				return new long[] { UNLIMITED_QUOTA, UNLIMITED_QUOTA };
			}
			final Quota[] folderQuota;
			try {
				final long start = System.currentTimeMillis();
				folderQuota = imapFolder.getQuota();
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			} catch (final MessagingException mexc) {
				if (mexc.getNextException() instanceof ParsingException) {
					return new long[] { UNLIMITED_QUOTA, UNLIMITED_QUOTA };
				}
				throw mexc;
			}
			if (folderQuota.length == 0) {
				return new long[] { UNLIMITED_QUOTA, UNLIMITED_QUOTA };
			}
			final Quota.Resource[] resources = folderQuota[0].resources;
			if (resources.length == 0) {
				return new long[] { UNLIMITED_QUOTA, UNLIMITED_QUOTA };
			}
			Resource storageResource = null;
			for (int i = 0; i < resources.length; i++) {
				if (QUOTA_RES_STORAGE.equalsIgnoreCase(resources[i].name)) {
					storageResource = resources[i];
				}
			}
			if (null == storageResource) {
				/*
				 * No storage limitations
				 */
				if (LOG.isWarnEnabled()) {
					logUnsupportedQuotaResources(resources, 0);
				}
				return new long[] { UNLIMITED_QUOTA, UNLIMITED_QUOTA };
			}
			if (resources.length > 1 && LOG.isWarnEnabled()) {
				logUnsupportedQuotaResources(resources, 1);
			}
			return new long[] { storageResource.limit, storageResource.usage };
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	/**
	 * Logs unsupported QUOTA resources
	 * 
	 * @param resources
	 *            The QUOTA resources
	 */
	private static void logUnsupportedQuotaResources(final Quota.Resource[] resources, final int start) {
		final StringBuilder sb = new StringBuilder(128)
				.append("Unsupported QUOTA resource(s) [<name> (<usage>/<limit>]:\n");
		sb.append(resources[start].name).append(" (").append(resources[start].usage).append('/').append(
				resources[start].limit).append(')');
		for (int i = start + 1; i < resources.length; i++) {
			sb.append(", ").append(resources[i].name).append(" (").append(resources[i].usage).append('/').append(
					resources[i].limit).append(')');

		}
		LOG.warn(sb.toString());
	}

	/**
	 * Get the QUOTA resource with the highest usage-per-limitation value
	 * 
	 * @param resources
	 *            The QUOTA resources
	 * @return The QUOTA resource with the highest usage to limitation relation
	 */
	private static Resource getMaxUsageResource(final Quota.Resource[] resources) {
		final Resource maxUsageResource;
		{
			int index = 0;
			long maxUsage = resources[0].usage / resources[0].limit;
			for (int i = 1; i < resources.length; i++) {
				final long tmp = resources[i].usage / resources[i].limit;
				if (tmp > maxUsage) {
					maxUsage = tmp;
					index = i;
				}
			}
			maxUsageResource = resources[index];
		}
		return maxUsageResource;
	}

	public CommonObject[] saveVersitAttachment(final String folder, final long msgUID, final String sequenceId)
			throws MailException {
		try {
			// imapFolder = setAndOpenFolder(imapFolder,
			// prepareMailFolderParam(folder), Folder.READ_ONLY);
			final MailPartHandler handler = new MailPartHandler(sequenceId);
			new MailMessageParser().parseMailMessage(imapConnection.getMessageStorage().getMessage(folder, msgUID),
					handler);
			final MailPart versitPart = handler.getMailPart();
			if (versitPart == null) {
				throw new IMAPException(IMAPException.Code.NO_ATTACHMENT_FOUND, sequenceId);
			}
			/*
			 * Save dependent on content type
			 */
			final List<CommonObject> retvalList = new ArrayList<CommonObject>();
			if (versitPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_X_VCARD)
					|| versitPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_VCARD)) {
				/*
				 * Save VCard
				 */
				VersitUtility.saveVCard(versitPart, retvalList, session);
			} else if (versitPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_X_VCALENDAR)
					|| versitPart.getContentType().isMimeType(MIMETypes.MIME_TEXT_CALENDAR)) {
				/*
				 * Save ICalendar
				 */
				VersitUtility.saveICal(versitPart, retvalList, session);
			} else {
				throw new IMAPException(IMAPException.Code.UNSUPPORTED_VERSIT_ATTACHMENT, versitPart.getContentType());
			}
			return retvalList.toArray(new CommonObject[retvalList.size()]);
		} catch (final IOException e) {
			throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	public void releaseResources() throws IMAPException {
		if (null != imapFolder) {
			try {
				imapFolder.close(false);
				resetIMAPFolder();
			} catch (final IllegalStateException e) {
				LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
			} catch (final MessagingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}

}
