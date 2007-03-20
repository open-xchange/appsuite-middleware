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

package com.openexchange.api2;

import static com.openexchange.groupware.container.mail.parser.MessageUtils.decodeMultiEncodedHeader;
import static com.openexchange.groupware.container.mail.parser.MessageUtils.getMessageUniqueIdentifier;
import static com.openexchange.groupware.container.mail.parser.MessageUtils.removeHdrLineBreak;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import sun.net.ConnectionResetException;

import com.openexchange.ajax.Mail;
import com.openexchange.cache.IMAPConnectionCacheManager;
import com.openexchange.groupware.AccountExistenceException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.groupware.container.mail.filler.MessageFiller;
import com.openexchange.groupware.container.mail.parser.ForwardTextMessageHandler;
import com.openexchange.groupware.container.mail.parser.ImageMessageHandler;
import com.openexchange.groupware.container.mail.parser.JSONAttachmentMessageHandler;
import com.openexchange.groupware.container.mail.parser.JSONMessageHandler;
import com.openexchange.groupware.container.mail.parser.MessageDumper;
import com.openexchange.groupware.container.mail.parser.PartMessageHandler;
import com.openexchange.groupware.container.mail.parser.ReplyTextMessageHandler;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.imap.DefaultIMAPConnection;
import com.openexchange.groupware.imap.IMAPCapabilities;
import com.openexchange.groupware.imap.IMAPException;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.groupware.imap.IMAPPropertiesFactory;
import com.openexchange.groupware.imap.IMAPUtils;
import com.openexchange.groupware.imap.OXMailException;
import com.openexchange.groupware.imap.TreeNode;
import com.openexchange.groupware.imap.UserSettingMail;
import com.openexchange.groupware.imap.OXMailException.MailCode;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.i18n.StringHelper;
import com.openexchange.monitoring.MonitorAgent;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.mail.spam.SpamAssassin;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.smtp.SMTPMessage;

