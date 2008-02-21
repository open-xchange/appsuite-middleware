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

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link MailException} - Base class for mail exceptions
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailException extends AbstractOXException {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -1250976535705226442L;

	public static enum Code {
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAMETER("Missing parameter %s", Category.CODE_ERROR, 1),
		/**
		 * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
		 */
		INVALID_PERMISSION("Invalid permission values: fp=%d orp=%d owp=%d odp=%d", Category.CODE_ERROR, 2),
		/**
		 * A JSON error occurred: %s
		 */
		JSON_ERROR("A JSON error occurred: %s", Category.CODE_ERROR, 3),
		/**
		 * Missing parameter in mail connection: %s
		 */
		MISSING_CONNECT_PARAM("Missing parameter in mail connection: %s", Category.CODE_ERROR, 4),
		/**
		 * Property error: %s
		 */
		PROPERTY_ERROR("Property error: %s", Category.SETUP_ERROR, 5),
		/**
		 * Invalid multipart content. Number of enclosed contents is 0
		 */
		INVALID_MULTIPART_CONTENT("Invalid multipart content. Number of enclosed contents is 0", Category.CODE_ERROR, 6),
		/**
		 * A part's content could not be read from message %s in mail folder %s
		 */
		UNREADBALE_PART_CONTENT("A part's content could not be read from message %s in mail folder %s",
				Category.INTERNAL_ERROR, 7),
		/**
		 * An I/O error occurred %s
		 */
		IO_ERROR("An I/O error occurred %s", Category.CODE_ERROR, 8),
		/**
		 * Invalid message path: %s
		 */
		INVALID_MAIL_IDENTIFIER("Invalid message path: %s", Category.CODE_ERROR, 9),
		/**
		 * Unknown color label index: %d
		 */
		UNKNOWN_COLOR_LABEL("Unknown color label: %s", Category.CODE_ERROR, 10),
		/**
		 * Cannot instantiate class %s.
		 */
		INSTANTIATION_PROBLEM("Cannot instantiate class %s.", Category.SETUP_ERROR, 11),
		/**
		 * Cannot initialize
		 */
		INITIALIZATION_PROBLEM("Cannot initialize", Category.SETUP_ERROR, 12),
		/**
		 * No mail module access permitted
		 */
		NO_MAIL_ACCESS("No mail module access permitted", Category.PERMISSION, 13),
		/**
		 * No mail account exists for admin user
		 */
		ACCOUNT_DOES_NOT_EXIST("No mail account exists for admin user in context %d", Category.SETUP_ERROR, 14),
		/**
		 * Process was interrupted while waiting for a free mail connection.
		 * Please try again.
		 */
		INTERRUPT_ERROR("Process was interrupted while waiting for a free mail connection. Please try again.",
				Category.TRY_AGAIN, 15),
		/**
		 * Unsupported charset-encoding: %s
		 */
		ENCODING_ERROR("Unsupported charset-encoding: %s", Category.CODE_ERROR, 16),
		/**
		 * Header %s could not be properly parsed
		 */
		HEADER_PARSE_ERROR("Header %s could not be properly parsed", Category.CODE_ERROR, 17),
		/**
		 * Missing default %s folder in user mail settings
		 */
		MISSING_DEFAULT_FOLDER_NAME("Missing default %s folder in user mail settings", Category.CODE_ERROR, 18),
		/**
		 * Spam handler initialization failed: %s
		 */
		SPAM_HANDLER_INIT_FAILED("Spam handler initialization failed: %s", Category.SETUP_ERROR, 19),
		/**
		 * Invalid Content-Type value: %s
		 */
		INVALID_CONTENT_TYPE("Invalid Content-Type value: %s", Category.CODE_ERROR, 20),
		/**
		 * Messaging error: %s
		 */
		MESSAGING_ERROR("Messaging error: %s", Category.CODE_ERROR, 21),
		/**
		 * Message field %s cannot be handled
		 */
		INVALID_FIELD("Message field %s cannot be handled", Category.CODE_ERROR, 22),
		/**
		 * Versit error: %s
		 */
		VERSIT_ERROR("Versit error: %s", Category.CODE_ERROR, 23),
		/**
		 * No attachment was found with id %s in message
		 */
		NO_ATTACHMENT_FOUND("No attachment was found with id %s in message", Category.USER_INPUT, 24),
		/**
		 * Versit attachment could not be saved due to an unsupported MIME type:
		 * %s
		 */
		UNSUPPORTED_VERSIT_ATTACHMENT("Versit attachment could not be saved due to an unsupported MIME type: %s",
				Category.USER_INPUT, 25),
		/**
		 * Invalid parameter name: %s
		 */
		INVALID_PARAMETER("Invalid parameter name: %s", Category.CODE_ERROR, 26),
		/**
		 * Could not create a PartModifier instance from name %s
		 */
		PART_MODIFIER_CREATION_FAILED("Could not create a PartModifier instance from name %s", Category.CODE_ERROR, 27),
		/**
		 * Upload quota (%d) exceeded for file %s (size=%d)
		 */
		UPLOAD_QUOTA_EXCEEDED_FOR_FILE("Upload quota (%d) exceeded for file %s (size=%d)", Category.USER_INPUT, 28),
		/**
		 * Upload quota (%d) exceeded
		 */
		UPLOAD_QUOTA_EXCEEDED("Upload quota (%d) exceeded", Category.USER_INPUT, 29),
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAM("Missing parameter %s", Category.CODE_ERROR, 30),
		/**
		 * Invalid integer value %s
		 */
		INVALID_INT_VALUE("Invalid integer value %s", Category.CODE_ERROR, 31),
		/**
		 * Mail(s) %s could not be found in folder %s
		 */
		MAIL_NOT_FOUND("Mail(s) %s could not be found in folder %s", Category.CODE_ERROR, 32),
		/**
		 * Action %s is not supported by %s
		 */
		UNSUPPORTED_ACTION("Action %s is not supported by %s", Category.CODE_ERROR, 33),
		/**
		 * Message could not be sent
		 */
		SEND_FAILED_UNKNOWN("Message could not be sent", Category.CODE_ERROR, 35),
		/**
		 * Unknown action: %s
		 */
		UNKNOWN_ACTION("Unknown or unsupported action: %s", Category.CODE_ERROR, 36),
		/**
		 * Missing field %s
		 */
		MISSING_FIELD("Missing field %s", Category.CODE_ERROR, 37),
		/**
		 * Unsupported MIME type %s
		 */
		UNSUPPORTED_MIME_TYPE("Unsupported MIME type %s", Category.CODE_ERROR, 38),
		/**
		 * Mail could not be moved to trash folder. Quota exceeded
		 */
		DELETE_FAILED_OVER_QUOTA("Mail could not be moved to trash folder. Quota exceeded",
				Category.EXTERNAL_RESOURCE_FULL, 39),
		/**
		 * The message part with sequence ID %s could not be found in message %s
		 * in folder %s
		 */
		PART_NOT_FOUND("The message part with sequence ID %s could not be found in message %s in folder %s",
				Category.CODE_ERROR, 40),
		/**
		 * No content available in mail part
		 */
		NO_CONTENT("No content available in mail part", Category.CODE_ERROR, 41),
		/**
		 * Message has been successfully sent, but a copy could not be placed in
		 * your sent folder due to exceeded quota.
		 */
		COPY_TO_SENT_FOLDER_FAILED_QUOTA(
				"Message has been successfully sent, but a copy could not be placed in your sent folder due to exceeded quota.",
				Category.EXTERNAL_RESOURCE_FULL, 42),
		/**
		 * Message has been successfully sent, but a copy could not be placed in
		 * your sent folder
		 */
		COPY_TO_SENT_FOLDER_FAILED(
				"Message has been successfully sent, but a copy could not be placed in your sent folder.",
				Category.EXTERNAL_RESOURCE_FULL, 43),
		/**
		 * No provider could be found for protocol/URL "%s"
		 */
		UNKNOWN_PROTOCOL("No provider could be found for protocol/URL \"%s\"", Category.SETUP_ERROR, 44),
		/**
		 * Protocol cannot be parsed: %s
		 */
		PROTOCOL_PARSE_ERROR("Protocol cannot be parsed: %s", Category.CODE_ERROR, 45),
		/**
		 * Bad value %s in parameter %s
		 */
		BAD_PARAM_VALUE("Bad value %s in parameter %s", Category.USER_INPUT, 46),
		/**
		 * No reply on multiple message possible
		 */
		NO_MULTIPLE_REPLY("No reply on multiple message possible", Category.USER_INPUT, 47),
		/**
		 * legal system flag argument %s. Flag must be to the power of 2
		 */
		ILLEGAL_FLAG_ARGUMENT("Illegal system flag argument %s. Flag must be to the power of 2", Category.CODE_ERROR,
				48);

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

	public MailException(final AbstractOXException cause) {
		super(cause);
	}

	public MailException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	public MailException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.MAIL, code.getCategory(), code.getNumber(), code.getMessage(), cause);
		super.setMessageArgs(messageArgs);
	}

	public MailException(final Component component, final Category category, final int detailNumber,
			final String message, final Throwable cause) {
		super(component, category, detailNumber, message, cause);
	}

	public MailException(final Component component, final String message, final AbstractOXException cause) {
		super(component, message, cause);
	}
}
