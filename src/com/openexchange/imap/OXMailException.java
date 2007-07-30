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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * OXMailException
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class OXMailException extends OXException {

	private static final long serialVersionUID = -8215419480751315209L;

	public static enum MailCode {
		/**
		 * Connect error: Connection was refused while attempting to connect to
		 * remote IMAP server %s for user %s
		 * <p>
		 * An error occurred while attempting to connect to remote IMAP server.
		 * Typically, the connection was refused remotely (e.g., no process is
		 * listening on the remote address/port).
		 * </p>
		 */
		CONNECT_ERROR("Connection was refused or timed out while attempting to connect to remote server %s for user %s",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 1),
		/**
		 * No route to host: IMAP server %s cannot be reached
		 * <p>
		 * Signals that an error occurred while attempting to connect to remote
		 * IMAP server. Typically, the remote IMAP server cannot be reached
		 * because of an intervening firewall, or if an intermediate router is
		 * down.
		 * </p>
		 */
		NO_ROUTE_TO_HOST("No route to host: server (%s) cannot be reached", Category.SUBSYSTEM_OR_SERVICE_DOWN, 2),
		/**
		 * Connection was reset
		 */
		CONNECTION_RESET("Connection was reset. Please try again.", Category.TRY_AGAIN, 3),
		/**
		 * Port %s was unreachabe on remote IMAP server
		 */
		PORT_UNREACHABLE("Port %s was unreachable on remote server", Category.SUBSYSTEM_OR_SERVICE_DOWN, 4),
		/**
		 * Could not bind IMAP connection to local port %s
		 * <p>
		 * Signals that an error occurred while attempting to bind a socket to a
		 * local address and port. Typically, the port is in use, or the
		 * requested local address could not be assigned.
		 * </p>
		 */
		BIND_ERROR("Could not bind connection to local port %s", Category.SETUP_ERROR, 5),
		/**
		 * Connection is broken due to a socket exception on remote IMAP server:
		 * %s
		 */
		BROKEN_CONNECTION("Connection is broken due to a socket exception on remote server: %s",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 6),
		/**
		 * The IP address of host "%s" could not be determined
		 */
		UNKNOWN_HOST("The IP address of host \"%s\" could not be determined", Category.SUBSYSTEM_OR_SERVICE_DOWN, 7),
		/**
		 * Wrong or missing login data to access server %s: User: %s | Context: %s
		 * <p>
		 * Invalid credentials
		 * </p>
		 */
		INVALID_CREDENTIALS("Wrong or missing login data to access server %s: User: %s | Context: %s", Category.PERMISSION, 8),
		/**
		 * Folder is closed: %s
		 * <p>
		 * This exception is thrown when a method is invoked on a Messaging
		 * object and the Folder that owns that object has died due to some
		 * reason. Following the exception, the Folder is reset to the "closed"
		 * state.
		 * </p>
		 */
		FOLDER_CLOSED("Folder is closed: %s", Category.CODE_ERROR, 9),
		/**
		 * Folder not found: %s
		 * <p>
		 * This exception is thrown by Folder methods, when those methods are
		 * invoked on a non existent folder.
		 * </p>
		 */
		FOLDER_NOT_FOUND("Folder not found: %s", Category.CODE_ERROR, 10),
		/**
		 * Illegal write attempt: %s
		 * <p>
		 * The exception thrown when a write is attempted on a read-only
		 * attribute of any Messaging object.
		 * </p>
		 */
		ILLEGAL_WRITE("Illegal write attempt: %s", Category.CODE_ERROR, 11),
		/**
		 * Invalid method on a expunged message: %s
		 * <p>
		 * The exception thrown when an invalid method is invoked on an expunged
		 * Message. The only valid methods on an expunged Message are
		 * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
		 * </p>
		 */
		MESSAGE_REMOVED("Invalid method on an expunged message: %s", Category.CODE_ERROR, 12),
		/**
		 * Method not supported: %s
		 * <p>
		 * The exception thrown when a method is not supported by the
		 * implementation
		 * </p>
		 */
		METHOD_NOT_SUPPORTED("Method not supported: %s", Category.CODE_ERROR, 13),
		/**
		 * Session attempts to instantiate a provider that doesn't exist: %s
		 */
		NO_SUCH_PROVIDER("Session attempts to instantiate a provider that doesn't exist: %s",
				Category.CODE_ERROR, 14),
		/**
		 * Wrong message %s header: %s
		 * <p>
		 * The exception thrown due to an error in parsing RFC822 or MIME
		 * headers
		 * </p>
		 */
		PARSE_ERROR("Wrong message %s header: %s", Category.USER_INPUT, 15),
		/**
		 * An attempt was made to open a read-only folder with read-write: %s
		 */
		READ_ONLY_FOLDER("An attempt was made to open a read-only folder with read-write: %s", Category.PERMISSION, 16),
		/**
		 * Invalid search expression: %s
		 */
		SEARCH_ERROR("Invalid search expression: %s", Category.CODE_ERROR, 17),
		/**
		 * Message could not be sent to following recipients: %s
		 * <p>
		 * The exception includes those addresses to which the message could not
		 * be sent as well as the valid addresses to which the message was sent
		 * and valid addresses to which the message was not sent.
		 * </p>
		 */
		SEND_FAILED("Message could not be sent to the following recipients: %s", Category.USER_INPUT, 18),
		/**
		 * Store already closed: %s
		 */
		STORE_CLOSED("Store already closed: %s", Category.CODE_ERROR, 19),
		/**
		 * Internal error: %s
		 */
		INTERNAL_ERROR("Internal error: %s", Category.CODE_ERROR, 20),
		/**
		 * User %s has no access on IMAP-Folder %s
		 */
		NO_ACCESS("User %s has no access to mail folder %s", Category.PERMISSION, 21),
		/**
		 * User %s has no read access on IMAP-Folder %s
		 */
		NO_READ_ACCESS("User %s has no read access to mail folder %s", Category.PERMISSION, 22),
		/**
		 * Folder %s does not hold messages and is therefore not selectable
		 */
		FOLDER_DOES_NOT_HOLD_MESSAGES("Folder %s does not hold messages and is therefore not selectable", Category.PERMISSION, 23),
		/**
		 * Missing field %s in message %s
		 */
		MISSING_FIELD("Missing field %s in message %s", Category.CODE_ERROR, 24),
		/**
		 * Missing header %s in message %s
		 */
		MISSING_HEADER("Missing header %s in message %s", Category.CODE_ERROR, 25),
		/**
		 * User %s has no delete access on IMAP-Folder %s
		 */
		NO_DELETE_ACCESS("User %s has no delete access to mail folder %s", Category.PERMISSION, 26),
		/**
		 * Missing %s folder in mail move operation
		 */
		MISSING_SOURCE_TARGET_FOLDER_ON_MOVE("Missing %s folder in mail move operation", Category.CODE_ERROR, 27),
		/**
		 * User %s has no insert access on IMAP-Folder %s
		 */
		NO_INSERT_ACCESS("User %s has no insert access to mail folder %s", Category.PERMISSION, 28),
		/**
		 * Message(s) %s could not be found in folder %s
		 */
		MESSAGE_NOT_FOUND("Message(s) %s could not be found in folder %s", Category.CODE_ERROR, 29),
		/**
		 * User %s has no lookup access on IMAP-Folder %s
		 */
		NO_LOOKUP_ACCESS("User %s has no lookup access to mail folder %s", Category.PERMISSION, 30),
		/**
		 * Folder %s must not be updated
		 */
		NO_FOLDER_UPDATE("Folder %s cannot be updated", Category.PERMISSION, 31),
		/**
		 * Default folder %s must not be updated
		 */
		NO_DEFAULT_FOLDER_UPDATE("Default folder %s cannot be updated", Category.PERMISSION, 32),
		/**
		 * User %s has no create access on IMAP-Folder %s
		 */
		NO_CREATE_ACCESS("User %s has no create access to mail folder %s", Category.PERMISSION, 33),
		/**
		 * A folder %s already exists
		 */
		DUPLICATE_FOLDER("A folder named %s already exists", Category.PERMISSION, 34),
		/**
		 * Update of folder %s failed
		 */
		UPDATE_FAILED("Update of folder %s failed", Category.CODE_ERROR, 35),
		/**
		 * User %s has no administer access on IMAP-Folder %s
		 */
		NO_ADMINISTER_ACCESS("User %s has no administer access to mail folder %s", Category.PERMISSION, 36),
		/**
		 * Deletion of folder %s failed
		 */
		DELETE_FAILED("Deletion of folder %s failed", Category.CODE_ERROR, 37),
		/**
		 * Folder %s must not be deleted
		 */
		NO_FOLDER_DELETE("Folder %s cannot be deleted", Category.PERMISSION, 38),
		/**
		 * Default folder %s must not be deleted
		 */
		NO_DEFAULT_FOLDER_DELETE("Default folder %s cannot be deleted", Category.PERMISSION, 39),
		/**
		 * User %s has no mail module access due to user configuration
		 */
		NO_MAIL_MODULE_ACCESS("User %s has no mail module access due to user configuration",
				Category.USER_CONFIGURATION, 40),
		/**
		 * The message to send contains an invalid attachment %s: neither
		 * content nor attachment's filename is set
		 */
		INVALID_ATTACHMENT_ON_SEND(
				"The message to send contains an invalid attachment %s: neither content nor attachment's filename is set",
				Category.CODE_ERROR, 41),
		/**
		 * User %s has no write access to IMAP folder %s
		 */
		NO_WRITE_ACCESS("User %s has no write access to IMAP folder %s", Category.PERMISSION, 42),
		/**
		 * IMAP default folder %s could not be created
		 */
		NO_DEFAULT_FOLDER_CREATION("IMAP default folder %s could not be created", Category.CODE_ERROR, 43),
		/**
		 * Missing default %s folder in user mail settings
		 */
		MISSING_DEFAULT_FOLDER_NAME("Missing default %s folder in user mail settings", Category.CODE_ERROR, 44),
		/**
		 * Number of search fields (%d) do not match number of search patterns
		 * (%d)
		 */
		INVALID_SEARCH_PARAMS("Number of search fields (%d) do not match number of search patterns (%d)",
				Category.CODE_ERROR, 45),
		/**
		 * No send address could be found for user %s in user configuration
		 */
		NO_SEND_ADDRESS_FOUND("No send address could be found for user %s in user configuration",
				Category.CODE_ERROR, 46),
		/**
		 * Folder "%s" could not be created (maybe due to insufficient permission on parental folder %s)
		 */
		FOLDER_CREATION_FAILED("Mail folder \"%s\" could not be created (maybe due to insufficient permission on parental folder %s)", Category.CODE_ERROR, 47),
		/**
		 * Message could not be moved to trash folder
		 */
		MOVE_ON_DELETE_FAILED("Message could not be moved to trash folder", Category.EXTERNAL_RESOURCE_FULL, 48),
		/**
		 * Invalid attachment %s: %s
		 */
		INVAILD_ATTACHMENT("Invalid attachment %s: %s", Category.CODE_ERROR, 49),
		/**
		 * Missing key %s in message's JSON representation
		 */
		MISSING_JSON_KEY("Missing key %s in message's JSON representation", Category.CODE_ERROR, 50),
		/**
		 * Missing value in key %s in message's JSON representation
		 */
		MISSING_JSON_VALUE("Missing value in key %s in message's JSON representation", Category.CODE_ERROR, 51),
		/**
		 * Either key %s or key %s is mssing in message's JSON representation
		 */
		MISSING_JSON_KEY_XOR("Either key %s or key %s is mssing in message's JSON representation",
				Category.CODE_ERROR, 52),
		/**
		 * Both %s and %s must not be present in message's JSON representation
		 */
		INVALID_KEY_COMBINATION("Both %s and %s must not be present in message's JSON representation",
				Category.CODE_ERROR, 53),
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAM("Missing parameter %s", Category.CODE_ERROR, 54),
		/**
		 * Invalid email address %s
		 */
		INVALID_EMAIL_ADDRESS("Invalid email address %s", Category.USER_INPUT, 55),
		/**
		 * Message could not be sent
		 */
		SEND_FAILED_UNKNOWN("Message could not be sent", Category.CODE_ERROR, 56),
		/**
		 * Invalid unique message identifier: %s
		 */
		INVALID_MAIL_IDENTIFIER("Invalid unique message identifier: %s", Category.CODE_ERROR, 57),
		/**
		 * Could not create a PartModifier instance from name %s
		 */
		PART_MODIFIER_CREATION_FAILED("Could not create a PartModifier instance from name %s",
				Category.CODE_ERROR, 58),
		/**
		 * User %s has no keep-seen access on IMAP-Folder %s
		 */
		NO_KEEP_SEEN_ACCESS("User %s has no keep-seen access to mail folder %s", Category.PERMISSION, 59),
		/**
		 * Unknown parameter container type %d
		 */
		UNKNOWN_PARAM_CONTAINER_TYPE("Unknown parameter container type %d", Category.CODE_ERROR, 60),
		/**
		 * Invalid integer value %s
		 */
		INVALID_INT_VALUE("Invalid integer value %s", Category.CODE_ERROR, 61),
		/**
		 * Action %s is not supported by %s
		 */
		UNSUPPORTED_ACTION("Action %s is not supported by %s", Category.CODE_ERROR, 62),
		/**
		 * Deletion of message %s failed
		 */
		MESSAGE_DELETE_FAILED("Deletion of message %s failed", Category.CODE_ERROR, 63),
		/**
		 * No attachment was found with id %s in message %s
		 */
		NO_ATTACHMENT_FOUND("No attachment was found with id %s in message %s", Category.USER_INPUT, 64),
		/**
		 * Upload quota (%d) exceeded for file %s (size=%d)
		 */
		UPLOAD_QUOTA_EXCEEDED_FOR_FILE("Upload quota (%d) exceeded for file %s (size=%d)",
				Category.USER_INPUT, 65),
		/**
		 * Upload quota (%d) exceeded
		 */
		UPLOAD_QUOTA_EXCEEDED("Upload quota (%d) exceeded", Category.USER_INPUT, 66),
		/**
		 * Missing user default email address in mail settings
		 */
		MISSING_DEFAULT_EMAIL_ADDRESS("Missing user default email address in mail settings", Category.SETUP_ERROR, 67),
		/**
		 * Unknown (IMAP) folder open mode %d
		 */
		UNKNOWN_FOLDER_MODE("Unknown (IMAP) folder open mode %d", Category.CODE_ERROR, 68),
		/**
		 * %s attachment could not be save due to an unsupported MIME type %s
		 */
		UNSUPPORTED_VERSIT_ATTACHMENT("%s attachment could not be save due to an unsupported MIME type %s",
				Category.USER_INPUT, 69),
		/**
		 * Versit object %s could not be saved
		 */
		FAILED_VERSIT_SAVE("Versit object could not be saved", Category.CODE_ERROR, 70),
		/**
		 * Unsupported MIME type %s
		 */
		UNSUPPORTED_MIME_TYPE("Unsupported MIME type %s", Category.CODE_ERROR, 71),
		/**
		 * IMAP search failed due to following reason: %s Switching to
		 * application-based search
		 */
		IMAP_SEARCH_FAILED("IMAP search failed due to following reason: %s Switching to application-based search",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 72),
		/**
		 * IMAP sort failed due to following reason: %s Switching to
		 * application-based sorting
		 */
		IMAP_SORT_FAILED("IMAP sort failed due to following reason: %s Switching to application-based sorting",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 73),
		/**
		 * No recipient(s) has been defined for new message
		 */
		MISSING_RECIPIENTS("There are no recipient(s) for the new message.", Category.USER_INPUT, 74),
		/**
		 * Draft could not be edited in read-only IMAP-Folder %s
		 */
		NO_DRAFT_EDIT("Draft message could not be edited in read-only mail folder %s", Category.PERMISSION, 75),
		/**
		 * Folder read-only check failed
		 */
		FAILED_READ_ONLY_CHECK("Folder read-only check failed", Category.CODE_ERROR, 76),
		/**
		 * IMAP property error: %s
		 */
		IMAP_PROPERTY_ERROR("IMAP property error: %s", Category.SETUP_ERROR, 77),
		/**
		 * Unknown action: %s
		 */
		UNKNOWN_ACTION("Unknown or unsupported action: %s", Category.CODE_ERROR, 78),
		/**
		 * Unknown color label index: %d
		 */
		UNKNOWN_COLOR_LABEL("Unknown color label: %s", Category.CODE_ERROR, 79),
		/**
		 * Move/Copy failed. New message with UID %d could not be found in
		 * destination folder %s
		 */
		MOVE_COPY_FAILED("Move/Copy failed. New message with UID %d could not be found in destination folder %s",
				Category.CODE_ERROR, 80),
		/**
		 * Invalid Content-Type value: %s
		 */
		INVALID_CONTENT_TYPE("Invalid Content-Type value: %s", Category.INTERNAL_ERROR, 81),
		/**
		 * Message could not be moved to trash folder. Quota exceeded
		 */
		DELETE_FAILED_OVER_QUOTA("Message could not be moved to trash folder. Quota exceeded",
				Category.EXTERNAL_RESOURCE_FULL, 82),
		/**
		 * Message has been successfully sent, but could not be copied to sent
		 * folder due to exceeded quota
		 */
		COPY_TO_SENT_FOLDER_FAILED(
				"Message has been successfully sent, but a copy was not placed in your sent folder due to exceeded quota.",
				Category.EXTERNAL_RESOURCE_FULL, 83),
		/**
		 * A protocol exception occurred during execution of an IMAP request: %s
		 */
		PROTOCOL_ERROR("A protocol exception occurred during execution of an IMAP request: %s", Category.INTERNAL_ERROR,
				84),
		/**
		 * Folder %s does not allow subfolders.
		 */
		FOLDER_DOES_NOT_HOLD_FOLDERS("Folder %s does not allow subfolders.", Category.PERMISSION, 85),
		/**
		 * Process was interrupted while waiting for a free IMAP connection.
		 * Please try again.
		 */
		INTERRUPT_ERROR("Process was interrupted while waiting for a free IMAP connection. Please try again.",
				Category.TRY_AGAIN, 86),
		/**
		 * Flag %s could not be set in message %d in folder %s due to following
		 * reason: %s
		 */
		FLAG_FAILED("Flag %s could not be changed in message %d in folder %s due to following reason: %s",
				Category.INTERNAL_ERROR, 87),
		/**
		 * Mail folder cannot be created. Name must not contain character '%s'
		 */
		INVALID_FOLDER_NAME("Mail folder cannot be created. Name must not contain character '%s'", Category.USER_INPUT, 88),
		/**
		 * A JSON syntax error occurred: %s
		 */
		JSON_ERROR("A JSON syntax error occurred: %s", Category.CODE_ERROR, 89),
		/**
		 * A part's content could not be read from message %s in mail folder %s of user %s
		 */
		UNREADBALE_PART_CONTENT("A part's content could not be read from message %s in mail folder %s of user %s", Category.INTERNAL_ERROR, 90),
		/**
		 * An IMAP error occurred: %s
		 */
		IMAP_ERROR("An IMAP error occurred: %s", Category.CODE_ERROR, 91),
		/**
		 * A socket error occurred: %s
		 */
		SOCKET_ERROR("A socket error occurred: %s", Category.CODE_ERROR, 92),
		/**
		 * Html-2-Text conversion failed: %s
		 */
		HTML2TEXT_CONVERTER_ERROR("Html-2-Text conversion failed: %s", Category.CODE_ERROR, 93),
		/**
		 * There was an issue in authenticating your email password. This may be because of a recent password change.
		 * To continue please logout now and then log back in with your most current password. (server=%s | user=%s | context=%s)
		 */
		LOGIN_FAILED("There was an issue in authenticating your email password. This may be because of a recent password change. " +
				"To continue please logout now and then log back in with your most current password. (server=%s | user=%s | context=%s)",
				Category.PERMISSION, 94),
		/**
		 * SpamAssassin executable not found
		 */
		SPAMASSASSIN_NOT_FOUND("SpamAssassin not installed or its executable has not been found", Category.SUBSYSTEM_OR_SERVICE_DOWN, 95),
		/**
		 * No IMAP account exists for user %s in context %s
		 */
		ACCOUNT_DOES_NOT_EXIST("No IMAP account exists for user %s in context %s", Category.SETUP_ERROR, 96),
		/**
		 * Move partially completed for user %s. Source message(s) %s in folder %s could not be deleted due to following error: %s
		 */
		MOVE_PARTIALLY_COMPLETED("Move partially completed for user %s. Source message(s) %s in folder %s could not be deleted due to following error: %s", Category.INTERNAL_ERROR, 97),
		/**
		 * Message move aborted for user %s. Source and destination folder are equal: %s
		 */
		NO_EQUAL_MOVE("Message move aborted for user %s. Source and destination folder are equal: %s", Category.USER_INPUT, 98),
		/**
		 * No admin permission specified by user %s for folder %s
		 */
		NO_ADMIN_ACL("No administer permission specified by user %s for folder %s", Category.USER_INPUT, 99),
		/**
		 * Folder owner's administer permission must not be removed from folder %s
		 */
		NO_FOLDER_OWNERACL_REMOVE("Folder owner's administer permission must not be removed from folder %s", Category.USER_INPUT, 100),
		/**
		 * Message could not be sent because it is too large
		 */
		MESSAGE_TOO_LARGE("Message could not be sent because it is too large", Category.INTERNAL_ERROR, 101),
		/**
		 * The rights choosed by user %s could not be applied to new folder %s due to missing administer right in its initial rights specified by IMAP server. The folder has been created anyway.
		 */
		NO_ADMINISTER_ACCESS_ON_INITIAL("The rights composed by user %s could not be applied to new folder %s due to missing administer right in its initial rights specified by IMAP server. The folder has been created anyway.", Category.PERMISSION, 102);

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
		private final int detailNumber;

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
		private MailCode(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.category = category;
			this.detailNumber = detailNumber;
		}

		public int getNumber() {
			return detailNumber;
		}

		public String getMessage() {
			return message;
		}

		public Category getCategory() {
			return category;
		}
	}

	public static final String getFormattedMessage(final MailCode mailCode, final Object... msgArgs) {
		return String.format(mailCode.getMessage(), msgArgs);
	}
	
	public OXMailException(final AbstractOXException e) {
		super(e);
	}

	public OXMailException(final MailCode mailCode) {
		this(mailCode, null, new Object[0]);
	}

	public OXMailException(final MailCode mailCode, final Object... messageArgs) {
		this(mailCode, null, messageArgs);
	}

	public OXMailException(final MailCode mailCode, final Exception cause, final Object... messageArgs) {
		super(Component.EMAIL, mailCode.category, mailCode.detailNumber, mailCode.message, cause, messageArgs);
	}

}