/**
 * MailInterfaceImpl
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailInterfaceImpl implements MailInterface {

	private static final Log LOG = LogFactory.getLog(MailInterfaceImpl.class);

	public static final MailInterfaceMonitor mailInterfaceMonitor;

	private static final String INBOX = "INBOX";

	private static final String CHARSET = "charset";

	private static final String UTF8 = "UTF-8";

	private static final String DISP_TO = "Disposition-Notification-To";

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";
	
	private static final String STR_EMPTY = "";
	
	private static final String SWITCH_DEFAULT_FOLDER = "Switching to default value %s";

	private static final String PROPERTY_ALLOWREADONLYSELECT = "mail.imap.allowreadonlyselect";

	private static final Properties IMAP_PROPS;

	private static boolean imapPropsInitialized;

	private static final Lock LOCK_CREATE = new ReentrantLock();

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static final Lock LOCK_CON = new ReentrantLock();

	private static final Condition LOCK_CON_CONDITION = LOCK_CON.newCondition();

	private Properties imapProps;

	private final SessionObject sessionObj;

	private final UserSettingMail usm;

	private final TimeZone userTimeZone;

	private DefaultIMAPConnection imapCon;

	private IMAPStore imapStore;

	private boolean init;

	private IMAPFolder tmpFolder;

	private Message markAsSeen;

	private Rights tmpRights;

	static {
		mailInterfaceMonitor = new MailInterfaceMonitor();
		try {
			/*
			 * Register monitor
			 */
			final String[] sa = MonitorAgent.getDomainAndName(mailInterfaceMonitor.getClass().getName(), true);
			MonitorAgent.registerMBeanGlobal(new ObjectName(sa[0], "name", sa[1]), mailInterfaceMonitor);
		} catch (MalformedObjectNameException e) {
			LOG.error(e.getMessage(), e);
		} catch (NullPointerException e) {
			LOG.error(e.getMessage(), e);
		}
		/*
		 * Define imap properties
		 */
		IMAP_PROPS = ((Properties) (System.getProperties().clone()));
		/*
		 * mail.mime.base64.ignoreerrors: If set to "true", the BASE64 decoder
		 * will ignore errors in the encoded data, returning EOF. This may be
		 * useful when dealing with improperly encoded messages that contain
		 * extraneous data at the end of the encoded stream. Note however that
		 * errors anywhere in the stream will cause the decoder to stop decoding
		 * so this should be used with extreme caution.
		 */
		IMAP_PROPS.put("mail.mime.base64.ignoreerrors", STR_TRUE);
		IMAP_PROPS.put(PROPERTY_ALLOWREADONLYSELECT, STR_TRUE);
		IMAP_PROPS.put("mail.mime.encodeeol.strict", STR_TRUE);
		IMAP_PROPS.put("mail.mime.decodetext.strict", STR_FALSE);
		IMAP_PROPS.put("mail.mime.charset", UTF8);
		/*
		 * Following properties define if IMAPS and/or SMTPS should be enabled
		 */
		try {
			if (IMAPProperties.isImapsEnabled()) {
				IMAP_PROPS.put("mail.imap.socketFactory.class", "com.openexchange.tools.ssl.TrustAllSSLSocketFactory");
				IMAP_PROPS.put("mail.imap.socketFactory.port", String.valueOf(IMAPProperties.getImapsPort()));
				IMAP_PROPS.put("mail.imap.socketFactory.fallback", STR_FALSE);
				IMAP_PROPS.put("mail.smtp.starttls.enable", STR_TRUE);
			}
		} catch (IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
		try {
			if (IMAPProperties.isSmtpsEnabled()) {
				IMAP_PROPS.put("mail.smtp.socketFactory.class", "com.openexchange.tools.ssl.TrustAllSSLSocketFactory");
				IMAP_PROPS.put("mail.smtp.socketFactory.port", String.valueOf(IMAPProperties.getSmtpsPort()));
				IMAP_PROPS.put("mail.smtp.socketFactory.fallback", STR_FALSE);
				IMAP_PROPS.put("mail.smtp.starttls.enable", STR_TRUE);
			}
		} catch (IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private final static void initializeCapabilities(final IMAPStore imapStore) throws MessagingException {
		if (!IMAPProperties.isCapabilitiesLoaded()) {
			LOCK_INIT.lock();
			try {
				if (IMAPProperties.isCapabilitiesLoaded()) {
					return;
				}
				final IMAPCapabilities imapCaps = new IMAPCapabilities();
				imapCaps.setACL(imapStore.hasCapability(IMAPCapabilities.CAP_ACL));
				imapCaps.setThreadReferences(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_REFERENCES));
				imapCaps.setThreadOrderedSubject(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_ORDEREDSUBJECT));
				imapCaps.setQuota(imapStore.hasCapability(IMAPCapabilities.CAP_QUOTA));
				imapCaps.setSort(imapStore.hasCapability(IMAPCapabilities.CAP_SORT));
				imapCaps.setIMAP4(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4));
				imapCaps.setIMAP4rev1(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4_REV1));
				imapCaps.setUIDPlus(imapStore.hasCapability(IMAPCapabilities.CAP_UIDPLUS));
				try {
					imapCaps.setHasSubscription(!IMAPProperties.isIgnoreSubscription());
				} catch (IMAPException e) {
					LOG.error(e.getMessage(), e);
					imapCaps.setHasSubscription(false);
				}
				IMAPProperties.setImapCapabilities(imapCaps);
				IMAPProperties.setCapabilitiesLoaded(true);
			} finally {
				LOCK_INIT.unlock();
			}
		}
	}

	private final static void initializeIMAPProperties() throws OXException {
		/*
		 * Fill global IMAP Properties only once and switch flag
		 */
		if (!IMAPProperties.isGlobalPropertiesLoaded()) {
			LOCK_INIT.lock();
			try {
				if (IMAPProperties.isGlobalPropertiesLoaded()) {
					return;
				}
				IMAPPropertiesFactory.loadGlobalImapProperties();
			} catch (IMAPException e) {
				throw new OXException(e);
			} finally {
				LOCK_INIT.unlock();
			}
		}
		try {
			if (IMAPProperties.getJavaMailProperties() != null) {
				IMAP_PROPS.putAll(IMAPProperties.getJavaMailProperties());
			}
		} catch (IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
		if (IMAPProperties.getImapConnectionTimeout() > 0) {
			IMAP_PROPS.put("mail.imap.connectiontimeout", String.valueOf(IMAPProperties.getImapConnectionTimeout()));
		}
		IMAP_PROPS.put("mail.smtp.auth", IMAPProperties.isSmtpAuth() ? STR_TRUE : STR_FALSE);
	}

	/**
	 * Creates a <b>cloned</b> version of default IMAP properties
	 * 
	 * @return a cloned version of default IMAP properties
	 * @throws OXException
	 */
	public final static Properties getDefaultIMAPProperties() throws OXException {
		if (!imapPropsInitialized) {
			initializeIMAPProperties();
			imapPropsInitialized = true;
		}
		return (Properties) IMAP_PROPS.clone();
	}

	private MailInterfaceImpl(SessionObject sessionObj) throws OXException {
		super();
		this.sessionObj = sessionObj;
		this.usm = sessionObj.getUserConfiguration().getUserSettingMail();
		userTimeZone = TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone());
		if (!sessionObj.getUserConfiguration().hasWebMail()) {
			throw new OXMailException(MailCode.NO_MAIL_MODULE_ACCESS, getUserName());
		}
	}

	/**
	 * Creates an instance of <code>MailInterface</code> that tries to fetch a
	 * cached connection. If none available a new connection to IMAP server is
	 * going to be established.
	 * 
	 * @param sessionObj -
	 *            the groupware session
	 * @return an instance of <code>MailInterface</code>
	 * @throws OXException
	 */
	public static final MailInterface getInstance(final SessionObject sessionObj) throws OXException {
		return getInstance(sessionObj, true);
	}

	/**
	 * Creates an instance of <code>MailInterface</code>.
	 * 
	 * @param sessionObj -
	 *            the groupware session
	 * @param fetchCachedCon -
	 *            <code>true</code> if a cached connection should be used;
	 *            <code>false</code> otherwise
	 * @return an instance of <code>MailInterface</code>
	 * @throws OXException
	 */
	public static final MailInterface getInstance(final SessionObject sessionObj, final boolean fetchCachedCon)
			throws OXException {
		if (/* IMAPProperties.noAdminMailbox() && */sessionObj.getUserObject().getId() == sessionObj.getContext()
				.getMailadmin()) {
			throw new AccountExistenceException(com.openexchange.tools.oxfolder.OXFolderManagerImpl
					.getUserName(sessionObj), sessionObj.getContext().getContextId());
		}
		DefaultIMAPConnection imapCon = fetchCachedCon ? getCachedConnection(sessionObj) : null;
		if (imapCon != null) {
			final MailInterfaceImpl retval = new MailInterfaceImpl(sessionObj);
			try {
				/*
				 * Apply cached connection
				 */
				retval.imapCon = imapCon;
				retval.imapStore = imapCon.connect();
			} catch (NoSuchProviderException e) {
				throw handleMessagingException(e);
			} catch (MessagingException e) {
				throw handleMessagingException(e);
			}
			return retval;
		}
		/*
		 * No cached connection available, check if a new one may be established
		 */
		if (IMAPProperties.getMaxNumOfIMAPConnections() > 0
				&& DefaultIMAPConnection.getCounter() > IMAPProperties.getMaxNumOfIMAPConnections()) {
			LOCK_CON.lock();
			try {
				while (DefaultIMAPConnection.getCounter() > IMAPProperties.getMaxNumOfIMAPConnections()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Too many IMAP connections currently established. Going asleep.");
					}
					LOCK_CON_CONDITION.await();
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Woke up & IMAP connection(s) may again be established");
				}
				/*
				 * Try to fetch from cache again
				 */
				if (fetchCachedCon) {
					imapCon = getCachedConnection(sessionObj);
				}
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
				throw new OXMailException(MailCode.INTERRUPT_ERROR, e, new Object[0]);
			} finally {
				LOCK_CON.unlock();
			}
		}
		if (imapCon == null) {
			/*
			 * Return a new instance with an empty connection. Thus a new
			 * connection is going to be created through calling init() method
			 */
			return new MailInterfaceImpl(sessionObj);
		}
		final MailInterfaceImpl retval = new MailInterfaceImpl(sessionObj);
		try {
			/*
			 * Apply cached connection
			 */
			retval.imapCon = imapCon;
			retval.imapStore = imapCon.connect();
		} catch (NoSuchProviderException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
		return retval;
	}

	private static final DefaultIMAPConnection getCachedConnection(final SessionObject sessionObj) throws OXException {
		DefaultIMAPConnection imapCon = null;
		if (LOCK_CREATE.tryLock()) {
			try {
				imapCon = (DefaultIMAPConnection) IMAPConnectionCacheManager.getInstance().removeIMAPConnection(
						sessionObj);
			} finally {
				LOCK_CREATE.unlock();
			}
		}
		return imapCon;
	}

	/**
	 * Performs a new login with user-supplied credentials to user-supplied IMAP
	 * server and checks his default mailbox folders (drafts, sent, trash &
	 * spam)
	 */
	private final void init() throws OXException, NoSuchProviderException, MessagingException {
		if (init) {
			return;
		}
		imapProps = getDefaultIMAPProperties();
		imapProps.put("mail.smtp.host", sessionObj.getIMAPProperties().getSmtpServer());
		imapProps.put("mail.smtp.port", String.valueOf(sessionObj.getIMAPProperties().getSmtpPort()));
		if (imapStore == null || !imapStore.isConnected()) {
			imapCon = new DefaultIMAPConnection();
			imapCon.setProperties(imapProps);
			imapCon.setImapServer(sessionObj.getIMAPProperties().getImapServer(), sessionObj.getIMAPProperties()
					.getImapPort());
			imapCon.setUsername(sessionObj.getIMAPProperties().getImapLogin());
			imapCon.setPassword(sessionObj.getIMAPProperties().getImapPassword());
			final long start = System.currentTimeMillis();
			try {
				imapStore = imapCon.connect();
				mailInterfaceMonitor.changeNumActive(true);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				mailInterfaceMonitor.changeNumSuccessfulLogins(true);
			} catch (AuthenticationFailedException e) {
				mailInterfaceMonitor.changeNumFailedLogins(true);
				throw e;
			}
			MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.IMAP);
			/*
			 * Check if IMAP server capabilities were already loaded
			 */
			initializeCapabilities(imapStore);
		}
		/*
		 * Apply mail session
		 */
		sessionObj.setMailSession(imapCon.getSession());
		/*
		 * Check user's default mailbox folders
		 */
		CheckDefaultFolders: if (!usm.isStdFoldersSetDuringSession()) {
			final Lock creationLock = usm.getStdFolderCreationLock();
			creationLock.lock();
			try {
				/*
				 * Already set by previous thread?
				 */
				if (usm.isStdFoldersSetDuringSession()) {
					break CheckDefaultFolders;
				}
				String[] stdFolderNames = new String[4];
				if (usm.getStdDraftsName() == null || usm.getStdDraftsName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME, "Drafts");
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_DRAFTS), e);
					}
					stdFolderNames[INDEX_DRAFTS] = UserSettingMail.STD_DRAFTS;
				} else {
					stdFolderNames[INDEX_DRAFTS] = usm.getStdDraftsName();
				}
				if (usm.getStdSentName() == null || usm.getStdSentName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME, "Sent");
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SENT), e);
					}
					stdFolderNames[INDEX_SENT] = UserSettingMail.STD_SENT;
				} else {
					stdFolderNames[INDEX_SENT] = usm.getStdSentName();
				}
				if (usm.getStdSpamName() == null || usm.getStdSpamName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME, "Spam");
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SPAM), e);
					}
					stdFolderNames[INDEX_SPAM] = UserSettingMail.STD_SPAM;
				} else {
					stdFolderNames[INDEX_SPAM] = usm.getStdSpamName();
				}
				if (usm.getStdTrashName() == null || usm.getStdTrashName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME, "Trash");
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_TRASH), e);
					}
					stdFolderNames[INDEX_TRASH] = UserSettingMail.STD_TRASH;
				} else {
					stdFolderNames[INDEX_TRASH] = usm.getStdTrashName();
				}
				checkDefaultFolders(stdFolderNames, false);
				usm.setStdFoldersSetDuringSession(true);
			} finally {
				creationLock.unlock();
			}
		}
		init = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#checkDefaultFolders(java.lang.String[])
	 */
	public void checkDefaultFolders(final String[] defaultFolderNames) throws OXException {
		checkDefaultFolders(defaultFolderNames, true);
	}

	private final void checkDefaultFolders(final String[] defaultFolderNames, final boolean initCon) throws OXException {
		try {
			if (initCon) {
				usm.setStdFoldersSetDuringSession(false);
				init();
			} else {
				/*
				 * Get INBOX folder
				 */
				final Folder inboxFolder = imapStore.getFolder(INBOX);
				if (!inboxFolder.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, INBOX);
				}
				final boolean noInferiors = ((inboxFolder.getType() & Folder.HOLDS_FOLDERS) == 0);
				final StringBuilder tmp = new StringBuilder(100);
				/*
				 * Determine where to create default folders and store as a
				 * prefix for folder fullname
				 */
				if (!noInferiors
						&& (!isAltNamespaceEnabled(imapStore) || IMAPProperties
								.isAllowNestedDefaultFolderOnAltNamespace())) {
					/*
					 * Only allow default folder below INBOX if inferiors are
					 * permitted and either altNamespace is disabled or nested
					 * default folder are explicitely allowed
					 */
					tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
				}
				final String prefix = tmp.toString();
				tmp.setLength(0);
				final int type = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
				/*
				 * Check draft folder
				 */
				boolean checkSubscribed = true;
				Folder f = imapStore.getFolder(tmp.append(prefix).append(
						prepareMailFolderParam(defaultFolderNames[INDEX_DRAFTS])).toString());
				tmp.setLength(0);
				long start = System.currentTimeMillis();
				if (!f.exists()) {
					if (!f.create(type)) {
						final OXMailException oxme = new OXMailException(MailCode.NO_DEFAULT_FOLDER_CREATION,
								new StringBuilder().append(prefix).append(defaultFolderNames[INDEX_DRAFTS]).toString());
						LOG.error(oxme.getMessage(), oxme);
						checkSubscribed = false;
					}
				}
				if (checkSubscribed && !f.isSubscribed()) {
					try {
						f.setSubscribed(true);
					} catch (MethodNotSupportedException e) {
						LOG.error(e.getMessage(), e);
					} catch (MessagingException e) {
						LOG.error(e.getMessage(), e);
					}
				}
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				usm
						.setStandardFolder(INDEX_DRAFTS, MailFolderObject.prepareFullname(f.getFullName(), f
								.getSeparator()));
				/*
				 * Check sent folder
				 */
				checkSubscribed = true;
				f = imapStore.getFolder(tmp.append(prefix).append(
						prepareMailFolderParam(defaultFolderNames[INDEX_SENT])).toString());
				tmp.setLength(0);
				start = System.currentTimeMillis();
				if (!f.exists()) {
					if (!f.create(type)) {
						final OXMailException oxme = new OXMailException(MailCode.NO_DEFAULT_FOLDER_CREATION,
								new StringBuilder().append(prefix).append(defaultFolderNames[INDEX_SENT]).toString());
						LOG.error(oxme.getMessage(), oxme);
						checkSubscribed = false;
					}
				}
				if (checkSubscribed && !f.isSubscribed()) {
					try {
						f.setSubscribed(true);
					} catch (MethodNotSupportedException e) {
						LOG.error(e.getMessage(), e);
					} catch (MessagingException e) {
						LOG.error(e.getMessage(), e);
					}
				}
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				usm.setStandardFolder(INDEX_SENT, MailFolderObject.prepareFullname(f.getFullName(), f.getSeparator()));
				/*
				 * Check spam folder
				 */
				checkSubscribed = true;
				f = imapStore.getFolder(tmp.append(prefix).append(
						prepareMailFolderParam(defaultFolderNames[INDEX_SPAM])).toString());
				tmp.setLength(0);
				start = System.currentTimeMillis();
				if (!f.exists()) {
					if (!f.create(type)) {
						final OXMailException oxme = new OXMailException(MailCode.NO_DEFAULT_FOLDER_CREATION,
								new StringBuilder().append(prefix).append(defaultFolderNames[INDEX_SPAM]).toString());
						LOG.error(oxme.getMessage(), oxme);
						checkSubscribed = false;
					}
				}
				if (checkSubscribed && !f.isSubscribed()) {
					try {
						f.setSubscribed(true);
					} catch (MethodNotSupportedException e) {
						LOG.error(e.getMessage(), e);
					} catch (MessagingException e) {
						LOG.error(e.getMessage(), e);
					}
				}
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				usm.setStandardFolder(INDEX_SPAM, MailFolderObject.prepareFullname(f.getFullName(), f.getSeparator()));
				/*
				 * Check trash folder
				 */
				checkSubscribed = true;
				f = imapStore.getFolder(tmp.append(prefix).append(
						prepareMailFolderParam(defaultFolderNames[INDEX_TRASH])).toString());
				tmp.setLength(0);
				start = System.currentTimeMillis();
				if (!f.exists()) {
					if (!f.create(type)) {
						final OXMailException oxme = new OXMailException(MailCode.NO_DEFAULT_FOLDER_CREATION,
								new StringBuilder().append(prefix).append(defaultFolderNames[INDEX_TRASH]).toString());
						LOG.error(oxme.getMessage(), oxme);
						checkSubscribed = false;
					}
				}
				if (checkSubscribed && !f.isSubscribed()) {
					try {
						f.setSubscribed(true);
					} catch (MethodNotSupportedException e) {
						LOG.error(e.getMessage(), e);
					} catch (MessagingException e) {
						LOG.error(e.getMessage(), e);
					}
				}
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				usm.setStandardFolder(INDEX_TRASH, MailFolderObject.prepareFullname(f.getFullName(), f.getSeparator()));
			}
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	private static final boolean isAltNamespaceEnabled(final IMAPStore imapStore) throws MessagingException {
		boolean altnamespace = false;
		final Folder[] pn = imapStore.getPersonalNamespaces();
		if (pn.length != 0 && pn[0].getFullName().trim().length() == 0) {
			altnamespace = true;
		}
		return altnamespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getStore()
	 */
	public Store getStore() throws OXException {
		try {
			init();
			return imapStore;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#close()
	 */
	public void close(final boolean putIntoCache) throws OXException {
		if (!init) {
			return;
		}
		try {
			keepSeen(imapCon.getImapFolder());
			tmpRights = null;
			sessionObj.setMailSession(null);
			if (tmpFolder != null && tmpFolder.isOpen()) {
				try {
					tmpFolder.close(true); //expunge
				} catch (MessagingException e) {
					LOG.error(e.getMessage(), e);
					mailInterfaceMonitor.changeNumActive(false);
					tmpFolder = null;
				}
			}
			closeIMAPConnection(putIntoCache, sessionObj, imapCon);
			this.imapStore = null;
			this.imapCon = null;
			init = false;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	public static final boolean closeIMAPConnection(final DefaultIMAPConnection imapCon) throws MessagingException {
		return closeIMAPConnection(false, null, imapCon);
	}

	private static final boolean closeIMAPConnection(final boolean putIntoCache, final SessionObject sessionObj,
			final DefaultIMAPConnection imapCon) throws MessagingException {
		if (imapCon != null) {
			/*
			 * Expunge folder
			 */
			try {
				expungeDefaultIMAPConnection(imapCon);
			} catch (MessagingException e) {
				LOG.error(
						new StringBuilder(100).append("Mail folder ").append(imapCon.getImapFolder().getFullName())
								.append(" could NOT be expunged for user ").append(
										com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(sessionObj))
								.toString(), e);
			}
			boolean cached = false;
			try {
				cached = putIntoCache
						&& IMAPConnectionCacheManager.getInstance().putIMAPConnection(sessionObj, imapCon);
			} catch (OXException e) {
				LOG.error(e.getMessage(), e);
				cached = false;
			}
			/*
			 * Return immediately if connection could be put into cache
			 */
			if (cached) {
				return false;
			}
			/*
			 * Release rights
			 */
			imapCon.setMyRights(null);
			/*
			 * Release folder
			 */
			if (imapCon.getImapFolder() != null) {
				try {
					final long start = System.currentTimeMillis();
					imapCon.getImapFolder().close(false);
					mailInterfaceMonitor.changeNumActive(false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} catch (MessagingException e) {
					LOG.error(e.getMessage(), e);
				} finally {
					imapCon.setImapFolder(null);
				}
			}
			/*
			 * Close connection
			 */
			imapCon.close();
			MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.IMAP);
			LOCK_CON.lock();
			try {
				LOCK_CON_CONDITION.signalAll();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Sending signal to possible waiting threads");
				}
			} finally {
				LOCK_CON.unlock();
			}
			return true;
		}
		return true;
	}

	private static final void expungeDefaultIMAPConnection(final DefaultIMAPConnection imapCon)
			throws MessagingException {
		if (!imapCon.isExpunge()) {
			return;
		}
		final IMAPFolder imapFolder = imapCon.getImapFolder();
		if (imapFolder == null) {
			return;
		} else if (imapFolder.getMode() != Folder.READ_WRITE) {
			throw new MessagingException(new StringBuilder(100).append("Cannot expunge READ_ONLY folder: ").append(
					imapFolder.getFullName()).toString());
		}
		/*
		 * Expunge folder
		 */
		try {
			final long start = System.currentTimeMillis();
			imapFolder.expunge();
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append("Mail folder ").append(imapFolder.getFullName()).append(
						" successfully expunged"));
			}
		} finally {
			imapCon.setExpunge(false);
		}
	}

	private static final Flags FLAGS_SEEN = new Flags(Flags.Flag.SEEN);

	private static final String ERROR_KEEP_SEEN = "/SEEN flag cannot be set: ";

	private final void keepSeen(final IMAPFolder imapFolder) throws OXException {
		if (markAsSeen == null) {
			return;
		}
		try {
			try {
				if (imapFolder.getMode() == Folder.READ_ONLY) {
					imapFolder.close(false);
					mailInterfaceMonitor.changeNumActive(false);
					imapFolder.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
				}
			} catch (MessagingException e) {
				/*
				 * Folder is closed
				 */
				try {
					if (!imapFolder.isOpen()) {
						imapFolder.open(Folder.READ_WRITE);
						mailInterfaceMonitor.changeNumActive(true);
					}
				} catch (ReadOnlyFolderException e1) {
					LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e1.getMessage()).toString(), e1);
					return;
				}
			}
			if (IMAPProperties.isSupportsACLs()) {
				try {
					if (!imapCon.getMyRights().contains(Rights.Right.KEEP_SEEN)) {
						/*
						 * User has no \KEEP_SEEN right
						 */
						if (LOG.isWarnEnabled()) {
							LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append("Missing KEEP_SEEN right").toString());
						}
						return;
					}
				} catch (MessagingException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
					}
					return;
				}
			}
			markAsSeen.setFlags(FLAGS_SEEN, true);
		} catch (MessagingException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
			}
			return;
		} finally {
			markAsSeen = null;
		}
	}

	private final Rights getTmpRights() throws MessagingException {
		if (tmpRights == null) {
			return (tmpRights = tmpFolder.myRights());
		}
		return tmpRights;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getQuota()
	 */
	public long[] getQuota() throws OXException {
		try {
			init();
			if (!IMAPProperties.getImapCapabilities().hasQuota()) {
				return new long[] { MailInterface.UNLIMITED_QUOTA, MailInterface.UNLIMITED_QUOTA };
			}
			final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
			final Quota[] folderQuota;
			final long start = System.currentTimeMillis();
			try {
				folderQuota = inboxFolder.getQuota();
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (folderQuota.length == 0) {
				return new long[] { MailInterface.UNLIMITED_QUOTA, MailInterface.UNLIMITED_QUOTA };
			}
			final Quota.Resource[] resources = folderQuota[0].resources;
			if (resources.length == 0) {
				return new long[] { MailInterface.UNLIMITED_QUOTA, MailInterface.UNLIMITED_QUOTA };
			}
			return new long[] { resources[0].limit, resources[0].usage };
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getQuotaLimit()
	 */
	public long getQuotaLimit() throws OXException {
		try {
			init();
			if (!IMAPProperties.getImapCapabilities().hasQuota()) {
				return MailInterface.UNLIMITED_QUOTA;
			}
			final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
			final long start = System.currentTimeMillis();
			final Quota[] folderQuota;
			try {
				folderQuota = inboxFolder.getQuota();
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (folderQuota.length == 0) {
				return MailInterface.UNLIMITED_QUOTA;
			}
			final Quota.Resource[] resources = folderQuota[0].resources;
			if (resources.length == 0) {
				return MailInterface.UNLIMITED_QUOTA;
			}
			return resources[0].limit;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getQuotaUsage()
	 */
	public long getQuotaUsage() throws OXException {
		try {
			init();
			if (!IMAPProperties.getImapCapabilities().hasQuota()) {
				return MailInterface.UNLIMITED_QUOTA;
			}
			final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
			final long start = System.currentTimeMillis();
			final Quota[] folderQuota;
			try {
				folderQuota = inboxFolder.getQuota();
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (folderQuota.length == 0) {
				return MailInterface.UNLIMITED_QUOTA;
			}
			final Quota.Resource[] resources = folderQuota[0].resources;
			if (resources.length == 0) {
				return MailInterface.UNLIMITED_QUOTA;
			}
			return resources[0].usage;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getAllMessageCount(java.lang.String)
	 */
	public int[] getAllMessageCount(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? INBOX : folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			final int[] retval = new int[4];
			final long start = System.currentTimeMillis();
			try {
				retval[0] = imapCon.getImapFolder().getMessageCount();
				retval[1] = imapCon.getImapFolder().getNewMessageCount();
				retval[2] = imapCon.getImapFolder().getUnreadMessageCount();
				retval[3] = imapCon.getImapFolder().getDeletedMessageCount();
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessageCount(java.lang.String)
	 */
	public int getMessageCount(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getMessageCount();
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getNewMessageCount(java.lang.String)
	 */
	public int getNewMessageCount(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getNewMessageCount();
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getUnreadMessageCount(java.lang.String)
	 */
	public int getUnreadMessageCount(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getUnreadMessageCount();
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getDeletedMessageCount(java.lang.String)
	 */
	public int getDeletedMessageCount(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getDeletedMessageCount();
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessageUID(javax.mail.Message)
	 */
	public long getMessageUID(final Message msg) throws OXException {
		try {
			final long start = System.currentTimeMillis();
			final long retval;
			try {
				retval = imapCon.getImapFolder().getUID(msg);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getNewMessages(java.lang.String,
	 *      int, int)
	 */
	public SearchIterator getNewMessages(final String folderArg, final int sortCol, final int order, final int[] fields)
			throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? INBOX : folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder().getFullName());
			}
			/*
			 * Get ( & fetch) new messages
			 */
			Message[] newMsgs = null;
			boolean tryAgain = true;
			if (IMAPProperties.isCapabilitiesLoaded() && IMAPProperties.getImapCapabilities().hasSort()) {
				final long start = System.currentTimeMillis();
				newMsgs = IMAPUtils.getNewMessages(imapCon.getImapFolder());
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				tryAgain = false;
			}
			if (tryAgain
					&& IMAPProperties.isCapabilitiesLoaded()
					&& (IMAPProperties.getImapCapabilities().hasIMAP4rev1() || IMAPProperties.getImapCapabilities()
							.hasIMAP4())) {
				/*
				 * Just try SEARCH command
				 */
				/*
				 * Request sequence numbers of unseen messages
				 */
				final long start = System.currentTimeMillis();
				final int[] newMsgsSeqNum = IMAPUtils.getNewMsgsSeqNums(imapCon.getImapFolder(), true);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (newMsgsSeqNum.length == 0) {
					/*
					 * Return empty iterator from empty array
					 */
					return SearchIteratorAdapter.createEmptyIterator();
				}
				newMsgs = new MessageCacheObject[newMsgsSeqNum.length];
				for (int i = 0; i < newMsgsSeqNum.length; i++) {
					newMsgs[i] = new MessageCacheObject(imapCon.getImapFolder().getFullName(), imapCon.getImapFolder()
							.getSeparator(), newMsgsSeqNum[i]);
				}
				tryAgain = false;
			}
			if (tryAgain) {
				/*
				 * Nothing works at all. Do it the long way: Request ALL
				 * messages and check each one if /SEEN flag is not set.
				 */
				Message[] msgs = imapCon.getImapFolder().getMessages();
				final FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE);
				fp.add(FetchProfile.Item.FLAGS);
				try {
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgs, fp, true);
				} catch (ProtocolException e1) {
					throw new OXMailException(MailCode.PROTOCOL_ERROR, e1, e1.getMessage());
				}
				final List<Message> tmp = new ArrayList<Message>();
				for (int i = 0; i < msgs.length; i++) {
					final Message msg = msgs[i];
					if (!msg.isSet(Flags.Flag.SEEN)) {
						tmp.add(msg);
					}
				}
				final int size = tmp.size();
				if (size == 0) {
					/*
					 * Return empty iterator from empty array
					 */
					return SearchIteratorAdapter.createEmptyIterator();
				}
				newMsgs = new Message[size];
				tmp.toArray(newMsgs);
			}
			if (newMsgs == null) {
				/*
				 * Return empty iterator from empty array
				 */
				return SearchIteratorAdapter.createEmptyIterator();
			}
			final long start = System.currentTimeMillis();
			try {
				if (newMsgs.length <= IMAPProperties.getMessageFetchLimit()) {
					newMsgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), newMsgs, IMAPUtils
							.getDefaultFetchProfile(), false);
				} else {
					newMsgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), newMsgs, fields, sortCol, false);
				}
			} catch (ProtocolException e) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			return SearchIteratorAdapter.createArrayIterator(newMsgs);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getAllMessages(java.lang.String,
	 *      int, int)
	 */
	public SearchIterator getAllMessages(final String folder, final int sortCol, final int order, final int[] fields)
			throws OXException {
		return getMessages(folder, null, sortCol, order, null, null, true, fields);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessages(java.lang.String,
	 *      int[], int, int, int[], java.lang.String[], boolean)
	 */
	public SearchIterator getMessages(final String folderArg, final int[] fromToIndices, final int sortCol,
			final int order, final int[] searchCols, final String[] searchPatterns,
			final boolean linkSearchTermsWithOR, final int[] fields) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder().getFullName());
			}
			Message[] retval = null;
			/*
			 * Shall a search be performed?
			 */
			final boolean search = (searchCols != null && searchCols.length > 0 && searchPatterns != null && searchPatterns.length > 0);
			if (search) {
				/*
				 * Preselect message list according to given search pattern
				 */
				retval = searchMessages(imapCon.getImapFolder(), searchCols, searchPatterns, linkSearchTermsWithOR,
						fields, sortCol);
				if (retval == null || retval.length == 0) {
					return SearchIteratorAdapter.createEmptyIterator();
				}
			}
			boolean applicationSort = true;
			if (IMAPProperties.isImapSort()) {
				try {
					if (search) {
						if (retval == null || retval.length == 0) {
							return SearchIteratorAdapter.createEmptyIterator();
						}
						/*
						 * Define sequence of valid message numbers: e.g.:
						 * 2,34,35,43,51
						 */
						final StringBuilder sortRange = new StringBuilder();
						sortRange.append(retval[0].getMessageNumber());
						for (int i = 1; i < retval.length; i++) {
							sortRange.append(',').append(retval[i].getMessageNumber());
						}
						final long start = System.currentTimeMillis();
						retval = IMAPUtils.getServerSortList(imapCon.getImapFolder(), sortCol,
								order == MailInterface.ORDER_DESC, sortRange);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					} else {
						final long start = System.currentTimeMillis();
						retval = IMAPUtils.getServerSortList(imapCon.getImapFolder(), sortCol,
								order == MailInterface.ORDER_DESC);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
					if (retval == null || retval.length == 0) {
						return SearchIteratorAdapter.createEmptyIterator();
					}
					if (!search) {
						final long start = System.currentTimeMillis();
						try {
							if (retval.length < IMAPProperties.getMessageFetchLimit()) {
								retval = IMAPUtils.fetchMessages(imapCon.getImapFolder(), retval, IMAPUtils
										.getDefaultFetchProfile(), true);
							} else {
								retval = IMAPUtils
										.fetchMessages(imapCon.getImapFolder(), retval, fields, sortCol, true);
							}
						} catch (ProtocolException e) {
							throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
						} finally {
							mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						}
					}
					applicationSort = false;
				} catch (Throwable t) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new OXMailException(MailCode.IMAP_SORT_FAILED, t.getMessage()).getMessage());
					}
					/*
					 * Switch to application sorting if any exception occurs.
					 */
					applicationSort = true;
				}
			}
			if (applicationSort) {
				/*
				 * Select all messages if user does not want a search being
				 * performed
				 */
				if (!search) {
					retval = imapCon.getImapFolder().getMessages();
					final long start = System.currentTimeMillis();
					try {
						if (retval.length < IMAPProperties.getMessageFetchLimit()) {
							retval = IMAPUtils.fetchMessages(imapCon.getImapFolder(), retval, IMAPUtils
									.getDefaultFetchProfile(), true);
						} else {
							/*
							 * No messages were fetched. At least, prefetch
							 * according to given columns and sort criteria
							 */
							retval = IMAPUtils.fetchMessages(imapCon.getImapFolder(), retval, fields, sortCol, true);
						}
					} catch (ProtocolException e) {
						throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
					} finally {
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
				}
				final List<Message> msgList = Arrays.asList(retval);
				Collections.sort(msgList, IMAPUtils.getComparator(sortCol, order == MailInterface.ORDER_DESC,
						sessionObj.getLocale()));
				msgList.toArray(retval);
			}
			if (fromToIndices != null && fromToIndices.length == 2) {
				final int fromIndex = fromToIndices[0];
				int toIndex = fromToIndices[1];
				if (retval == null || retval.length == 0) {
					return SearchIteratorAdapter.createEmptyIterator();
				}
				if ((fromIndex) > retval.length) {
					/*
					 * Return empty iterator if start is out of range
					 */
					return SearchIteratorAdapter.createEmptyIterator();
				}
				/*
				 * Reset end index if out of range
				 */
				if (toIndex > retval.length) {
					toIndex = retval.length;
				}
				final Message[] tmp = retval;
				final int retvalLength = toIndex - fromIndex + 1;
				retval = new Message[retvalLength];
				System.arraycopy(tmp, fromIndex, retval, 0, retvalLength);
			}
			return SearchIteratorAdapter.createArrayIterator(retval);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		} catch (IMAPException e) {
			throw new OXMailException(MailCode.IMAP_ERROR, e, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#searchMessages(java.lang.String,
	 *      int[], java.lang.String[], boolean)
	 */
	public SearchIterator searchMessages(final String folderArg, final int[] searchCols, final String[] searchPatterns,
			final boolean linkWithOR, final int[] fields) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			return SearchIteratorAdapter.createArrayIterator(searchMessages(imapCon.getImapFolder(), searchCols,
					searchPatterns, linkWithOR, fields, -1));
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		} catch (IMAPException e) {
			throw new OXMailException(MailCode.IMAP_ERROR, e, e.getMessage());
		}
	}

	private final Message[] searchMessages(final Folder folder, final int[] searchCols, final String[] searchPatterns,
			final boolean linkWithOR, final int[] fields, final int sortCol) throws MessagingException, OXException,
			IMAPException {
		boolean applicationSearch = true;
		Message[] msgs = null;
		if (!folder.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, folder);
		}
		final IMAPFolder imapFolder = (IMAPFolder) folder;
		if (!imapFolder.isOpen()) {
			imapFolder.open(Folder.READ_ONLY);
			mailInterfaceMonitor.changeNumActive(true);
		}
		if (IMAPProperties.isImapSearch()) {
			try {
				if (searchCols.length != searchPatterns.length) {
					throw new OXMailException(MailCode.INVALID_SEARCH_PARAMS, searchCols.length, searchPatterns.length);
				}
				final SearchTerm searchTerm = IMAPUtils.getSearchTerm(searchCols, searchPatterns, linkWithOR);
				msgs = imapFolder.search(searchTerm);
				final long start = System.currentTimeMillis();
				try {
					if (msgs.length < IMAPProperties.getMessageFetchLimit()) {
						msgs = IMAPUtils.fetchMessages(imapFolder, msgs, IMAPUtils.getDefaultFetchProfile(), false);
					} else {
						msgs = IMAPUtils.fetchMessages(imapFolder, msgs, fields, sortCol, false);
					}
				} catch (ProtocolException e) {
					throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
				} finally {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
				applicationSearch = false;
			} catch (Throwable t) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(new OXMailException(MailCode.IMAP_SEARCH_FAILED, t.getMessage()).getMessage());
				}
				applicationSearch = true;
			}
		}
		if (applicationSearch) {
			Message[] allMsgs = imapFolder.getMessages();
			long start = System.currentTimeMillis();
			try {
				if (allMsgs.length < IMAPProperties.getMessageFetchLimit()) {
					allMsgs = IMAPUtils.fetchMessages(imapFolder, allMsgs, IMAPUtils.getDefaultFetchProfile(), true);
				} else {
					Set<Integer> tmp = new HashSet<Integer>();
					for (int i = 0; i < searchCols.length; i++) {
						tmp.add(Integer.valueOf(searchCols[i]));
					}
					for (int i = 0; i < fields.length; i++) {
						tmp.add(Integer.valueOf(fields[i]));
					}
					final int[] trimmedFields = new int[tmp.size()];
					final Iterator<Integer> iter = tmp.iterator();
					for (int i = 0; i < trimmedFields.length; i++) {
						trimmedFields[i] = iter.next().intValue();
					}
					tmp = null;
					allMsgs = IMAPUtils.fetchMessages(imapFolder, allMsgs, trimmedFields, sortCol, true);
				}
			} catch (ProtocolException e) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			start = System.currentTimeMillis();
			try {
				final List<Message> tmp = new ArrayList<Message>();
				for (int i = 0; i < allMsgs.length; i++) {
					final Message currentMsg = allMsgs[i];
					if (IMAPUtils.findPatternInField(searchCols, searchPatterns, linkWithOR, currentMsg)) {
						tmp.add(currentMsg);
					}
				}
				msgs = new Message[tmp.size()];
				tmp.toArray(msgs);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
		}
		return msgs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getAllThreadedMessages(java.lang.String)
	 */
	public SearchIterator getAllThreadedMessages(final String folder, final int[] fields) throws OXException {
		return getThreadedMessages(folder, null, null, null, true, fields);
	}

	private static final String COMMAND_FAILED_THREAD = "IMAP server does not support THREAD=REFERENCES command";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getThreadedMessages(java.lang.String,
	 *      int[], int[], java.lang.String[], boolean)
	 */
	public SearchIterator getThreadedMessages(final String folderArg, final int[] fromToIndices,
			final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR,
			final int[] fields) throws OXException {
		try {
			if (!IMAPProperties.getImapCapabilities().hasThreadReferences()) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, null, COMMAND_FAILED_THREAD);
			}
			Message[] msgArr = null;
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder().getFullName());
			}
			/*
			 * Check if a search should be done and if IMAP server supports
			 * SEARCH command
			 */
			final boolean search = ((searchCols != null && searchCols.length > -1 && searchPatterns != null && searchPatterns.length > 0) && (IMAPProperties
					.getImapCapabilities().hasIMAP4() || IMAPProperties.getImapCapabilities().hasIMAP4rev1()));
			if (search) {
				/*
				 * Preselect message list according to given search pattern
				 */
				msgArr = searchMessages(imapCon.getImapFolder(), searchCols, searchPatterns, linkSearchTermsWithOR,
						fields, -1);
				if (msgArr == null || msgArr.length == 0) {
					return SearchIteratorAdapter.createEmptyIterator();
				}
			}
			final String threadResp;
			long start = System.currentTimeMillis();
			try {
				if (search) {
					if (msgArr == null || msgArr.length == 0) {
						return SearchIteratorAdapter.createEmptyIterator();
					}
					/*
					 * Define sequence of valid message numbers: e.g.:
					 * 2,34,35,43,51
					 */
					final StringBuilder sortRange = new StringBuilder();
					sortRange.append(msgArr[0].getMessageNumber());
					for (int i = 1; i < msgArr.length; i++) {
						sortRange.append(msgArr[i].getMessageNumber()).append(',');
					}
					threadResp = IMAPUtils.getThreadResponse(imapCon.getImapFolder(), sortRange);
				} else {
					threadResp = IMAPUtils.getThreadResponse(imapCon.getImapFolder(), new StringBuilder("ALL"));
				}
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			/*
			 * Parse THREAD response
			 */
			final List<TreeNode> threadList = IMAPUtils.parseThreadResponse(threadResp);
			MessageCacheObject[] msgs = IMAPUtils.getMessagesFromThreadResponse(imapCon.getImapFolder().getFullName(),
					imapCon.getImapFolder().getSeparator(), threadResp);
			/*
			 * Fetch messages
			 */
			start = System.currentTimeMillis();
			try {
				if (msgs.length < IMAPProperties.getMessageFetchLimit()) {
					msgs = (MessageCacheObject[]) IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgs, IMAPUtils
							.getDefaultFetchProfile(), false);
				} else {
					msgs = (MessageCacheObject[]) IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgs, fields, -1,
							false);
				}
			} catch (ProtocolException e) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			createThreadSortMessages(threadList, 0, msgs, 0);
			if (fromToIndices != null && fromToIndices.length == 2) {
				final int fromIndex = fromToIndices[0];
				int toIndex = fromToIndices[1];
				if ((fromIndex) > msgs.length) {
					return SearchIteratorAdapter.createEmptyIterator();
				}
				/*
				 * Reset end index if out of range
				 */
				toIndex = toIndex > msgs.length ? msgs.length : toIndex;
				final MessageCacheObject[] tmp = new MessageCacheObject[toIndex - fromIndex];
				System.arraycopy(msgs, fromIndex, tmp, 0, tmp.length);
				return SearchIteratorAdapter.createArrayIterator(tmp);
			}
			return SearchIteratorAdapter.createArrayIterator(msgs);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		} catch (IMAPException e) {
			throw new OXMailException(MailCode.IMAP_ERROR, e, e.getMessage());
		}
	}

	private static final int createThreadSortMessages(final List<TreeNode> threadList, final int level,
			final MessageCacheObject[] msgs, final int indexArg) {
		int index = indexArg;
		final int threadListSize = threadList.size();
		final Iterator<TreeNode> iter = threadList.iterator();
		for (int i = 0; i < threadListSize; i++) {
			final TreeNode currentNode = iter.next();
			msgs[index].setThreadLevel(level);
			index++;
			index = createThreadSortMessages(currentNode.getChilds(), level + 1, msgs, index);
		}
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessageList(java.lang.String,
	 *      long[])
	 */
	public Message[] getMessageList(final String folderArg, final long[] uids, final int[] fields) throws OXException {
		try {
			final String folder = prepareMailFolderParam(folderArg);
			if (MailFolderObject.DEFAULT_IMAP_FOLDER.equals(folder)) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, MailFolderObject.DEFAULT_IMAP_FOLDER);
			}
			init();
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			final Message[] msgs;
			final long start = System.currentTimeMillis();
			try {
				if (uids.length < IMAPProperties.getMessageFetchLimit()) {
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), uids, IMAPUtils.getDefaultFetchProfile(),
							false);
				} else {
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), uids, fields, -1, false);
				}
			} catch (ProtocolException e) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			return msgs;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessage(java.lang.String,
	 *      long, boolean)
	 */
	public Message getMessage(final String folderArg, final long msgUID) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			if (MailFolderObject.DEFAULT_IMAP_FOLDER.equals(folder)) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, MailFolderObject.DEFAULT_IMAP_FOLDER);
			}
			if (imapCon.getImapFolder() == null) {
				/*
				 * Not initialized
				 */
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapStore.getFolder(INBOX) : imapStore
						.getFolder(folder)));
			} else if (!imapCon.getImapFolder().getFullName().equals(folder)) {
				/*
				 * Another folder than previous one
				 */
				if (imapCon.getImapFolder().isOpen()) {
					if (markAsSeen != null) {
						/*
						 * Mark stored message as seen
						 */
						keepSeen(imapCon.getImapFolder());
					}
					imapCon.getImapFolder().close(false);
					mailInterfaceMonitor.changeNumActive(false);
				}
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapStore.getFolder(INBOX) : imapStore
						.getFolder(folder)));
			}
			/*
			 * Open
			 */
			if (!imapCon.getImapFolder().isOpen()) {
				imapCon.getImapFolder().open(Folder.READ_WRITE);
				mailInterfaceMonitor.changeNumActive(true);
			} else if (markAsSeen != null) {
				/*
				 * Folder is already open, mark stored message as seen
				 */
				keepSeen(imapCon.getImapFolder());
			}
			final long start = System.currentTimeMillis();
			final Message msg;
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (msg == null) {
				throw new OXMailException(MailCode.MESSAGE_NOT_FOUND, msgUID, imapCon.getImapFolder().toString());
			}
			if (imapCon.getImapFolder().getMode() == Folder.READ_WRITE && !msg.isExpunged()) {
				/*
				 * Check for drafts folder. This is done here, cause sometimes
				 * copy operation does not properly add \Draft flag.
				 */
				final boolean isDraftFld = imapCon.getImapFolder().getFullName().equals(
						prepareMailFolderParam(getDraftsFolder()));
				final boolean isDraft = msg.getFlags().contains(Flags.Flag.DRAFT);
				if (isDraftFld && !isDraft) {
					try {
						msg.setFlags(FLAGS_DRAFT, true);
					} catch (MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(OXMailException.getFormattedMessage(MailCode.FLAG_FAILED, IMAPUtils.FLAG_DRAFT,
									msg.getMessageNumber(), imapCon.getImapFolder().getFullName(), e.getMessage()), e);
						}
					}
				} else if (!isDraftFld && isDraft) {
					try {
						msg.setFlags(FLAGS_DRAFT, false);
					} catch (MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(OXMailException.getFormattedMessage(MailCode.FLAG_FAILED, IMAPUtils.FLAG_DRAFT,
									msg.getMessageNumber(), imapCon.getImapFolder().getFullName(), e.getMessage()), e);
						}
					}
				}
			}
			markAsSeen = msg;
			return msg;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessageAttachment(java.lang.String,
	 *      long, java.lang.String)
	 */
	public JSONMessageAttachmentObject getMessageAttachment(final String folderArg, final long msgUID,
			final String attachmentPosition) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			if (imapCon.getImapFolder() == null) {
				/*
				 * Not initialized
				 */
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapStore.getFolder(INBOX) : imapStore
						.getFolder(folder)));
			} else if (!imapCon.getImapFolder().getFullName().equals(folder)) {
				/*
				 * Another folder than previous one
				 */
				if (imapCon.getImapFolder().isOpen()) {
					imapCon.getImapFolder().close(false);
					mailInterfaceMonitor.changeNumActive(false);
				}
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapStore.getFolder(INBOX) : imapStore
						.getFolder(folder)));
			}
			/*
			 * Open
			 */
			if (!imapCon.getImapFolder().isOpen()) {
				imapCon.getImapFolder().open(Folder.READ_ONLY);
				mailInterfaceMonitor.changeNumActive(true);
			}
			final Message msg;
			final long start = System.currentTimeMillis();
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final JSONAttachmentMessageHandler msgHandler = new JSONAttachmentMessageHandler(sessionObj,
					attachmentPosition);
			new MessageDumper(sessionObj).dumpMessage(msg, msgHandler);
			return msgHandler.getAttachmentObject();
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#saveVersitAttachment(java.lang.String, long, java.lang.String)
	 */
	public CommonObject[] saveVersitAttachment(final String folderArg, final long msgUID, final String partIdentifier)
			throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			/*
			 * Retrieve part object out of mail
			 */
			final Message msg;
			final long start = System.currentTimeMillis();
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final PartMessageHandler msgHandler = new PartMessageHandler(sessionObj, partIdentifier);
			new MessageDumper(sessionObj).dumpMessage(msg, msgHandler);
			final Part versitPart = msgHandler.getPart();
			if (versitPart == null) {
				throw new OXMailException(MailCode.NO_ATTACHMENT_FOUND, partIdentifier, new Mail.MailIdentifier(folder,
						msgUID).toString());
			}
			/*
			 * Save dependent on content type
			 */
			final List<CommonObject> retvalList = new ArrayList<CommonObject>();
			if (versitPart.isMimeType("text/x-vcard") || versitPart.isMimeType("text/vcard")) {
				/*
				 * Define versit reader
				 */
				final ContentType contentTypeObj = new ContentType(versitPart.getContentType());
				final VersitDefinition def = Versit.getDefinition(contentTypeObj.getBaseType());
				final VersitDefinition.Reader r = def.getReader(versitPart.getInputStream(), contentTypeObj
						.containsParameter(CHARSET) ? UTF8 : contentTypeObj.getParameter(CHARSET));
				/*
				 * Ok, convert versit object to corresponding data object and
				 * save this object via its interface
				 */
				OXContainerConverter oxc = null;
				AppointmentSQLInterface appointmentInterface = null;
				TasksSQLInterface taskInterface = null;
				try {
					oxc = new OXContainerConverter(sessionObj);
					final VersitObject rootVersitObj = def.parseBegin(r);
					VersitObject vo = null;
					int defaultCalendarFolder = -1;
					int defaultTaskFolder = -1;
					while ((vo = def.parseChild(r, rootVersitObj)) != null) {
						try {
							if ("VEVENT".equals(vo.name)) {
								/*
								 * An appointment
								 */
								final CalendarDataObject appointmentObj = oxc.convertAppointment(vo);
								appointmentObj.setContext(sessionObj.getContext());
								if (defaultCalendarFolder == -1) {
									defaultCalendarFolder = new OXFolderAccess(sessionObj.getContext())
											.getDefaultFolder(sessionObj.getUserObject().getId(), FolderObject.CALENDAR)
											.getObjectID();
								}
								appointmentObj.setParentFolderID(defaultCalendarFolder);
								/*
								 * Create interface if not done, yet
								 */
								if (appointmentInterface == null) {
									appointmentInterface = new CalendarSql(sessionObj);
								}
								appointmentInterface.insertAppointmentObject(appointmentObj);
								/*
								 * Add to list
								 */
								retvalList.add(appointmentObj);
							} else if ("VTODO".equals(vo.name)) {
								/*
								 * A task
								 */
								final Task taskObj = oxc.convertTask(vo);
								if (defaultTaskFolder == -1) {
									defaultTaskFolder = new OXFolderAccess(sessionObj.getContext()).getDefaultFolder(
											sessionObj.getUserObject().getId(), FolderObject.TASK).getObjectID();
								}
								taskObj.setParentFolderID(defaultTaskFolder);
								/*
								 * Create interface if not done, yet
								 */
								if (taskInterface == null) {
									taskInterface = new TasksSQLInterfaceImpl(sessionObj);
								}
								taskInterface.insertTaskObject(taskObj);
								/*
								 * Add to list
								 */
								retvalList.add(taskObj);
							} else {
								if (LOG.isWarnEnabled()) {
									LOG.warn("invalid versit object: " + vo.name);
								}
							}
						} catch (ConverterException e) {
							throw new OXMailException(MailCode.FAILED_VERSIT_SAVE);
						}
					}
				} finally {
					if (oxc != null) {
						oxc.close();
						oxc = null;
					}
				}
			} else if (versitPart.isMimeType("text/x-vCalendar") || versitPart.isMimeType("text/calendar")) {
				/*
				 * Define versit reader for VCard
				 */
				final ContentType contentTypeObj = new ContentType(versitPart.getContentType());
				final VersitDefinition def = Versit.getDefinition(contentTypeObj.getBaseType());
				final VersitDefinition.Reader r = def.getReader(versitPart.getInputStream(), contentTypeObj
						.containsParameter(CHARSET) ? UTF8 : contentTypeObj.getParameter(CHARSET));
				/*
				 * Ok, convert versit object to contact object and save this
				 * object via its interface
				 */
				OXContainerConverter oxc = null;
				try {
					oxc = new OXContainerConverter(sessionObj);
					final ContactSQLInterface contactInterface = new RdbContactSQLInterface(sessionObj);
					final VersitObject vo = def.parse(r);
					if (vo != null) {
						try {
							final ContactObject contactObj = oxc.convertContact(vo);
							contactObj.setParentFolderID(new OXFolderAccess(sessionObj.getContext()).getDefaultFolder(
									sessionObj.getUserObject().getId(), FolderObject.CONTACT).getObjectID());
							contactObj.setContextId(sessionObj.getContext().getContextId());
							contactInterface.insertContactObject(contactObj);
							/*
							 * Add to list
							 */
							retvalList.add(contactObj);
						} catch (ConverterException e) {
							throw new OXMailException(MailCode.FAILED_VERSIT_SAVE);
						}
					}
				} finally {
					if (oxc != null) {
						oxc.close();
						oxc = null;
					}
				}
			} else {
				throw new OXMailException(MailCode.UNSUPPORTED_VERSIT_ATTACHMENT, versitPart.getContentType()
						.toLowerCase(Locale.ENGLISH).indexOf("cal") == -1 ? "VCard" : "ICalendar", versitPart
						.getContentType());
			}
			return retvalList.toArray(new CommonObject[retvalList.size()]);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		} catch (IOException e) {
			throw new OXMailException(MailCode.FAILED_VERSIT_SAVE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getMessageImage(java.lang.String,
	 *      long, java.lang.String)
	 */
	public JSONMessageAttachmentObject getMessageImage(final String folderArg, final long msgUID, final String cid)
			throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			final Message msg;
			final long start = System.currentTimeMillis();
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final ImageMessageHandler msgHandler = new ImageMessageHandler(sessionObj, cid);
			new MessageDumper(sessionObj).dumpMessage(msg, msgHandler);
			return msgHandler.getImageAttachment();
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getReplyMessage(java.lang.String,
	 *      long, boolean)
	 */
	public JSONMessageObject getReplyMessageForDisplay(final String folderArg, final long replyMsgUID,
			final boolean replyToAll) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder().getFullName());
			}
			final MimeMessage originalMsg;
			final long start = System.currentTimeMillis();
			try {
				originalMsg = (MimeMessage) imapCon.getImapFolder().getMessageByUID(replyMsgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			/*
			 * Create the reply message
			 */
			final JSONMessageObject retval = new JSONMessageObject(usm, userTimeZone);
			/*
			 * Set headers of reply message
			 */
			final String subjectHeader = "Subject";
			final String subjectPrefix = "Re: ";
			final String rawSubject = removeHdrLineBreak(originalMsg.getHeader(subjectHeader, null));
			try {
				final String decodedSubject = decodeMultiEncodedHeader(rawSubject);
				final String newSubject = decodedSubject.regionMatches(true, 0, subjectPrefix, 0, 4) ? rawSubject
						: MimeUtility.encodeText(new StringBuilder().append(subjectPrefix).append(decodedSubject)
								.toString(), IMAPProperties.getDefaultMimeCharset(), "Q");
				retval.setSubject(MimeUtility.decodeText(newSubject));
			} catch (UnsupportedEncodingException e) {
				/*
				 * Handle raw value: setting prefix to raw subject value still
				 * leaves a valid and correct encoded header
				 */
				originalMsg.setHeader(subjectHeader, new StringBuilder().append(subjectPrefix).append(rawSubject)
						.toString());
			} catch (IMAPException e) {
				originalMsg.setHeader(subjectHeader, new StringBuilder().append(subjectPrefix).append(rawSubject)
						.toString());
			}
			/*
			 * Set the appropiate recipients
			 */
			final InternetAddress[] recipientAddrs;
			if (originalMsg.getHeader("Reply-To") == null) {
				/*
				 * Set from as recipient
				 */
				recipientAddrs = (InternetAddress[]) originalMsg.getFrom();
			} else {
				/*
				 * Message indicated Reply-To address
				 */
				recipientAddrs = InternetAddress.parseHeader(originalMsg.getHeader("Reply-To", ","), true);
			}
			retval.addToAddresses(recipientAddrs);
			if (replyToAll) {
				final List<InternetAddress> addrList = new ArrayList<InternetAddress>();
				final Session mailSession = imapCon.getSession();
				/*
				 * Add user's address to list
				 */
				final InternetAddress userAddr = InternetAddress.getLocalAddress(mailSession);
				if (userAddr != null) {
					addrList.add(userAddr);
				}
				/*
				 * Add any user's alternate addresses
				 */
				final String alternates = mailSession == null ? null : mailSession.getProperty("mail.alternates");
				if (alternates != null) {
					eliminateDuplicates(addrList, InternetAddress.parse(alternates, false));
				}
				/*
				 * Cc all other original recipients
				 */
				final String replyallccStr = mailSession == null ? null : mailSession.getProperty("mail.replyallcc");
				final boolean replyallcc = STR_TRUE.equalsIgnoreCase(replyallccStr);
				/*
				 * Add recipients from To field
				 */
				eliminateDuplicates(addrList, recipientAddrs);
				InternetAddress[] internetAddrs = eliminateDuplicates(addrList, ((InternetAddress[]) (originalMsg
						.getRecipients(Message.RecipientType.TO))));
				final String[] userAddrs = sessionObj.getUserObject().getAliases();
				if (internetAddrs != null && internetAddrs.length > 0) {
					if (replyallcc) {
						retval.addCCAddresses(removeUserAddresses(internetAddrs, userAddrs));
					} else {
						retval.addToAddresses(removeUserAddresses(internetAddrs, userAddrs));
					}
				}
				/*
				 * Add recipients from Cc field
				 */
				internetAddrs = eliminateDuplicates(addrList, ((InternetAddress[]) (originalMsg
						.getRecipients(Message.RecipientType.CC))));
				if (internetAddrs != null && internetAddrs.length > 0) {
					retval.addCCAddresses(removeUserAddresses(internetAddrs, userAddrs));
				}
				// /*
				// * Don't remove duplicate newsgroups
				// */
				// internetAddrs = (InternetAddress[])
				// originalMsg.getRecipients(javax.mail.internet.MimeMessage.RecipientType.NEWSGROUPS);
				// if (internetAddrs != null && internetAddrs.length > 0) {
				// retval.setNewsgroupRecipients(internetAddrs);
				// }
			}
			/*
			 * Set mail text of reply message
			 */
			final String msgUID = getMessageUniqueIdentifier(imapCon.getImapFolder(), replyMsgUID);
			if (!usm.isIgnoreOriginalMailTextOnReply()) {
				/*
				 * Append quoted original mail text
				 */
				final ReplyTextMessageHandler msgHandler = new ReplyTextMessageHandler(sessionObj, msgUID);
				new MessageDumper(sessionObj).dumpMessage(originalMsg, msgHandler);
				final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject();
				mao.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
				mao.setContent(msgHandler.isHtml() ? msgHandler.getReplyText() : msgHandler.getReplyText().replaceAll(
						"(\\r)?\\n", "<br>"));
				mao.setContentType(msgHandler.isHtml() ? "text/html" : "text/plain");
				mao.setDisposition(Part.INLINE);
				mao.setSize(-1);
				retval.addMessageAttachment(mao);
			}
			/*
			 * Set message reference
			 */
			retval.setMsgref(msgUID);
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	private static final InternetAddress[] removeUserAddresses(final InternetAddress[] internetAddrs,
			final String[] userAddrs) {
		if (userAddrs == null || userAddrs.length == 0) {
			return internetAddrs;
		}
		final List<InternetAddress> tmp = new ArrayList<InternetAddress>(internetAddrs.length);
		NextAddrs: for (int i = 0; i < internetAddrs.length; i++) {
			for (int j = 0; j < userAddrs.length; j++) {
				if (internetAddrs[i].getAddress().equalsIgnoreCase(userAddrs[j])) {
					continue NextAddrs;
				}
			}
			tmp.add(internetAddrs[i]);
		}
		return tmp.toArray(new InternetAddress[tmp.size()]);
	}

	private final InternetAddress[] eliminateDuplicates(final List<InternetAddress> editMe,
			final InternetAddress[] staticAddrs) {
		if (staticAddrs == null) {
			return null;
		}
		final List<InternetAddress> addrs = Arrays.asList(staticAddrs);
		final Iterator<InternetAddress> iter = addrs.iterator();
		for (int i = 0; i < staticAddrs.length; i++) {
			final InternetAddress intAddr = iter.next();
			boolean found = false;
			/*
			 * Search in list for current address
			 */
			int j = 0;
			while (j < editMe.size()) {
				if (editMe.get(j).equals(intAddr)) {
					/*
					 * Address found, so remove it
					 */
					found = true;
					editMe.remove(j);
				} else {
					j++;
				}
			}
			if (!found) {
				/*
				 * Add new address to list
				 */
				editMe.add(intAddr);
			}
		}
		return addrs.toArray(new InternetAddress[addrs.size()]);
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner
	 */
	private final void setAndOpenFolder(final String folderName, final int mode) throws MessagingException, OXException {
		final boolean isDefaultFolder = folderName.equals(MailFolderObject.DEFAULT_IMAP_FOLDER);
		final boolean isIdenticalFolder;
		if (isDefaultFolder) {
			isIdenticalFolder = (imapCon.getImapFolder() == null ? false
					: imapCon.getImapFolder() instanceof DefaultFolder);
		} else {
			isIdenticalFolder = (imapCon.getImapFolder() == null ? false : imapCon.getImapFolder().getFullName()
					.equals(folderName));
		}
		if (imapCon.getImapFolder() != null) {
			if (isIdenticalFolder && imapCon.getImapFolder().isOpen()) {
				if (imapCon.getImapFolder().getMode() == mode) {
					/*
					 * Identical folder is already opened in right mode
					 */
					return;
				} else if (imapCon.getImapFolder().getMode() == Folder.READ_WRITE && mode == Folder.READ_ONLY) {
					/*
					 * Although folder is opened read-write instead of
					 * read-only, all operations allowed in read-only also work
					 * in read-write. Therefore return here.
					 */
					return;
				}
			}
			if (imapCon.getImapFolder().isOpen()) {
				imapCon.getImapFolder().close(false);
				mailInterfaceMonitor.changeNumActive(false);
				imapCon.setMyRights(null);
			}
			if (isIdenticalFolder) {
				if (mode == Folder.READ_WRITE && ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
						&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROPERTY_ALLOWREADONLYSELECT, STR_FALSE))
						&& IMAPUtils.isReadOnly(imapCon.getImapFolder())) {
					throw new OXMailException(MailCode.READ_ONLY_FOLDER, imapCon.getImapFolder().getFullName());
				}
				/*
				 * Open identical folder in right mode
				 */
				imapCon.getImapFolder().open(mode);
				mailInterfaceMonitor.changeNumActive(true);
				return;
			}
		}
		imapCon.setImapFolder(isDefaultFolder ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore
				.getFolder(folderName));
		if (!isDefaultFolder && !imapCon.getImapFolder().exists()) {
			imapCon.setImapFolder(null);
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, imapCon.getImapFolder().getFullName());
		}
		if (mode != Folder.READ_ONLY && mode != Folder.READ_WRITE) {
			throw new OXMailException(MailCode.UNKNOWN_FOLDER_MODE, mode);
		} else if (mode == Folder.READ_WRITE && ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
				&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROPERTY_ALLOWREADONLYSELECT, STR_FALSE))
				&& IMAPUtils.isReadOnly(imapCon.getImapFolder())) {
			throw new OXMailException(MailCode.READ_ONLY_FOLDER, imapCon.getImapFolder().getFullName());
		}
		imapCon.getImapFolder().open(mode);
		mailInterfaceMonitor.changeNumActive(true);
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner
	 */
	private final void setAndOpenTmpFolder(final String folderName, final int mode) throws MessagingException,
			OXException {
		final boolean isDefaultFolder = folderName.equals(MailFolderObject.DEFAULT_IMAP_FOLDER);
		final boolean isIdenticalFolder;
		if (isDefaultFolder) {
			isIdenticalFolder = (tmpFolder == null ? false : tmpFolder instanceof DefaultFolder);
		} else {
			isIdenticalFolder = (tmpFolder == null ? false : tmpFolder.getFullName().equals(folderName));
		}
		if (tmpFolder != null) {
			if (isIdenticalFolder && tmpFolder.isOpen()) {
				if (tmpFolder.getMode() == mode) {
					/*
					 * Identical folder is already opened in right mode
					 */
					return;
				} else if (tmpFolder.getMode() == Folder.READ_WRITE && mode == Folder.READ_ONLY) {
					/*
					 * Although folder is opened read-write instead of
					 * read-only, all operations allowed in read-only also work
					 * in read-write. Therefore return here.
					 */
					return;
				}
			}
			if (tmpFolder.isOpen()) {
				tmpFolder.close(false);
				mailInterfaceMonitor.changeNumActive(false);
				tmpRights = null;
			}
			if (isIdenticalFolder) {
				if (mode == Folder.READ_WRITE && ((tmpFolder.getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
						&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROPERTY_ALLOWREADONLYSELECT, STR_FALSE))
						&& IMAPUtils.isReadOnly(tmpFolder)) {
					throw new OXMailException(MailCode.READ_ONLY_FOLDER, tmpFolder.getFullName());
				}
				/*
				 * Open identical folder in right mode
				 */
				tmpFolder.open(mode);
				mailInterfaceMonitor.changeNumActive(true);
				return;
			}
		}
		tmpFolder = (isDefaultFolder ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore
				.getFolder(folderName));
		if (!isDefaultFolder && !tmpFolder.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, tmpFolder.getFullName());
		}
		if (mode != Folder.READ_ONLY && mode != Folder.READ_WRITE) {
			throw new OXMailException(MailCode.UNKNOWN_FOLDER_MODE, mode);
		} else if (mode == Folder.READ_WRITE && ((tmpFolder.getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
				&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROPERTY_ALLOWREADONLYSELECT, STR_FALSE))
				&& IMAPUtils.isReadOnly(tmpFolder)) {
			throw new OXMailException(MailCode.READ_ONLY_FOLDER, tmpFolder.getFullName());
		}
		tmpFolder.open(mode);
		mailInterfaceMonitor.changeNumActive(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getForwardMessageForDisplay(java.lang.String,
	 *      long)
	 */
	public JSONMessageObject getForwardMessageForDisplay(final String folderArg, final long forwardMsgUID)
			throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder().getFullName());
			}
			final MimeMessage originalMsg;
			final long start = System.currentTimeMillis();
			try {
				originalMsg = (MimeMessage) imapCon.getImapFolder().getMessageByUID(forwardMsgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			/*
			 * Create the foward message
			 */
			final JSONMessageObject retval = new JSONMessageObject(usm, userTimeZone);
			/*
			 * Set its headers. Start with subject.
			 */
			final String subject = removeHdrLineBreak(originalMsg.getHeader("Subject", null));
			if (subject != null) {
				final String subjectPrefix = new StringHelper(sessionObj.getLocale())
						.getString(MailStrings.FORWARD_SUBJECT_PREFIX);
				retval.setSubject(decodeMultiEncodedHeader(subject.regionMatches(true, 0, subjectPrefix,
						0, subjectPrefix.length()) ? subject : new StringBuilder().append(subjectPrefix)
						.append(subject).toString()));
			}
			/*
			 * Set from
			 */
			if (usm.getSendAddr() != null) {
				retval.addFromAddress(usm.getSendAddr());
			}
			/*
			 * Settings indicate to forward message along as an attachment
			 */
			final MessageDumper msgDumper = new MessageDumper(sessionObj);
			final String msgUID = getMessageUniqueIdentifier(imapCon.getImapFolder(), forwardMsgUID);
			if (usm.isForwardAsAttachment()) {
				/*
				 * Add dummy content
				 */
				final JSONMessageAttachmentObject dummy = new JSONMessageAttachmentObject();
				dummy.setContentID(JSONMessageAttachmentObject.CONTENT_STRING);
				dummy.setContentType("text/plain");
				dummy.setContent(STR_EMPTY);
				dummy.setDisposition(Part.INLINE);
				dummy.setSize(0);
				retval.addMessageAttachment(dummy);
				/*
				 * Add original message to nested messages
				 */
				final JSONMessageHandler jsonMsgHandler = new JSONMessageHandler(sessionObj, msgUID, false);
				msgDumper.dumpMessage(originalMsg, jsonMsgHandler);
				retval.addNestedMessage(jsonMsgHandler.getMessageObject());
			} else {
				/*
				 * Inline Forward: Append first seen text from original message
				 */
				final JSONMessageAttachmentObject mao = new JSONMessageAttachmentObject();
				final ForwardTextMessageHandler msgHandler = new ForwardTextMessageHandler(sessionObj, msgUID);
				msgDumper.dumpMessage(originalMsg, msgHandler);
				mao.setContent(msgHandler.getForwardText());
				mao.setContentType(msgHandler.isHtml() ? "text/html" : "text/plain");
				retval.addMessageAttachment(mao);
				/*
				 * Append attachments - no inline or displayable attachments -
				 * from original message
				 */
				final JSONMessageHandler jsonMsgHandler = new JSONMessageHandler(sessionObj, msgUID, false);
				msgDumper.reset();
				msgDumper.dumpMessage(originalMsg, jsonMsgHandler);
				final JSONMessageObject originalMsgObj = jsonMsgHandler.getMessageObject();
				appendAttachments(retval, originalMsgObj.getMsgAttachments());
				appendAttachmentsFromNestedMessages(retval, originalMsgObj.getNestedMsgs());
			}
			retval.setMsgref(msgUID);
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/**
	 * Appends list of attachments to message object
	 */
	private final void appendAttachments(final JSONMessageObject msgObj,
			final List<JSONMessageAttachmentObject> attachments) {
		final int size = attachments.size();
		for (int i = 0; i < size; i++) {
			final JSONMessageAttachmentObject mao = attachments.get(i);
			if (Part.ATTACHMENT.equalsIgnoreCase(mao.getDisposition())) {
				msgObj.addMessageAttachment(mao);
			} else if (mao.getContentID() == JSONMessageAttachmentObject.CONTENT_NONE) {
				msgObj.addMessageAttachment(mao);
			}
		}
	}

	private final void appendAttachmentsFromNestedMessages(final JSONMessageObject msgObj,
			final List<JSONMessageObject> nestedMsgs) {
		final int size = nestedMsgs.size();
		for (int i = 0; i < size; i++) {
			final JSONMessageObject nestedMsg = nestedMsgs.get(i);
			appendAttachments(msgObj, nestedMsg.getMsgAttachments());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#sendReceiptAck(java.lang.String,
	 *      long)
	 */
	public void sendReceiptAck(final String folderArg, final long msgUID, final String fromAddr) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder().getFullName());
			}
			final MimeMessage msg;
			final long start = System.currentTimeMillis();
			try {
				msg = (MimeMessage) imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final String[] dispNotification = msg.getHeader(DISP_TO);
			if (dispNotification == null || dispNotification.length == 0) {
				throw new OXMailException(MailCode.MISSING_HEADER, DISP_TO, msgUID);
			}
			InternetAddress[] to = null;
			for (int i = 0; i < dispNotification.length; i++) {
				final InternetAddress[] addrs = InternetAddress.parse(dispNotification[i], false);
				if (to == null) {
					to = addrs;
				} else {
					final InternetAddress[] tmp = to;
					to = new InternetAddress[tmp.length + addrs.length];
					System.arraycopy(tmp, 0, to, 0, tmp.length);
					System.arraycopy(addrs, 0, to, tmp.length, addrs.length);
				}
			}
			final String msgId = msg.getHeader("Message-Id", null);
			sendReceiptAck(to, fromAddr, (msgId == null ? "[not available]" : msgId));
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	private final void sendReceiptAck(final InternetAddress[] to, final String fromAddr, final String msgID)
			throws OXException, MessagingException {
		final MimeMessage msg = new MimeMessage(imapCon.getSession());
		final StringHelper strHelper = new StringHelper(sessionObj.getLocale());
		/*
		 * Set from
		 */
		final String from;
		if (fromAddr != null) {
			from = fromAddr;
		} else if (usm.getSendAddr() == null && sessionObj.getUserObject().getMail() == null) {
			throw new OXMailException(MailCode.NO_SEND_ADDRESS_FOUND, getUserName());
		} else {
			from = usm.getSendAddr() == null ? sessionObj.getUserObject().getMail() : usm.getSendAddr();
		}
		final Address[] addrs = InternetAddress.parse(from, false);
		msg.addFrom(addrs);
		/*
		 * Set to
		 */
		msg.addRecipients(RecipientType.TO, to);
		/*
		 * Set header
		 */
		msg.setHeader("X-Priority", "3 (normal)");
		/*
		 * Subject
		 */
		msg.setSubject(strHelper.getString(MailStrings.ACK_SUBJECT));
		/*
		 * Sent date
		 */
		final Date date = new Date();
		final int offset = TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()).getOffset(date.getTime());
		msg.setSentDate(new Date(System.currentTimeMillis() - offset));
		/*
		 * Compose body
		 */
		final ContentType ct = new ContentType("text/plain; charset=UTF-8");
		final Multipart mixedMultipart = new MimeMultipart("mixed");
		/*
		 * Define text content
		 */
		final MimeBodyPart text = new MimeBodyPart();
		text.setText(strHelper.getString(MailStrings.ACK_RECEIPT_TEXT), IMAPProperties.getDefaultMimeCharset());
		text.setHeader("MIME-Version", "1.0");
		text.setHeader("Content-Type", ct.toString());
		mixedMultipart.addBodyPart(text);
		/*
		 * Define ack
		 */
		ct.setContentType("text/plain; name=MDNPart1.txt; charset=UTF-8");
		final MimeBodyPart ack = new MimeBodyPart();
		ack.setText(strHelper.getString(MailStrings.ACK_TEXT).replaceFirst("#FROM#", fromAddr).replaceFirst("#MSG ID#",
				msgID), IMAPProperties.getDefaultMimeCharset());
		ack.setHeader("MIME-Version", "1.0");
		ack.setHeader("Content-Type", ct.toString());
		ack.setHeader("Content-Disposition", "attachment; filename=MDNPart1.txt");
		mixedMultipart.addBodyPart(ack);
		/*
		 * Set message content
		 */
		msg.setContent(mixedMultipart);
		/*
		 * Send message
		 */
		final long start = System.currentTimeMillis();
		Transport transport = null;
		try {
			transport = imapCon.getSession().getTransport("smtp");
			if (IMAPProperties.isSmtpAuth()) {
				transport.connect(sessionObj.getIMAPProperties().getSmtpServer(), sessionObj.getIMAPProperties()
						.getImapLogin(), sessionObj.getIMAPProperties().getImapPassword());
			} else {
				transport.connect();
			}
			mailInterfaceMonitor.changeNumActive(true);
			msg.saveChanges();
			transport.sendMessage(msg, msg.getAllRecipients());
		} finally {
			if (transport != null) {
				transport.close();
				mailInterfaceMonitor.changeNumActive(false);
			}
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
	}

	private static final Flags FLAGS_ANSWERED = new Flags(Flags.Flag.ANSWERED);

	private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	private static final Flags FLAGS_DRAFT = new Flags(Flags.Flag.DRAFT);
	
	public static final int SENDTYPE_NEW = 0;
	
	public static final int SENDTYPE_REPLY = 1;
	
	public static final int SENDTYPE_FORWARD = 2;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#sendMessage(com.openexchange.groupware.container.mail.JSONMessageObject,
	 *      com.openexchange.groupware.upload.UploadEvent)
	 */
	public String sendMessage(final JSONMessageObject msgObj, final UploadEvent uploadEvent, final int sendType)
			throws OXException {
		try {
			init();
			final SMTPMessage newSMTPMsg = new SMTPMessage(imapCon.getSession());
			IMAPFolder originalMsgFolder = null;
			MimeMessage originalMsg = null;
			MessageFiller msgFiller = null;
			try {
				if (msgObj.getMsgref() != null) {
					/*
					 * A message reference is present. Either a reply, forward
					 * or draft-edit message.
					 */
					final Mail.MailIdentifier mailId = new Mail.MailIdentifier(msgObj.getMsgref());
					originalMsgFolder = (IMAPFolder) imapStore.getFolder(mailId.getFolder());
					/*
					 * Check folder existence
					 */
					if (!originalMsgFolder.exists()) {
						throw new OXMailException(MailCode.FOLDER_NOT_FOUND, originalMsgFolder.getFullName());
					}
					boolean isReadWrite = true;
					try {
						originalMsgFolder.open(Folder.READ_WRITE);
					} catch (ReadOnlyFolderException e) {
						originalMsgFolder.open(Folder.READ_ONLY);
						isReadWrite = false;
					}
					mailInterfaceMonitor.changeNumActive(true);
					originalMsg = (MimeMessage) originalMsgFolder.getMessageByUID(mailId.getMsgUID());
					if (originalMsg.isSet(Flags.Flag.DRAFT) && msgObj.isDraft()) {
						if (!isReadWrite) {
							throw new OXMailException(MailCode.NO_DRAFT_EDIT, originalMsgFolder.getFullName());
						}
						/*
						 * A draft-edit. Delete old draft version to replace
						 * with newer one.
						 */
						originalMsg.setFlags(FLAGS_DELETED, true);
						originalMsgFolder.close(true);
						mailInterfaceMonitor.changeNumActive(false);
						/*
						 * Reset message reference cause not needed anymore
						 */
						msgObj.setMsgref(null);
					} else if (sendType == SENDTYPE_REPLY) {
						/*
						 * A reply! Appropiately set message headers
						 */
						final String pMsgId = originalMsg.getHeader("Message-Id", null);
						if (pMsgId != null) {
							newSMTPMsg.setHeader("In-Reply-To", pMsgId);
						}
						/*
						 * Set References header field
						 */
						final String pReferences = originalMsg.getHeader("References", null);
						final String pInReplyTo = originalMsg.getHeader("In-Reply-To", null);
						final StringBuilder refBuilder = new StringBuilder();
						if (pReferences != null) {
							/*
							 * The "References:" field will contain the contents
							 * of the parent's "References:" field (if any)
							 * followed by the contents of the parent's
							 * "Message-ID:" field (if any).
							 */
							refBuilder.append(pReferences);
						} else if (pInReplyTo != null) {
							/*
							 * If the parent message does not contain a
							 * "References:" field but does have an
							 * "In-Reply-To:" field containing a single message
							 * identifier, then the "References:" field will
							 * contain the contents of the parent's
							 * "In-Reply-To:" field followed by the contents of
							 * the parent's "Message-ID:" field (if any).
							 */
							refBuilder.append(pInReplyTo);
						}
						if (pMsgId != null) {
							if (refBuilder.length() > 0) {
								refBuilder.append(' ');
							}
							refBuilder.append(pMsgId);
						}
						if (refBuilder.length() > 0) {
							/*
							 * If the parent has none of the "References:",
							 * "In-Reply-To:", or "Message-ID:" fields, then the
							 * new message will have no "References:" field.
							 */
							newSMTPMsg.setHeader("References", refBuilder.toString());
						}
						/*
						 * Mark original message as answered
						 */
						if (isReadWrite) {
							try {
								originalMsg.setFlags(FLAGS_ANSWERED, true);
							} catch (MessagingException e) {
								LOG.error("Flag /ANSWERED could not be set in original message", e);
							}
						}
					}
				}
				/*
				 * Save draft message or send message
				 */
				if (msgObj.isDraft()) {
					/*
					 * Append message to folder "DRAFT"
					 */
					final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
					final IMAPFolder draftFolder = (IMAPFolder) imapStore
							.getFolder(prepareMailFolderParam(getDraftsFolder()));
					/*
					 * Fill message
					 */
					msgFiller = new MessageFiller(sessionObj, originalMsg, imapCon.getSession(), draftFolder);
					msgFiller.fillMessage(msgObj, newSMTPMsg, uploadEvent, sendType);
					checkAndCreateFolder(draftFolder, inboxFolder);
					if (!draftFolder.isOpen()) {
						draftFolder.open(Folder.READ_WRITE);
						mailInterfaceMonitor.changeNumActive(true);
					}
					newSMTPMsg.setFlag(Flags.Flag.DRAFT, true);
					long uidNext = draftFolder.getUIDNext();
					if (uidNext == -1) {
						/*
						 * UIDNEXT not supported
						 */
						uidNext = IMAPUtils.getUIDNext(draftFolder);
					}
					newSMTPMsg.saveChanges();
					final long start = System.currentTimeMillis();
					try {
						draftFolder.appendMessages(new Message[] { newSMTPMsg });
					} finally {
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						draftFolder.close(false);
						mailInterfaceMonitor.changeNumActive(false);
					}
					return new StringBuilder(draftFolder.getFullName()).append(Mail.SEPERATOR).append(uidNext)
							.toString();
				}
				final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
				final IMAPFolder sentFolder = (IMAPFolder) imapStore.getFolder(prepareMailFolderParam(getSentFolder()));
				/*
				 * Fill message
				 */
				msgFiller = new MessageFiller(sessionObj, originalMsg, imapCon.getSession(), usm
						.isNoCopyIntoStandardSentFolder() ? null : sentFolder);
				msgFiller.fillMessage(msgObj, newSMTPMsg, uploadEvent, sendType);
				/*
				 * Check recipients
				 */
				final Address[] allRecipients = newSMTPMsg.getAllRecipients();
				if (allRecipients == null || allRecipients.length == 0) {
					throw new OXMailException(MailCode.MISSING_RECIPIENTS);
				}
				/*
				 * Set the Reply-To header for future replies to this new
				 * message
				 */
				final InternetAddress[] ia;
				if (usm.getReplyToAddr() == null) {
					ia = new InternetAddress[msgObj.getFrom().size()];
					msgObj.getFrom().toArray(ia);
				} else {
					ia = InternetAddress.parse(usm.getReplyToAddr(), false);
				}
				newSMTPMsg.setReplyTo(ia);
				/*
				 * Set sent date if not done, yet
				 */
				if (newSMTPMsg.getSentDate() == null) {
					newSMTPMsg.setSentDate(new Date());
				}
				/*
				 * Set default subject if none set
				 */
				final String subject;
				if ((subject = newSMTPMsg.getSubject()) == null || subject.length() == 0) {
					newSMTPMsg.setSubject(MailStrings.DEFAULT_SUBJECT);
				}
				/*
				 * ENVELOPE-FROM
				 */
				if (IMAPProperties.isSMTPEnvelopeFrom()) {
					/*
					 * Set ENVELOPE-FROM in SMTP message to user's primary email
					 * address
					 */
					newSMTPMsg.setEnvelopeFrom(sessionObj.getUserObject().getMail());
				}
				try {
					final long start = System.currentTimeMillis();
					Transport transport = null;
					try {
						transport = imapCon.getSession().getTransport("smtp");
						if (IMAPProperties.isSmtpAuth()) {
							transport.connect(sessionObj.getIMAPProperties().getSmtpServer(), sessionObj
									.getIMAPProperties().getImapLogin(), sessionObj.getIMAPProperties()
									.getImapPassword());
						} else {
							transport.connect();
						}
						mailInterfaceMonitor.changeNumActive(true);
						newSMTPMsg.saveChanges();
						transport.sendMessage(newSMTPMsg, allRecipients);
					} finally {
						if (transport != null) {
							transport.close();
							mailInterfaceMonitor.changeNumActive(false);
						}
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
					if (LOG.isInfoEnabled()) {
						LOG.info("Message successfully sent ! ! ! (subject=" + newSMTPMsg.getSubject() + ')');
					}
				} catch (MessagingException e) {
					throw handleMessagingException(e, sessionObj.getIMAPProperties());
				}
				if (usm.isNoCopyIntoStandardSentFolder()) {
					/*
					 * No copy in sent folder
					 */
					return STR_EMPTY;
				}
				/*
				 * Append message to folder "SENT"
				 */
				checkAndCreateFolder(sentFolder, inboxFolder);
				if (!sentFolder.isOpen()) {
					sentFolder.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
				}
				long uidNext = sentFolder.getUIDNext();
				if (uidNext == -1) {
					/*
					 * UIDNEXT not supported
					 */
					uidNext = IMAPUtils.getUIDNext(sentFolder);
				}
				newSMTPMsg.setFlag(Flags.Flag.SEEN, true);
				final long start = System.currentTimeMillis();
				try {
					sentFolder.appendMessages(new Message[] { newSMTPMsg });
				} catch (MessagingException e) {
					if (e.getNextException() instanceof CommandFailedException) {
						final CommandFailedException exc = (CommandFailedException) e.getNextException();
						if (exc.getMessage().indexOf("Over quota") > 1) {
							throw new OXMailException(MailCode.COPY_TO_SENT_FOLDER_FAILED);
						}
					}
					LOG.error(new StringBuilder().append("Sent message could not be appended to default sent folder: ")
							.append(e.getMessage()).toString(), e);
					return STR_EMPTY;
				} finally {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					sentFolder.close(false);
					mailInterfaceMonitor.changeNumActive(false);
				}
				return new StringBuilder(sentFolder.getFullName()).append(Mail.SEPERATOR).append(uidNext).toString();

			} finally {
				if (originalMsgFolder != null && originalMsgFolder.isOpen()) {
					originalMsgFolder.close(false);
					mailInterfaceMonitor.changeNumActive(false);
					originalMsgFolder = null;
				}
				if (msgFiller != null) {
					msgFiller.close();
					msgFiller = null;
				}
			}
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		} catch (IOException e) {
			throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
		} catch (JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		}
	}

	private final void checkAndCreateFolder(final IMAPFolder newFolder, final IMAPFolder parent)
			throws MessagingException, OXException {
		if (!parent.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, parent.getFullName());
		}
		try {
			if (IMAPProperties.isSupportsACLs() && !parent.myRights().contains(Rights.Right.CREATE)) {
				throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(), parent.getFullName());
			}
		} catch (MessagingException e) {
			throw new OXMailException(MailCode.NO_ACCESS, getUserName(), parent.getFullName());
		}
		if (!newFolder.exists()) {
			final long start = System.currentTimeMillis();
			if (!newFolder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)) {
				throw new OXMailException(MailCode.FOLDER_CREATION_FAILED, newFolder.getFullName());
			}
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
	}

	private Folder getFolder(final IMAPFolder parent, final String folderName) throws MessagingException,
			OXMailException {
		if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
			throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, null, parent.getFullName());
		}
		if (parent instanceof DefaultFolder) {
			return imapStore.getFolder(folderName);
		}
		return imapStore.getFolder(new StringBuilder(100).append(parent.getFullName()).append(parent.getSeparator())
				.append(folderName).toString());
	}
	
	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#clearFolder(java.lang.String)
	 */
	public boolean clearFolder(final String folderArg) throws OXException {
		try {
			init();
			final String folder = folderArg == null ? INBOX : folderArg;
			setAndOpenFolder(folder, Folder.READ_WRITE);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			/*
			 * Mark all messages as /DELETED
			 */
			IMAPUtils.setAllSystemFlags(imapCon.getImapFolder(), FLAGS_DELETED, true);
			/*
			 * Force expunge on close()
			 */
			imapCon.setExpunge(true);
			return true;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#deleteMessage(java.lang.String,
	 *      long)
	 */
	public boolean deleteMessages(final String folderArg, final long[] msgUIDs, final boolean hardDelete)
			throws OXException {
		try {
			init();
			final String folder = folderArg == null ? INBOX : folderArg;
			setAndOpenFolder(folder, Folder.READ_WRITE);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			Message[] msgs;
			long start = System.currentTimeMillis();
			try {
				msgs = imapCon.getImapFolder().getMessagesByUID(msgUIDs);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			msgs = cleanMessageArray(msgs);
			if (msgs == null || msgs.length == 0) {
				throw new OXMailException(MailCode.MESSAGE_NOT_FOUND, Arrays.toString(msgUIDs), imapCon.getImapFolder().getFullName());
			}
			/*
			 * Perform "soft delete", means to copy message to default trash
			 * folder
			 */
			final boolean isTrashFolder = (folder.endsWith(usm.getStdTrashName()));
			if (!usm.isHardDeleteMsgs() && !hardDelete && !isTrashFolder) {
				/*
				 * Append message to folder "TRASH"
				 */
				final IMAPFolder inboxFolder = (IMAPFolder) imapStore.getFolder(INBOX);
				final IMAPFolder trashFolder = (IMAPFolder) imapStore.getFolder(prepareMailFolderParam(getTrashFolder()));
				checkAndCreateFolder(trashFolder, inboxFolder);
				start = System.currentTimeMillis();
				try {
					imapCon.getImapFolder().copyMessages(msgs, trashFolder);
				} catch (MessagingException e) {
					if (e.getNextException() instanceof CommandFailedException) {
						final CommandFailedException exc = (CommandFailedException) e.getNextException();
						if (exc.getMessage().indexOf("Over quota") > -1) {
							/*
							 * We face an Over-Quota-Exception
							 */
							throw new OXMailException(MailCode.DELETE_FAILED_OVER_QUOTA);
						}
					}
					throw new OXMailException(MailCode.MOVE_ON_DELETE_FAILED);
				} finally {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
			}
			/*
			 * Mark messages as \DELETED
			 */
			IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, FLAGS_DELETED, true);
			imapCon.setExpunge(true);
			return true;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}
	
	/**
	 * Cleans <tt>null</tt> elements out of given array of
	 * <tt>javax.mail.Message</tt> instances
	 * 
	 * @return cleaned array of <tt>javax.mail.Message</tt> instances
	 */
	private static final Message[] cleanMessageArray(final Message[] cleanMe) {
		final List<Message> tmp = new ArrayList<Message>(cleanMe.length);
		for (int i = 0; i < cleanMe.length; i++) {
			if (cleanMe[i] != null) {
				tmp.add(cleanMe[i]);
			}
		}
		return tmp.toArray(new Message[tmp.size()]);
	}
	
	private static final int SPAM_HAM = -1;
	
	private static final int SPAM_NOOP = 0;
	
	private static final int SPAM_SPAM = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#copyMessage(java.lang.String,
	 *      java.lang.String, long, boolean)
	 */
	public long[] copyMessage(final String sourceFolderArg, final String destFolderArg, final long[] msgUIDs,
			final boolean move) throws OXException {
		try {
			if (sourceFolderArg == null || sourceFolderArg.length() == 0) {
				throw new OXMailException(MailCode.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "source");
			} else if (destFolderArg == null || destFolderArg.length() == 0) {
				throw new OXMailException(MailCode.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "target");
			}
			init();
			final String sourceFolder = prepareMailFolderParam(sourceFolderArg);
			final String destFolder = prepareMailFolderParam(destFolderArg);
			/*
			 * Open and check user rights on source folder
			 */
			setAndOpenFolder(sourceFolder, Folder.READ_WRITE);
			try {
				if (move && IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			/*
			 * Open and check user rights on destination folder
			 */
			setAndOpenTmpFolder(destFolder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs() && !getTmpRights().contains(Rights.Right.INSERT)) {
					throw new OXMailException(MailCode.NO_INSERT_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			/*
			 * Copy operation depending on whether UIDPLUS capability is
			 * supported or not.
			 */
			if (IMAPProperties.getImapCapabilities().hasUIDPlus()) {
				long start = System.currentTimeMillis();
				try {
					final long[] res = IMAPUtils.copyUID(imapCon.getImapFolder(), msgUIDs, destFolder, false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					/*
					 * Spam related action
					 */
					final String spamFullName = prepareMailFolderParam(getSpamFolder());
					final int spamAction = spamFullName.equals(imapCon.getImapFolder().getFullName()) ? SPAM_HAM
							: (spamFullName.equals(tmpFolder.getFullName()) ? SPAM_SPAM : SPAM_NOOP);
					if (spamAction != SPAM_NOOP) {
						try {
							for (int i = 0; i < msgUIDs.length; i++) {
								if (spamAction == SPAM_SPAM) {
									SpamAssassin.trainMessageAsSpam(imapCon.getImapFolder().getMessageByUID(msgUIDs[i]));
								} else if (spamAction == SPAM_HAM) {
									SpamAssassin.trainMessageAsHam(imapCon.getImapFolder().getMessageByUID(msgUIDs[i]));
								}
							}
						} catch (OXException e) {
							LOG.error(e.getMessage(), e);
						}
					}
					if (move) {
						start = System.currentTimeMillis();
						IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, FLAGS_DELETED, true);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						/*
						 * Expunge "moved" messages immediately
						 */
						try {
							imapCon.getImapFolder().getProtocol().uidexpunge(IMAPUtils.toUIDSet(msgUIDs));
						} catch (ProtocolException e) {
							throw new OXMailException(
									MailCode.MOVE_PARTIALLY_COMPLETED, e,
									com.openexchange.tools.oxfolder.OXFolderManagerImpl
											.getUserName(sessionObj), Arrays
											.toString(msgUIDs), imapCon
											.getImapFolder().getFullName(), e
											.getMessage());
						}
					}
					return res;
				} catch (MessagingException e) {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					throw handleMessagingException(e);
				}
			}
			Message[] msgs = null;
			long start = System.currentTimeMillis();
			try {
				msgs = imapCon.getImapFolder().getMessagesByUID(msgUIDs);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			msgs = cleanMessageArray(msgs);
			if (msgs == null || msgs.length == 0) {
				throw new OXMailException(MailCode.MESSAGE_NOT_FOUND, Arrays.toString(msgUIDs), sourceFolder);
			}
			/*
			 * Perform "move" or "copy" operation
			 */
			long uidNext = tmpFolder.getUIDNext();
			if (uidNext == -1) {
				/*
				 * UIDNEXT not supported
				 */
				uidNext = IMAPUtils.getUIDNext(tmpFolder);
			}
			final long[] retval = new long[msgUIDs.length];
			for (int i = 0; i < retval.length; i++) {
				if (msgs[i] == null) {
					retval[i] = -1;
				} else {
					retval[i] = uidNext++;
				}
			}
			start = System.currentTimeMillis();
			try {
				imapCon.getImapFolder().copyMessages(msgs, tmpFolder);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			/*
			 * Spam related action
			 */
			final String spamFullName = prepareMailFolderParam(getSpamFolder());
			final int spamAction = spamFullName.equals(imapCon.getImapFolder().getFullName()) ? SPAM_HAM
					: (spamFullName.equals(tmpFolder.getFullName()) ? SPAM_SPAM : SPAM_NOOP);
			if (spamAction != SPAM_NOOP) {
				for (int i = 0; i < msgs.length; i++) {
					if (spamAction == SPAM_SPAM) {
						SpamAssassin.trainMessageAsSpam(msgs[i]);
					} else if (spamAction == SPAM_HAM) {
						SpamAssassin.trainMessageAsHam(msgs[i]);
					}
				}
			}
			/*
			 * Delete source messages on move
			 */
			if (move) {
				for (int i = 0; i < msgs.length; i++) {
					if (msgs[i] != null) {
						msgs[i].setFlags(FLAGS_DELETED, true);
					}
				}
				/*
				 * Expunge is going to be invoked in close() method
				 */
				imapCon.setExpunge(true);
			}
			/*
			 * Return new message id
			 */
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	public boolean updateMessageColorLabel(final String folderArg, final long msgUID, final int newColorLabel)
			throws OXException {
		if (!IMAPProperties.isUserFlagsEnabled()) {
			/*
			 * User flags are disabled
			 */
			if (LOG.isDebugEnabled()) {
				LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
			}
			return true;
		}
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_WRITE);
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			if (!IMAPUtils.supportsUserDefinedFlags(imapCon.getImapFolder())) {
				LOG.error(new StringBuilder().append("Folder \"").append(imapCon.getImapFolder().getFullName()).append(
						"\" does not support user-defined flags. Update of color flag ignored."));
				return true;
			}
			final String colorLabel = JSONMessageObject.getColorLabelStringValue(newColorLabel);
			final Message msg;
			final long start = System.currentTimeMillis();
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			/*
			 * Remove old color label flag
			 */
			Flags newMsgFlags = new Flags();
			final String[] userFlags = msg.getFlags().getUserFlags();
			NextFlag: for (int i = 0; i < userFlags.length; i++) {
				if (userFlags[i].startsWith(JSONMessageObject.COLOR_LABEL_PREFIX)) {
					newMsgFlags.add(userFlags[i]);
					break NextFlag;
				}
			}
			msg.setFlags(newMsgFlags, false);
			/*
			 * Add new color label flag
			 */
			newMsgFlags = new Flags();
			newMsgFlags.add(colorLabel);
			msg.setFlags(newMsgFlags, true);
			return true;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#updateMessageFlags(java.lang.String,
	 *      long, int, boolean)
	 */
	public boolean updateMessageFlags(final String folderArg, final long msgUID, final int flagBitsArg,
			final boolean flagsVal) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_WRITE);
			/*
			 * r - read (SELECT the mailbox, perform CHECK, FETCH, PARTIAL, SEARCH, COPY from mailbox)
			 * s - keep seen/unseen information across sessions (STORE SEEN flag)
			 * w - write (STORE flags other than SEEN and DELETED)
			 */
			try {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), imapCon.getImapFolder().getFullName());
			}
			final Message msg;
			final long start = System.currentTimeMillis();
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			/*
			 * Remove non user-alterable system flags
			 */
			int flagBits = flagBitsArg;
			if (((flagBits & JSONMessageObject.BIT_RECENT) > 0)) {
				flagBits = flagBits ^ JSONMessageObject.BIT_RECENT;
			}
			if (((flagBits & JSONMessageObject.BIT_USER) > 0)) {
				flagBits = flagBits ^ JSONMessageObject.BIT_USER;
			}
			/*
			 * Set new flags...
			 */
			final Flags affectedFlags = new Flags();
			if (((flagBits & JSONMessageObject.BIT_ANSWERED) > 0)) {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
				affectedFlags.add(Flags.Flag.ANSWERED);
			}
			if (((flagBits & JSONMessageObject.BIT_DELETED) > 0)) {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
				affectedFlags.add(Flags.Flag.DELETED);
			}
			if (((flagBits & JSONMessageObject.BIT_DRAFT) > 0)) {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
				affectedFlags.add(Flags.Flag.DRAFT);
			}
			if (((flagBits & JSONMessageObject.BIT_FLAGGED) > 0)) {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
				affectedFlags.add(Flags.Flag.FLAGGED);
			}
			if (((flagBits & JSONMessageObject.BIT_SEEN) > 0)) {
				if (IMAPProperties.isSupportsACLs() && !imapCon.getMyRights().contains(Rights.Right.KEEP_SEEN)) {
					throw new OXMailException(MailCode.NO_KEEP_SEEN_ACCESS, getUserName(), imapCon.getImapFolder()
							.getFullName());
				}
				affectedFlags.add(Flags.Flag.SEEN);
			}
			if (affectedFlags.getSystemFlags().length > 0) {
				msg.setFlags(affectedFlags, flagsVal);
			}
			return true;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getRootFolders()
	 */
	public SearchIterator getRootFolders() throws OXException {
		try {
			init();
			final List<MailFolderObject> list = new ArrayList<MailFolderObject>(1);
			final IMAPFolder defaultFolder = (IMAPFolder) imapStore.getDefaultFolder();
			list.add(new MailFolderObject(defaultFolder));
			return new SearchIteratorAdapter(list.iterator(), 1);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}
	
	private static final String PATTERN_ALL = "%";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getChildFolders(java.lang.String)
	 */
	public SearchIterator getChildFolders(final String parentFolderArg, final boolean all) throws OXException {
		try {
			init();
			final String parentFolder = prepareMailFolderParam(parentFolderArg);
			final IMAPFolder p;
			if (parentFolder.equals(MailFolderObject.DEFAULT_IMAP_FOLDER)) {
				p = (IMAPFolder) imapStore.getDefaultFolder();
				if (!p.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, MailFolderObject.DEFAULT_IMAP_FOLDER);
				}
			} else {
				p = (IMAPFolder) imapStore.getFolder(parentFolder);
				canLookUpFolder(p);
			}
			final Folder[] childFolders;
			final long start = System.currentTimeMillis();
			try {
				if (IMAPProperties.isIgnoreSubscription() || all) {
					childFolders = p.list(PATTERN_ALL);
				} else {
					childFolders = p.listSubscribed(PATTERN_ALL);
				}
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final List<MailFolderObject> list = new ArrayList<MailFolderObject>(childFolders.length);
			for (int i = 0; i < childFolders.length; i++) {
				list.add(new MailFolderObject((IMAPFolder) childFolders[i]));
			}
			return new SearchIteratorAdapter(list.iterator(), childFolders.length);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getAllFolders()
	 */
	public SearchIterator getAllFolders() throws OXException {
		try {
			init();
			final IMAPFolder defaultFolder = (IMAPFolder) imapStore.getDefaultFolder();
			final Folder[] allFolders;
			final long start = System.currentTimeMillis();
			try {
				allFolders = defaultFolder.list("*");
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final List<MailFolderObject> list = new ArrayList<MailFolderObject>(allFolders.length);
			for (int i = 0; i < allFolders.length; i++) {
				list.add(new MailFolderObject((IMAPFolder) allFolders[i]));
			}
			return new SearchIteratorAdapter(list.iterator(), allFolders.length);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getFolder(java.lang.String,
	 *      boolean)
	 */
	public MailFolderObject getFolder(final String folderArg, final boolean checkFolder) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			if (folder.equals(MailFolderObject.DEFAULT_IMAP_FOLDER)) {
				return new MailFolderObject((IMAPFolder) imapStore.getDefaultFolder());
			}
			final IMAPFolder retval = (IMAPFolder) imapStore.getFolder(folder);
			if (checkFolder) {
				if (!retval.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, folder);
				} else if (IMAPProperties.isSupportsACLs()) {
					try {
						if (!retval.myRights().contains(Rights.Right.READ)) {
							throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), retval.getFullName());
						}
					} catch (MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(), retval.getFullName());
					}
				}
			}
			return new MailFolderObject(retval);
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getPathToDefaultFolder(java.lang.String)
	 */
	public SearchIterator getPathToDefaultFolder(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			if (folder.equals(MailFolderObject.DEFAULT_IMAP_FOLDER)) {
				return SearchIteratorAdapter.createEmptyIterator();
			}
			final String defaultFolder = imapStore.getDefaultFolder().getFullName();
			IMAPFolder f = (IMAPFolder) imapStore.getFolder(folder);
			if (!f.exists()) {
				throw new OXMailException(MailCode.FOLDER_NOT_FOUND, folder);
			} else if (IMAPProperties.isSupportsACLs()) {
				try {
					if (!f.myRights().contains(Rights.Right.READ)) {
						throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), f.getFullName());
					}
				} catch (MessagingException e) {
					throw new OXMailException(MailCode.NO_ACCESS, getUserName(), f.getFullName());
				}
			}
			final List<MailFolderObject> retval = new ArrayList<MailFolderObject>();
			while (!f.getFullName().equals(defaultFolder)) {
				retval.add(new MailFolderObject(f));
				f = (IMAPFolder) f.getParent();
			}
			return new SearchIteratorAdapter(retval.iterator(), retval.size());
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	private final void canLookUpFolder(final IMAPFolder f) throws OXException {
		try {
			if (!f.exists()) {
				throw new OXMailException(MailCode.FOLDER_NOT_FOUND, f.getFullName());
			} else if (IMAPProperties.isSupportsACLs()) {
				try {
					if (!f.myRights().contains(Rights.Right.LOOKUP)) {
						throw new OXMailException(MailCode.NO_LOOKUP_ACCESS, getUserName(), f.getFullName());
					}
				} catch (MessagingException e) {
					/*
					 * No rights defined on folder. Allow look up.
					 */
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder("No rights defined for folder ").append(f.getFullName()).append(": ")
								.append(e.getMessage()).toString());
					}
				}
			}
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#saveFolder(com.openexchange.groupware.container.MailFolderObject)
	 */
	public String saveFolder(final MailFolderObject folderObj) throws OXException {
		String retval = null;
		try {
			init();
			if (folderObj.exists()) {
				/*
				 * Update
				 */
				IMAPFolder updateMe = folderObj.getImapFolder();
				if (updateMe.getFullName().equalsIgnoreCase(INBOX)) {
					throw new OXMailException(MailCode.NO_FOLDER_UPDATE, INBOX);
				}
				if (isDefaultFolder(updateMe.getFullName())) {
					throw new OXMailException(MailCode.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
				}
				/*
				 * Is move operation?
				 */
				final String oldParent = updateMe.getParent().getFullName();
				final String newParent = prepareMailFolderParam(folderObj.getParentFullName());
				final boolean move = (newParent != null && !newParent.equalsIgnoreCase(oldParent));
				/*
				 * Is rename operation?
				 */
				final String oldName = updateMe.getName();
				final String newName = folderObj.getName();
				final boolean rename = (!move && newName != null && !newName.equalsIgnoreCase(oldName));
				if (move) {
					final IMAPFolder destFolder = ((IMAPFolder) (MailFolderObject.DEFAULT_IMAP_FOLDER.equals(newParent) ? imapStore
							.getDefaultFolder()
							: imapStore.getFolder(newParent)));
					if (!destFolder.exists()) {
						throw new OXMailException(MailCode.FOLDER_NOT_FOUND, newParent);
					}
					if (destFolder instanceof DefaultFolder) {
						if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
							throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS,
									MailCode.NO_DEFAULT_FOLDER_UPDATE);
						}
					} else {
						try {
							if (IMAPProperties.isSupportsACLs()
									&& !destFolder.myRights().contains(Rights.Right.CREATE)) {
								throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(), newParent);
							}
						} catch (MessagingException e) {
							throw new OXMailException(MailCode.NO_ACCESS, getUserName(), newParent);
						}
					}
					updateMe = moveFolder(updateMe, destFolder, newName);
				}
				if (rename) {
					/*
					 * Rename.
					 */
					try {
						if (IMAPProperties.isSupportsACLs() && !updateMe.myRights().contains(Rights.Right.CREATE)) {
							throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(), updateMe.getFullName());
						}
					} catch (MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(), updateMe.getFullName());
					}
					/*
					 * Rename can only be invoked on a closed folder
					 */
					if (updateMe.isOpen()) {
						updateMe.close(false);
						mailInterfaceMonitor.changeNumActive(false);
					}
					final String parentFullName = folderObj.getImapFolder().getParent().getFullName();
					StringBuilder tmp = new StringBuilder();
					if (parentFullName.length() > 0) {
						tmp.append(parentFullName).append(folderObj.getSeparator());
					}
					tmp.append(folderObj.getName());
					final IMAPFolder renameFolder = (IMAPFolder) imapStore.getFolder(tmp.toString());
					tmp = null;
					if (renameFolder.exists()) {
						throw new OXMailException(MailCode.DUPLICATE_FOLDER, renameFolder.getFullName());
					}
					final String newFullName = renameFolder.getFullName();
					final String oldFullName = updateMe.getFullName();
					boolean success = false;
					updateMe.setSubscribed(false);
					final long start = System.currentTimeMillis();
					try {
						success = updateMe.renameTo(renameFolder);
						updateMe.setSubscribed(true);
					} finally {
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
					/*
					 * Success?
					 */
					if (!success) {
						throw new OXMailException(MailCode.UPDATE_FAILED, updateMe.getFullName());
					}
					updateMe = (IMAPFolder) imapStore.getFolder(oldFullName);
					if (updateMe.exists()) {
						deleteFolder(updateMe);
					}
					updateMe = (IMAPFolder) imapStore.getFolder(newFullName);
				}
				if (!IMAPProperties.isIgnoreSubscription() && folderObj.containsSubscribe()) {
					updateMe.setSubscribed(folderObj.isSubscribed());
					IMAPUtils.forceSetSubscribed(imapStore, updateMe.getFullName(), folderObj.isSubscribed());
				}
				ACLS: if (folderObj.containsACLs()) {
					/*
					 * Wrapper object contains rights. No simple rename but a
					 * whole ACL re-set
					 */
					ACL[] oldACLs = updateMe.getACL();
					ACL[] newACLs = folderObj.getACL();
					if (equals(oldACLs, newACLs)) {
						break ACLS;
					}
					try {
						if (IMAPProperties.isSupportsACLs() && !updateMe.myRights().contains(Rights.Right.ADMINISTER)) {
							throw new OXMailException(MailCode.NO_ADMINISTER_ACCESS, getUserName(), updateMe
									.getFullName());
						}
					} catch (MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(), updateMe.getFullName());
					}
					/*
					 * Delete old ACLs
					 */
					String[] oldNames = new String[oldACLs.length];
					for (int i = 0; i < oldACLs.length; i++) {
						oldNames[i] = oldACLs[i].getName();
					}
					oldACLs = null;
					for (int i = 0; i < oldNames.length; i++) {
						updateMe.removeACL(oldNames[i]);
					}
					oldNames = null;
					/*
					 * Add new ACLs from folderObj
					 */
					for (int i = 0; i < newACLs.length; i++) {
						updateMe.addACL(newACLs[i]);
					}
					newACLs = null;
				}
				retval = updateMe.getFullName();
			} else {
				/*
				 * Insert
				 */
				final String parentStr = prepareMailFolderParam(folderObj.getParentFullName());
				final IMAPFolder parent = MailFolderObject.DEFAULT_IMAP_FOLDER.equals(parentStr) ? (IMAPFolder) imapStore
						.getDefaultFolder()
						: (IMAPFolder) imapStore.getFolder(parentStr);
				if (!parent.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, parentStr);
				} else if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, parentStr);
				} else if (parent instanceof DefaultFolder) {
					if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
						throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS,
								MailCode.NO_DEFAULT_FOLDER_UPDATE);
					}
				} else if (IMAPProperties.isSupportsACLs()) {
					try {
						if (!parent.myRights().contains(Rights.Right.CREATE)) {
							throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(), parentStr);
						}
					} catch (MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(), parentStr);
					}
				}
				if (folderObj.getName().indexOf(parent.getSeparator()) != -1) {
					throw new OXMailException(MailCode.INVALID_FOLDER_NAME, parent.getSeparator());
				}
				final IMAPFolder createMe = (IMAPFolder) getFolder(parent, folderObj.getName());
				if (createMe.exists()) {
					throw new OXMailException(MailCode.DUPLICATE_FOLDER, createMe.getFullName());
				}
				final long start = System.currentTimeMillis();
				try {
					if (!createMe.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)) {
						throw new OXMailException(MailCode.FOLDER_CREATION_FAILED, createMe.getFullName());
					}
					createMe.setSubscribed(true);
					IMAPUtils.forceSetSubscribed(imapStore, createMe.getFullName(), true);
				} finally {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
				if (folderObj.containsACLs()) {
					/*
					 * Wrapper object contains rights. Add new ACLs from
					 * folderObj
					 */
					for (int i = 0; i < folderObj.getACL().length; i++) {
						createMe.addACL(folderObj.getACL()[i]);
					}
				}
				retval = createMe.getFullName();
			}
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}
	
	private static final boolean equals(final ACL[] acls1, final ACL[] acls2) {
		if (acls1.length != acls2.length) {
			return false;
		}
		for ( ACL acl1 : acls1 ) {
			boolean found = false;
			Inner: for (ACL acl2 : acls2) {
				if (acl1.getName().equals(acl2.getName())) {
					found = true;
					if (!acl1.getRights().equals(acl2.getRights())) {
						return false;
					}
					break Inner;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	private final IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName)
			throws MessagingException, OXException {
		String name = folderName;
		if (name == null) {
			name = toMove.getName();
		}
		return moveFolder(toMove, destFolder, name, true);
	}

	private final IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName,
			final boolean checkForDuplicate) throws MessagingException, OXException {
		if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
			throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder.getFullName());
		} else if (IMAPProperties.isSupportsACLs()) {
			try {
				if (!toMove.myRights().contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(), toMove.getFullName());
				} else if (!toMove.myRights().contains(Rights.Right.CREATE)) {
					throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(), toMove.getFullName());
				}
			} catch (MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(), toMove.getFullName());
			}
		}
		/*
		 * Move by creating a new folder, copying all messages and deleting old
		 * folder
		 */
		StringBuilder sb = new StringBuilder();
		if (destFolder.getFullName().length() > 0) {
			sb.append(destFolder.getFullName()).append(destFolder.getSeparator());
		}
		sb.append(folderName);
		final IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.toString());
		sb = null;
		if (checkForDuplicate && newFolder.exists()) {
			throw new OXMailException(MailCode.DUPLICATE_FOLDER, folderName);
		}
		if (!newFolder.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES)) {
			throw new OXMailException(MailCode.FOLDER_CREATION_FAILED, newFolder.getFullName());
		}
		try {
			try {
				newFolder.open(Folder.READ_WRITE);
				mailInterfaceMonitor.changeNumActive(true);
			} catch (ReadOnlyFolderException e) {
				throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(), newFolder.getFullName());
			}
			if (!toMove.isOpen()) {
				toMove.open(Folder.READ_ONLY);
				mailInterfaceMonitor.changeNumActive(true);
			}
			final long start = System.currentTimeMillis();
			try {
				toMove.copyMessages(toMove.getMessages(), newFolder);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
		} finally {
			newFolder.close(false);
			MailInterfaceImpl.mailInterfaceMonitor.changeNumActive(false);
		}
		/*
		 * Iterate subfolders
		 */
		final Folder[] subFolders = toMove.list();
		for (int i = 0; i < subFolders.length; i++) {
			moveFolder((IMAPFolder) subFolders[i], newFolder, subFolders[i].getName(), false);
		}
		/*
		 * Delete old folder
		 */
		if (toMove.isOpen()) {
			toMove.close(false);
			mailInterfaceMonitor.changeNumActive(false);
		}
		final long start = System.currentTimeMillis();
		try {
			toMove.setSubscribed(false);
		} finally {
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
		if (!toMove.delete(true)) {
			throw new OXMailException(MailCode.DELETE_FAILED, toMove.getFullName());
		}
		return newFolder;
	}

	private final void deleteFolder(final IMAPFolder deleteMe) throws OXException, MessagingException {
		if (deleteMe.getFullName().equalsIgnoreCase(INBOX)) {
			throw new OXMailException(MailCode.NO_FOLDER_DELETE, INBOX);
		}
		if (isDefaultFolder(deleteMe.getFullName())) {
			throw new OXMailException(MailCode.NO_DEFAULT_FOLDER_DELETE, deleteMe.getFullName());
		}
		if (!deleteMe.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, deleteMe.getFullName());
		}
		try {
			if (IMAPProperties.isSupportsACLs() && !deleteMe.myRights().contains(Rights.Right.CREATE)) {
				throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(), deleteMe.getFullName());
			}
		} catch (MessagingException e) {
			throw new OXMailException(MailCode.NO_ACCESS, getUserName(), deleteMe.getFullName());
		}
		if (deleteMe.isOpen()) {
			deleteMe.close(false);
			mailInterfaceMonitor.changeNumActive(false);
		}
		final long start = System.currentTimeMillis();
		try {
			deleteMe.setSubscribed(false);
			if (!deleteMe.delete(true)) {
				throw new OXMailException(MailCode.DELETE_FAILED, deleteMe.getFullName());
			}
		} finally {
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
	}

	private boolean isDefaultFolder(final String folderFullName) throws OXException {
		boolean isDefaultFolder = false;
		isDefaultFolder = (folderFullName.equalsIgnoreCase(INBOX));
		for (int index = 0; index < 4 && !isDefaultFolder; index++) {
			isDefaultFolder |= (folderFullName.equalsIgnoreCase(prepareMailFolderParam(getStdFolder(index))));
		}
		return isDefaultFolder;
	}

	public String deleteFolder(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			final IMAPFolder deleteMe = (IMAPFolder) imapStore.getFolder(folder);
			final String retval = deleteMe.getFullName();
			deleteFolder(deleteMe);
			return retval;
		} catch (MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties());
		}
	}

	public String getInboxFolder() throws OXException {
		return getStdFolder(INDEX_INBOX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getDraftsFolder()
	 */
	public String getDraftsFolder() throws OXException {
		return getStdFolder(INDEX_DRAFTS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getSentFolder()
	 */
	public String getSentFolder() throws OXException {
		return getStdFolder(INDEX_SENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getSpamFolder()
	 */
	public String getSpamFolder() throws OXException {
		return getStdFolder(INDEX_SPAM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getTrashFolder()
	 */
	public String getTrashFolder() throws OXException {
		return getStdFolder(INDEX_TRASH);
	}

	private String getStdFolder(final int index) throws OXException {
		try {
			init();
			if (INDEX_INBOX == index) {
				final Folder inbox = imapStore.getFolder("INBOX");
				return MailFolderObject.prepareFullname(inbox.getFullName(), inbox.getSeparator());
			}
			return usm.getStandardFolder(index);
		} catch (MessagingException e) {
			throw handleMessagingException(e);
		}
	}

	private final String getUserName() {
		if (sessionObj == null) {
			return STR_EMPTY;
		}
		return new StringBuilder().append(sessionObj.getUserObject().getDisplayName()).append(" (").append(
				sessionObj.getUserObject().getId()).append(')').toString();
	}

	public static OXMailException handleMessagingException(final MessagingException e) {
		return handleMessagingException(e, null);
	}
	
	private static final String ERR_AUTH_FAILED = "bad authentication failed";
	
	private static final String ERR_TMP = "temporary error, please try again later";

	public static OXMailException handleMessagingException(final MessagingException e, final IMAPProperties imapProps) {
		final OXMailException oxme;
		if (e instanceof AuthenticationFailedException) {
			final boolean temporary = ERR_TMP.equals(e.getMessage().toLowerCase(Locale.ENGLISH));
			if (imapProps == null) {
				if (temporary) {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, STR_EMPTY, STR_EMPTY, STR_EMPTY);
				} else {
					oxme = new OXMailException(MailCode.INVALID_CREDENTIALS, e, STR_EMPTY, STR_EMPTY, STR_EMPTY);
				}
			} else {
				if (temporary) {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, imapProps.getImapServer(),
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(imapProps.getUser(),
									imapProps.getContext()), imapProps.getContext().getContextId());
				} else {
					oxme = new OXMailException(MailCode.INVALID_CREDENTIALS, e, imapProps.getImapServer(),
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(imapProps.getUser(),
									imapProps.getContext()), imapProps.getContext().getContextId());
				}
			}
		} else if (e instanceof FolderClosedException) {
			oxme = new OXMailException(MailCode.FOLDER_CLOSED, e, e.getMessage());
		} else if (e instanceof FolderNotFoundException) {
			oxme = new OXMailException(MailCode.FOLDER_NOT_FOUND, e, e.getMessage());
		} else if (e instanceof IllegalWriteException) {
			oxme = new OXMailException(MailCode.ILLEGAL_WRITE, e, e.getMessage());
		} else if (e instanceof MessageRemovedException) {
			oxme = new OXMailException(MailCode.MESSAGE_REMOVED, e, e.getMessage());
		} else if (e instanceof MethodNotSupportedException) {
			oxme = new OXMailException(MailCode.METHOD_NOT_SUPPORTED, e, e.getMessage());
		} else if (e instanceof NoSuchProviderException) {
			oxme = new OXMailException(MailCode.NO_SUCH_PROVIDER, e, e.getMessage());
		} else if (e instanceof ParseException) {
			if (e instanceof AddressException) {
				final String ref = ((AddressException) e).getRef() == null ? STR_EMPTY : ((AddressException) e)
						.getRef();
				oxme = new OXMailException(MailCode.INVALID_EMAIL_ADDRESS, e, ref);
			} else {
				oxme = new OXMailException(MailCode.PARSE_ERROR, e, STR_EMPTY, e.getMessage());
			}
		} else if (e instanceof ReadOnlyFolderException) {
			oxme = new OXMailException(MailCode.READ_ONLY_FOLDER, e, e.getMessage());
		} else if (e instanceof SearchException) {
			oxme = new OXMailException(MailCode.SEARCH_ERROR, e, e.getMessage());
		} else if (e instanceof SendFailedException) {
			final SendFailedException exc = (SendFailedException) e;
			oxme = new OXMailException(MailCode.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
		} else if (e instanceof StoreClosedException) {
			oxme = new OXMailException(MailCode.STORE_CLOSED, e, e.getMessage());
		} else {
			/*
			 * No subclass of MessagingException
			 */
			if (e.getNextException() instanceof BindException) {
				oxme = new OXMailException(MailCode.BIND_ERROR, e, imapProps == null ? STR_EMPTY : imapProps
						.getImapPort());
			} else if (e.getNextException() instanceof ConnectException) {
				OXMailException tmp = null;
				try {
					if (IMAPProperties.getImapConnectionTimeout() > 0) {
						/*
						 * Most modern IP stack implementations sense connection
						 * idleness, and abort the connection attempt, resulting
						 * in a java.net.ConnectionException
						 */
						mailInterfaceMonitor.changeNumTimeoutConnections(true);
						tmp = new OXMailException(MailCode.CONNECT_ERROR, e, imapProps == null ? STR_EMPTY : imapProps
								.getImapServer(), imapProps == null ? STR_EMPTY : imapProps.getImapLogin());
						tmp.setCategory(Category.TRY_AGAIN);
					} else {
						tmp = new OXMailException(MailCode.CONNECT_ERROR, e, imapProps == null ? STR_EMPTY : imapProps
								.getImapServer(), imapProps == null ? STR_EMPTY : imapProps.getImapLogin());
					}
				} catch (IMAPException oxExc) {
					LOG.error(oxExc.getMessage(), e);
					tmp = new OXMailException(MailCode.IMAP_ERROR, e, e.getMessage());
				}
				oxme = tmp;
			} else if (e.getNextException() instanceof ConnectionResetException) {
				mailInterfaceMonitor.changeNumBrokenConnections(true);
				oxme = new OXMailException(MailCode.CONNECTION_RESET, e, new Object[0]);
			} else if (e.getNextException() instanceof NoRouteToHostException) {
				oxme = new OXMailException(MailCode.NO_ROUTE_TO_HOST, e, imapProps == null ? STR_EMPTY : imapProps
						.getImapServer());
			} else if (e.getNextException() instanceof PortUnreachableException) {
				oxme = new OXMailException(MailCode.PORT_UNREACHABLE, e, imapProps == null ? STR_EMPTY : imapProps
						.getImapPort());
			} else if (e.getNextException() instanceof SocketException) {
				/*
				 * Treat dependent on message
				 */
				final SocketException se = (SocketException) e.getNextException();
				if ("Socket closed".equals(se.getMessage()) || "Connection reset".equals(se.getMessage())) {
					mailInterfaceMonitor.changeNumBrokenConnections(true);
					oxme = new OXMailException(MailCode.BROKEN_CONNECTION, e, imapProps == null ? STR_EMPTY : imapProps
							.getImapServer());
				} else {
					oxme = new OXMailException(MailCode.SOCKET_ERROR, e, e.getMessage());
				}
			} else if (e.getNextException() instanceof UnknownHostException) {
				oxme = new OXMailException(MailCode.UNKNOWN_HOST, e, e.getMessage());
			} else if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_AUTH_FAILED) != -1) {
				if (imapProps == null) {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, STR_EMPTY, STR_EMPTY, STR_EMPTY);
				} else {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, imapProps.getImapServer(),
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(imapProps.getUser(),
									imapProps.getContext()), imapProps.getContext().getContextId());
				}
			} else {
				/*
				 * Default case
				 */
				oxme = new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
		}
		return oxme;
	}

	public DefaultIMAPConnection getImapConnection() {
		return imapCon;
	}

	private final static String prepareMailFolderParam(final String folderStringArg) {
		if (folderStringArg == null) {
			return null;
		} else if (MailFolderObject.DEFAULT_IMAP_FOLDER.equals(folderStringArg)) {
			return folderStringArg;
		} else if (folderStringArg.startsWith(MailFolderObject.DEFAULT_IMAP_FOLDER)) {
			return folderStringArg.substring(8);
		}
		return folderStringArg;
	}

}
