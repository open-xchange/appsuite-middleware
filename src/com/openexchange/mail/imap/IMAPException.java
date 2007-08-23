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

package com.openexchange.mail.imap;

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

import sun.net.ConnectionResetException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * IMAPException
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPException extends MailException {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -8226676160145457046L;

	public static enum Code {

		/**
		 * Missing parameter in mail connection: %s
		 */
		MISSING_CONNECT_PARAM("Missing parameter in mail connection: %s", Category.CODE_ERROR, 1),
		/**
		 * There was an issue in authenticating your E-Mail password. This may
		 * be because of a recent password change. To continue please logout now
		 * and then log back in with your most current password. (server=%s |
		 * user=%s)
		 */
		LOGIN_FAILED(
				"There was an issue in authenticating your E-Mail password. This may be because of a recent password change. "
						+ "To continue please logout now and then log back in with your most current password. (server=%s | user=%s)",
				Category.PERMISSION, 2),
		/**
		 * Wrong or missing login data to access server %s: User: %s | Context:
		 * %s
		 */
		INVALID_CREDENTIALS("Wrong or missing login data to access server %s.", Category.PERMISSION, 3),
		/**
		 * No connection available to access mailbox
		 */
		NOT_CONNECTED("No connection available to access mailbox", Category.CODE_ERROR, 4),
		/**
		 * Mail folder %s could not be found
		 */
		FOLDER_NOT_FOUND("Mail folder could not be found: %s", Category.CODE_ERROR, 5),
		/**
		 * Folder is closed: %s
		 * <p>
		 * This exception is thrown when a method is invoked on a Messaging
		 * object and the Folder that owns that object has died due to some
		 * reason. Following the exception, the Folder is reset to the "closed"
		 * state.
		 * </p>
		 */
		FOLDER_CLOSED("Folder is closed: %s", Category.CODE_ERROR, 6),
		/**
		 * Illegal write attempt: %s
		 * <p>
		 * The exception thrown when a write is attempted on a read-only
		 * attribute of any Messaging object.
		 * </p>
		 */
		ILLEGAL_WRITE("Illegal write attempt: %s", Category.CODE_ERROR, 7),
		/**
		 * Invalid method on a expunged message: %s
		 * <p>
		 * The exception thrown when an invalid method is invoked on an expunged
		 * Message. The only valid methods on an expunged Message are
		 * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
		 * </p>
		 */
		MESSAGE_REMOVED("Invalid method on an expunged message: %s", Category.CODE_ERROR, 8),
		/**
		 * Method not supported: %s
		 * <p>
		 * The exception thrown when a method is not supported by the
		 * implementation
		 * </p>
		 */
		METHOD_NOT_SUPPORTED("Method not supported: %s", Category.CODE_ERROR, 9),
		/**
		 * Session attempts to instantiate a provider that doesn't exist: %s
		 */
		NO_SUCH_PROVIDER("Session attempts to instantiate a provider that doesn't exist: %s", Category.CODE_ERROR, 10),
		/**
		 * Invalid email address %s
		 */
		INVALID_EMAIL_ADDRESS("Invalid email address %s", Category.USER_INPUT, 11),
		/**
		 * Wrong message header: %s
		 * <p>
		 * The exception thrown due to an error in parsing RFC822 or MIME
		 * headers
		 * </p>
		 */
		PARSE_ERROR("Wrong message header: %s", Category.USER_INPUT, 12),
		/**
		 * An attempt was made to open a read-only folder with read-write: %s
		 */
		READ_ONLY_FOLDER("An attempt was made to open a read-only folder with read-write: %s", Category.PERMISSION, 13),
		/**
		 * Invalid search expression: %s
		 */
		SEARCH_ERROR("Invalid search expression: %s", Category.CODE_ERROR, 14),
		/**
		 * Message could not be sent because it is too large
		 */
		MESSAGE_TOO_LARGE("Message could not be sent because it is too large", Category.INTERNAL_ERROR, 15),
		/**
		 * Message could not be sent to following recipients: %s
		 * <p>
		 * The exception includes those addresses to which the message could not
		 * be sent as well as the valid addresses to which the message was sent
		 * and valid addresses to which the message was not sent.
		 * </p>
		 */
		SEND_FAILED("Message could not be sent to the following recipients: %s", Category.USER_INPUT, 16),
		/**
		 * Store already closed: %s
		 */
		STORE_CLOSED("Store already closed: %s", Category.CODE_ERROR, 17),
		/**
		 * Could not bind IMAP connection to local port %s
		 * <p>
		 * Signals that an error occurred while attempting to bind a socket to a
		 * local address and port. Typically, the port is in use, or the
		 * requested local address could not be assigned.
		 * </p>
		 */
		BIND_ERROR("Could not bind connection to local port %s", Category.SETUP_ERROR, 18),
		/**
		 * Connect error: Connection was refused while attempting to connect to
		 * remote IMAP server %s for user %s
		 * <p>
		 * An error occurred while attempting to connect to remote IMAP server.
		 * Typically, the connection was refused remotely (e.g., no process is
		 * listening on the remote address/port).
		 * </p>
		 */
		CONNECT_ERROR(
				"Connection was refused or timed out while attempting to connect to remote server %s for user %s",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 19),
		/**
		 * Connection was reset
		 */
		CONNECTION_RESET("Connection was reset. Please try again.", Category.TRY_AGAIN, 20),
		/**
		 * No route to host: IMAP server %s cannot be reached
		 * <p>
		 * Signals that an error occurred while attempting to connect to remote
		 * IMAP server. Typically, the remote IMAP server cannot be reached
		 * because of an intervening firewall, or if an intermediate router is
		 * down.
		 * </p>
		 */
		NO_ROUTE_TO_HOST("No route to host: server (%s) cannot be reached", Category.SUBSYSTEM_OR_SERVICE_DOWN, 21),
		/**
		 * Port %s was unreachabe on remote IMAP server
		 */
		PORT_UNREACHABLE("Port %s was unreachable on remote server", Category.SUBSYSTEM_OR_SERVICE_DOWN, 22),
		/**
		 * Connection is broken due to a socket exception on remote IMAP server:
		 * %s
		 */
		BROKEN_CONNECTION("Connection is broken due to a socket exception on remote server: %s",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 23),
		/**
		 * A socket error occurred: %s
		 */
		SOCKET_ERROR("A socket error occurred: %s", Category.CODE_ERROR, 24),
		/**
		 * The IP address of host "%s" could not be determined
		 */
		UNKNOWN_HOST("The IP address of host \"%s\" could not be determined", Category.SUBSYSTEM_OR_SERVICE_DOWN, 25),
		/**
		 * Messaging error: %s
		 */
		MESSAGING_ERROR("Messaging error: %s", Category.CODE_ERROR, 26),
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAMETER("Missing parameter %s", Category.CODE_ERROR, 27),
		/**
		 * A JSON error occured: %s
		 */
		JSON_ERROR("A JSON error occured: %s", Category.CODE_ERROR, 28),
		/**
		 * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
		 */
		INVALID_PERMISSION("Invalid permission values: fp=%d orp=%d owp=%d odp=%d", Category.CODE_ERROR, 29),
		/**
		 * User %s has no mail module access due to user configuration
		 */
		NO_MAIL_MODULE_ACCESS("User %s has no mail module access due to user configuration",
				Category.USER_CONFIGURATION, 30),
		/**
		 * No access to mail folder %s
		 */
		NO_ACCESS("No access to mail folder %s", Category.PERMISSION, 31),
		/**
		 * No lookup access to mail folder %s
		 */
		NO_LOOKUP_ACCESS("No lookup access to mail folder %s", Category.PERMISSION, 32),
		/**
		 * No read access on IMAP-Folder %s
		 */
		NO_READ_ACCESS("No read access to mail folder %s", Category.PERMISSION, 33),
		/**
		 * No delete access on IMAP-Folder %s
		 */
		NO_DELETE_ACCESS("No delete access to mail folder %s", Category.PERMISSION, 34),
		/**
		 * No insert access on IMAP-Folder %s
		 */
		NO_INSERT_ACCESS("No insert access to mail folder %s", Category.PERMISSION, 35),
		/**
		 * No create access on IMAP-Folder %s
		 */
		NO_CREATE_ACCESS("No create access to mail folder %s", Category.PERMISSION, 36),
		/**
		 * No administer access on IMAP-Folder %s
		 */
		NO_ADMINISTER_ACCESS("No administer access to mail folder %s", Category.PERMISSION, 37),
		/**
		 * No write access to IMAP folder %s
		 */
		NO_WRITE_ACCESS("No write access to IMAP folder %s", Category.PERMISSION, 38),
		/**
		 * No keep-seen access on IMAP-Folder %s
		 */
		NO_KEEP_SEEN_ACCESS("No keep-seen access to mail folder %s", Category.PERMISSION, 39),
		/**
		 * Folder %s does not allow subfolders.
		 */
		FOLDER_DOES_NOT_HOLD_FOLDERS("Folder %s does not allow subfolders.", Category.PERMISSION, 40),
		/**
		 * Mail folder cannot be created. Name must not contain character '%s'
		 */
		INVALID_FOLDER_NAME("Mail folder cannot be created. Name must not contain character '%s'", Category.USER_INPUT,
				41),
		/**
		 * A folder named %s already exists
		 */
		DUPLICATE_FOLDER("A folder named %s already exists", Category.PERMISSION, 42),
		/**
		 * Mail folder "%s" could not be created (maybe due to insufficient
		 * permission on parent folder %s)
		 */
		FOLDER_CREATION_FAILED(
				"Mail folder \"%s\" could not be created (maybe due to insufficient permission on parent folder %s)",
				Category.CODE_ERROR, 43),
		/**
		 * The composed rights could not be applied to new folder %s due to
		 * missing administer right in its initial rights specified by IMAP
		 * server. However, the folder has been created.
		 */
		NO_ADMINISTER_ACCESS_ON_INITIAL(
				"The composed rights could not be applied to new folder %s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created.",
				Category.PERMISSION, 44),
		/**
		 * No admin permission specified for folder %s
		 */
		NO_ADMIN_ACL("No administer permission specified for folder %s", Category.USER_INPUT, 45),
		/**
		 * Default folder %s must not be updated
		 */
		NO_DEFAULT_FOLDER_UPDATE("Default folder %s cannot be updated", Category.PERMISSION, 46),
		/**
		 * Deletion of folder %s failed
		 */
		DELETE_FAILED("Deletion of folder %s failed", Category.CODE_ERROR, 47),
		/**
		 * IMAP default folder %s could not be created
		 */
		NO_DEFAULT_FOLDER_CREATION("IMAP default folder %s could not be created", Category.CODE_ERROR, 48),
		/**
		 * Missing default %s folder in user mail settings
		 */
		MISSING_DEFAULT_FOLDER_NAME("Missing default %s folder in user mail settings", Category.CODE_ERROR, 49),
		/**
		 * Update of folder %s failed
		 */
		UPDATE_FAILED("Update of folder %s failed", Category.CODE_ERROR, 50),
		/**
		 * Folder %s must not be deleted
		 */
		NO_FOLDER_DELETE("Folder %s cannot be deleted", Category.PERMISSION, 51),
		/**
		 * Default folder %s must not be deleted
		 */
		NO_DEFAULT_FOLDER_DELETE("Default folder %s cannot be deleted", Category.PERMISSION, 52),
		/**
		 * An I/O error occured: %s
		 */
		IO_ERROR("An I/O error occured: %s", Category.CODE_ERROR, 53),
		/**
		 * Flag %s could not be changed due to following reason: %s
		 */
		FLAG_FAILED("Flag %s could not be changed due to following reason: %s", Category.INTERNAL_ERROR, 54),
		/**
		 * Message(s) %s could not be found in folder %s
		 */
		MESSAGE_NOT_FOUND("Message(s) %s could not be found in folder %s", Category.CODE_ERROR, 55),
		/**
		 * Folder %s does not hold messages and is therefore not selectable
		 */
		FOLDER_DOES_NOT_HOLD_MESSAGES("Folder %s does not hold messages and is therefore not selectable",
				Category.PERMISSION, 56),
		/**
		 * Number of search fields (%d) do not match number of search patterns
		 * (%d)
		 */
		INVALID_SEARCH_PARAMS("Number of search fields (%d) do not match number of search patterns (%d)",
				Category.CODE_ERROR, 57),
		/**
		 * IMAP search failed due to following reason: %s Switching to
		 * application-based search
		 */
		IMAP_SEARCH_FAILED("IMAP search failed due to following reason: %s Switching to application-based search",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 58),
		/**
		 * IMAP sort failed due to following reason: %s Switching to
		 * application-based sorting
		 */
		IMAP_SORT_FAILED("IMAP sort failed due to following reason: %s Switching to application-based sorting",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 59),
		/**
		 * Unknow serach field: %s
		 */
		UNKNOWN_SEARCH_FIELD("Unknow serach field: %s", Category.CODE_ERROR, 60),
		/**
		 * Message field %s cannot be handled
		 */
		INVALID_FIELD("Message field %s cannot be handled", Category.CODE_ERROR, 61),
		/**
		 * Mail folder %s must not be moved to subsequent folder %s
		 */
		NO_MOVE_TO_SUBFLD("Mail folder %s must not be moved to subsequent folder %s", Category.PERMISSION, 62);

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

	public static String getFormattedMessage(final Code code, final Object... msgArgs) {
		return String.format(code.getMessage(), msgArgs);
	}

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPException.class);

	private static final String STR_EMPTY = "";

	private static final transient Object[] EMPTY_ARGS = new Object[0];

	public IMAPException(final AbstractOXException cause) {
		super(cause);
	}

	public IMAPException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	public IMAPException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.IMAP, code.category, code.detailNumber, code.message, cause);
		super.setMessageArgs(messageArgs);
	}

	public IMAPException(Code code) {
		this(code, EMPTY_ARGS);
	}

	/*
	 * +++++++++++++++++++++ handle messaging exception +++++++++++++++++++++
	 */

	/**
	 * Handles given instance of {@link MessagingException} and creates an
	 * appropiate instance of {@link IMAPException}
	 * <p>
	 * This is just a convenience method that simply invokes
	 * {@link #handleMessagingException(MessagingException, MailConnection)}
	 * with the latter parameter set to <code>null</code>.
	 * 
	 * @param e
	 *            The messaging exception
	 * @return An appropiate instance of {@link IMAPException}
	 */
	public static IMAPException handleMessagingException(final MessagingException e) {
		return handleMessagingException(e, null);
	}

	private static final String ERR_TMP = "temporary error, please try again later";

	private static final String ERR_AUTH_FAILED = "bad authentication failed";

	private static final String ERR_MSG_TOO_LARGE = "message too large";

	/**
	 * Handles given instance of {@link MessagingException} and creates an
	 * appropiate instance of {@link IMAPException}
	 * 
	 * @param e
	 *            The messaging exception
	 * @param mailConnection
	 *            The corresponding mail connection used to add informations
	 *            like mail server etc.
	 * @return An appropiate instance of {@link IMAPException}
	 */
	public static IMAPException handleMessagingException(final MessagingException e, final MailConnection mailConnection) {
		if (e instanceof AuthenticationFailedException
				|| e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_AUTH_FAILED) != -1) {
			final boolean temporary = e.getMessage() != null
					&& ERR_TMP.equals(e.getMessage().toLowerCase(Locale.ENGLISH));
			if (temporary) {
				return new IMAPException(IMAPException.Code.LOGIN_FAILED, e, mailConnection == null ? STR_EMPTY
						: mailConnection.getMailServer(), mailConnection == null ? STR_EMPTY : mailConnection
						.getLogin());
			}
			return new IMAPException(IMAPException.Code.INVALID_CREDENTIALS, e, mailConnection == null ? STR_EMPTY
					: mailConnection.getMailServer());
		} else if (e instanceof FolderClosedException) {
			return new IMAPException(Code.FOLDER_CLOSED, e, e.getLocalizedMessage());
		} else if (e instanceof FolderNotFoundException) {
			return new IMAPException(Code.FOLDER_NOT_FOUND, e, e.getLocalizedMessage());
		} else if (e instanceof IllegalWriteException) {
			return new IMAPException(Code.ILLEGAL_WRITE, e, e.getLocalizedMessage());
		} else if (e instanceof MessageRemovedException) {
			return new IMAPException(Code.MESSAGE_REMOVED, e, e.getLocalizedMessage());
		} else if (e instanceof MethodNotSupportedException) {
			return new IMAPException(Code.METHOD_NOT_SUPPORTED, e, e.getMessage());
		} else if (e instanceof NoSuchProviderException) {
			return new IMAPException(Code.NO_SUCH_PROVIDER, e, e.getMessage());
		} else if (e instanceof ParseException) {
			if (e instanceof AddressException) {
				final String ref = ((AddressException) e).getRef() == null ? STR_EMPTY : ((AddressException) e)
						.getRef();
				return new IMAPException(Code.INVALID_EMAIL_ADDRESS, e, ref);
			}
			return new IMAPException(Code.PARSE_ERROR, e, e.getMessage());
		} else if (e instanceof ReadOnlyFolderException) {
			return new IMAPException(Code.READ_ONLY_FOLDER, e, e.getMessage());
		} else if (e instanceof SearchException) {
			return new IMAPException(Code.SEARCH_ERROR, e, e.getMessage());
		} else if (e instanceof SMTPSendFailedException) {
			final SMTPSendFailedException exc = (SMTPSendFailedException) e;
			if (exc.getReturnCode() == 552
					|| exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
				return new IMAPException(Code.MESSAGE_TOO_LARGE, exc, new Object[0]);
			}
			return new IMAPException(Code.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
		} else if (e instanceof SendFailedException) {
			final SendFailedException exc = (SendFailedException) e;
			if (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
				return new IMAPException(Code.MESSAGE_TOO_LARGE, exc, new Object[0]);
			}
			return new IMAPException(Code.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
		} else if (e instanceof StoreClosedException) {
			return new IMAPException(Code.STORE_CLOSED, e, e.getMessage());
		} else if (e.getNextException() instanceof BindException) {
			return new IMAPException(Code.BIND_ERROR, e, mailConnection == null ? STR_EMPTY : Integer
					.valueOf(mailConnection.getMailServerPort()));
		} else if (e.getNextException() instanceof ConnectionException) {
			// TODO: mailInterfaceMonitor.changeNumBrokenConnections(true);
			return new IMAPException(Code.CONNECT_ERROR, e, mailConnection == null ? STR_EMPTY : mailConnection
					.getMailServer(), mailConnection == null ? STR_EMPTY : mailConnection.getLogin());
		} else if (e.getNextException() instanceof ConnectException) {
			try {
				// TODO: mailInterfaceMonitor.changeNumTimeoutConnections(true);
				final IMAPException me = new IMAPException(Code.CONNECT_ERROR, e, mailConnection == null ? STR_EMPTY
						: mailConnection.getMailServer(), mailConnection == null ? STR_EMPTY : mailConnection
						.getLogin());
				if (IMAPProperties.getImapConnectionTimeout() > 0) {
					/*
					 * Most modern IP stack implementations sense connection
					 * idleness, and abort the connection attempt, resulting in
					 * a java.net.ConnectionException
					 */
					me.setCategory(Category.TRY_AGAIN);
				}
				return me;
			} catch (final IMAPPropertyException imapExc) {
				/*
				 * Log messaging exception
				 */
				LOG.error(e.getMessage(), e);
				return new IMAPException(imapExc);
			}
		} else if (e.getNextException() instanceof ConnectionResetException) {
			// TODO: mailInterfaceMonitor.changeNumBrokenConnections(true);
			return new IMAPException(Code.CONNECTION_RESET, e, new Object[0]);
		} else if (e.getNextException() instanceof NoRouteToHostException) {
			return new IMAPException(Code.NO_ROUTE_TO_HOST, e, mailConnection == null ? STR_EMPTY : mailConnection
					.getMailServer());
		} else if (e.getNextException() instanceof PortUnreachableException) {
			return new IMAPException(Code.PORT_UNREACHABLE, e, mailConnection == null ? STR_EMPTY : Integer
					.valueOf(mailConnection.getMailServerPort()));
		} else if (e.getNextException() instanceof SocketException) {
			/*
			 * Treat dependent on message
			 */
			final SocketException se = (SocketException) e.getNextException();
			if ("Socket closed".equals(se.getMessage()) || "Connection reset".equals(se.getMessage())) {
				// TODO: mailInterfaceMonitor.changeNumBrokenConnections(true);
				return new IMAPException(Code.BROKEN_CONNECTION, e, mailConnection == null ? STR_EMPTY : mailConnection
						.getMailServer());
			}
			return new IMAPException(Code.SOCKET_ERROR, e, e.getMessage());
		} else if (e.getNextException() instanceof UnknownHostException) {
			return new IMAPException(Code.UNKNOWN_HOST, e, e.getMessage());
		}
		/*
		 * Default case
		 */
		return new IMAPException(Code.MESSAGING_ERROR, e, e.getMessage());
	}

}
