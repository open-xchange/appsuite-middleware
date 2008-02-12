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

package com.openexchange.mail.mime;

import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;

import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;

import javax.mail.AuthenticationFailedException;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.NoSuchProviderException;
import javax.mail.ReadOnlyFolderException;
import javax.mail.SendFailedException;
import javax.mail.StoreClosedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.ParseException;
import javax.mail.search.SearchException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * {@link MIMEMailException}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MIMEMailException extends MailException {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MIMEMailException.class);

	private static final long serialVersionUID = -3401580182929349354L;

	public static enum Code {

		/**
		 * There was an issue in authenticating your E-Mail password. This may
		 * be because of a recent password change. To continue please logout now
		 * and then log back in with your most current password. (server=%s |
		 * user=%s)
		 */
		LOGIN_FAILED(
				"There was an issue in authenticating your E-Mail password. This may be because of a recent password change. "
						+ "To continue please logout now and then log back in with your most current password. (server=%s | user=%s)",
				Category.PERMISSION, 1000),
		/**
		 * Wrong or missing login data to access server %s: User: %s | Context:
		 * %s
		 */
		INVALID_CREDENTIALS("Wrong or missing login data to access server %s.", Category.PERMISSION, 1001),
		/**
		 * Mail folder %s could not be found
		 */
		FOLDER_NOT_FOUND("Mail folder could not be found: %s", Category.CODE_ERROR, 1002),
		/**
		 * Folder is closed: %s
		 * <p>
		 * This exception is thrown when a method is invoked on a Messaging
		 * object and the Folder that owns that object has died due to some
		 * reason. Following the exception, the Folder is reset to the "closed"
		 * state.
		 * </p>
		 */
		FOLDER_CLOSED("Folder is closed: %s", Category.CODE_ERROR, 1003),
		/**
		 * Illegal write attempt: %s
		 * <p>
		 * The exception thrown when a write is attempted on a read-only
		 * attribute of any Messaging object.
		 * </p>
		 */
		ILLEGAL_WRITE("Illegal write attempt: %s", Category.CODE_ERROR, 1004),
		/**
		 * Invalid method on a expunged message: %s
		 * <p>
		 * The exception thrown when an invalid method is invoked on an expunged
		 * Message. The only valid methods on an expunged Message are
		 * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
		 * </p>
		 */
		MESSAGE_REMOVED("Invalid method on an expunged message: %s", Category.CODE_ERROR, 1005),
		/**
		 * Method not supported: %s
		 * <p>
		 * The exception thrown when a method is not supported by the
		 * implementation
		 * </p>
		 */
		METHOD_NOT_SUPPORTED("Method not supported: %s", Category.CODE_ERROR, 1006),
		/**
		 * Session attempts to instantiate a provider that doesn't exist: %s
		 */
		NO_SUCH_PROVIDER("Session attempts to instantiate a provider that doesn't exist: %s", Category.CODE_ERROR, 1007),
		/**
		 * Invalid email address %s
		 */
		INVALID_EMAIL_ADDRESS("Invalid email address %s", Category.USER_INPUT, 1008),
		/**
		 * Wrong message header: %s
		 * <p>
		 * The exception thrown due to an error in parsing RFC822 or MIME
		 * headers
		 * </p>
		 */
		PARSE_ERROR("Wrong message header: %s", Category.USER_INPUT, 1009),
		/**
		 * An attempt was made to open a read-only folder with read-write: %s
		 */
		READ_ONLY_FOLDER("An attempt was made to open a read-only folder with read-write: %s", Category.PERMISSION,
				1010),
		/**
		 * Invalid search expression: %s
		 */
		SEARCH_ERROR("Invalid search expression: %s", Category.CODE_ERROR, 1011),
		/**
		 * Message could not be sent because it is too large
		 */
		MESSAGE_TOO_LARGE("Message could not be sent because it is too large", Category.INTERNAL_ERROR, 1012),
		/**
		 * Message could not be sent to following recipients: %s
		 * <p>
		 * The exception includes those addresses to which the message could not
		 * be sent as well as the valid addresses to which the message was sent
		 * and valid addresses to which the message was not sent.
		 * </p>
		 */
		SEND_FAILED("Message could not be sent to the following recipients: %s", Category.USER_INPUT, 1013),
		/**
		 * Store already closed: %s
		 */
		STORE_CLOSED("Store already closed: %s", Category.CODE_ERROR, 1014),
		/**
		 * Could not bind mail connection to local port %s
		 * <p>
		 * Signals that an error occurred while attempting to bind a socket to a
		 * local address and port. Typically, the port is in use, or the
		 * requested local address could not be assigned.
		 * </p>
		 */
		BIND_ERROR("Could not bind connection to local port %s", Category.SETUP_ERROR, 1015),
		/**
		 * Connect error: Connection was refused or timed out while attempting
		 * to connect to remote mail server %s for user %s
		 * <p>
		 * An error occurred while attempting to connect to remote mail server.
		 * Typically, the connection was refused remotely (e.g., no process is
		 * listening on the remote address/port).
		 * </p>
		 */
		CONNECT_ERROR(
				"Connection was refused or timed out while attempting to connect to remote server %s for user %s",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 1016),
		/**
		 * Connection was reset
		 */
		CONNECTION_RESET("Connection was reset. Please try again.", Category.TRY_AGAIN, 1017),
		/**
		 * No route to host: mail server %s cannot be reached
		 * <p>
		 * Signals that an error occurred while attempting to connect to remote
		 * mail server. Typically, the remote mail server cannot be reached
		 * because of an intervening firewall, or if an intermediate router is
		 * down.
		 * </p>
		 */
		NO_ROUTE_TO_HOST("No route to host: server (%s) cannot be reached", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1018),
		/**
		 * Port %s was unreachabe on remote mail server
		 */
		PORT_UNREACHABLE("Port %s was unreachable on remote server", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1019),
		/**
		 * Connection is broken due to a socket exception on remote mail server:
		 * %s
		 */
		BROKEN_CONNECTION("Connection is broken due to a socket exception on remote server: %s",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 1020),
		/**
		 * A socket error occurred: %s
		 */
		SOCKET_ERROR("A socket error occurred: %s", Category.CODE_ERROR, 1021),
		/**
		 * The IP address of host "%s" could not be determined
		 */
		UNKNOWN_HOST("The IP address of host \"%s\" could not be determined", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1022),
		/**
		 * Messaging error: %s
		 */
		MESSAGING_ERROR("Messaging error: %s", Category.CODE_ERROR, 1023),
		/**
		 * The quota on mail server is exceeded
		 */
		QUOTA_EXCEEDED("Mail server's quota is exceeded", Category.EXTERNAL_RESOURCE_FULL, 1024);

		private final String message;

		private final int detailNumber;

		private final Category category;

		private Code(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.detailNumber = detailNumber;
			this.category = category;
		}

		public Category getCategory() {
			return category;
		}

		public int getNumber() {
			return detailNumber;
		}

		public String getMessage() {
			return message;
		}
	}

	protected MIMEMailException(final AbstractOXException cause) {
		super(cause);
	}

	protected MIMEMailException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	protected MIMEMailException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.MAIL, code.category, code.detailNumber, code.message, cause);
		super.setMessageArgs(messageArgs);
	}

	private static final transient Object[] EMPTY_ARGS = new Object[0];

	protected MIMEMailException(Code code) {
		this(code, EMPTY_ARGS);
	}

	protected MIMEMailException(final Component component, final Category category, final int detailNumber,
			final String message, final Throwable cause) {
		super(component, category, detailNumber, message, cause);
	}

	/**
	 * Handles given instance of {@link MessagingException} and creates an
	 * appropiate instance of {@link MIMEMailException}
	 * <p>
	 * This is just a convenience method that simply invokes
	 * {@link #handleMessagingException(MessagingException, MailConnection)}
	 * with the latter parameter set to <code>null</code>.
	 * 
	 * @param e
	 *            The messaging exception
	 * @return An appropiate instance of {@link MIMEMailException}
	 */
	public static MIMEMailException handleMessagingException(final MessagingException e) {
		return handleMessagingException(e, null);
	}

	private static final String STR_EMPTY = "";

	private static final String ERR_TMP = "temporary error, please try again later";

	private static final String ERR_AUTH_FAILED = "bad authentication failed";

	private static final String ERR_MSG_TOO_LARGE = "message too large";

	private static final String ERR_QUOTA = "quota";

	/**
	 * ConnectionResetException
	 */
	private static final String EXC_CONNECTION_RESET_EXCEPTION = "ConnectionResetException";

	/**
	 * Handles given instance of {@link MessagingException} and creates an
	 * appropriate instance of {@link MIMEMailException}
	 * 
	 * @param e
	 *            The messaging exception
	 * @param mailConnection
	 *            The corresponding mail connection used to add informations
	 *            like mail server etc.
	 * @return An appropriate instance of {@link MIMEMailException}
	 */
	public static MIMEMailException handleMessagingException(final MessagingException e,
			final MailConnection<?, ?, ?> mailConnection) {
		try {
			if (e instanceof AuthenticationFailedException
					|| (e.getMessage() != null && e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_AUTH_FAILED) != -1)) {
				final boolean temporary = e.getMessage() != null
						&& ERR_TMP.equals(e.getMessage().toLowerCase(Locale.ENGLISH));
				if (temporary) {
					return new MIMEMailException(MIMEMailException.Code.LOGIN_FAILED, e, mailConnection == null
							|| mailConnection.getMailConfig() == null ? STR_EMPTY : mailConnection.getMailConfig()
							.getServer(), mailConnection == null || mailConnection.getMailConfig() == null ? STR_EMPTY
							: mailConnection.getMailConfig().getLogin());
				}
				return new MIMEMailException(MIMEMailException.Code.INVALID_CREDENTIALS, e, mailConnection == null
						|| mailConnection.getMailConfig() == null ? STR_EMPTY : mailConnection.getMailConfig()
						.getServer());
			} else if (e instanceof FolderClosedException) {
				return new MIMEMailException(Code.FOLDER_CLOSED, e, e.getLocalizedMessage());
			} else if (e instanceof FolderNotFoundException) {
				return new MIMEMailException(Code.FOLDER_NOT_FOUND, e, e.getLocalizedMessage());
			} else if (e instanceof IllegalWriteException) {
				return new MIMEMailException(Code.ILLEGAL_WRITE, e, e.getLocalizedMessage());
			} else if (e instanceof MessageRemovedException) {
				return new MIMEMailException(Code.MESSAGE_REMOVED, e, e.getLocalizedMessage());
			} else if (e instanceof MethodNotSupportedException) {
				return new MIMEMailException(Code.METHOD_NOT_SUPPORTED, e, e.getMessage());
			} else if (e instanceof NoSuchProviderException) {
				return new MIMEMailException(Code.NO_SUCH_PROVIDER, e, e.getMessage());
			} else if (e instanceof ParseException) {
				if (e instanceof AddressException) {
					final String ref = ((AddressException) e).getRef() == null ? STR_EMPTY : ((AddressException) e)
							.getRef();
					return new MIMEMailException(Code.INVALID_EMAIL_ADDRESS, e, ref);
				}
				return new MIMEMailException(Code.PARSE_ERROR, e, e.getMessage());
			} else if (e instanceof ReadOnlyFolderException) {
				return new MIMEMailException(Code.READ_ONLY_FOLDER, e, e.getMessage());
			} else if (e instanceof SearchException) {
				return new MIMEMailException(Code.SEARCH_ERROR, e, e.getMessage());
			} else if (e instanceof SMTPSendFailedException) {
				final SMTPSendFailedException exc = (SMTPSendFailedException) e;
				if (exc.getReturnCode() == 552
						|| exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
					return new MIMEMailException(Code.MESSAGE_TOO_LARGE, exc, new Object[0]);
				}
				return new MIMEMailException(Code.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
			} else if (e instanceof SendFailedException) {
				final SendFailedException exc = (SendFailedException) e;
				if (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
					return new MIMEMailException(Code.MESSAGE_TOO_LARGE, exc, new Object[0]);
				}
				return new MIMEMailException(Code.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
			} else if (e instanceof StoreClosedException) {
				return new MIMEMailException(Code.STORE_CLOSED, e, e.getMessage());
			} else if (e.getNextException() instanceof BindException) {
				return new MIMEMailException(Code.BIND_ERROR, e, mailConnection == null
						|| mailConnection.getMailConfig() == null ? STR_EMPTY : Integer.valueOf(mailConnection
						.getMailConfig().getPort()));
			} else if (e.getNextException() instanceof ConnectionException) {
				mailInterfaceMonitor.changeNumBrokenConnections(true);
				return new MIMEMailException(Code.CONNECT_ERROR, e, mailConnection == null
						|| mailConnection.getMailConfig() == null ? STR_EMPTY : mailConnection.getMailConfig()
						.getServer(), mailConnection == null ? STR_EMPTY : mailConnection.getMailConfig().getLogin());
			} else if (e.getNextException() instanceof ConnectException) {
				/*
				 * Most modern IP stack implementations sense connection
				 * idleness, and abort the connection attempt, resulting in a
				 * java.net.ConnectionException
				 */
				mailInterfaceMonitor.changeNumTimeoutConnections(true);
				final MIMEMailException me = new MIMEMailException(Code.CONNECT_ERROR, e, mailConnection == null
						|| mailConnection.getMailConfig() == null ? STR_EMPTY : mailConnection.getMailConfig()
						.getServer(), mailConnection == null ? STR_EMPTY : mailConnection.getMailConfig().getLogin());
				return me;
			} else if (e.getNextException().getClass().getName().endsWith(EXC_CONNECTION_RESET_EXCEPTION)) {
				mailInterfaceMonitor.changeNumBrokenConnections(true);
				return new MIMEMailException(Code.CONNECTION_RESET, e, new Object[0]);
			} else if (e.getNextException() instanceof NoRouteToHostException) {
				return new MIMEMailException(Code.NO_ROUTE_TO_HOST, e, mailConnection == null
						|| mailConnection.getMailConfig() == null ? STR_EMPTY : mailConnection.getMailConfig()
						.getServer());
			} else if (e.getNextException() instanceof PortUnreachableException) {
				return new MIMEMailException(Code.PORT_UNREACHABLE, e, mailConnection == null
						|| mailConnection.getMailConfig() == null ? STR_EMPTY : Integer.valueOf(mailConnection
						.getMailConfig().getPort()));
			} else if (e.getNextException() instanceof SocketException) {
				/*
				 * Treat dependent on message
				 */
				final SocketException se = (SocketException) e.getNextException();
				if ("Socket closed".equals(se.getMessage()) || "Connection reset".equals(se.getMessage())) {
					mailInterfaceMonitor.changeNumBrokenConnections(true);
					return new MIMEMailException(Code.BROKEN_CONNECTION, e, mailConnection == null
							|| mailConnection.getMailConfig() == null ? STR_EMPTY : mailConnection.getMailConfig()
							.getServer());
				}
				return new MIMEMailException(Code.SOCKET_ERROR, e, e.getMessage());
			} else if (e.getNextException() instanceof UnknownHostException) {
				return new MIMEMailException(Code.UNKNOWN_HOST, e, e.getMessage());
			} else if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_QUOTA) != -1) {
				return new MIMEMailException(Code.QUOTA_EXCEEDED, e, EMPTY_ARGS);
			}
			/*
			 * Default case
			 */
			return new MIMEMailException(Code.MESSAGING_ERROR, e, e.getMessage());
		} catch (final Throwable t) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(t.getMessage(), t);
			}
			/*
			 * This routine should not fail since it's purpose is wrap a
			 * corresponding mail error around specified messaging error
			 */
			return new MIMEMailException(Code.MESSAGING_ERROR, e, e.getMessage());
		}
	}
}
