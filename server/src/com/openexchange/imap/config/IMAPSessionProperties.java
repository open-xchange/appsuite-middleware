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

package com.openexchange.imap.config;

import java.security.Security;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.mail.mime.MIMEDefaultSession;
import com.openexchange.mail.mime.MIMESessionPropertyNames;

/**
 * {@link IMAPSessionProperties} - Default properties for an IMAP session
 * established via <code>JavaMail</code> API
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPSessionProperties {

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	private static Properties imapSessionProperties;

	private static final Lock LOCK = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	/**
	 * No instantiation
	 */
	private IMAPSessionProperties() {
		super();
	}

	/**
	 * Creates a <b>cloned</b> version of default IMAP session properties
	 * 
	 * @return a cloned version of default IMAP session properties
	 */
	public static Properties getDefaultSessionProperties() {
		if (!initialized.get()) {
			LOCK.lock();
			try {
				if (null == imapSessionProperties) {
					initializeIMAPProperties();
					initialized.set(true);
				}
			} finally {
				LOCK.unlock();
			}
		}
		return (Properties) imapSessionProperties.clone();
	}

	private static final String STR_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

	private static final String STR_SECURITY_FACTORY = "com.openexchange.tools.ssl.TrustAllSSLSocketFactory";

	/**
	 * This method can only be exclusively accessed
	 */
	private static void initializeIMAPProperties() {
		/*
		 * Force initialization of default MIME session
		 */
		MIMEDefaultSession.getDefaultSession();
		/*
		 * Define imap session properties
		 */
		imapSessionProperties = ((Properties) (System.getProperties().clone()));
		/*
		 * Set some global JavaMail properties
		 */
		if (!imapSessionProperties.containsKey(MIMESessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS)) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, STR_TRUE);
			System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, STR_TRUE);
		}
		if (!imapSessionProperties.containsKey(MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT)) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT, STR_TRUE);
			System.getProperties().put(MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT, STR_TRUE);
		}
		if (!imapSessionProperties.containsKey(MIMESessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT)) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, STR_TRUE);
			System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, STR_TRUE);
		}
		if (!imapSessionProperties.containsKey(MIMESessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT)) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, STR_FALSE);
			System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, STR_FALSE);
		}
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
		imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLSIZE, "1");
		/*
		 * A mechanism is provided for timing out idle connection pool IMAP
		 * protocol objects. Timed out connections are closed and removed
		 * (pruned) from the connection pool.
		 */
		imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT, "1000");
		if (!imapSessionProperties.containsKey(MIMESessionPropertyNames.PROP_MAIL_MIME_CHARSET)) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_MIME_CHARSET, IMAPConfig.getDefaultMimeCharset());
			System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_CHARSET,
					IMAPConfig.getDefaultMimeCharset());
		}
		/*
		 * Following properties define if IMAPS should be enabled
		 */
		if (IMAPConfig.isImapsEnabled()) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS, STR_SECURITY_FACTORY);
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_PORT, String
					.valueOf(IMAPConfig.getImapsPort()));
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK, STR_FALSE);
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_STARTTLS_ENABLE, STR_TRUE);
			/*
			 * Needed for JavaMail >= 1.4
			 */
			Security.setProperty(STR_SECURITY_PROVIDER, STR_SECURITY_FACTORY);
		}
		if (IMAPConfig.getImapTimeout() > 0) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_TIMEOUT, String.valueOf(IMAPConfig
					.getImapTimeout()));
		}
		if (IMAPConfig.getImapConnectionTimeout() > 0) {
			imapSessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONTIMEOUT, String.valueOf(IMAPConfig
					.getImapConnectionTimeout()));
		}
		if (IMAPConfig.getJavaMailProperties() != null) {
			/*
			 * Overwrite current JavaMail-Specific properties with the ones
			 * defined in javamail.properties
			 */
			imapSessionProperties.putAll(IMAPConfig.getJavaMailProperties());
		}
	}
}
