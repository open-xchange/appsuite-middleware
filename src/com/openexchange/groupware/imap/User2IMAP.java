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

package com.openexchange.groupware.imap;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.imap.IMAPPropertiesFactory.IMAPLoginType;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.SessionObject;

/**
 * User2IMAPInterface
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class User2IMAP {

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(User2IMAP.class);

	public static enum IMAPServer {
		/**
		 * Cyrus: <code>com.openexchange.groupware.imap.CyrusUser2IMAP</code>
		 */
		CYRUS("Cyrus", "com.openexchange.groupware.imap.CyrusUser2IMAP"),
		/**
		 * Courier:
		 * <code>com.openexchange.groupware.imap.CourierUser2IMAP</code>
		 */
		COURIER("Courier", "com.openexchange.groupware.imap.CourierUser2IMAP");

		private final String name;

		private final String impl;

		private IMAPServer(final String name, final String impl) {
			this.name = name;
			this.impl = impl;
		}

		public String getName() {
			return name;
		}

		public String getImpl() {
			return impl;
		}
	}

	private static final String getIMAPServerImpl(final String name) {
		final IMAPServer[] imapServers = IMAPServer.values();
		for (int i = 0; i < imapServers.length; i++) {
			if (imapServers[i].getName().equalsIgnoreCase(name)) {
				return imapServers[i].impl;
			}
		}
		return null;
	}

	public static final class User2IMAPException extends AbstractOXException {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 5973482982793524052L;

		/**
		 * Initializes a new exception using the information provided by the
		 * cause.
		 * 
		 * @param cause
		 *            the cause of the exception.
		 */
		public User2IMAPException(final AbstractOXException cause) {
			super(cause);
		}

		/**
		 * Initializes a new exception using the information provided by the
		 * code.
		 * 
		 * @param code
		 *            code for the exception.
		 * @param messageArgs
		 *            arguments that will be formatted into the message.
		 */
		public User2IMAPException(final Code code, final Object... messageArgs) {
			this(code, null, messageArgs);
		}

		/**
		 * Initializes a new exception using the information provided by the
		 * code.
		 * 
		 * @param code
		 *            code for the exception.
		 * @param cause
		 *            the cause of the exception.
		 * @param messageArgs
		 *            arguments that will be formatted into the message.
		 */
		public User2IMAPException(final Code code, final Throwable cause, final Object... messageArgs) {
			super(Component.ACL_ERROR, code.category, code.number, code.message, cause);
			setMessageArgs(messageArgs);
		}

		public static enum Code {

			/**
			 * Instanciating the class failed.
			 */
			INSTANCIATION_FAILED("Instanciating the class failed.", Category.CODE_ERROR, 1),
			/**
			 * Implementing class could not be found
			 */
			CLASS_NOT_FOUND("Implementing class could not be found", Category.CODE_ERROR, 2),
			/**
			 * Missing property %1$s in system.properties.
			 */
			MISSING_SETTING("Missing property %1$s in system.properties.", Category.SETUP_ERROR, 3),
			/**
			 * Unknown IMAP server: %1$s
			 */
			UNKNOWN_IMAP_SERVER("Unknown IMAP server: %1$s", Category.CODE_ERROR, 4);

			/**
			 * Message of the exception.
			 */
			private final String message;

			/**
			 * Category of the exception.
			 */
			private final Category category;

			/**
			 * Detail number of the exception.
			 */
			private final int number;

			/**
			 * Default constructor.
			 * 
			 * @param message
			 *            message.
			 * @param category
			 *            category.
			 * @param detailNumber
			 *            detail number.
			 */
			private Code(final String message, final Category category, final int detailNumber) {
				this.message = message;
				this.category = category;
				this.number = detailNumber;
			}

			/**
			 * @return the category.
			 */
			public Category getCategory() {
				return category;
			}

			/**
			 * @return the message.
			 */
			public String getMessage() {
				return message;
			}

			/**
			 * @return the number.
			 */
			public int getNumber() {
				return number;
			}
		}
	}

	/**
	 * Proxy attribute for the class implementing this interface.
	 */
	private static Class<? extends User2IMAP> implementingClass;

	private static final Lock INIT_LOCK = new ReentrantLock();

	private static final Lock INSTANCE_LOCK = new ReentrantLock();

	/**
	 * Singleton
	 */
	private static User2IMAP singleton;

	private static boolean instancialized;

	private static boolean initialized;

	protected User2IMAP() {
		super();
	}

	/**
	 * Creates a new instance implementing the user2imap interface.
	 * 
	 * @return an instance implementing the user2imap interface.
	 * @throws User2IMAPException
	 *             if the instance can't be created.
	 */
	public static final User2IMAP getInstance(final User sessionUser) throws User2IMAPException {
		if (!instancialized) {
			INSTANCE_LOCK.lock();
			try {
				if (null == singleton) {
					init();
					try {
						if (implementingClass == null) {
							/*
							 * Auto-Detection turned on
							 */
							try {
								final Object[] args = getIMAPServer(sessionUser);
								return IMAPServerImpl.getUser2IMAPImpl((String) args[0], ((Integer) args[1])
										.intValue());
							} catch (final IOException e) {
								throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e,
										EMPTY_ARGS);
							} catch (final IMAPException e) {
								throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e,
										EMPTY_ARGS);
							}
						}
						singleton = implementingClass.newInstance();
						instancialized = true;
					} catch (final InstantiationException e) {
						throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e, EMPTY_ARGS);
					} catch (final IllegalAccessException e) {
						throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e, EMPTY_ARGS);
					} catch (final SecurityException e) {
						throw new User2IMAPException(User2IMAPException.Code.INSTANCIATION_FAILED, e, EMPTY_ARGS);
					}
				}
			} finally {
				INSTANCE_LOCK.unlock();
			}
		}
		return singleton;
	}

	private static final Object[] getIMAPServer(final User sessionUser) throws IMAPException {
		final String imapServer;
		if (IMAPLoginType.GLOBAL.equals(IMAPProperties.getImapLoginType())) {
			imapServer = IMAPPropertiesFactory.getProperties().getProperty(IMAPPropertiesFactory.PROP_IMAPSERVER);
		} else if (IMAPLoginType.USER.equals(IMAPProperties.getImapLoginTypeInternal())) {
			imapServer = sessionUser.getImapServer();
		} else if (IMAPLoginType.ANONYMOUS.equals(IMAPProperties.getImapLoginTypeInternal())) {
			imapServer = sessionUser.getImapServer();
		} else {
			return EMPTY_ARGS;
		}
		final int pos = imapServer.indexOf(':');
		if (pos > -1) {
			return new Object[] { imapServer.substring(0, pos), Integer.valueOf(imapServer.substring(pos + 1)) };
		}
		return new Object[] { imapServer, Integer.valueOf(143) };
	}

	/**
	 * Initializes the user2imap implementation.
	 * 
	 * @throws User2IMAPException
	 *             if initialization fails.
	 */
	public static final void init() throws User2IMAPException {
		if (!initialized) {
			INIT_LOCK.lock();
			try {
				if (null == implementingClass) {
					String classNameProp = SystemConfig.getProperty(SystemConfig.Property.User2IMAPImpl);
					if (null == classNameProp) {
						throw new User2IMAPException(User2IMAPException.Code.MISSING_SETTING,
								SystemConfig.Property.User2IMAPImpl.getPropertyName());
					}
					classNameProp = classNameProp.trim();
					if ("auto".equalsIgnoreCase(classNameProp)) {
						/*
						 * Try to detect dependent on IMAP server greeting
						 */
						implementingClass = null;
						if (LOG.isInfoEnabled()) {
							LOG.info("Auto-Detection for IMAP server implementation");
						}
						initialized = true;
						return;
					}
					final String className = getIMAPServerImpl(classNameProp);
					implementingClass = className == null ? Class.forName(classNameProp).asSubclass(User2IMAP.class)
							: Class.forName(className).asSubclass(User2IMAP.class);
					if (LOG.isInfoEnabled()) {
						LOG.info("Used IMAP server implementation: " + implementingClass.getName());
					}
					initialized = true;
				}
			} catch (final ClassNotFoundException e) {
				throw new User2IMAPException(User2IMAPException.Code.CLASS_NOT_FOUND, e, EMPTY_ARGS);
			} finally {
				INIT_LOCK.unlock();
			}
		}
	}

	/**
	 * <p>
	 * This method is invoked when initializing user's IMAP properties on system
	 * startup
	 * <p>
	 * Determines IMAP login for session-associated user. If
	 * <code>lookUpIMAPLogin</code> is <code>true</code>, this routine
	 * tries to fetch the IMAP login from <code>User.getImapLogin()</code> and
	 * falls back to session-supplied user login info. Otherwise
	 * session-supplied user login info is directly taken as return value.
	 * 
	 * @param session -
	 *            the user's session
	 * @param lookUpIMAPLogin -
	 *            determines whether to look up <code>User.getImapLogin()</code>
	 *            or not
	 * @return current user's IMAP login
	 */
	public final String getLocalIMAPLogin(final SessionObject session, final boolean lookUpIMAPLogin) {
		String imapLogin = lookUpIMAPLogin ? session.getUserObject().getImapLogin() : null;
		if (imapLogin == null || imapLogin.length() == 0) {
			imapLogin = session.getUserlogin() != null && session.getUserlogin().length() > 0 ? session.getUserlogin()
					: session.getUsername();
		}
		return imapLogin;
	}

	/**
	 * Determines the entity name of the user whose ID matches given
	 * <code>userId</code> that is used in IMAP server's ACL list.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @param u2iListener -
	 *            the info container
	 * @return the IMAP login of the user whose ID matches given
	 *         <code>userId</code>
	 * @throws LdapException -
	 *             if user could not be found
	 */
	public abstract String getACLName(int userId, Context ctx, User2IMAPInfo user2IMAPInfo) throws AbstractOXException;

	/**
	 * Determines the entity name of the user whose ID matches given
	 * <code>userId</code> that is used in IMAP server's ACL list.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param userStorage -
	 *            associated user storage implementation
	 * @param u2iListener -
	 *            the info container
	 * @return the IMAP login of the user whose ID matches given
	 *         <code>userId</code>
	 * @throws LdapException -
	 *             if user could not be found
	 */
	public abstract String getACLName(final int userId, final UserStorage userStorage, User2IMAPInfo user2IMAPInfo)
			throws AbstractOXException;

	/**
	 * Determines the user ID whose either ACL entity name or user name matches
	 * given <code>pattern</code>. <b>NOTE:</b> this routine returns
	 * <code>-1</code> if ACLs are not supported by underlying IMAP server or
	 * if ACLs are disabled per config file (imap.properties).
	 * 
	 * @param pattern -
	 *            the pattern for either IMAP login or user name
	 * @param userStorage -
	 *            the associated user storage implementation
	 * @param u2iListener -
	 *            the info container
	 * @return the user ID whose IMAP login matches given <code>pattern</code>
	 * @throws IMAPException -
	 *             if IMAP properties could not be initialized
	 * @throws LdapException -
	 *             if user search fails
	 */
	public abstract int getUserID(final String pattern, final UserStorage userStorage, User2IMAPInfo user2IMAPInfo)
			throws AbstractOXException;

}
