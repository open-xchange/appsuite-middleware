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

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;

/**
 * {@link IMAPException}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPException extends MIMEMailException {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -8226676160145457046L;

	public static enum Code {

		/**
		 * Missing parameter in mail connection: %s
		 */
		MISSING_CONNECT_PARAM(MailException.Code.MISSING_CONNECT_PARAM),
		/**
		 * No connection available to access mailbox
		 */
		NOT_CONNECTED("No connection available to access mailbox", Category.CODE_ERROR, 2001),
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAMETER(MailException.Code.MISSING_PARAMETER),
		/**
		 * A JSON error occured: %s
		 */
		JSON_ERROR(MailException.Code.JSON_ERROR),
		/**
		 * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
		 */
		INVALID_PERMISSION("Invalid permission values: fp=%d orp=%d owp=%d odp=%d", Category.CODE_ERROR, 2002),
		/**
		 * User %s has no mail module access due to user configuration
		 */
		NO_MAIL_MODULE_ACCESS("User %s has no mail module access due to user configuration",
				Category.USER_CONFIGURATION, 2003),
		/**
		 * No access to mail folder %s
		 */
		NO_ACCESS("No access to mail folder %s", Category.PERMISSION, 2003),
		/**
		 * No lookup access to mail folder %s
		 */
		NO_LOOKUP_ACCESS("No lookup access to mail folder %s", Category.PERMISSION, 2004),
		/**
		 * No read access on IMAP-Folder %s
		 */
		NO_READ_ACCESS("No read access to mail folder %s", Category.PERMISSION, 2005),
		/**
		 * No delete access on IMAP-Folder %s
		 */
		NO_DELETE_ACCESS("No delete access to mail folder %s", Category.PERMISSION, 2006),
		/**
		 * No insert access on IMAP-Folder %s
		 */
		NO_INSERT_ACCESS("No insert access to mail folder %s", Category.PERMISSION, 2007),
		/**
		 * No create access on IMAP-Folder %s
		 */
		NO_CREATE_ACCESS("No create access to mail folder %s", Category.PERMISSION, 2008),
		/**
		 * No administer access on IMAP-Folder %s
		 */
		NO_ADMINISTER_ACCESS("No administer access to mail folder %s", Category.PERMISSION, 2009),
		/**
		 * No write access to IMAP folder %s
		 */
		NO_WRITE_ACCESS("No write access to IMAP folder %s", Category.PERMISSION, 2010),
		/**
		 * No keep-seen access on IMAP-Folder %s
		 */
		NO_KEEP_SEEN_ACCESS("No keep-seen access to mail folder %s", Category.PERMISSION, 2011),
		/**
		 * Folder %s does not allow subfolders.
		 */
		FOLDER_DOES_NOT_HOLD_FOLDERS("Folder %s does not allow subfolders.", Category.PERMISSION, 2012),
		/**
		 * Mail folder cannot be created/rename. Name must not contain character '%s'
		 */
		INVALID_FOLDER_NAME("Mail folder cannot be created/renamed. Name must not contain character '%s'", Category.USER_INPUT,
				2013),
		/**
		 * A folder named %s already exists
		 */
		DUPLICATE_FOLDER("A folder named %s already exists", Category.PERMISSION, 2014),
		/**
		 * Mail folder "%s" could not be created (maybe due to insufficient
		 * permission on parent folder %s or due to an invalid folder name)
		 */
		FOLDER_CREATION_FAILED(
				"Mail folder \"%s\" could not be created (maybe due to insufficient permission on parent folder %s or due to an invalid folder name)",
				Category.CODE_ERROR, 2015),
		/**
		 * The composed rights could not be applied to new folder %s due to
		 * missing administer right in its initial rights specified by IMAP
		 * server. However, the folder has been created.
		 */
		NO_ADMINISTER_ACCESS_ON_INITIAL(
				"The composed rights could not be applied to new folder %s due to missing administer right in its initial rights specified by IMAP server. However, the folder has been created.",
				Category.PERMISSION, 2016),
		/**
		 * No admin permission specified for folder %s
		 */
		NO_ADMIN_ACL("No administer permission specified for folder %s", Category.USER_INPUT, 2017),
		/**
		 * Default folder %s must not be updated
		 */
		NO_DEFAULT_FOLDER_UPDATE("Default folder %s cannot be updated", Category.PERMISSION, 2018),
		/**
		 * Deletion of folder %s failed
		 */
		DELETE_FAILED("Deletion of folder %s failed", Category.CODE_ERROR, 2019),
		/**
		 * IMAP default folder %s could not be created
		 */
		NO_DEFAULT_FOLDER_CREATION("IMAP default folder %s could not be created", Category.CODE_ERROR, 2020),
		/**
		 * Missing default %s folder in user mail settings
		 */
		MISSING_DEFAULT_FOLDER_NAME("Missing default %s folder in user mail settings", Category.CODE_ERROR, 2021),
		/**
		 * Update of folder %s failed
		 */
		UPDATE_FAILED("Update of folder %s failed", Category.CODE_ERROR, 2022),
		/**
		 * Folder %s must not be deleted
		 */
		NO_FOLDER_DELETE("Folder %s cannot be deleted", Category.PERMISSION, 2023),
		/**
		 * Default folder %s must not be deleted
		 */
		NO_DEFAULT_FOLDER_DELETE("Default folder %s cannot be deleted", Category.PERMISSION, 2024),
		/**
		 * An I/O error occurred: %s
		 */
		IO_ERROR(MailException.Code.IO_ERROR),
		/**
		 * Flag %s could not be changed due to following reason: %s
		 */
		FLAG_FAILED("Flag %s could not be changed due to following reason: %s", Category.INTERNAL_ERROR, 2025),
		/**
		 * Folder %s does not hold messages and is therefore not selectable
		 */
		FOLDER_DOES_NOT_HOLD_MESSAGES(MailException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES),
		/**
		 * Number of search fields (%d) do not match number of search patterns
		 * (%d)
		 */
		INVALID_SEARCH_PARAMS("Number of search fields (%d) do not match number of search patterns (%d)",
				Category.CODE_ERROR, 2028),
		/**
		 * IMAP search failed due to following reason: %s. Switching to
		 * application-based search
		 */
		IMAP_SEARCH_FAILED("IMAP search failed due to following reason: %s. Switching to application-based search",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 2029),
		/**
		 * IMAP sort failed due to following reason: %s Switching to
		 * application-based sorting
		 */
		IMAP_SORT_FAILED("IMAP sort failed due to following reason: %s Switching to application-based sorting",
				Category.SUBSYSTEM_OR_SERVICE_DOWN, 2030),
		/**
		 * Unknow search field: %s
		 */
		UNKNOWN_SEARCH_FIELD("Unknow search field: %s", Category.CODE_ERROR, 2031),
		/**
		 * Message field %s cannot be handled
		 */
		INVALID_FIELD(MailException.Code.INVALID_FIELD),
		/**
		 * Mail folder %s must not be moved to subsequent folder %s
		 */
		NO_MOVE_TO_SUBFLD("Mail folder %s must not be moved to subsequent folder %s", Category.PERMISSION, 2032),
		/**
		 * Message could not be moved to trash folder
		 */
		MOVE_ON_DELETE_FAILED("Message could not be moved to trash folder", Category.EXTERNAL_RESOURCE_FULL, 2034),
		/**
		 * Missing %s folder in mail move operation
		 */
		MISSING_SOURCE_TARGET_FOLDER_ON_MOVE("Missing %s folder in mail move operation", Category.CODE_ERROR, 2035),
		/**
		 * Message move aborted for user %s. Source and destination folder are
		 * equal: %s
		 */
		NO_EQUAL_MOVE("Message move aborted for user %s. Source and destination folder are equal: %s",
				Category.USER_INPUT, 2036),
		/**
		 * Folder read-only check failed
		 */
		FAILED_READ_ONLY_CHECK("IMAP folder read-only check failed", Category.CODE_ERROR, 2037),
		/**
		 * Unknown folder open mode %d
		 */
		UNKNOWN_FOLDER_MODE("Unknown folder open mode %d", Category.CODE_ERROR, 2038),
		/**
		 * Message(s) %s in folder %s could not be deleted due to following
		 * error: %s
		 */
		UID_EXPUNGE_FAILED("Message(s) %s in folder %s could not be deleted due to following error: %s",
				Category.INTERNAL_ERROR, 2039),
		/**
		 * Not allowed to open folder %s due to missing read access
		 */
		NO_FOLDER_OPEN("Not allowed to open folder %s due to missing read access", Category.PERMISSION, 2041),
		/**
		 * The raw content's input stream of message %d in folder %s cannot be
		 * read
		 */
		MESSAGE_CONTENT_ERROR("The raw content's input stream of message %d in folder %s cannot be read",
				Category.CODE_ERROR, 2042),
		/**
		 * No attachment was found with id %s in message
		 */
		NO_ATTACHMENT_FOUND("No attachment was found with id %s in message", Category.USER_INPUT, 2043),
		/**
		 * Versit attachment could not be saved due to an unsupported MIME type:
		 * %s
		 */
		UNSUPPORTED_VERSIT_ATTACHMENT(MailException.Code.UNSUPPORTED_VERSIT_ATTACHMENT),
		/**
		 * Versit object %s could not be saved
		 */
		FAILED_VERSIT_SAVE("Versit object could not be saved", Category.CODE_ERROR, 2045),
		/**
		 * IMAP server does not support capability "THREAD=REFERENCES"
		 */
		THREAD_SORT_NOT_SUPPORTED("IMAP server does not support capability \"THREAD=REFERENCES\"", Category.CODE_ERROR,
				2046),
		/**
		 * Unsupported charset-encoding: %s
		 */
		ENCODING_ERROR(MailException.Code.ENCODING_ERROR),
		/**
		 * A protocol exception occurred during execution of an IMAP request: %s
		 */
		PROTOCOL_ERROR("A protocol exception occurred during execution of an IMAP request: %s",
				Category.INTERNAL_ERROR, 2047),
		/**
		 * Mail folder %s could not be found
		 */
		FOLDER_NOT_FOUND(MIMEMailException.Code.FOLDER_NOT_FOUND),
		/**
		 * An attempt was made to open a read-only folder with read-write: %s
		 */
		READ_ONLY_FOLDER(MIMEMailException.Code.READ_ONLY_FOLDER),
		/**
		 * Connection was refused or timed out while attempting to connect to
		 * remote server %s for user %s
		 */
		CONNECTION_ERROR(MIMEMailException.Code.CONNECT_ERROR),
		/**
		 * Mailbox' root folder must not be source or the destination fullname
		 * of a move operation.
		 */
		NO_ROOT_MOVE("Mailbox' root folder must not be source or the destination fullname of a move operation.",
				Category.CODE_ERROR, 2048);

		private final String message;

		private final int detailNumber;

		private final Category category;

		private Code(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.detailNumber = detailNumber;
			this.category = category;
		}

		private Code(final MailException.Code code) {
			this.message = code.getMessage();
			this.detailNumber = code.getNumber();
			this.category = code.getCategory();
		}

		private Code(final MIMEMailException.Code code) {
			this.message = code.getMessage();
			this.detailNumber = code.getNumber();
			this.category = code.getCategory();
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

	/**
	 * Gets the message corresponding to specified error code with given message
	 * arguments applied.
	 * 
	 * @param code
	 *            The code
	 * @param msgArgs
	 *            The message arguments
	 * @return The message corresponding to specified error code with given
	 *         message arguments applied
	 */
	public static String getFormattedMessage(final Code code, final Object... msgArgs) {
		return String.format(code.getMessage(), msgArgs);
	}

	private static final transient Object[] EMPTY_ARGS = new Object[0];

	/**
	 * Initializes a new {@link IMAPException}
	 * 
	 * @param cause
	 *            The cause
	 */
	public IMAPException(final AbstractOXException cause) {
		super(cause);
	}

	/**
	 * Initializes a new {@link IMAPException}
	 * 
	 * @param code
	 *            The code
	 * @param messageArgs
	 *            The message arguments
	 */
	public IMAPException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	/**
	 * Initializes a new {@link IMAPException}
	 * 
	 * @param code
	 *            The code
	 * @param cause
	 *            The cause
	 * @param messageArgs
	 *            The message arguments
	 */
	public IMAPException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(IMAPProvider.PROTOCOL_IMAP, code.category, code.detailNumber, code.message, cause);
		super.setMessageArgs(messageArgs);
	}

	/**
	 * Initializes a new {@link IMAPException}
	 * 
	 * @param code
	 *            The code
	 */
	public IMAPException(Code code) {
		this(code, EMPTY_ARGS);
	}

}
