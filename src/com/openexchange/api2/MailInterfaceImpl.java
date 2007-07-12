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
import static com.openexchange.groupware.container.mail.parser.MessageUtils.performLineWrap;
import static com.openexchange.groupware.container.mail.parser.MessageUtils.removeHdrLineBreak;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import com.openexchange.groupware.AbstractOXException;
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
import com.openexchange.groupware.container.mail.parser.SpamMessageHandler;
import com.openexchange.groupware.contexts.Context;
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
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.Collections.SmartLongArray;
import com.openexchange.tools.ajp13.AJPv13Config;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.mail.ContentType;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
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

	/*
	 * Message header constants
	 */
	private static final String HDR_MESSAGE_ID = "Message-Id";

	private static final String HDR_IN_REPLY_TO = "In-Reply-To";

	private static final String HDR_REFERENCES = "References";

	private static final String HDR_CONTENT_DISPOSITION = "Content-Disposition";

	private static final String HDR_CONTENT_TYPE = "Content-Type";

	private static final String HDR_MIME_VERSION = "MIME-Version";

	private static final String HDR_X_PRIORITY = "X-Priority";

	private static final String HDR_REPLY_TO = "Reply-To";

	private static final String HDR_TO = "To";

	private static final String HDR_CC = "Cc";

	private static final String HDR_BCC = "Bcc";

	private static final String HDR_SUBJECT = "Subject";

	private static final String HDR_DISP_TO = "Disposition-Notification-To";

	private static final String HDR_ORGANIZATION = "Organization";

	private static final String HDR_X_MAILER = "X-Mailer";

	private static final String HDR_ADDR_DELIM = ",";

	private static final String HDR_X_SPAM_FLAG = "X-Spam-Flag";

	/*
	 * MIME type constants
	 */
	private static final String MIME_MSG_DISPNOT_MDN_CHARSET_UTF8 = "message/disposition-notification; name=MDNPart1.txt; charset=UTF-8";

	private static final String MIME_TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=UTF-8";

	private static final String MIME_TEXT_PLAIN = "text/plain";

	private static final String MIME_TEXT_HTML = "text/html";

	private static final String MIME_TEXT_CALENDAR = "text/calendar";

	private static final String MIME_TEXT_X_V_CALENDAR = "text/x-vCalendar";

	private static final String MIME_TEXT_VCARD = "text/vcard";

	private static final String MIME_TEXT_X_VCARD = "text/x-vcard";

	/*
	 * Property name constants
	 */
	private static final String PROP_MAIL_REPLYALLCC = "mail.replyallcc";

	private static final String PROP_MAIL_ALTERNATES = "mail.alternates";

	private static final String PROP_ALLOWREADONLYSELECT = "mail.imap.allowreadonlyselect";

	private static final String PROP_SMTPHOST = "mail.smtp.host";

	private static final String PROP_SMTPPORT = "mail.smtp.port";

	private static final String PROP_SMTPLOCALHOST = "mail.smtp.localhost";

	private static final String PROP_MAIL_SMTP_AUTH = "mail.smtp.auth";

	private static final String PROP_MAIL_IMAP_CONNECTIONTIMEOUT = "mail.imap.connectiontimeout";

	private static final String PROP_MAIL_IMAP_TIMEOUT = "mail.imap.timeout";

	private static final String PROP_MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

	private static final String PROP_MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

	private static final String PROP_MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";

	private static final String PROP_MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

	private static final String PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK = "mail.imap.socketFactory.fallback";

	private static final String PROP_MAIL_IMAP_SOCKET_FACTORY_PORT = "mail.imap.socketFactory.port";

	private static final String PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS = "mail.imap.socketFactory.class";

	private static final String PROP_MAIL_MIME_CHARSET = "mail.mime.charset";

	private static final String PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT = "mail.imap.connectionpooltimeout";

	private static final String PROP_MAIL_IMAP_CONNECTIONPOOLSIZE = "mail.imap.connectionpoolsize";

	private static final String PROP_MAIL_MIME_DECODETEXT_STRICT = "mail.mime.decodetext.strict";

	private static final String PROP_MAIL_MIME_ENCODEEOL_STRICT = "mail.mime.encodeeol.strict";

	private static final String PROP_MAIL_MIME_BASE64_IGNOREERRORS = "mail.mime.base64.ignoreerrors";

	/*
	 * Other string constants
	 */
	private static final String CLASS_TRUSTALLSSLSOCKETFACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

	private static final String PROTOCOL_SMTP = "smtp";

	private static final String MP_REPORT_DISPNOT = "report; report-type=disposition-notification";

	private static final String PREFIX_RE = "Re: ";

	private static final String STR_ALL = "ALL";

	private static final String VERSIT_VTODO = "VTODO";

	private static final String VERSIT_VEVENT = "VEVENT";

	private static final String STR_1DOT0 = "1.0";

	private static final String STR_INBOX = "INBOX";

	private static final String STR_CHARSET = "charset";

	private static final String CHARENC_UTF8 = "UTF-8";

	private static final String CHARENC_ISO_8859_1 = "ISO-8859-1";

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	private static final String STR_EMPTY = "";

	private static final String STR_YES = "YES";

	private static final String SWITCH_DEFAULT_FOLDER = "Switching to default value %s";

	private static final String WARN_FLD_ALREADY_CLOSED = "Invoked close() on a closed folder";

	/*
	 * Other constants
	 */
	private static Properties IMAP_PROPS;

	private static boolean imapPropsInitialized;

	private static final Lock LOCK_CREATE = new ReentrantLock();

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static final Lock LOCK_CON = new ReentrantLock();

	private static final Condition LOCK_CON_CONDITION = LOCK_CON.newCondition();

	private static final int QUEUE_SIZE = AJPv13Config.getAJPListenerPoolSize() * 2;

	private static final Queue<MailInterfaceImpl> MI_QUEUE = new ConcurrentLinkedQueue<MailInterfaceImpl>();

	private Properties imapProps;

	private SessionObject sessionObj;

	private UserSettingMail usm;

	private TimeZone userTimeZone;

	private DefaultIMAPConnection imapCon;

	private boolean init;

	private IMAPFolder tmpFolder;

	private Message markAsSeen;

	private final boolean queued;

	static {
		/*
		 * Init queue
		 */
		for (int i = 0; i < QUEUE_SIZE; i++) {
			MI_QUEUE.offer(new MailInterfaceImpl());
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("\n\t" + MI_QUEUE.size() + " MailInterface implementations created in advance & queued\n");
		}
		/*
		 * Proceed
		 */
		mailInterfaceMonitor = new MailInterfaceMonitor();
		try {
			/*
			 * Register monitor
			 */
			final String[] sa = MonitorAgent.getDomainAndName(mailInterfaceMonitor.getClass().getName(), true);
			MonitorAgent.registerMBeanGlobal(new ObjectName(sa[0], "name", sa[1]), mailInterfaceMonitor);
		} catch (final MalformedObjectNameException e) {
			LOG.error(e.getMessage(), e);
		} catch (final NullPointerException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private static final MailInterfaceImpl getQueuedMailInterface(final SessionObject sessionObj) throws OXException {
		if (MI_QUEUE.isEmpty()) {
			return new MailInterfaceImpl(sessionObj);
		}
		final MailInterfaceImpl mi = MI_QUEUE.poll();
		if (mi == null) {
			return new MailInterfaceImpl(sessionObj);
		}
		mi.applySessionObject(sessionObj);
		return mi;
	}

	private static final boolean putQueuedMailInterface(final MailInterfaceImpl mi) {
		return mi.queued ? MI_QUEUE.offer(mi) : false;
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
				} catch (final IMAPException e) {
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
	
	/**
	 * Creates a <b>cloned</b> version of default IMAP properties
	 * 
	 * @return a cloned version of default IMAP properties
	 * @throws OXException
	 */
	public final static Properties getDefaultIMAPProperties() throws OXException {
		if (!imapPropsInitialized) {
			LOCK_INIT.lock();
			try {
				if (null == IMAP_PROPS) {
					initializeIMAPProperties();
					imapPropsInitialized = true;
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
		return (Properties) IMAP_PROPS.clone();
	}

	/**
	 * This method can only be exclusively accessed
	 */
	private final static void initializeIMAPProperties() throws OXException {
		/*
		 * Define imap properties
		 */
		IMAP_PROPS = ((Properties) (System.getProperties().clone()));
		/*
		 * Set some global JavaMail properties
		 */
		IMAP_PROPS.put(PROP_MAIL_MIME_BASE64_IGNOREERRORS, STR_TRUE);
		IMAP_PROPS.put(PROP_ALLOWREADONLYSELECT, STR_TRUE);
		IMAP_PROPS.put(PROP_MAIL_MIME_ENCODEEOL_STRICT, STR_TRUE);
		IMAP_PROPS.put(PROP_MAIL_MIME_DECODETEXT_STRICT, STR_FALSE);
		/*
		 * A connected IMAPStore maintains a pool of IMAP protocol objects for
		 * use in communicating with the IMAP server. The IMAPStore will create
		 * the initial AUTHENTICATED connection and seed the pool with this
		 * connection. As folders are opened and new IMAP protocol objects are
		 * needed, the IMAPStore will provide them from the connection pool, or
		 * create them if none are available. When a folder is closed, its IMAP
		 * protocol object is returned to the connection pool if the pool is not
		 * over capacity.
		 */
		IMAP_PROPS.put(PROP_MAIL_IMAP_CONNECTIONPOOLSIZE, "1");
		/*
		 * A mechanism is provided for timing out idle connection pool IMAP
		 * protocol objects. Timed out connections are closed and removed
		 * (pruned) from the connection pool.
		 */
		IMAP_PROPS.put(PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT, "1000"); // 1 sec
		/*
		 * Fill global IMAP Properties only once and switch flag
		 */
		if (!IMAPProperties.isGlobalPropertiesLoaded()) {
			IMAPPropertiesFactory.loadGlobalImapProperties();
		}
		/*
		 * Initialize properties
		 */
		try {
			IMAP_PROPS.put(PROP_MAIL_MIME_CHARSET, IMAPProperties.getDefaultMimeCharset());
		} catch (final IMAPException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		/*
		 * Following properties define if IMAPS and/or SMTPS should be enabled
		 */
		try {
			if (IMAPProperties.isImapsEnabled()) {
				IMAP_PROPS.put(PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS, CLASS_TRUSTALLSSLSOCKETFACTORY);
				IMAP_PROPS.put(PROP_MAIL_IMAP_SOCKET_FACTORY_PORT, String.valueOf(IMAPProperties.getImapsPort()));
				IMAP_PROPS.put(PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK, STR_FALSE);
				IMAP_PROPS.put(PROP_MAIL_SMTP_STARTTLS_ENABLE, STR_TRUE);
			}
		} catch (final IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
		try {
			if (IMAPProperties.isSmtpsEnabled()) {
				IMAP_PROPS.put(PROP_MAIL_SMTP_SOCKET_FACTORY_CLASS, CLASS_TRUSTALLSSLSOCKETFACTORY);
				IMAP_PROPS.put(PROP_MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf(IMAPProperties.getSmtpsPort()));
				IMAP_PROPS.put(PROP_MAIL_SMTP_SOCKET_FACTORY_FALLBACK, STR_FALSE);
				IMAP_PROPS.put(PROP_MAIL_SMTP_STARTTLS_ENABLE, STR_TRUE);
			}
		} catch (final IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
		if (IMAPProperties.getSmtpLocalhost() != null) {
			IMAP_PROPS.put(PROP_SMTPLOCALHOST, IMAPProperties.getSmtpLocalhost());
		}
		try {
			if (IMAPProperties.getJavaMailProperties() != null) {
				/*
				 * Overwrite current JavaMail-Specific properties with the ones
				 * defined in javamail.properties
				 */
				IMAP_PROPS.putAll(IMAPProperties.getJavaMailProperties());
			}
		} catch (final IMAPException e) {
			LOG.error(e.getMessage(), e);
		}
		if (IMAPProperties.getImapTimeout() > 0) {
			IMAP_PROPS.put(PROP_MAIL_IMAP_TIMEOUT, Integer.valueOf(IMAPProperties.getImapTimeout()));
		}
		if (IMAPProperties.getImapConnectionTimeout() > 0) {
			IMAP_PROPS
					.put(PROP_MAIL_IMAP_CONNECTIONTIMEOUT, Integer.valueOf(IMAPProperties.getImapConnectionTimeout()));
		}
		IMAP_PROPS.put(PROP_MAIL_SMTP_AUTH, IMAPProperties.isSmtpAuth() ? STR_TRUE : STR_FALSE);
	}

	private MailInterfaceImpl() {
		super();
		queued = true;
	}

	private MailInterfaceImpl(final SessionObject sessionObj) throws OXException {
		super();
		queued = false;
		applySessionObject(sessionObj);
	}

	private final void applySessionObject(final SessionObject sessionObj) throws OXException {
		if (!sessionObj.getUserConfiguration().hasWebMail()) {
			throw new OXMailException(MailCode.NO_MAIL_MODULE_ACCESS, getUserName(sessionObj));
		} else if (sessionObj.getIMAPProperties().hasError()) {
			throw new OXMailException(sessionObj.getIMAPProperties().getError());
		}
		this.sessionObj = sessionObj;
		this.usm = sessionObj.getUserConfiguration().getUserSettingMail();
		userTimeZone = TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone());
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
					.getUserName(sessionObj), Integer.valueOf(sessionObj.getContext().getContextId()));
		}
		DefaultIMAPConnection imapCon = fetchCachedCon ? getCachedConnection(sessionObj) : null;
		if (imapCon != null) {
			try {
				final MailInterfaceImpl retval = getQueuedMailInterface(sessionObj);
				/*
				 * Apply cached connection
				 */
				retval.imapCon = imapCon;
				imapCon.connect();
				return retval;
			} catch (final NoSuchProviderException e) {
				throw handleMessagingException(e);
			} catch (final MessagingException e) {
				throw handleMessagingException(e);
			}
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
			} catch (final InterruptedException e) {
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
			return getQueuedMailInterface(sessionObj);
		}
		try {
			final MailInterfaceImpl retval = getQueuedMailInterface(sessionObj);
			/*
			 * Apply cached connection
			 */
			retval.imapCon = imapCon;
			imapCon.connect();
			return retval;
		} catch (final NoSuchProviderException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
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
		imapProps.put(PROP_SMTPHOST, sessionObj.getIMAPProperties().getSmtpServer());
		imapProps.put(PROP_SMTPPORT, String.valueOf(sessionObj.getIMAPProperties().getSmtpPort()));
		if (imapCon == null || !imapCon.isConnected()) {
			if (imapCon != null) {
				imapCon.close();
				imapCon = null;
			}
			imapCon = new DefaultIMAPConnection();
			imapCon.setProperties(imapProps);
			imapCon.setImapServer(sessionObj.getIMAPProperties().getImapServer(), sessionObj.getIMAPProperties()
					.getImapPort());
			imapCon.setUsername(sessionObj.getIMAPProperties().getImapLogin());
			imapCon.setPassword(sessionObj.getIMAPProperties().getImapPassword());
			final long start = System.currentTimeMillis();
			try {
				imapCon.connect();
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				mailInterfaceMonitor.changeNumSuccessfulLogins(true);
			} catch (final AuthenticationFailedException e) {
				mailInterfaceMonitor.changeNumFailedLogins(true);
				throw e;
			}
			/*
			 * Check if IMAP server capabilities were already loaded
			 */
			initializeCapabilities(imapCon.getIMAPStore());
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
				final String[] stdFolderNames = new String[IMAPProperties.isSpamEnabled() ? 6 : 4];
				if (usm.getStdDraftsName() == null || usm.getStdDraftsName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME,
								UserSettingMail.STD_DRAFTS);
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_DRAFTS), e);
					}
					stdFolderNames[INDEX_DRAFTS] = UserSettingMail.STD_DRAFTS;
				} else {
					stdFolderNames[INDEX_DRAFTS] = usm.getStdDraftsName();
				}
				if (usm.getStdSentName() == null || usm.getStdSentName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME,
								UserSettingMail.STD_SENT);
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SENT), e);
					}
					stdFolderNames[INDEX_SENT] = UserSettingMail.STD_SENT;
				} else {
					stdFolderNames[INDEX_SENT] = usm.getStdSentName();
				}
				if (usm.getStdSpamName() == null || usm.getStdSpamName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME,
								UserSettingMail.STD_SPAM);
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SPAM), e);
					}
					stdFolderNames[INDEX_SPAM] = UserSettingMail.STD_SPAM;
				} else {
					stdFolderNames[INDEX_SPAM] = usm.getStdSpamName();
				}
				if (usm.getStdTrashName() == null || usm.getStdTrashName().length() == 0) {
					if (LOG.isWarnEnabled()) {
						final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME,
								UserSettingMail.STD_TRASH);
						LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_TRASH), e);
					}
					stdFolderNames[INDEX_TRASH] = UserSettingMail.STD_TRASH;
				} else {
					stdFolderNames[INDEX_TRASH] = usm.getStdTrashName();
				}
				if (usm.isSpamEnabled()) {
					if (usm.getConfirmedSpam() == null || usm.getConfirmedSpam().length() == 0) {
						if (LOG.isWarnEnabled()) {
							final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME,
									UserSettingMail.STD_CONFIRMED_SPAM);
							LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_CONFIRMED_SPAM), e);
						}
						stdFolderNames[INDEX_CONFIRMED_SPAM] = UserSettingMail.STD_CONFIRMED_SPAM;
					} else {
						stdFolderNames[INDEX_CONFIRMED_SPAM] = usm.getConfirmedSpam();
					}
					if (usm.getConfirmedHam() == null || usm.getConfirmedHam().length() == 0) {
						if (LOG.isWarnEnabled()) {
							final OXMailException e = new OXMailException(MailCode.MISSING_DEFAULT_FOLDER_NAME,
									UserSettingMail.STD_CONFIRMED_HAM);
							LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_CONFIRMED_HAM), e);
						}
						stdFolderNames[INDEX_CONFIRMED_HAM] = UserSettingMail.STD_CONFIRMED_HAM;
					} else {
						stdFolderNames[INDEX_CONFIRMED_HAM] = usm.getConfirmedHam();
					}
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
				final Folder inboxFolder = imapCon.getIMAPStore().getFolder(STR_INBOX);
				if (!inboxFolder.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, STR_INBOX);
				}
				if (!inboxFolder.isSubscribed()) {
					/*
					 * Subscribe INBOX folder
					 */
					inboxFolder.setSubscribed(true);
				}
				final boolean noInferiors = ((inboxFolder.getType() & Folder.HOLDS_FOLDERS) == 0);
				final StringBuilder tmp = new StringBuilder(128);
				/*
				 * Determine where to create default folders and store as a
				 * prefix for folder fullname
				 */
				if (!noInferiors
						&& (!isAltNamespaceEnabled(imapCon.getIMAPStore()) || IMAPProperties
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
				 * Check default folders
				 */
				final int l = usm.isSpamEnabled() ? defaultFolderNames.length : defaultFolderNames.length - 2;
				for (int i = 0; i < l; i++) {
					usm.setStandardFolder(i, checkDefaultFolder(imapCon.getIMAPStore(), prefix, defaultFolderNames[i],
							type, tmp));
				}
			}
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}
	
	private static final String checkDefaultFolder(final IMAPStore store, final String prefix, final String name,
			final int type, final StringBuilder tmp) throws MessagingException {
		/*
		 * Check default folder
		 */
		boolean checkSubscribed = true;
		final Folder f = store.getFolder(tmp.append(prefix).append(prepareMailFolderParam(name)).toString());
		tmp.setLength(0);
		final long start = System.currentTimeMillis();
		if (!f.exists() && !f.create(type)) {
			final OXMailException oxme = new OXMailException(MailCode.NO_DEFAULT_FOLDER_CREATION, tmp.append(prefix)
					.append(name).toString());
			tmp.setLength(0);
			LOG.error(oxme.getMessage(), oxme);
			checkSubscribed = false;
		}
		if (checkSubscribed && !f.isSubscribed()) {
			try {
				f.setSubscribed(true);
			} catch (final MethodNotSupportedException e) {
				LOG.error(e.getMessage(), e);
			} catch (final MessagingException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		if (LOG.isDebugEnabled()) {
			LOG.debug(tmp.append("Default folder \"").append(f.getFullName()).append("\" successfully checked")
					.toString());
			tmp.setLength(0);
		}
		return MailFolderObject.prepareFullname(f.getFullName(), f.getSeparator());
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
			return imapCon.getIMAPStore();
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#close()
	 */
	public void close(final boolean putIntoCache) throws OXException {
		boolean close = true;
		try {
			if (init) {
				try {
					keepSeen();
					sessionObj.setMailSession(null);
					if (tmpFolder != null) {
						try {
							tmpFolder.close(true); // expunge
						} catch (final MessagingException e) {
							LOG.error("Temporary folder could not be closed", e);
						} catch (final IllegalStateException e) {
							LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
						} finally {
							mailInterfaceMonitor.changeNumActive(false);
							tmpFolder = null;
						}
					}
					closeIMAPConnection(putIntoCache, sessionObj, imapCon);
					close = false;
				} catch (final MessagingException e) {
					throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
				}
			}
		} finally {
			if (close) {
				try {
					closeIMAPConnection(putIntoCache, sessionObj, imapCon);
				} catch (final MessagingException e) {
					LOG.error(e.getMessage(), e);
				}
				this.imapCon = null;
			}
			this.imapCon = null;
			this.imapProps = null;
			this.init = false;
			this.sessionObj = null;
			this.usm = null;
			this.userTimeZone = null;
			putQueuedMailInterface(this);
		}
	}

	/**
	 * Either closes or caches given IMAP connection. Because of only one
	 * connection is allowed to be cached per user, given IMAP connection is
	 * going to be closed if cache already contains a cached connection for
	 * current user; otherwise it is going to be put into cache. But in any case
	 * the associated IMAP folder is expunged if expunge flag is set in
	 * <code>DefaultIMAPConnection</code> instance.
	 * 
	 * @param imapCon -
	 *            the IMAP connection
	 * @return <code>true</code> if connection has been closed;
	 *         <code>false</code> if connection has been cached
	 * @throws MessagingException
	 */
	public static final boolean closeIMAPConnection(final DefaultIMAPConnection imapCon) throws MessagingException {
		return closeIMAPConnection(false, null, imapCon);
	}

	private static final boolean closeIMAPConnection(final boolean putIntoCache, final SessionObject sessionObj,
			final DefaultIMAPConnection imapCon) throws MessagingException {
		if (imapCon != null) {
			boolean closeCon = true;
			try {
				/*
				 * Expunge folder
				 */
				try {
					expungeDefaultIMAPConnection(imapCon);
				} catch (final MessagingException e) {
					LOG.error(new StringBuilder(100).append("Mail folder ").append(
							imapCon.getImapFolder().getFullName()).append(" could NOT be expunged for user ").append(
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(sessionObj)).toString(), e);
				}
				/*
				 * Release folder
				 */
				if (imapCon.getImapFolder() != null) {
					try {
						imapCon.getImapFolder().close(false);
						mailInterfaceMonitor.changeNumActive(false);
					} catch (final IllegalStateException e) {
						LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
					} finally {
						imapCon.resetImapFolder();
					}
				}
				/*
				 * Close connection or put into cache
				 */
				boolean cached = false;
				try {
					cached = putIntoCache && imapCon.isConnected()
							&& IMAPConnectionCacheManager.getInstance().putIMAPConnection(sessionObj, imapCon);
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
					cached = false;
				}
				/*
				 * Return if connection could be put into cache
				 */
				if (cached) {
					closeCon = false;
					return false;
				}
				/*
				 * Close connection
				 */
				imapCon.close();
				closeCon = false;
				try {
					if (IMAPProperties.getMaxNumOfIMAPConnections() > 0) {
						LOCK_CON.lock();
						try {
							LOCK_CON_CONDITION.signalAll();
							if (LOG.isDebugEnabled()) {
								LOG.debug("Sending signal to possible waiting threads");
							}
						} finally {
							LOCK_CON.unlock();
						}
					}
				} catch (final IMAPException e) {
					throw new MessagingException(e.getMessage(), e);
				}
			} finally {
				if (closeCon) {
					try {
						imapCon.close();
					} catch (final Throwable t) {
						LOG.error("IMAP conenction could not be closed!", t);
					}
				}
			}
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
			IMAPUtils.fastExpunge(imapFolder);
			// imapFolder.expunge();
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		} catch (final ProtocolException e) {
			throw new MessagingException(e.getLocalizedMessage(), e);
		} finally {
			imapCon.setExpunge(false);
		}
	}

	private static final Flags FLAGS_SEEN = new Flags(Flags.Flag.SEEN);

	private static final String ERROR_KEEP_SEEN = "/SEEN flag cannot be set: ";

	private final void keepSeen() throws OXException {
		if (imapCon == null || !imapCon.isConnectedUnsafe()) {
			return;
		} else if (markAsSeen == null) {
			return;
		}
		final IMAPFolder imapFolder = imapCon.getImapFolder();
		try {
			if (!imapCon.isHoldsMessages()) {
				/*
				 * Folder is not selectable, further working working on this
				 * folder will result in an IMAP error telling "Mailbox does not
				 * exist".
				 */
				return;
			}
		} catch (final MessagingException e2) {
			throw handleMessagingException(e2, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
		try {
			try {
				if (imapFolder.getMode() == Folder.READ_ONLY) {
					imapFolder.close(false);
					mailInterfaceMonitor.changeNumActive(false);
					imapFolder.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
				}
			} catch (final IllegalStateException e) {
				/*
				 * Folder is closed
				 */
				try {
					if (imapFolder.isOpen()) {
						imapFolder.close(false);
						mailInterfaceMonitor.changeNumActive(false);
					}
					imapFolder.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
				} catch (final ReadOnlyFolderException e1) {
					LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e1.getMessage()).toString(), e1);
					return;
				}
			}
			if (IMAPProperties.isSupportsACLs()) {
				try {
					if (!sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.KEEP_SEEN)) {
						/*
						 * User has no \KEEP_SEEN right
						 */
						if (LOG.isWarnEnabled()) {
							LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append("Missing KEEP_SEEN right").toString());
						}
						return;
					}
				} catch (final MessagingException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
					}
					return;
				}
			}
			markAsSeen.setFlags(FLAGS_SEEN, true);
		} catch (final MessageRemovedException e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
			}
			return;
		} catch (final MessagingException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
			}
			return;
		} finally {
			markAsSeen = null;
		}
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
			final IMAPFolder inboxFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(STR_INBOX);
			final Quota[] folderQuota;
			final long start = System.currentTimeMillis();
			try {
				folderQuota = inboxFolder.getQuota();
			} catch (final MessagingException mexc) {
				if (mexc.getNextException() instanceof ParsingException) {
					return new long[] { MailInterface.UNLIMITED_QUOTA, MailInterface.UNLIMITED_QUOTA };
				}
				throw mexc;
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			final IMAPFolder inboxFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(STR_INBOX);
			final long start = System.currentTimeMillis();
			final Quota[] folderQuota;
			try {
				folderQuota = inboxFolder.getQuota();
			} catch (final MessagingException mexc) {
				if (mexc.getNextException() instanceof ParsingException) {
					return MailInterface.UNLIMITED_QUOTA;
				}
				throw mexc;
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			final IMAPFolder inboxFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(STR_INBOX);
			final long start = System.currentTimeMillis();
			final Quota[] folderQuota;
			try {
				folderQuota = inboxFolder.getQuota();
			} catch (final MessagingException mexc) {
				if (mexc.getNextException() instanceof ParsingException) {
					return MailInterface.UNLIMITED_QUOTA;
				}
				throw mexc;
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			setAndOpenFolder(folder == null ? STR_INBOX : folder, Folder.READ_ONLY);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			setAndOpenFolder(folder == null ? STR_INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getMessageCount();
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			setAndOpenFolder(folder == null ? STR_INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getNewMessageCount();
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			setAndOpenFolder(folder == null ? STR_INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getUnreadMessageCount();
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			setAndOpenFolder(folder == null ? STR_INBOX : folder, Folder.READ_ONLY);
			return imapCon.getImapFolder().getDeletedMessageCount();
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getNewMessages(java.lang.String,
	 *      int, int)
	 */
	public SearchIterator getNewMessages(final String folderArg, final int sortCol, final int order,
			final int[] fields, final int limit) throws OXException {
		try {
			if (limit == 0) {
				return SearchIterator.EMPTY_ITERATOR;
			}
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder == null ? STR_INBOX : folder, Folder.READ_ONLY);
			try {
				if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
				} catch (final ProtocolException e1) {
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
			} catch (final ProtocolException e) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (limit > 0) {
				final int newLength = Math.min(limit, newMsgs.length);
				final Message[] retval = new Message[newLength];
				for (int i = 0; i < newLength; i++) {
					retval[i] = newMsgs[i];
				}
				return SearchIteratorAdapter.createArrayIterator(retval);
			}
			return SearchIteratorAdapter.createArrayIterator(newMsgs);
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
						} catch (final ProtocolException e) {
							throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
						} finally {
							mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						}
					}
					applicationSort = false;
				} catch (final Throwable t) {
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
					} catch (final ProtocolException e) {
						throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
					} finally {
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
				}
				if (retval == null || retval.length == 0) {
					/*
					 * No messages found
					 */
					return SearchIteratorAdapter.createEmptyIterator();
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		} catch (final IMAPException e) {
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		} catch (final IMAPException e) {
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
					throw new OXMailException(MailCode.INVALID_SEARCH_PARAMS, Integer.valueOf(searchCols.length),
							Integer.valueOf(searchPatterns.length));
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
				} catch (final ProtocolException e) {
					throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
				} finally {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
				applicationSearch = false;
			} catch (final Throwable t) {
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
			} catch (final ProtocolException e) {
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
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
					threadResp = IMAPUtils.getThreadResponse(imapCon.getImapFolder(), new StringBuilder(STR_ALL));
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
			} catch (final ProtocolException e) {
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		} catch (final IMAPException e) {
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
			if (MailFolderObject.DEFAULT_IMAP_FOLDER_ID.equals(folder)) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES,
						MailFolderObject.DEFAULT_IMAP_FOLDER_NAME);
			}
			init();
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
			}
			final Message[] msgs;
			try {
				final long start = System.currentTimeMillis();
				if (uids.length < IMAPProperties.getMessageFetchLimit()) {
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), uids, IMAPUtils.getDefaultFetchProfile(),
							false);
				} else {
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), uids, fields, -1, false);
				}
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				/*
				 * Force message cache update
				 */
				imapCon.getImapFolder().close(false);
				imapCon.resetImapFolder();
			} catch (final ProtocolException e) {
				throw new OXMailException(MailCode.PROTOCOL_ERROR, e, e.getMessage());
			}
			return msgs;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			if (MailFolderObject.DEFAULT_IMAP_FOLDER_ID.equals(folder)) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES,
						MailFolderObject.DEFAULT_IMAP_FOLDER_NAME);
			}
			if (imapCon.getImapFolder() == null) {
				/*
				 * Not initialized
				 */
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapCon.getIMAPStore().getFolder(STR_INBOX)
						: imapCon.getIMAPStore().getFolder(folder)));
			} else if (!imapCon.getImapFolder().getFullName().equals(folder)) {
				/*
				 * Another folder than previous one
				 */
				if (imapCon.getImapFolder().isOpen()) {
					if (markAsSeen != null) {
						/*
						 * Mark stored message as seen
						 */
						keepSeen();
					}
					imapCon.getImapFolder().close(false);
					mailInterfaceMonitor.changeNumActive(false);
					imapCon.resetImapFolder();
				}
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapCon.getIMAPStore().getFolder(STR_INBOX)
						: imapCon.getIMAPStore().getFolder(folder)));
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
				keepSeen();
			}
			final long start = System.currentTimeMillis();
			final Message msg;
			try {
				msg = imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (msg == null) {
				throw new OXMailException(MailCode.MESSAGE_NOT_FOUND, String.valueOf(msgUID), imapCon.getImapFolder()
						.toString());
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
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(OXMailException.getFormattedMessage(MailCode.FLAG_FAILED, IMAPUtils.FLAG_DRAFT,
									String.valueOf(msg.getMessageNumber()), imapCon.getImapFolder().getFullName(), e
											.getMessage()), e);
						}
					}
				} else if (!isDraftFld && isDraft) {
					try {
						msg.setFlags(FLAGS_DRAFT, false);
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(OXMailException.getFormattedMessage(MailCode.FLAG_FAILED, IMAPUtils.FLAG_DRAFT,
									String.valueOf(msg.getMessageNumber()), imapCon.getImapFolder().getFullName(), e
											.getMessage()), e);
						}
					}
				}
			}
			markAsSeen = msg;
			return msg;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapCon.getIMAPStore().getFolder(STR_INBOX)
						: imapCon.getIMAPStore().getFolder(folder)));
			} else if (!imapCon.getImapFolder().getFullName().equals(folder)) {
				/*
				 * Another folder than previous one
				 */
				if (imapCon.getImapFolder().isOpen()) {
					imapCon.getImapFolder().close(false);
					mailInterfaceMonitor.changeNumActive(false);
					imapCon.resetImapFolder();
				}
				imapCon.setImapFolder((IMAPFolder) (folder == null ? imapCon.getIMAPStore().getFolder(STR_INBOX)
						: imapCon.getIMAPStore().getFolder(folder)));
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#saveVersitAttachment(java.lang.String,
	 *      long, java.lang.String)
	 */
	public CommonObject[] saveVersitAttachment(final String folderArg, final long msgUID, final String partIdentifier)
			throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_ONLY);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
			if (versitPart.isMimeType(MIME_TEXT_X_VCARD) || versitPart.isMimeType(MIME_TEXT_VCARD)) {
				/*
				 * Define versit reader
				 */
				final ContentType contentTypeObj = new ContentType(versitPart.getContentType());
				final VersitDefinition def = Versit.getDefinition(contentTypeObj.getBaseType());
				final VersitDefinition.Reader r = def.getReader(versitPart.getInputStream(), contentTypeObj
						.containsParameter(STR_CHARSET) ? CHARENC_UTF8 : contentTypeObj.getParameter(STR_CHARSET));
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
							if (VERSIT_VEVENT.equals(vo.name)) {
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
							} else if (VERSIT_VTODO.equals(vo.name)) {
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
						} catch (final ConverterException e) {
							throw new OXMailException(MailCode.FAILED_VERSIT_SAVE);
						}
					}
				} finally {
					if (oxc != null) {
						oxc.close();
						oxc = null;
					}
				}
			} else if (versitPart.isMimeType(MIME_TEXT_X_V_CALENDAR) || versitPart.isMimeType(MIME_TEXT_CALENDAR)) {
				/*
				 * Define versit reader for VCard
				 */
				final ContentType contentTypeObj = new ContentType(versitPart.getContentType());
				final VersitDefinition def = Versit.getDefinition(contentTypeObj.getBaseType());
				final VersitDefinition.Reader r = def.getReader(versitPart.getInputStream(), contentTypeObj
						.containsParameter(STR_CHARSET) ? CHARENC_UTF8 : contentTypeObj.getParameter(STR_CHARSET));
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
						} catch (final ConverterException e) {
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		} catch (final IOException e) {
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
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
			final String subjectHeader = HDR_SUBJECT;
			final String subjectPrefix = PREFIX_RE;
			String subjectHdrValue = originalMsg.getHeader(subjectHeader, null);
			if (subjectHdrValue == null) {
				subjectHdrValue = STR_EMPTY;
			}
			final String rawSubject = removeHdrLineBreak(subjectHdrValue);
			try {
				final String decodedSubject = decodeMultiEncodedHeader(MimeUtility.decodeText(rawSubject));
				final String newSubject = decodedSubject.regionMatches(true, 0, subjectPrefix, 0, 4) ? decodedSubject
						: new StringBuilder().append(subjectPrefix).append(decodedSubject).toString();
				retval.setSubject(newSubject);
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
				/*
				 * Handle raw value: setting prefix to raw subject value still
				 * leaves a valid and correct encoded header
				 */
				originalMsg.setHeader(subjectHeader, new StringBuilder().append(subjectPrefix).append(rawSubject)
						.toString());
			}
			/*
			 * Set the appropiate recipients
			 */
			final InternetAddress[] recipientAddrs;
			if (originalMsg.getHeader(HDR_REPLY_TO) == null) {
				/*
				 * Set from as recipient
				 */
				recipientAddrs = (InternetAddress[]) originalMsg.getFrom();
			} else {
				/*
				 * Message holds field 'Reply-To'
				 */
				final String replyToStr = originalMsg.getHeader(HDR_REPLY_TO, HDR_ADDR_DELIM);
				if (replyToStr == null) {
					recipientAddrs = new InternetAddress[0];
				} else {
					recipientAddrs = InternetAddress.parseHeader(removeHdrLineBreak(replyToStr), true);
				}
			}
			if (replyToAll) {
				/*
				 * Create a filter which is used to sort out addresses before
				 * adding them to either field 'To' or 'Cc'
				 */
				final Set<InternetAddress> filter = new HashSet<InternetAddress>();
				/*
				 * Add user's address to filter
				 */
				if (InternetAddress.getLocalAddress(imapCon.getSession()) != null) {
					filter.add(InternetAddress.getLocalAddress(imapCon.getSession()));
				}
				/*
				 * Add any other address the user is known by to filter
				 */
				final String alternates = imapCon.getSession().getProperty(PROP_MAIL_ALTERNATES);
				if (alternates != null) {
					filter.addAll(Arrays.asList(InternetAddress.parse(alternates, false)));
				}
				/*
				 * Add user's aliases to filter
				 */
				final String[] userAddrs = sessionObj.getUserObject().getAliases();
				if (userAddrs != null && userAddrs.length > 0) {
					final StringBuilder addrBuilder = new StringBuilder();
					addrBuilder.append(userAddrs[0]);
					for (int i = 1; i < userAddrs.length; i++) {
						addrBuilder.append(',').append(userAddrs[i]);
					}
					filter.addAll(Arrays.asList(InternetAddress.parse(addrBuilder.toString(), false)));
				}
				/*
				 * Determine if other original recipients should be added to Cc
				 */
				final boolean replyallcc = STR_TRUE.equalsIgnoreCase(imapCon.getSession().getProperty(
						PROP_MAIL_REPLYALLCC));
				/*
				 * Filter recipients from 'Reply-To'/'From' field
				 */
				final Set<InternetAddress> filteredAddrs = filter(filter, recipientAddrs);
				/*
				 * Add filtered recipients from 'To' field
				 */
				String hdrVal = originalMsg.getHeader(HDR_TO, HDR_ADDR_DELIM);
				InternetAddress[] toAddrs = null;
				if (hdrVal != null) {
					filteredAddrs.addAll(filter(filter, (toAddrs = InternetAddress.parse(removeHdrLineBreak(hdrVal),
							true))));
				}
				/*
				 * ... and add filtered addresses to either 'To' or 'Cc' field
				 */
				if (!filteredAddrs.isEmpty()) {
					if (replyallcc) {
						retval.addCCAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()]));
					} else {
						retval.addToAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()]));
					}
				} else if (toAddrs != null) {
					final Set<InternetAddress> tmpSet = new HashSet<InternetAddress>(Arrays.asList(recipientAddrs));
					tmpSet.removeAll(Arrays.asList(toAddrs));
					if (tmpSet.isEmpty()) {
						/*
						 * The message was sent from the user to hisself. In
						 * this special case allow user's own address in field
						 * 'To' to avoid an empty 'To' field
						 */
						retval.addToAddresses(recipientAddrs);
					}
				}
				/*
				 * Filter recipients from 'Cc' field
				 */
				filteredAddrs.clear();
				hdrVal = originalMsg.getHeader(HDR_CC, HDR_ADDR_DELIM);
				if (hdrVal != null) {
					filteredAddrs.addAll(filter(filter, InternetAddress.parse(removeHdrLineBreak(hdrVal), true)));
				}
				if (!filteredAddrs.isEmpty()) {
					retval.addCCAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()]));
				}
				/*
				 * Filter recipients from 'Bcc' field
				 */
				filteredAddrs.clear();
				hdrVal = originalMsg.getHeader(HDR_BCC, HDR_ADDR_DELIM);
				if (hdrVal != null) {
					filteredAddrs.addAll(filter(filter, InternetAddress.parse(removeHdrLineBreak(hdrVal), true)));
				}
				if (!filteredAddrs.isEmpty()) {
					retval.addBccAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()]));
				}
				// /*
				// * Don't remove duplicate newsgroups
				// */
				// internetAddrs = (InternetAddress[])
				// originalMsg.getRecipients(javax.mail.internet.MimeMessage.RecipientType.NEWSGROUPS);
				// if (internetAddrs != null && internetAddrs.length > 0) {
				// retval.setNewsgroupRecipients(internetAddrs);
				// }
			} else {
				/*
				 * Plain reply: Just add recipients from 'Reply-To'/'From' field
				 */
				retval.addToAddresses(recipientAddrs);
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
				mao.setContent(msgHandler.getReplyText());
				mao.setContentType(msgHandler.isHtml() ? MIME_TEXT_HTML : MIME_TEXT_PLAIN);
				mao.setDisposition(Part.INLINE);
				mao.setSize(-1);
				retval.addMessageAttachment(mao);
			}
			/*
			 * Set message reference
			 */
			retval.setMsgref(msgUID);
			return retval;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	private static final Set<InternetAddress> EMPTY_SET = new HashSet<InternetAddress>(0);

	private static final Set<InternetAddress> filter(final Set<InternetAddress> filter, final InternetAddress[] addrs) {
		if (addrs == null) {
			return EMPTY_SET;
		}
		final Set<InternetAddress> set = new HashSet<InternetAddress>(Arrays.asList(addrs));
		/*
		 * Remove all addresses from set which are contained in filter
		 */
		set.removeAll(filter);
		/*
		 * Add new addresses to filter
		 */
		filter.addAll(set);
		return set;
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner
	 */
	private final void setAndOpenFolder(final String folderName, final int mode) throws MessagingException, OXException {
		final boolean isDefaultFolder = folderName.equals(MailFolderObject.DEFAULT_IMAP_FOLDER_ID);
		final boolean isIdenticalFolder;
		if (isDefaultFolder) {
			isIdenticalFolder = (imapCon.getImapFolder() == null ? false
					: imapCon.getImapFolder() instanceof DefaultFolder);
		} else {
			isIdenticalFolder = (imapCon.getImapFolder() == null ? false : imapCon.getImapFolder().getFullName()
					.equals(folderName));
		}
		if (imapCon.getImapFolder() != null) {
			IMAPUtils.forceNoopCommand(imapCon.getImapFolder());
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
			IMAPUtils.forceNoopCommand(imapCon.getImapFolder());
			if (imapCon.getImapFolder().isOpen()) {
				if (markAsSeen != null) {
					/*
					 * Mark stored message as seen
					 */
					keepSeen();
				}
				imapCon.getImapFolder().close(false);
				mailInterfaceMonitor.changeNumActive(false);
				imapCon.resetImapFolder();
			}
			if (isIdenticalFolder) {
				if (mode == Folder.READ_WRITE
						&& ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
						&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROP_ALLOWREADONLYSELECT, STR_FALSE))
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
		imapCon.setImapFolder(isDefaultFolder ? (IMAPFolder) imapCon.getIMAPStore().getDefaultFolder()
				: (IMAPFolder) imapCon.getIMAPStore().getFolder(folderName));
		if (!isDefaultFolder && !imapCon.getImapFolder().exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, imapCon.getImapFolder().getFullName());
		}
		if (mode != Folder.READ_ONLY && mode != Folder.READ_WRITE) {
			throw new OXMailException(MailCode.UNKNOWN_FOLDER_MODE, Integer.valueOf(mode));
		} else if (mode == Folder.READ_WRITE
				&& ((imapCon.getImapFolder().getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
				&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROP_ALLOWREADONLYSELECT, STR_FALSE))
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
		final boolean isDefaultFolder = folderName.equals(MailFolderObject.DEFAULT_IMAP_FOLDER_ID);
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
				tmpFolder = null;
			}
			if (isIdenticalFolder) {
				if (mode == Folder.READ_WRITE
						&& ((tmpFolder.getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
						&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROP_ALLOWREADONLYSELECT, STR_FALSE))
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
		tmpFolder = (isDefaultFolder ? (IMAPFolder) imapCon.getIMAPStore().getDefaultFolder() : (IMAPFolder) imapCon
				.getIMAPStore().getFolder(folderName));
		if (!isDefaultFolder && !tmpFolder.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, tmpFolder.getFullName());
		}
		if (mode != Folder.READ_ONLY && mode != Folder.READ_WRITE) {
			throw new OXMailException(MailCode.UNKNOWN_FOLDER_MODE, Integer.valueOf(mode));
		} else if (mode == Folder.READ_WRITE
				&& ((tmpFolder.getType() & Folder.HOLDS_MESSAGES) == 0) // NoSelect
				&& STR_FALSE.equalsIgnoreCase(imapProps.getProperty(PROP_ALLOWREADONLYSELECT, STR_FALSE))
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
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
			final String subject = removeHdrLineBreak(originalMsg.getHeader(HDR_SUBJECT, null));
			if (subject != null) {
				final String subjectPrefix = new StringHelper(sessionObj.getLocale())
						.getString(MailStrings.FORWARD_SUBJECT_PREFIX);
				retval.setSubject(decodeMultiEncodedHeader(subject.regionMatches(true, 0, subjectPrefix, 0,
						subjectPrefix.length()) ? subject : new StringBuilder().append(subjectPrefix).append(subject)
						.toString()));
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
				dummy.setContentType(MIME_TEXT_PLAIN);
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
				mao.setContentType(msgHandler.isHtml() ? MIME_TEXT_HTML : MIME_TEXT_PLAIN);
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
			}
			final MimeMessage msg;
			final long start = System.currentTimeMillis();
			try {
				msg = (MimeMessage) imapCon.getImapFolder().getMessageByUID(msgUID);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final String[] dispNotification = msg.getHeader(HDR_DISP_TO);
			if (dispNotification == null || dispNotification.length == 0) {
				throw new OXMailException(MailCode.MISSING_HEADER, HDR_DISP_TO, Long.valueOf(msgUID));
			}
			InternetAddress[] to = null;
			for (int i = 0; i < dispNotification.length; i++) {
				final InternetAddress[] addrs = InternetAddress.parse(dispNotification[i], false); // TODO:
				// Should
				// be
				// strict
				// parsing
				if (to == null) {
					to = addrs;
				} else {
					final InternetAddress[] tmp = to;
					to = new InternetAddress[tmp.length + addrs.length];
					System.arraycopy(tmp, 0, to, 0, tmp.length);
					System.arraycopy(addrs, 0, to, tmp.length, addrs.length);
				}
			}
			final String msgId = msg.getHeader(HDR_MESSAGE_ID, null);
			sendReceiptAck(to, fromAddr, (msgId == null ? "[not available]" : msgId), msg.getSubject(), msg
					.getSentDate());
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	private static final String ACK_TEXT = "Reporting-UA: OPEN-XCHANGE - WebMail\nFinal-Recipient: rfc822; #FROM#\n"
			+ "Original-Message-ID: #MSG ID#\nDisposition: manual-action/MDN-sent-manually; displayed\n";

	private final void sendReceiptAck(final InternetAddress[] to, final String fromAddr, final String msgID,
			final String origSubject, final Date sentDate) throws OXException, MessagingException {
		final SMTPMessage msg = new SMTPMessage(imapCon.getSession());
		final StringHelper strHelper = new StringHelper(sessionObj.getLocale());
		/*
		 * Set from
		 */
		final String from;
		if (fromAddr != null) {
			from = fromAddr;
		} else if (usm.getSendAddr() == null && sessionObj.getUserObject().getMail() == null) {
			throw new OXMailException(MailCode.NO_SEND_ADDRESS_FOUND, getUserName(sessionObj));
		} else {
			from = usm.getSendAddr() == null ? sessionObj.getUserObject().getMail() : usm.getSendAddr();
		}
		msg.addFrom(InternetAddress.parse(from, false));
		/*
		 * Set to
		 */
		msg.addRecipients(RecipientType.TO, to);
		/*
		 * Set header
		 */
		msg.setHeader(HDR_X_PRIORITY, "3 (normal)");
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
		 * ENVELOPE-FROM
		 */
		if (IMAPProperties.isSMTPEnvelopeFrom()) {
			/*
			 * Set ENVELOPE-FROM in SMTP message to user's primary email address
			 */
			msg.setEnvelopeFrom(sessionObj.getUserObject().getMail());
		}
		/*
		 * Set mailer TODO: Read in mailer from file
		 */
		msg.setHeader(HDR_X_MAILER, "Open-Xchange v6.0 Mailer");
		/*
		 * Set organization TODO: read in organization from file
		 */
		msg.setHeader(HDR_ORGANIZATION, "Open-Xchange, Inc.");
		/*
		 * Compose body
		 */
		final ContentType ct = new ContentType(MIME_TEXT_PLAIN_CHARSET_UTF_8);
		final Multipart mixedMultipart = new MimeMultipart(MP_REPORT_DISPNOT);
		/*
		 * Define text content
		 */
		final MimeBodyPart text = new MimeBodyPart();
		text.setText(performLineWrap(strHelper.getString(MailStrings.ACK_NOTIFICATION_TEXT.replaceFirst(
				"#DATE#",
				sentDate == null ? STR_EMPTY : DateFormat.getDateInstance(DateFormat.LONG, sessionObj.getLocale())
						.format(sentDate)).replaceFirst("#RECIPIENT#", from).replaceFirst("#SUBJECT#", origSubject)),
				false, usm.getAutoLinebreak()), IMAPProperties.getDefaultMimeCharset());
		text.setHeader(HDR_MIME_VERSION, STR_1DOT0);
		text.setHeader(HDR_CONTENT_TYPE, ct.toString());
		mixedMultipart.addBodyPart(text);
		/*
		 * Define ack
		 */
		ct.setContentType(MIME_MSG_DISPNOT_MDN_CHARSET_UTF8);
		final MimeBodyPart ack = new MimeBodyPart();
		ack.setText(strHelper.getString(MailInterfaceImpl.ACK_TEXT).replaceFirst("#FROM#", fromAddr).replaceFirst(
				"#MSG ID#", msgID), IMAPProperties.getDefaultMimeCharset());
		ack.setHeader(HDR_MIME_VERSION, STR_1DOT0);
		ack.setHeader(HDR_CONTENT_TYPE, ct.toString());
		ack.setHeader(HDR_CONTENT_DISPOSITION, "attachment; filename=MDNPart1.txt");
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
			transport = imapCon.getSession().getTransport(PROTOCOL_SMTP);
			if (IMAPProperties.isSmtpAuth()) {
				transport.connect(sessionObj.getIMAPProperties().getSmtpServer(), sessionObj.getIMAPProperties()
						.getImapLogin(), encodePassword(sessionObj.getIMAPProperties().getImapPassword()));
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
	 *      com.openexchange.groupware.upload.UploadEvent, int)
	 */
	public String sendMessage(final JSONMessageObject msgObj, final UploadEvent uploadEvent, final int sendType)
			throws OXException {
		try {
			init();
			final SMTPMessage newSMTPMsg = new SMTPMessage(imapCon.getSession());
			IMAPFolder originalMsgFolder = null;
			boolean originalMsgFolderOpened = false;
			MimeMessage originalMsg = null;
			boolean isReadWrite = true;
			Mail.MailIdentifier mailId = null;
			try {
				if (msgObj.getMsgref() != null) {
					/*
					 * A message reference is present. Either a reply, forward
					 * or draft-edit message.
					 */
					mailId = new Mail.MailIdentifier(msgObj.getMsgref());
					originalMsgFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(mailId.getFolder());
					/*
					 * Check folder existence
					 */
					if (!originalMsgFolder.exists()) {
						throw new OXMailException(MailCode.FOLDER_NOT_FOUND, originalMsgFolder.getFullName());
					}
					try {
						originalMsgFolder.open(Folder.READ_WRITE);
					} catch (final ReadOnlyFolderException e) {
						originalMsgFolder.open(Folder.READ_ONLY);
						isReadWrite = false;
					}
					originalMsgFolderOpened = true;
					mailInterfaceMonitor.changeNumActive(true);
					originalMsg = (MimeMessage) originalMsgFolder.getMessageByUID(mailId.getMsgUID());
					if (originalMsg != null && sendType == SENDTYPE_REPLY) {
						/*
						 * A reply! Appropiately set message headers
						 */
						final String pMsgId = originalMsg.getHeader(HDR_MESSAGE_ID, null);
						if (pMsgId != null) {
							newSMTPMsg.setHeader(HDR_IN_REPLY_TO, pMsgId);
						}
						/*
						 * Set References header field
						 */
						final String pReferences = originalMsg.getHeader(HDR_REFERENCES, null);
						final String pInReplyTo = originalMsg.getHeader(HDR_IN_REPLY_TO, null);
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
							newSMTPMsg.setHeader(HDR_REFERENCES, refBuilder.toString());
						}
						/*
						 * Mark original message as answered
						 */
						if (isReadWrite) {
							try {
								originalMsg.setFlags(FLAGS_ANSWERED, true);
							} catch (final MessagingException e) {
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
					final IMAPFolder inboxFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(STR_INBOX);
					final IMAPFolder draftFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(
							prepareMailFolderParam(getDraftsFolder()));
					/*
					 * Fill message
					 */
					final MessageFiller msgFiller = new MessageFiller(sessionObj, originalMsg, imapCon.getSession(),
							draftFolder);
					msgFiller.fillMessage(msgObj, newSMTPMsg, uploadEvent, sendType);
					checkAndCreateFolder(draftFolder, inboxFolder);
					if (!draftFolder.isOpen()) {
						draftFolder.open(Folder.READ_WRITE);
						mailInterfaceMonitor.changeNumActive(true);
					}
					newSMTPMsg.setFlag(Flags.Flag.DRAFT, true);
					newSMTPMsg.saveChanges();
					/*
					 * Append message to draft folder
					 */
					long uidNext = -1;
					final long start = System.currentTimeMillis();
					try {
						if (IMAPProperties.isCapabilitiesLoaded() && IMAPProperties.getImapCapabilities().hasUIDPlus()) {
							final AppendUID appendUID = draftFolder.appendUIDMessages(new Message[] { newSMTPMsg })[0];
							if (appendUID != null) {
								uidNext = appendUID.uid;
							}
						} else {
							uidNext = draftFolder.getUIDNext();
							if (uidNext == -1) {
								/*
								 * UIDNEXT not supported
								 */
								uidNext = IMAPUtils.getUIDNext(draftFolder);
							}
							draftFolder.appendMessages(new Message[] { newSMTPMsg });
						}
					} finally {
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						draftFolder.close(false);
						mailInterfaceMonitor.changeNumActive(false);
					}
					/*
					 * Check for draft-edit operation
					 */
					DeleteOrig: if (originalMsg != null && originalMsg.isSet(Flags.Flag.DRAFT)) {
						/*
						 * Delete old draft version
						 */
						if (originalMsgFolder == null) {
							break DeleteOrig;
						} else if (!isReadWrite) {
							throw new OXMailException(MailCode.NO_DRAFT_EDIT, originalMsgFolder.getFullName());
						} else if (mailId == null) {
							break DeleteOrig;
						}
						/*
						 * Delete old draft version
						 */
						originalMsg.setFlags(FLAGS_DELETED, true);
						try {
							originalMsgFolder.getProtocol().uidexpunge(
									IMAPUtils.toUIDSet(new long[] { mailId.getMsgUID() }));
						} catch (final ProtocolException e) {
							LOG.error(e.getLocalizedMessage(), e);
						} finally {
							if (originalMsgFolderOpened) {
								originalMsgFolder.close(false);
								mailInterfaceMonitor.changeNumActive(false);
							}
						}
						/*
						 * Reset message reference cause not needed anymore
						 */
						msgObj.setMsgref(null);
					}
					return new StringBuilder(draftFolder.getFullName()).append(Mail.SEPERATOR).append(uidNext)
							.toString();
				}
				final IMAPFolder inboxFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(STR_INBOX);
				final IMAPFolder sentFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(
						prepareMailFolderParam(getSentFolder()));
				/*
				 * Fill message
				 */
				final MessageFiller msgFiller = new MessageFiller(sessionObj, originalMsg, imapCon.getSession(), usm
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
						transport = imapCon.getSession().getTransport(PROTOCOL_SMTP);
						if (IMAPProperties.isSmtpAuth()) {
							transport.connect(sessionObj.getIMAPProperties().getSmtpServer(), sessionObj
									.getIMAPProperties().getImapLogin(), encodePassword(sessionObj.getIMAPProperties()
									.getImapPassword()));
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
				} catch (final MessagingException e) {
					throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
				newSMTPMsg.setFlag(Flags.Flag.SEEN, true);
				newSMTPMsg.saveChanges();
				long uidNext = -1;
				final long start = System.currentTimeMillis();
				try {
					if (IMAPProperties.isCapabilitiesLoaded() && IMAPProperties.getImapCapabilities().hasUIDPlus()) {
						final AppendUID appendUID = sentFolder.appendUIDMessages(new Message[] { newSMTPMsg })[0];
						if (appendUID != null) {
							uidNext = appendUID.uid;
						}
					} else {
						uidNext = sentFolder.getUIDNext();
						if (uidNext == -1) {
							/*
							 * UIDNEXT not supported
							 */
							uidNext = IMAPUtils.getUIDNext(sentFolder);
						}
						sentFolder.appendMessages(new Message[] { newSMTPMsg });
					}
				} catch (final MessagingException e) {
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
				if (originalMsgFolder != null) {
					try {
						originalMsgFolder.close(false);
						mailInterfaceMonitor.changeNumActive(false);
					} catch (final IllegalStateException e) {
						LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
					}
					originalMsgFolder = null;
				}
			}
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		} catch (final IOException e) {
			throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
		} catch (final JSONException e) {
			throw new OXMailException(MailCode.JSON_ERROR, e, e.getMessage());
		}
	}

	private final void checkAndCreateFolder(final IMAPFolder newFolder, final IMAPFolder parent)
			throws MessagingException, OXException {
		if (newFolder.exists()) {
			return;
		} else if (!parent.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, parent.getFullName());
		}
		try {
			if ((parent.getType() & Folder.HOLDS_MESSAGES) == 0) {
				throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, parent.getFullName());
			} else if (IMAPProperties.isSupportsACLs()
					&& !sessionObj.getCachedRights(parent, true).contains(Rights.Right.CREATE)) {
				throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(sessionObj), parent.getFullName());
			}
		} catch (final MessagingException e) {
			throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), parent.getFullName());
		}
		final long start = System.currentTimeMillis();
		if (!newFolder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)) {
			throw new OXMailException(MailCode.FOLDER_CREATION_FAILED, newFolder.getFullName(),
					parent instanceof DefaultFolder ? MailFolderObject.DEFAULT_IMAP_FOLDER_NAME : parent.getFullName());
		}
		mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
	}

	private Folder getFolder(final IMAPFolder parent, final String folderName) throws MessagingException,
			OXMailException {
		if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
			throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, null, parent.getFullName());
		}
		if (parent instanceof DefaultFolder) {
			return imapCon.getIMAPStore().getFolder(folderName);
		}
		return imapCon.getIMAPStore().getFolder(
				new StringBuilder(100).append(parent.getFullName()).append(parent.getSeparator()).append(folderName)
						.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#clearFolder(java.lang.String)
	 */
	public boolean clearFolder(final String folderArg) throws OXException {
		try {
			init();
			final String folder = folderArg == null ? STR_INBOX : prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_WRITE);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	private static final String ERR_WORD_TOO_LONG = "word too long";

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
			final String folder = folderArg == null ? STR_INBOX : prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_WRITE);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
			}
			/*
			 * Perform "soft delete", means to copy message to default trash
			 * folder
			 */
			final boolean isTrashFolder = (folder.endsWith(usm.getStdTrashName()));
			if (!usm.isHardDeleteMsgs() && !hardDelete && !isTrashFolder) {
				/*
				 * Copy messages to folder "TRASH"
				 */
				final IMAPFolder trashFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(
						prepareMailFolderParam(getTrashFolder()));
				checkAndCreateFolder(trashFolder, (IMAPFolder) imapCon.getIMAPStore().getFolder(STR_INBOX));
				try {
					final long start = System.currentTimeMillis();
					IMAPUtils.copyUIDFast(imapCon.getImapFolder(), msgUIDs, trashFolder.getFullName(), false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("\"Soft Delete\": ").append(msgUIDs.length).append(
								" messages copied to default trash folder \"").append(trashFolder.getFullName())
								.append("\" in ").append((System.currentTimeMillis() - start)).append("msec")
								.toString());
					}
				} catch (final MessagingException e) {
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
				}
			}
			/*
			 * Mark messages as \DELETED
			 */
			final long start = System.currentTimeMillis();
			IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, FLAGS_DELETED, true);
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append(msgUIDs.length).append(
						" messages marked as deleted (through system flag \\DELETED) in ").append(
						(System.currentTimeMillis() - start)).append("msec").toString());
			}
			imapCon.setExpunge(true);
			return true;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
	public long[] copyMessages(final String sourceFolderArg, final String destFolderArg, final long[] msgUIDs,
			final boolean move) throws OXException {
		try {
			if (sourceFolderArg == null || sourceFolderArg.length() == 0) {
				throw new OXMailException(MailCode.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "source");
			} else if (destFolderArg == null || destFolderArg.length() == 0) {
				throw new OXMailException(MailCode.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "target");
			} else if (sourceFolderArg.equals(destFolderArg) && move) {
				throw new OXMailException(MailCode.NO_EQUAL_MOVE, getUserName(sessionObj),
						prepareMailFolderParam(sourceFolderArg));
			}
			init();
			final String sourceFolder = prepareMailFolderParam(sourceFolderArg);
			final String destFolder = prepareMailFolderParam(destFolderArg);
			/*
			 * Open and check user rights on source folder
			 */
			setAndOpenFolder(sourceFolder, Folder.READ_WRITE);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (move && IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
			}
			/*
			 * Open and check user rights on destination folder
			 */
			setAndOpenTmpFolder(destFolder, Folder.READ_ONLY);
			try {
				if ((tmpFolder.getType() & Folder.HOLDS_MESSAGES) == 0) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, tmpFolder.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(tmpFolder, true).contains(Rights.Right.INSERT)) {
					throw new OXMailException(MailCode.NO_INSERT_ACCESS, getUserName(sessionObj), tmpFolder
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), tmpFolder.getFullName());
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
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append(msgUIDs.length).append(" messages copied in ").append(
								(System.currentTimeMillis() - start)).append("msec").toString());
					}
					if (usm.isSpamEnabled()) {
						/*
						 * Spam related action
						 */
						final String spamFullName = prepareMailFolderParam(getSpamFolder());
						final int spamAction = spamFullName.equals(imapCon.getImapFolder().getFullName()) ? SPAM_HAM
								: (spamFullName.equals(tmpFolder.getFullName()) ? SPAM_SPAM : SPAM_NOOP);
						if (spamAction != SPAM_NOOP) {
							try {
								handleSpamUID(msgUIDs, spamAction == SPAM_SPAM, false);
							} catch (final OXException e) {
								if (LOG.isWarnEnabled()) {
									LOG.warn(e.getMessage(), e);
								}
							} catch (final MessagingException e) {
								if (LOG.isWarnEnabled()) {
									LOG.warn(e.getMessage(), e);
								}
							}
						}
					}
					if (move) {
						start = System.currentTimeMillis();
						IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, FLAGS_DELETED, true);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						if (LOG.isInfoEnabled()) {
							LOG.info(new StringBuilder(100).append(msgUIDs.length).append(
									" messages marked as expunged (through system flag \\DELETED) in ").append(
									(System.currentTimeMillis() - start)).append("msec").toString());
						}
						/*
						 * Expunge "moved" messages immediately
						 */
						try {
							start = System.currentTimeMillis();
							IMAPUtils.uidExpunge(imapCon.getImapFolder(), msgUIDs);
							mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
							if (LOG.isInfoEnabled()) {
								LOG.info(new StringBuilder(100).append(msgUIDs.length).append(" messages expunged in ")
										.append((System.currentTimeMillis() - start)).append("msec").toString());
							}
							/*
							 * Force folder cache update through a close
							 */
							imapCon.getImapFolder().close(false);
							imapCon.resetImapFolder();
						} catch (final ProtocolException e) {
							throw new OXMailException(MailCode.MOVE_PARTIALLY_COMPLETED, e,
									com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(sessionObj), Arrays
											.toString(msgUIDs), imapCon.getImapFolder().getFullName(), e.getMessage());
						}
					}
					return res;
				} catch (final MessagingException e) {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					throw handleMessagingException(e);
				}
			}
			Message[] msgs = null;
			try {
				final long start = System.currentTimeMillis();
				msgs = imapCon.getImapFolder().getMessagesByUID(msgUIDs);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			} catch (final MessagingException e) {
				if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_WORD_TOO_LONG) > -1) {
					try {
						final long start = System.currentTimeMillis();
						final int[] msgnums = IMAPUtils.getSequenceNumbers(imapCon.getImapFolder(), msgUIDs, false);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
						msgs = imapCon.getImapFolder().getMessages(msgnums);
					} catch (final ProtocolException e1) {
						throw new OXMailException(MailCode.INTERNAL_ERROR, e1, e1.getMessage());
					}
				}
				throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			final long start = System.currentTimeMillis();
			try {
				imapCon.getImapFolder().copyMessages(msgs, tmpFolder);
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			if (IMAPProperties.isSpamEnabled()) {
				/*
				 * Spam related action
				 */
				final String spamFullName = prepareMailFolderParam(getSpamFolder());
				final int spamAction = spamFullName.equals(imapCon.getImapFolder().getFullName()) ? SPAM_HAM
						: (spamFullName.equals(tmpFolder.getFullName()) ? SPAM_SPAM : SPAM_NOOP);
				if (spamAction != SPAM_NOOP) {
					for (int i = 0; i < msgs.length; i++) {
						if (spamAction == SPAM_SPAM) {
							handleSpam(msgs[i], true, false);
						} else if (spamAction == SPAM_HAM) {
							handleSpam(msgs[i], false, false);
						}
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
				 * Expunge "moved" messages immediately
				 */
				imapCon.getImapFolder().expunge(msgs);
			}
			/*
			 * Return new message id
			 */
			return retval;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#updateMessageColorLabel(java.lang.String,
	 *      long[], int)
	 */
	public Message[] updateMessageColorLabel(final String folderArg, final long[] msgUIDs, final int newColorLabel)
			throws OXException {
		if (!IMAPProperties.isUserFlagsEnabled()) {
			/*
			 * User flags are disabled
			 */
			if (LOG.isDebugEnabled()) {
				LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
			}
			return null;
		}
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_WRITE);
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
			}
			if (!sessionObj.getCachedUserFlags(imapCon.getImapFolder(), true)) {
				LOG.error(new StringBuilder().append("Folder \"").append(imapCon.getImapFolder().getFullName()).append(
						"\" does not support user-defined flags. Update of color flag ignored."));
				return null;
			}
			try {
				/*
				 * Remove all old color label flag(s) and set new color label
				 * flag
				 */
				long start = System.currentTimeMillis();
				IMAPUtils.clearAllColorLabels(imapCon.getImapFolder(), msgUIDs);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("All color flags cleared from ").append(msgUIDs.length)
							.append(" messages in ").append((System.currentTimeMillis() - start)).append("msec")
							.toString());
				}
				start = System.currentTimeMillis();
				IMAPUtils.setColorLabel(imapCon.getImapFolder(), msgUIDs, JSONMessageObject
						.getColorLabelStringValue(newColorLabel));
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("All color flags set in ").append(msgUIDs.length).append(
							" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
				final Message[] msgs;
				if (msgUIDs.length <= IMAPProperties.getMessageFetchLimit()) {
					/*
					 * Fetch modified messages
					 */
					start = System.currentTimeMillis();
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgUIDs,
							IMAPUtils.getDefaultFetchProfile(), false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} else {
					start = System.currentTimeMillis();
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgUIDs, IMAPUtils.getUIDFetchProfile(),
							false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
				imapCon.getImapFolder().close(false);
				imapCon.resetImapFolder();
				return msgs;
			} catch (final ProtocolException e) {
				throw new MessagingException(e.getMessage(), e);
			}
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#updateMessageFlags(java.lang.String,
	 *      long, int, boolean)
	 */
	public Message[] updateMessageFlags(final String folderArg, final long[] msgUIDs, final int flagBitsArg,
			final boolean flagsVal) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			setAndOpenFolder(folder, Folder.READ_WRITE);
			/*
			 * r - read (SELECT the mailbox, perform CHECK, FETCH, PARTIAL,
			 * SEARCH, COPY from mailbox) s - keep seen/unseen information
			 * across sessions (STORE SEEN flag) w - write (STORE flags other
			 * than SEEN and DELETED)
			 */
			try {
				if (!imapCon.isHoldsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapCon.getImapFolder()
							.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
							.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), imapCon.getImapFolder()
						.getFullName());
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
				if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
				affectedFlags.add(Flags.Flag.ANSWERED);
			}
			if (((flagBits & JSONMessageObject.BIT_DELETED) > 0)) {
				if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.DELETE)) {
					throw new OXMailException(MailCode.NO_DELETE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
				affectedFlags.add(Flags.Flag.DELETED);
			}
			if (((flagBits & JSONMessageObject.BIT_DRAFT) > 0)) {
				if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
				affectedFlags.add(Flags.Flag.DRAFT);
			}
			if (((flagBits & JSONMessageObject.BIT_FLAGGED) > 0)) {
				if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.WRITE)) {
					throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
				affectedFlags.add(Flags.Flag.FLAGGED);
			}
			if (((flagBits & JSONMessageObject.BIT_SEEN) > 0)) {
				if (IMAPProperties.isSupportsACLs()
						&& !sessionObj.getCachedRights(imapCon.getImapFolder(), true).contains(Rights.Right.KEEP_SEEN)) {
					throw new OXMailException(MailCode.NO_KEEP_SEEN_ACCESS, getUserName(sessionObj), imapCon
							.getImapFolder().getFullName());
				}
				affectedFlags.add(Flags.Flag.SEEN);
			}
			if (affectedFlags.getSystemFlags().length > 0) {
				final long start = System.currentTimeMillis();
				IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, affectedFlags, flagsVal);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("System Flags applied to ").append(msgUIDs.length).append(
							" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			}
			/*
			 * Check for spam action
			 */
			if (IMAPProperties.isSpamEnabled() && ((flagBits & JSONMessageObject.BIT_SPAM) > 0)) {
				handleSpamUID(msgUIDs, flagsVal, true);
				return MessageCacheObject.getExpungedMessageArr(msgUIDs);
			}
			try {
				final Message[] msgs;
				if (msgUIDs.length <= IMAPProperties.getMessageFetchLimit()) {
					/*
					 * Fetch modified messages in a fast manner
					 */
					final long start = System.currentTimeMillis();
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgUIDs,
							IMAPUtils.getDefaultFetchProfile(), false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} else {
					final long start = System.currentTimeMillis();
					msgs = IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgUIDs, IMAPUtils.getUIDFetchProfile(),
							false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
				imapCon.getImapFolder().close(false);
				imapCon.resetImapFolder();
				return msgs;
			} catch (final ProtocolException e) {
				throw new MessagingException(e.getMessage(), e);
			}
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	private final boolean handleSpam(final Message msg, final boolean isSpam, final boolean move) throws OXException,
			MessagingException {
		return handleSpam(new Message[] { msg }, isSpam, move);
	}

	private final boolean handleSpam(final Message[] msgs, final boolean isSpam, final boolean move)
			throws OXException, MessagingException {
		/*
		 * Check for spam handling
		 */
		if (usm.isSpamEnabled()) {
			init();
			final boolean locatedInSpamFolder = prepareMailFolderParam(getSpamFolder()).equals(
					imapCon.getImapFolder().getFullName());
			final Message[] msgArr = new Message[msgs.length];
			final String destFullname;
			if (isSpam) {
				if (locatedInSpamFolder) {
					/*
					 * A message that already has been detected as spam should
					 * again be learned as spam: Abort.
					 */
					return true;
				}
				/*
				 * Mark as spam
				 */
				Folder confirmedSpamFld = null;
				try {
					confirmedSpamFld = imapCon.getIMAPStore().getFolder(
							prepareMailFolderParam(getConfirmedSpamFolder()));
					confirmedSpamFld.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
					System.arraycopy(msgs, 0, msgArr, 0, msgs.length);
					/*
					 * Append spam message to dedicated folder for confirmed
					 * spam
					 */
					confirmedSpamFld.appendMessages(msgArr);
					/*
					 * Set destination folder for move operation
					 */
					destFullname = prepareMailFolderParam(getSpamFolder());
				} finally {
					if (confirmedSpamFld != null) {
						try {
							confirmedSpamFld.close(false);
							mailInterfaceMonitor.changeNumActive(false);
						} catch (final IllegalStateException e) {
							LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
						}
						confirmedSpamFld = null;
					}
				}
			} else {
				if (!locatedInSpamFolder) {
					/*
					 * A message that already has been detected as ham should
					 * again be learned as ham: Abort.
					 */
					return true;
				}
				/*
				 * Mark as ham
				 */
				Folder confirmedHamFld = null;
				try {
					confirmedHamFld = imapCon.getIMAPStore().getFolder(prepareMailFolderParam(getConfirmedHamFolder()));
					confirmedHamFld.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
					for (int i = 0; i < msgs.length; i++) {
						msgArr[i] = getInlinedSpamMessage(msgs[i]);
					}
					/*
					 * Append ham message to dedicated folder for confirmed ham
					 */
					confirmedHamFld.appendMessages(msgArr);
					/*
					 * Set destination folder for move operation
					 */
					destFullname = STR_INBOX;
				} finally {
					if (confirmedHamFld != null) {
						try {
							confirmedHamFld.close(false);
							mailInterfaceMonitor.changeNumActive(false);
						} catch (final IllegalStateException e) {
							LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
						}
						confirmedHamFld = null;
					}
				}
			}
			if (move) {
				/*
				 * Move message
				 */
				Folder destFld = null;
				try {
					/*
					 * Append to destination folder
					 */
					destFld = imapCon.getIMAPStore().getFolder(destFullname);
					destFld.open(Folder.READ_WRITE);
					mailInterfaceMonitor.changeNumActive(true);
					destFld.appendMessages(msgArr);
				} finally {
					if (destFld != null) {
						try {
							destFld.close(false);
							mailInterfaceMonitor.changeNumActive(false);
						} catch (final IllegalStateException e) {
							LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
						}
						destFld = null;
					}
				}
				/*
				 * Delete from original folder
				 */
				try {
					final long[] uids = IMAPUtils.getMessageUIDs(imapCon.getImapFolder(), msgArr);
					IMAPUtils.setSystemFlags(imapCon.getImapFolder(), uids, false, FLAGS_DELETED, true);
					imapCon.getImapFolder().getProtocol().uidexpunge(IMAPUtils.toUIDSet(uids));
					/*
					 * Close folder to force internal message cache update
					 */
					imapCon.getImapFolder().close(false);
					imapCon.resetImapFolder();
				} catch (final ProtocolException e) {
					throw new MessagingException(e.getMessage(), e);
				}
			}
		}
		return true;
	}

	private final boolean handleSpamUID(final long[] msgUIDs, final boolean isSpam, final boolean move)
			throws OXException, MessagingException {
		/*
		 * Check for spam handling
		 */
		if (usm.isSpamEnabled()) {
			init();
			final boolean locatedInSpamFolder = prepareMailFolderParam(getSpamFolder()).equals(
					imapCon.getImapFolder().getFullName());
			if (isSpam) {
				if (locatedInSpamFolder) {
					/*
					 * A message that already has been detected as spam should
					 * again be learned as spam: Abort.
					 */
					return true;
				}
				/*
				 * Copy to confirmed spam
				 */
				IMAPUtils.copyUIDFast(imapCon.getImapFolder(), msgUIDs,
						prepareMailFolderParam(getConfirmedSpamFolder()), false);
				if (move) {
					/*
					 * Copy messages to spam folder
					 */
					IMAPUtils.copyUIDFast(imapCon.getImapFolder(), msgUIDs, prepareMailFolderParam(getSpamFolder()),
							false);
					/*
					 * Delete messages
					 */
					IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, FLAGS_DELETED, true);
					/*
					 * Expunge messages immediately
					 */
					try {
						imapCon.getImapFolder().getProtocol().uidexpunge(IMAPUtils.toUIDSet(msgUIDs));
						/*
						 * Force folder cache update through a close
						 */
						imapCon.getImapFolder().close(false);
						imapCon.resetImapFolder();
					} catch (final ProtocolException e) {
						throw new OXMailException(MailCode.MOVE_PARTIALLY_COMPLETED, e,
								com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(sessionObj), Arrays
										.toString(msgUIDs), imapCon.getImapFolder().getFullName(), e.getMessage());
					}
				}
				return true;
			}
			if (!locatedInSpamFolder) {
				/*
				 * A message that already has been detected as ham should again
				 * be learned as ham: Abort.
				 */
				return true;
			}
			/*
			 * Mark as ham. In contrast to mark as spam this is a very time
			 * sucking operation. In order to deal with the original messages
			 * that are wrapped inside a SpamAssassin-created message it must be
			 * extracted. Therefore we need to access message's content and
			 * cannot deal only with UIDs
			 */
			long start = System.currentTimeMillis();
			MessageCacheObject[] msgs = null;
			try {
				final FetchProfile fp = new FetchProfile();
				fp.add(HDR_X_SPAM_FLAG);
				fp.add(FetchProfile.Item.CONTENT_INFO);
				msgs = (MessageCacheObject[]) IMAPUtils.fetchMessages(imapCon.getImapFolder(), msgUIDs, fp, false);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			} catch (final ProtocolException e1) {
				throw new MessagingException(e1.getMessage(), e1);
			}
			/*
			 * Seperate the plain from the nested messages inside spam folder
			 */
			SmartLongArray plainUIDs = new SmartLongArray(msgUIDs.length);
			SmartLongArray extractUIDs = new SmartLongArray(msgUIDs.length);
			for (int i = 0; i < msgs.length; i++) {
				final String[] spamHdr = msgs[i].getHeader(HDR_X_SPAM_FLAG);
				final BODYSTRUCTURE bodystructure = msgs[i].getBodystructure();
				if (spamHdr != null && STR_YES.regionMatches(true, 0, spamHdr[0], 0, 3) && bodystructure.isMulti()
						&& bodystructure.bodies[1].isNested()) {
					extractUIDs.append(msgUIDs[i]);
				} else {
					plainUIDs.append(msgUIDs[i]);
				}
			}
			final String confirmedHamFullname = prepareMailFolderParam(getConfirmedHamFolder());
			/*
			 * Copy plain messages to confirmed ham and INBOX
			 */
			long[] plainUIDsArr = plainUIDs.toArray();
			plainUIDs = null;
			start = System.currentTimeMillis();
			IMAPUtils.copyUIDFast(imapCon.getImapFolder(), plainUIDsArr, confirmedHamFullname, false);
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (move) {
				start = System.currentTimeMillis();
				IMAPUtils.copyUIDFast(imapCon.getImapFolder(), plainUIDsArr, STR_INBOX, false);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			plainUIDsArr = null;
			/*
			 * Handle spam messages
			 */
			long[] spamArr = extractUIDs.toArray();
			extractUIDs = null;
			IMAPFolder confirmedHamFld = null;
			try {
				confirmedHamFld = (IMAPFolder) imapCon.getIMAPStore().getFolder(confirmedHamFullname);
				confirmedHamFld.open(Folder.READ_WRITE);
				mailInterfaceMonitor.changeNumActive(true);
				/*
				 * Get nested spam messages
				 */
				start = System.currentTimeMillis();
				Message[] nestedMsgs = IMAPUtils.getNestedSpamMessages(imapCon.getImapFolder(), spamArr);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Nested SPAM messages fetched in ").append(
							(System.currentTimeMillis() - start)).append("msec").toString());
				}
				spamArr = null;
				/*
				 * ... and append them to confirmed ham folder and - if move
				 * enabled - copy them to INBOX.
				 */
				start = System.currentTimeMillis();
				AppendUID[] appendUIDs = confirmedHamFld.appendUIDMessages(nestedMsgs);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Nested SPAM messages appended to ").append(
							confirmedHamFullname).append(" in ").append((System.currentTimeMillis() - start)).append(
							"msec").toString());
				}
				nestedMsgs = null;
				if (move) { // Cannot be null
					start = System.currentTimeMillis();
					IMAPUtils.copyUIDFast(confirmedHamFld, appendUID2Long(appendUIDs), STR_INBOX, false);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("Nested SPAM messages copied to ").append(STR_INBOX)
								.append(" in ").append((System.currentTimeMillis() - start)).append("msec").toString());
					}
				}
				appendUIDs = null;
				if (move) {
					/*
					 * Expunge messages
					 */
					start = System.currentTimeMillis();
					IMAPUtils.setSystemFlags(imapCon.getImapFolder(), msgUIDs, false, FLAGS_DELETED, true);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					start = System.currentTimeMillis();
					imapCon.getImapFolder().getProtocol().uidexpunge(IMAPUtils.toUIDSet(msgUIDs));
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("Original spam messages expunged in ").append(
								(System.currentTimeMillis() - start)).append("msec").toString());
					}
					/*
					 * Close folder to force JavaMail-internal message cache
					 * update
					 */
					imapCon.getImapFolder().close(false);
					imapCon.resetImapFolder();
				}
			} catch (final ProtocolException e1) {
				throw new MessagingException(e1.getMessage(), e1);
			} finally {
				if (confirmedHamFld != null) {
					try {
						confirmedHamFld.close(false);
						mailInterfaceMonitor.changeNumActive(false);
					} catch (final IllegalStateException e) {
						LOG.warn(e.getMessage(), e);
					}
				}
			}

		}
		return true;
	}

	private static final long[] appendUID2Long(final AppendUID[] appendUIDs) {
		final long[] retval = new long[appendUIDs.length];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = appendUIDs[i].uid;
		}
		return retval;
	}

	private final Message getInlinedSpamMessage(final Message wrappingMsg) throws OXException, MessagingException {
		/*
		 * Get original message out of wrapping message from SpamAssassin
		 */
		final SpamMessageHandler msgHandler = new SpamMessageHandler();
		new MessageDumper(sessionObj, false, true).dumpMessage(wrappingMsg, msgHandler);
		return msgHandler.isSpam() && msgHandler.getInlineMessage() != null ? msgHandler.getInlineMessage()
				: wrappingMsg;
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
			final IMAPFolder defaultFolder = (IMAPFolder) imapCon.getIMAPStore().getDefaultFolder();
			list.add(new MailFolderObject(defaultFolder, sessionObj));
			return new SearchIteratorAdapter(list.iterator(), 1);
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			if (parentFolder.equals(MailFolderObject.DEFAULT_IMAP_FOLDER_ID)) {
				p = (IMAPFolder) imapCon.getIMAPStore().getDefaultFolder();
				if (!p.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, MailFolderObject.DEFAULT_IMAP_FOLDER_NAME);
				}
			} else {
				p = (IMAPFolder) imapCon.getIMAPStore().getFolder(parentFolder);
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
				final MailFolderObject mfo = new MailFolderObject((IMAPFolder) childFolders[i], sessionObj);
				if (mfo.exists()) {
					list.add(mfo);
				}
			}
			if (list.isEmpty()) {
				return SearchIterator.EMPTY_ITERATOR;
			}
			return new SearchIteratorAdapter(list.iterator(), list.size());
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getAllFolders()
	 */
	public SearchIterator getAllFolders() throws OXException {
		try {
			init();
			final IMAPFolder defaultFolder = (IMAPFolder) imapCon.getIMAPStore().getDefaultFolder();
			final Folder[] allFolders;
			final long start = System.currentTimeMillis();
			try {
				allFolders = defaultFolder.list("*");
			} finally {
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			}
			final List<MailFolderObject> list = new ArrayList<MailFolderObject>(allFolders.length);
			for (int i = 0; i < allFolders.length; i++) {
				list.add(new MailFolderObject((IMAPFolder) allFolders[i], sessionObj));
			}
			return new SearchIteratorAdapter(list.iterator(), allFolders.length);
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			if (folder.equals(MailFolderObject.DEFAULT_IMAP_FOLDER_ID)) {
				return new MailFolderObject((IMAPFolder) imapCon.getIMAPStore().getDefaultFolder(), sessionObj);
			}
			final IMAPFolder retval = (IMAPFolder) imapCon.getIMAPStore().getFolder(folder);
			if (checkFolder) {
				if (!retval.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, folder);
				} else if (IMAPProperties.isSupportsACLs() && ((retval.getType() & Folder.HOLDS_MESSAGES) > 0)) {
					try {
						if (!sessionObj.getCachedRights(retval, true).contains(Rights.Right.LOOKUP)) {
							throw new OXMailException(MailCode.NO_LOOKUP_ACCESS, getUserName(sessionObj), retval
									.getFullName());
						}
					} catch (final MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), retval.getFullName());
					}
				}
			}
			return new MailFolderObject(retval, sessionObj);
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
			if (folder.equals(MailFolderObject.DEFAULT_IMAP_FOLDER_ID)) {
				return SearchIteratorAdapter.createEmptyIterator();
			}
			final String defaultFolder = imapCon.getIMAPStore().getDefaultFolder().getFullName();
			IMAPFolder f = (IMAPFolder) imapCon.getIMAPStore().getFolder(folder);
			if (!f.exists()) {
				throw new OXMailException(MailCode.FOLDER_NOT_FOUND, folder);
			} else if (IMAPProperties.isSupportsACLs() && ((f.getType() & Folder.HOLDS_MESSAGES) > 0)) {
				try {
					if (!sessionObj.getCachedRights(f, true).contains(Rights.Right.LOOKUP)) {
						throw new OXMailException(MailCode.NO_LOOKUP_ACCESS, getUserName(sessionObj), f.getFullName());
					}
				} catch (final MessagingException e) {
					throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), f.getFullName());
				}
			}
			final List<MailFolderObject> retval = new ArrayList<MailFolderObject>();
			while (!f.getFullName().equals(defaultFolder)) {
				retval.add(new MailFolderObject(f, sessionObj));
				f = (IMAPFolder) f.getParent();
			}
			return new SearchIteratorAdapter(retval.iterator(), retval.size());
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	private final void canLookUpFolder(final IMAPFolder f) throws OXException {
		try {
			if (!f.exists()) {
				throw new OXMailException(MailCode.FOLDER_NOT_FOUND, f.getFullName());
			} else if (IMAPProperties.isSupportsACLs() && ((f.getType() & Folder.HOLDS_MESSAGES) > 0)) {
				try {
					if (!sessionObj.getCachedRights(f, true).contains(Rights.Right.LOOKUP)) {
						throw new OXMailException(MailCode.NO_LOOKUP_ACCESS, getUserName(sessionObj), f.getFullName());
					}
				} catch (final MessagingException e) {
					/*
					 * No rights defined on folder. Allow look up.
					 */
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder("No rights defined for folder ").append(f.getFullName()).append(
								": ").append(e.getMessage()).toString());
					}
				}
			}
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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
					if (isDefaultFolder(updateMe.getFullName())) {
						throw new OXMailException(MailCode.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
					}
					final IMAPFolder destFolder = ((IMAPFolder) (MailFolderObject.DEFAULT_IMAP_FOLDER_ID
							.equals(newParent) ? imapCon.getIMAPStore().getDefaultFolder() : imapCon.getIMAPStore()
							.getFolder(newParent)));
					if (!destFolder.exists()) {
						throw new OXMailException(MailCode.FOLDER_NOT_FOUND, newParent);
					}
					if (destFolder instanceof DefaultFolder) {
						if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
							throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder.getFullName());
						}
					} else {
						try {
							if (IMAPProperties.isSupportsACLs() && ((destFolder.getType() & Folder.HOLDS_MESSAGES) > 0)
									&& !sessionObj.getCachedRights(destFolder, true).contains(Rights.Right.CREATE)) {
								throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(sessionObj), newParent);
							}
						} catch (final MessagingException e) {
							LOG.error(e.getMessage(), e);
							throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), newParent);
						}
					}
					updateMe = moveFolder(updateMe, destFolder, newName);
				}
				if (rename) {
					/*
					 * Rename.
					 */
					if (isDefaultFolder(updateMe.getFullName())) {
						throw new OXMailException(MailCode.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
					}
					try {
						if (IMAPProperties.isSupportsACLs() && ((updateMe.getType() & Folder.HOLDS_MESSAGES) > 0)
								&& !sessionObj.getCachedRights(updateMe, true).contains(Rights.Right.CREATE)) {
							throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(sessionObj), updateMe
									.getFullName());
						}
					} catch (final MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), updateMe.getFullName());
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
					final IMAPFolder renameFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(tmp.toString());
					tmp = null;
					if (renameFolder.exists()) {
						throw new OXMailException(MailCode.DUPLICATE_FOLDER, renameFolder.getFullName());
					}
					/*
					 * Remember subscription status
					 */
					final String newFullName = renameFolder.getFullName();
					final String oldFullName = updateMe.getFullName();
					Map<String, Boolean> subscriptionStatus;
					try {
						subscriptionStatus = getSubscriptionStatus(updateMe, oldFullName, newFullName);
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(new StringBuilder(128).append("Subscription status of folder \"").append(
									updateMe.getFullName()).append(
									"\" and its subfolders could not be stored prior to rename operation"));
						}
						subscriptionStatus = null;
					}
					/*
					 * Rename
					 */
					boolean success = false;
					final long start = System.currentTimeMillis();
					try {
						success = updateMe.renameTo(renameFolder);
					} finally {
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
					/*
					 * Success?
					 */
					if (!success) {
						throw new OXMailException(MailCode.UPDATE_FAILED, updateMe.getFullName());
					}
					updateMe = (IMAPFolder) imapCon.getIMAPStore().getFolder(oldFullName);
					if (updateMe.exists()) {
						deleteFolder(updateMe);
					}
					updateMe = (IMAPFolder) imapCon.getIMAPStore().getFolder(newFullName);
					/*
					 * Apply remembered subscription status
					 */
					if (subscriptionStatus == null) {
						/*
						 * At least subscribe to renamed folder
						 */
						updateMe.setSubscribed(true);
					} else {
						applySubscriptionStatus(updateMe, subscriptionStatus);
					}
				}
				ACLS: if (IMAPProperties.isSupportsACLs()
						&& (folderObj.containsIMAPPermissions() || folderObj.containsACLs())) {
					/*
					 * Wrapper object contains rights. No simple rename but a
					 * whole ACL re-set
					 */
					final ACL[] oldACLs = updateMe.getACL();
					ACL[] newACLs;
					try {
						newACLs = folderObj.getACL();
					} catch (AbstractOXException e) {
						throw new OXMailException(e);
					}
					if (equals(oldACLs, newACLs)) {
						break ACLS;
					}
					if (isDefaultFolder(updateMe.getFullName())) {
						throw new OXMailException(MailCode.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
					}
					if (!sessionObj.getCachedRights(updateMe, true).contains(Rights.Right.ADMINISTER)) {
						throw new OXMailException(MailCode.NO_ADMINISTER_ACCESS, getUserName(sessionObj), updateMe
								.getFullName());
					}
					/*
					 * Check new ACLs
					 */
					if (newACLs.length == 0) {
						throw new OXMailException(MailCode.NO_ADMIN_ACL, getUserName(sessionObj),
								prepareMailFolderParam(folderObj.getFullName()));
					}
					boolean adminFound = false;
					for (int i = 0; i < newACLs.length && !adminFound; i++) {
						if (newACLs[i].getRights().contains(Rights.Right.ADMINISTER)) {
							adminFound = true;
						}
					}
					if (!adminFound) {
						throw new OXMailException(MailCode.NO_ADMIN_ACL, getUserName(sessionObj),
								prepareMailFolderParam(folderObj.getFullName()));
					}
					/*
					 * Remove deleted ACLs
					 */
					final ACL[] removedACLs = getRemovedACLs(newACLs, oldACLs);
					for (int i = 0; i < removedACLs.length; i++) {
						updateMe.removeACL(removedACLs[i].getName());
					}
					/*
					 * Change existing ACLs according to new ACLs
					 */
					for (int i = 0; i < newACLs.length; i++) {
						updateMe.addACL(newACLs[i]);
					}
					/*
					 * Since the ACLs have changed remove cached rights
					 */
					sessionObj.removeCachedRights(updateMe);
				}
				if (!IMAPProperties.isIgnoreSubscription() && folderObj.containsSubscribe()) {
					updateMe.setSubscribed(folderObj.isSubscribed());
					IMAPUtils.forceSetSubscribed(imapCon.getIMAPStore(), updateMe.getFullName(), folderObj
							.isSubscribed());
				}
				retval = updateMe.getFullName();
			} else {
				/*
				 * Insert
				 */
				final String parentStr = prepareMailFolderParam(folderObj.getParentFullName());
				final IMAPFolder parent = MailFolderObject.DEFAULT_IMAP_FOLDER_ID.equals(parentStr) ? (IMAPFolder) imapCon
						.getIMAPStore().getDefaultFolder()
						: (IMAPFolder) imapCon.getIMAPStore().getFolder(parentStr);
				if (!parent.exists()) {
					throw new OXMailException(MailCode.FOLDER_NOT_FOUND, parentStr);
				} else if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, parentStr);
				} else if (parent instanceof DefaultFolder) {
					if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
						throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_FOLDERS, parent.getFullName());
					}
				} else if (IMAPProperties.isSupportsACLs()) {
					try {
						if (!sessionObj.getCachedRights(parent, true).contains(Rights.Right.CREATE)) {
							throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(sessionObj), parentStr);
						}
					} catch (final MessagingException e) {
						throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), parentStr);
					}
				}
				if (folderObj.getName().indexOf(parent.getSeparator()) != -1) {
					throw new OXMailException(MailCode.INVALID_FOLDER_NAME, Character.valueOf(parent.getSeparator()));
				}
				final IMAPFolder createMe = (IMAPFolder) getFolder(parent, folderObj.getName());
				if (createMe.exists()) {
					throw new OXMailException(MailCode.DUPLICATE_FOLDER, createMe.getFullName());
				}
				final long start = System.currentTimeMillis();
				try {
					if (!createMe.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)) {
						throw new OXMailException(MailCode.FOLDER_CREATION_FAILED, createMe.getFullName(),
								parent instanceof DefaultFolder ? MailFolderObject.DEFAULT_IMAP_FOLDER_NAME : parent
										.getFullName());
					}
					createMe.setSubscribed(true);
					IMAPUtils.forceSetSubscribed(imapCon.getIMAPStore(), createMe.getFullName(), true);
				} finally {
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				}
				ACLS: if (folderObj.containsACLs() && IMAPProperties.isSupportsACLs()) {
					final ACL[] initialACLs = createMe.getACL();
					ACL[] newACLs;
					try {
						newACLs = folderObj.getACL();
					} catch (AbstractOXException e1) {
						throw new OXMailException(e1);
					}
					if (equals(initialACLs, newACLs)) {
						break ACLS;
					}
					boolean hasAdministerRight = false;
					for (final ACL current : initialACLs) {
						if (sessionObj.getIMAPProperties().getImapLogin().equals(current.getName())
								&& current.getRights().contains(Rights.Right.ADMINISTER)) {
							hasAdministerRight = true;
						}
					}
					if (!hasAdministerRight) {
						throw new OXMailException(MailCode.NO_ADMINISTER_ACCESS_ON_INITIAL, getUserName(sessionObj),
								createMe.getFullName());
					}
					boolean adminFound = false;
					for (int i = 0; i < newACLs.length && !adminFound; i++) {
						if (newACLs[i].getRights().contains(Rights.Right.ADMINISTER)) {
							adminFound = true;
						}
					}
					if (!adminFound) {
						throw new OXMailException(MailCode.NO_ADMIN_ACL, getUserName(sessionObj),
								prepareMailFolderParam(folderObj.getFullName()));
					}
					/*
					 * Wrapper object contains rights. Add new ACLs from
					 * folderObj
					 */
					try {
						for (int i = 0; i < folderObj.getACL().length; i++) {
							createMe.addACL(folderObj.getACL()[i]);
						}
					} catch (AbstractOXException e) {
						throw new OXMailException(e);
					}
				}
				retval = createMe.getFullName();
			}
			return retval;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
		}
	}

	private static final Map<String, Boolean> getSubscriptionStatus(final IMAPFolder f, final String oldFullName,
			final String newFullName) throws MessagingException {
		final Map<String, Boolean> retval = new HashMap<String, Boolean>();
		getSubscriptionStatus(retval, f, oldFullName, newFullName);
		return retval;
	}

	private static final void getSubscriptionStatus(final Map<String, Boolean> m, final IMAPFolder f,
			final String oldFullName, final String newFullName) throws MessagingException {
		if ((f.getType() & IMAPFolder.HOLDS_FOLDERS) > 0) {
			final Folder[] folders = f.list();
			for (int i = 0; i < folders.length; i++) {
				getSubscriptionStatus(m, (IMAPFolder) folders[i], oldFullName, newFullName);
			}
		}
		m.put(f.getFullName().replaceFirst(oldFullName, newFullName), Boolean.valueOf(f.isSubscribed()));
	}

	private static final void applySubscriptionStatus(final IMAPFolder f, final Map<String, Boolean> m)
			throws MessagingException {
		if ((f.getType() & IMAPFolder.HOLDS_FOLDERS) > 0) {
			final Folder[] folders = f.list();
			for (int i = 0; i < folders.length; i++) {
				applySubscriptionStatus((IMAPFolder) folders[i], m);
			}
		}
		Boolean b = m.get(f.getFullName());
		if (b == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(128).append("No stored subscription status found for \"").append(
						f.getFullName()).append('"').toString());
			}
			b = Boolean.TRUE;
		}
		f.setSubscribed(b.booleanValue());
	}

	private static final ACL[] getRemovedACLs(final ACL[] newACLs, final ACL[] oldACLs) {
		final List<ACL> retval = new ArrayList<ACL>();
		for (final ACL oldACL : oldACLs) {
			boolean found = false;
			for (int i = 0; i < newACLs.length && !found; i++) {
				if (newACLs[i].getName().equals(oldACL.getName())) {
					found = true;
				}
			}
			if (!found) {
				retval.add(oldACL);
			}
		}
		return retval.toArray(new ACL[retval.size()]);
	}

	private static final String STR_PAT = "p|P";

	private static final boolean equals(final ACL[] acls1, final ACL[] acls2) {
		if (acls1.length != acls2.length) {
			return false;
		}
		for (final ACL acl1 : acls1) {
			boolean found = false;
			Inner: for (final ACL acl2 : acls2) {
				if (acl1.getName().equals(acl2.getName())) {
					found = true;
					if (!acl1.getRights().toString().replaceAll(STR_PAT, STR_EMPTY).equals(
							acl2.getRights().toString().replaceAll(STR_PAT, STR_EMPTY))) {
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
		}
		final int toMoveType = toMove.getType();
		if (IMAPProperties.isSupportsACLs() && ((toMoveType & Folder.HOLDS_MESSAGES) > 0)) {
			try {
				if (!sessionObj.getCachedRights(toMove, true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, getUserName(sessionObj), toMove.getFullName());
				} else if (!sessionObj.getCachedRights(toMove, true).contains(Rights.Right.CREATE)) {
					throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(sessionObj), toMove.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), toMove.getFullName());
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
		final IMAPFolder newFolder = (IMAPFolder) imapCon.getIMAPStore().getFolder(sb.toString());
		sb = null;
		if (checkForDuplicate && newFolder.exists()) {
			throw new OXMailException(MailCode.DUPLICATE_FOLDER, folderName);
		}
		/*
		 * Create new folder. NOTE: It's not possible to create a folder only
		 * with type set to HOLDS_FOLDERS, cause created folder is selectable
		 * anyway and therefore does not hold flag \NoSelect.
		 */
		if (!newFolder.create(toMoveType)) {
			throw new OXMailException(MailCode.FOLDER_CREATION_FAILED, newFolder.getFullName(),
					destFolder instanceof DefaultFolder ? MailFolderObject.DEFAULT_IMAP_FOLDER_NAME : destFolder
							.getFullName());
		}
		try {
			newFolder.open(Folder.READ_WRITE);
			mailInterfaceMonitor.changeNumActive(true);
			newFolder.setSubscribed(toMove.isSubscribed());
			/*
			 * Copy ACLs
			 */
			final ACL[] acls = toMove.getACL();
			for (int i = 0; i < acls.length; i++) {
				newFolder.addACL(acls[i]);
			}
			newFolder.close(false);
			mailInterfaceMonitor.changeNumActive(false);
		} catch (final ReadOnlyFolderException e) {
			throw new OXMailException(MailCode.NO_WRITE_ACCESS, getUserName(sessionObj), newFolder.getFullName());
		}
		if ((toMoveType & Folder.HOLDS_MESSAGES) > 0) {
			/*
			 * Copy messages
			 */
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
			toMove.close(false);
			mailInterfaceMonitor.changeNumActive(false);
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
		IMAPUtils.forceSetSubscribed(imapCon.getIMAPStore(), toMove.getFullName(), false);
		if (!toMove.delete(true) && LOG.isWarnEnabled()) {
			final OXMailException e = new OXMailException(MailCode.DELETE_FAILED, toMove.getFullName());
			LOG.warn(e.getMessage(), e);
		}
		/*
		 * Remove cache entries
		 */
		sessionObj.removeCachedRights(toMove);
		sessionObj.removeCachedUserFlags(toMove);
		return newFolder;
	}

	private final void deleteFolder(final IMAPFolder deleteMe) throws OXException, MessagingException {
		if (deleteMe.getFullName().equalsIgnoreCase(STR_INBOX)) {
			throw new OXMailException(MailCode.NO_FOLDER_DELETE, STR_INBOX);
		}
		if (isDefaultFolder(deleteMe.getFullName())) {
			throw new OXMailException(MailCode.NO_DEFAULT_FOLDER_DELETE, deleteMe.getFullName());
		}
		if (!deleteMe.exists()) {
			throw new OXMailException(MailCode.FOLDER_NOT_FOUND, deleteMe.getFullName());
		}
		try {
			if (IMAPProperties.isSupportsACLs() && ((deleteMe.getType() & Folder.HOLDS_MESSAGES) > 0)
					&& !sessionObj.getCachedRights(deleteMe, true).contains(Rights.Right.CREATE)) {
				throw new OXMailException(MailCode.NO_CREATE_ACCESS, getUserName(sessionObj), deleteMe.getFullName());
			}
		} catch (final MessagingException e) {
			throw new OXMailException(MailCode.NO_ACCESS, getUserName(sessionObj), deleteMe.getFullName());
		}
		if (deleteMe.isOpen()) {
			deleteMe.close(false);
			mailInterfaceMonitor.changeNumActive(false);
		}
		/*
		 * Unsubscribe prio to deletion
		 */
		IMAPUtils.forceSetSubscribed(imapCon.getIMAPStore(), deleteMe.getFullName(), false);
		final long start = System.currentTimeMillis();
		try {
			if (!deleteMe.delete(true)) {
				throw new OXMailException(MailCode.DELETE_FAILED, deleteMe.getFullName());
			}
		} finally {
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		}
		/*
		 * Remove cache entries
		 */
		sessionObj.removeCachedRights(deleteMe);
		sessionObj.removeCachedUserFlags(deleteMe);
	}

	private boolean isDefaultFolder(final String folderFullName) throws OXException {
		boolean isDefaultFolder = false;
		isDefaultFolder = (folderFullName.equalsIgnoreCase(STR_INBOX));
		for (int index = 0; index < 6 && !isDefaultFolder; index++) {
			if (folderFullName.equalsIgnoreCase(prepareMailFolderParam(getStdFolder(index)))) {
				return true;
			}
		}
		return isDefaultFolder;
	}

	public String deleteFolder(final String folderArg) throws OXException {
		try {
			init();
			final String folder = prepareMailFolderParam(folderArg);
			final IMAPFolder deleteMe = (IMAPFolder) imapCon.getIMAPStore().getFolder(folder);
			final String retval = deleteMe.getFullName();
			deleteFolder(deleteMe);
			return retval;
		} catch (final MessagingException e) {
			throw handleMessagingException(e, sessionObj.getIMAPProperties(), sessionObj.getContext());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getConfirmedSpamFolder()
	 */
	public String getConfirmedSpamFolder() throws OXException {
		return IMAPProperties.isSpamEnabled() ? getStdFolder(INDEX_CONFIRMED_SPAM) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterface#getConfirmedHamFolder()
	 */
	public String getConfirmedHamFolder() throws OXException {
		return IMAPProperties.isSpamEnabled() ? getStdFolder(INDEX_CONFIRMED_HAM) : null;
	}

	private String getStdFolder(final int index) throws OXException {
		try {
			init();
			if (INDEX_INBOX == index) {
				final Folder inbox = imapCon.getIMAPStore().getFolder(STR_INBOX);
				return MailFolderObject.prepareFullname(inbox.getFullName(), inbox.getSeparator());
			}
			return usm.getStandardFolder(index);
		} catch (final MessagingException e) {
			throw handleMessagingException(e);
		}
	}

	private static final String getUserName(final SessionObject sessionObj) {
		if (sessionObj == null) {
			return STR_EMPTY;
		}
		return new StringBuilder().append(sessionObj.getUserObject().getDisplayName()).append(" (").append(
				sessionObj.getUserObject().getId()).append(')').toString();
	}

	public static OXMailException handleMessagingException(final MessagingException e) {
		return handleMessagingException(e, null, null);
	}

	private static final String ERR_AUTH_FAILED = "bad authentication failed";

	private static final String ERR_TMP = "temporary error, please try again later";

	private static final String ERR_MSG_TOO_LARGE = "message too large";

	public static OXMailException handleMessagingException(final MessagingException e, final IMAPProperties imapProps,
			final Context ctx) {
		final OXMailException oxme;
		if (e instanceof AuthenticationFailedException) {
			final boolean temporary = e.getMessage() != null
					&& ERR_TMP.equals(e.getMessage().toLowerCase(Locale.ENGLISH));
			if (imapProps != null && ctx != null) {
				if (temporary) {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, imapProps.getImapServer(),
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(imapProps.getUser(), ctx),
							Integer.valueOf(ctx.getContextId()));
				} else {
					oxme = new OXMailException(MailCode.INVALID_CREDENTIALS, e, imapProps.getImapServer(),
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(imapProps.getUser(), ctx),
							Integer.valueOf(ctx.getContextId()));
				}
			} else {
				if (temporary) {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, STR_EMPTY, STR_EMPTY, STR_EMPTY);
				} else {
					oxme = new OXMailException(MailCode.INVALID_CREDENTIALS, e, STR_EMPTY, STR_EMPTY, STR_EMPTY);
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
			if (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
				oxme = new OXMailException(MailCode.MESSAGE_TOO_LARGE, exc, new Object[0]);
			} else {
				oxme = new OXMailException(MailCode.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
			}
		} else if (e instanceof StoreClosedException) {
			oxme = new OXMailException(MailCode.STORE_CLOSED, e, e.getMessage());
		} else {
			/*
			 * No subclass of MessagingException
			 */
			if (e.getNextException() instanceof BindException) {
				oxme = new OXMailException(MailCode.BIND_ERROR, e, imapProps == null ? STR_EMPTY : Integer
						.valueOf(imapProps.getImapPort()));
			} else if (e.getNextException() instanceof com.sun.mail.iap.ConnectionException) {
				mailInterfaceMonitor.changeNumBrokenConnections(true);
				oxme = new OXMailException(MailCode.CONNECT_ERROR, e, imapProps == null ? STR_EMPTY : imapProps
						.getImapServer(), imapProps == null ? STR_EMPTY : imapProps.getImapLogin());
			} else if (e.getNextException() instanceof ConnectException) {
				OXMailException tmp = null;
				try {
					mailInterfaceMonitor.changeNumTimeoutConnections(true);
					tmp = new OXMailException(MailCode.CONNECT_ERROR, e, imapProps == null ? STR_EMPTY : imapProps
							.getImapServer(), imapProps == null ? STR_EMPTY : imapProps.getImapLogin());
					if (IMAPProperties.getImapConnectionTimeout() > 0) {
						/*
						 * Most modern IP stack implementations sense connection
						 * idleness, and abort the connection attempt, resulting
						 * in a java.net.ConnectionException
						 */
						tmp.setCategory(Category.TRY_AGAIN);
					}
				} catch (final IMAPException oxExc) {
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
				oxme = new OXMailException(MailCode.PORT_UNREACHABLE, e, imapProps == null ? STR_EMPTY : Integer
						.valueOf(imapProps.getImapPort()));
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
				if (imapProps != null && ctx != null) {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, imapProps.getImapServer(),
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(imapProps.getUser(), ctx),
							Integer.valueOf(ctx.getContextId()));
				} else {
					oxme = new OXMailException(MailCode.LOGIN_FAILED, e, STR_EMPTY, STR_EMPTY, STR_EMPTY);
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
		} else if (MailFolderObject.DEFAULT_IMAP_FOLDER_ID.equals(folderStringArg)) {
			return folderStringArg;
		} else if (folderStringArg.startsWith(MailFolderObject.DEFAULT_IMAP_FOLDER_ID)) {
			return folderStringArg.substring(8);
		}
		return folderStringArg;
	}

	public final static String encodePassword(final String password) {
		String tmpPass = password;
		if (password != null) {
			try {
				tmpPass = new String(password.getBytes(IMAPProperties.getImapAuthEnc()), CHARENC_ISO_8859_1);
			} catch (final UnsupportedEncodingException e) {
				LOG.error("Unsupported encoding in a message detected and monitored.", e);
				mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			} catch (final IMAPException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return tmpPass;
	}

}
