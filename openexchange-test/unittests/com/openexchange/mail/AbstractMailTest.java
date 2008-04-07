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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import com.openexchange.configuration.MailConfig;
import com.openexchange.groupware.Init;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;

/**
 * {@link AbstractMailTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class AbstractMailTest extends TestCase {

	private String server;

	private int port;

	private String login;

	private String secondlogin;

	private String password;

	private int user;

	private int cid;

	private String testMailDir;

	/**
	 * 
	 */
	public AbstractMailTest() {
		super();
	}

	/**
	 * @param name
	 */
	public AbstractMailTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		try {
			/*
			 * Init
			 */
			Init.startServer();
			/*
			 * Init test environment
			 */
			MailConfig.init();
			server = MailConfig.getProperty(MailConfig.Property.SERVER);
			port = Integer.parseInt(MailConfig.getProperty(MailConfig.Property.PORT));
			login = MailConfig.getProperty(MailConfig.Property.LOGIN);
			secondlogin = MailConfig.getProperty(MailConfig.Property.SECOND_LOGIN);
			password = MailConfig.getProperty(MailConfig.Property.PASSWORD);
			user = Integer.parseInt(MailConfig.getProperty(MailConfig.Property.USER));
			cid = Integer.parseInt(MailConfig.getProperty(MailConfig.Property.CONTEXT));
			testMailDir = MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR);
		} catch (final Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		Init.stopServer();
	}

	/**
	 * Gets the login
	 * 
	 * @return the login
	 */
	protected final String getLogin() {
		return login;
	}

	/**
	 * Gets the second login
	 * 
	 * @return the second login
	 */
	protected final String getSecondLogin() {
		return secondlogin;
	}

	/**
	 * Gets the password
	 * 
	 * @return the password
	 */
	protected final String getPassword() {
		return password;
	}

	/**
	 * Gets the port
	 * 
	 * @return the port
	 */
	protected final int getPort() {
		return port;
	}

	/**
	 * Gets the server
	 * 
	 * @return the server
	 */
	protected final String getServer() {
		return server;
	}

	/**
	 * Gets the cid
	 * 
	 * @return the cid
	 */
	protected final int getCid() {
		return cid;
	}

	/**
	 * Gets the user
	 * 
	 * @return the user
	 */
	protected final int getUser() {
		return user;
	}

	/**
	 * Gets the test mail directory
	 * 
	 * @return the test mail directory
	 */
	protected final String getTestMailDir() {
		return testMailDir;
	}

	private static final String STR_TRUE = "true";

	private static final String STR_FALSE = "false";

	private static Properties sessionProperties;

	/**
	 * Gets the default session properties
	 * 
	 * @return The default session properties
	 */
	protected static final Properties getDefaultSessionProperties() {
		synchronized (AbstractMailTest.class) {
			if (sessionProperties == null) {
				/*
				 * Define session properties
				 */
				System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, STR_TRUE);
				System.getProperties().put(MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT, STR_TRUE);
				System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, STR_TRUE);
				System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, STR_FALSE);
				System.getProperties().put(MIMESessionPropertyNames.PROP_MAIL_MIME_CHARSET, "UTF-8");
				/*
				 * Define imap session properties
				 */
				sessionProperties = ((Properties) (System.getProperties().clone()));
				/*
				 * A connected IMAPStore maintains a pool of IMAP protocol
				 * objects for use in communicating with the IMAP server. The
				 * IMAPStore will create the initial AUTHENTICATED connection
				 * and seed the pool with this connection. As folders are opened
				 * and new IMAP protocol objects are needed, the IMAPStore will
				 * provide them from the connection pool, or create them if none
				 * are available. When a folder is closed, its IMAP protocol
				 * object is returned to the connection pool if the pool is not
				 * over capacity.
				 */
				sessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLSIZE, "1");
				/*
				 * A mechanism is provided for timing out idle connection pool
				 * IMAP protocol objects. Timed out connections are closed and
				 * removed (pruned) from the connection pool.
				 */
				sessionProperties.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT, "1000");
				return sessionProperties;
			}
			return sessionProperties;
		}
	}

	/**
	 * Reads MIME messages (<code>*.eml</code> files) from specified
	 * directory
	 * 
	 * @param dir
	 *            The directory containing <code>*.eml</code> files
	 * @param limit
	 *            The limit or <code>-1</code> to read all available
	 *            <code>*.eml</code> files
	 * @return The read MIME messages
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 * @throws MailException
	 *             If conversion from RFC822 message fails
	 */
	protected static final MailMessage[] getMessages(final String dir, final int limit) throws MessagingException,
			IOException, MailException {
		final File fdir = new File(dir);
		final File[] messageFiles = fdir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".eml");
			}
		});
		final int len = limit < 0 ? messageFiles.length : Math.min(messageFiles.length, limit);
		final MimeMessage[] msgs = new MimeMessage[len];
		final Session session = Session.getInstance(getDefaultSessionProperties());
		for (int i = 0; i < msgs.length; i++) {
			InputStream in = null;
			try {
				in = new FileInputStream(messageFiles[i]);
				msgs[i] = new MimeMessage(session, in);
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		final MailMessage[] retval = new MailMessage[msgs.length];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = MIMEMessageConverter.convertMessage(msgs[i]);
		}
		return retval;
	}
}
