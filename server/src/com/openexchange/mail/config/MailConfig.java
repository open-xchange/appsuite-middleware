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

package com.openexchange.mail.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.partmodifier.PartModifier;
import com.openexchange.mail.spellcheck.SpellCheckConfig;
import com.openexchange.session.Session;

/**
 * {@link MailConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailConfig {

	public static enum BoolCapVal {

		/**
		 * TRUE
		 */
		TRUE("true"),
		/**
		 * FALSE
		 */
		FALSE("false"),
		/**
		 * AUTO
		 */
		AUTO("auto");

		private final String str;

		private BoolCapVal(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}

		/**
		 * Parses given capability value. If given value equals ignore-case to
		 * string <code>true</code>, constant {@link #TRUE} will be returned.
		 * Else if given value equals ignore-case to string <code>auto</code>,
		 * constant {@link #AUTO} will be returned. Otherwise {@link #FALSE}
		 * will be returned.
		 * 
		 * @param capVal -
		 *            the string value to parse
		 * @return an instance of <code>BoolCapVal</code>: either
		 *         {@link #TRUE}, {@link #FALSE}, or {@link #AUTO}
		 */
		public final static BoolCapVal parseBoolCapVal(final String capVal) {
			if (TRUE.str.equalsIgnoreCase(capVal)) {
				return TRUE;
			} else if (AUTO.str.equalsIgnoreCase(capVal)) {
				return AUTO;
			}
			return FALSE;
		}
	}

	public static enum CredSrc {
		SESSION("session"), USER_IMAPLOGIN("user.imapLogin"), OTHER("other");

		private final String str;

		private CredSrc(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public static enum LoginType {
		GLOBAL("global"), USER("user"), ANONYMOUS("anonymous");

		private final String str;

		private LoginType(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * Constructor
	 */
	protected MailConfig() {
		super();
	}

	/*
	 * User-specific fields
	 */
	private String login;

	private String password;

	private AbstractOXException error;

	/**
	 * Fills login and password in specified instance of {@link MailConfig}
	 * 
	 * @param mailConfig
	 *            The mail config
	 * @param session
	 *            The session providing needed user data
	 * @throws MailConfigException
	 */
	protected static final void fillLoginAndPassword(final MailConfig mailConfig, final Session session)
			throws MailConfigException {
		/*
		 * Fetch user object and create its mail properties
		 */
		final User user = UserStorage.getStorageUser(session.getUserId(), session.getContext());
		if (LoginType.GLOBAL.equals(getLoginType())) {
			final String masterPw = GlobalMailConfig.getInstance().getMasterPassword();
			if (masterPw == null) {
				throw new MailConfigException(new StringBuilder().append("Property \"").append("masterPassword")
						.append("\" not set").toString());
			}
			mailConfig.login = user.getMail();
			mailConfig.password = masterPw;
		} else if (LoginType.USER.equals(getLoginType())) {
			if (getCredSrc() == null || CredSrc.SESSION.equals(getCredSrc())) {
				mailConfig.login = getLocalMailLogin(session, false);
				mailConfig.password = session.getPassword();
			} else if (CredSrc.OTHER.equals(getCredSrc())) {
				mailConfig.password = TEST_PW;
				mailConfig.login = getRandomTestLogin();
			} else if (CredSrc.USER_IMAPLOGIN.equals(getCredSrc())) {
				mailConfig.password = session.getPassword();
				mailConfig.login = getLocalMailLogin(session, true);
			}
		} else if (LoginType.ANONYMOUS.equals(getLoginType())) {
			mailConfig.login = LoginType.ANONYMOUS.toString();
			mailConfig.password = "";
		}
	}

	/**
	 * Determines login for session-associated user. If <code>lookUp</code> is
	 * <code>true</code>, this routine tries to fetch the mail login from
	 * {@link User#getImapLogin()} and falls back to session-supplied user login
	 * info. Otherwise session-supplied user login info is directly taken as
	 * return value.
	 * 
	 * @param session -
	 *            the user's session
	 * @param lookUp -
	 *            determines whether to look up {@link User#getImapLogin()} or
	 *            not
	 * @return The session-associated user's login
	 */
	private static final String getLocalMailLogin(final Session session, final boolean lookUp) {
		String login = lookUp ? UserStorage.getStorageUser(session.getUserId(), session.getContext()).getImapLogin() : null;
		if (login == null || login.length() == 0) {
			login = session.getUserlogin() != null && session.getUserlogin().length() > 0 ? session.getUserlogin()
					: String.valueOf(session.getUserId());
		}
		return login;
	}

	/**
	 * Gets the loginType
	 * 
	 * @return the loginType
	 */
	public static final LoginType getLoginType() {
		return GlobalMailConfig.getInstance().getLoginType();
	}

	/**
	 * Gets the allowNestedDefaultFolderOnAltNamespace
	 * 
	 * @return the allowNestedDefaultFolderOnAltNamespace
	 */
	public static final boolean isAllowNestedDefaultFolderOnAltNamespace() {
		return GlobalMailConfig.getInstance().isAllowNestedDefaultFolderOnAltNamespace();
	}

	/**
	 * Gets the attachDisplaySize
	 * 
	 * @return the attachDisplaySize
	 */
	public static final int getAttachDisplaySize() {
		return GlobalMailConfig.getInstance().getAttachDisplaySize();
	}

	/**
	 * Gets the credSrc
	 * 
	 * @return the credSrc
	 */
	public static final CredSrc getCredSrc() {
		return GlobalMailConfig.getInstance().getCredSrc();
	}

	/**
	 * Gets the mailServer
	 * 
	 * @return the mailServer
	 */
	public static final String getMailServer() {
		return GlobalMailConfig.getInstance().getMailServer();
	}

	/**
	 * Gets the transportServer
	 * 
	 * @return the transportServer
	 */
	public static final String getTransportServer() {
		return GlobalMailConfig.getInstance().getTransportServer();
	}

	/**
	 * Gets the masterPassword
	 * 
	 * @return the masterPassword
	 */
	public static final String getMasterPassword() {
		return GlobalMailConfig.getInstance().getMasterPassword();
	}

	/**
	 * Gets the defaultMimeCharset
	 * 
	 * @return the defaultMimeCharset
	 */
	public static final String getDefaultMimeCharset() {
		return GlobalMailConfig.getInstance().getDefaultMimeCharset();
	}

	/**
	 * Gets the defaultSeparator
	 * 
	 * @return the defaultSeparator
	 */
	public static final char getDefaultSeparator() {
		return GlobalMailConfig.getInstance().getDefaultSeparator();
	}

	/**
	 * Gets the maxNumOfConnections
	 * 
	 * @return the maxNumOfConnections
	 */
	public static final int getMaxNumOfConnections() {
		return GlobalMailConfig.getInstance().getMaxNumOfConnections();
	}

	/**
	 * Gets the ignoreSubscription
	 * 
	 * @return the ignoreSubscription
	 */
	public static final boolean isIgnoreSubscription() {
		return GlobalMailConfig.getInstance().isIgnoreSubscription();
	}

	/**
	 * Gets the mailFetchLimit
	 * 
	 * @return the mailFetchLimit
	 */
	public static final int getMailFetchLimit() {
		return GlobalMailConfig.getInstance().getMailFetchLimit();
	}

	/**
	 * Gets the partModifier
	 * 
	 * @return the partModifier
	 */
	public static final PartModifier getPartModifier() {
		return PartModifier.getInstance();
	}

	/**
	 * Gets the quoteLineColors
	 * 
	 * @return the quoteLineColors
	 */
	public static final String[] getQuoteLineColors() {
		return GlobalMailConfig.getInstance().getQuoteLineColors();
	}

	/**
	 * Gets the spamEnabled
	 * 
	 * @return the spamEnabled
	 */
	public static final boolean isSpamEnabled() {
		return GlobalMailConfig.getInstance().isSpamEnabled();
	}

	/**
	 * Gets the spellCheckConfig
	 * 
	 * @return the spellCheckConfig
	 */
	public static final SpellCheckConfig getSpellCheckConfig() {
		return GlobalMailConfig.getInstance().getSpellCheckConfig();
	}

	/**
	 * Gets the userFlagsEnabled
	 * 
	 * @return the userFlagsEnabled
	 */
	public static final boolean isUserFlagsEnabled() {
		return GlobalMailConfig.getInstance().isUserFlagsEnabled();
	}

	/**
	 * Gets the javaMailProperties
	 * 
	 * @return the javaMailProperties
	 */
	public static Properties getJavaMailProperties() {
		return GlobalMailConfig.getInstance().getJavaMailProperties();
	}

	/**
	 * Gets the watcherEnabled
	 * 
	 * @return the watcherEnabled
	 */
	public static boolean isWatcherEnabled() {
		return GlobalMailConfig.getInstance().isWatcherEnabled();
	}

	/**
	 * Gets the watcherFrequency
	 * 
	 * @return the watcherFrequency
	 */
	public static int getWatcherFrequency() {
		return GlobalMailConfig.getInstance().getWatcherFrequency();
	}

	/**
	 * Gets the watcherShallClose
	 * 
	 * @return the watcherShallClose
	 */
	public static boolean isWatcherShallClose() {
		return GlobalMailConfig.getInstance().isWatcherShallClose();
	}

	/**
	 * Gets the watcherTime
	 * 
	 * @return the watcherTime
	 */
	public static int getWatcherTime() {
		return GlobalMailConfig.getInstance().getWatcherTime();
	}

	/**
	 * Gets the spamHandlerClass
	 * 
	 * @return the spamHandlerClass
	 */
	public static String getSpamHandlerClass() {
		return GlobalMailConfig.getInstance().getSpamHandlerClass();
	}

	/**
	 * Gets the login
	 * 
	 * @return the login
	 */
	public final String getLogin() {
		return login;
	}

	/**
	 * Gets the password
	 * 
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * @return The host name or IP address of the server
	 */
	public abstract String getServer();

	/**
	 * @return The port of the server obtained via {@link #getServer()}
	 */
	public abstract int getPort();

	/**
	 * @return Gets the encoded capabilities
	 */
	public abstract int getCapabilities();

	/**
	 * Gets occured error
	 * 
	 * @return the error (if any) or <code>null</code>
	 */
	public AbstractOXException getError() {
		return error;
	}

	/*
	 * TEST TEST TEST TEST TEST
	 */

	protected static final String TEST_PW = "oxTEST";

	private static Map<Integer, String> TEST_LOGIN_MAP;

	static {
		final StringBuilder sb = new StringBuilder();
		TEST_LOGIN_MAP = new HashMap<Integer, String>();
		for (int i = 1; i <= 10000; i++) {
			TEST_LOGIN_MAP.put(Integer.valueOf(i), sb.append("ox-test-").append(i).append("@nms112.de").toString());
			sb.setLength(0);
		}
	}

	private static final Random RANDOM = new Random();

	protected static String getRandomTestLogin() {
		final int num = 1 + Math.abs(RANDOM.nextInt()) % 10000;
		return TEST_LOGIN_MAP.get(Integer.valueOf(num));
	}
}
