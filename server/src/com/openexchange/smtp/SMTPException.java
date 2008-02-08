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

package com.openexchange.smtp;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;

/**
 * {@link SMTPException}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SMTPException extends MIMEMailException {

	private static final long serialVersionUID = -4944650865952255865L;

	private static final transient Object[] EMPTY_ARGS = new Object[0];

	public static enum Code {

		/**
		 * An I/O error occured: %s
		 */
		IO_ERROR(MailException.Code.IO_ERROR),
		/**
		 * Unsupported charset-encoding: %s
		 */
		ENCODING_ERROR(MailException.Code.ENCODING_ERROR),
		/**
		 * The message part with sequence ID %s could not be found in message %s
		 * in folder %s
		 */
		PART_NOT_FOUND("The message part with sequence ID %s could not be found in message %s in folder %s",
				Category.CODE_ERROR, 3003),
		/**
		 * Html-2-Text conversion failed: %s
		 */
		HTML2TEXT_CONVERTER_ERROR("Html-2-Text conversion failed: %s", Category.CODE_ERROR, 3004),
		/**
		 * An internal error occured: %s
		 */
		INTERNAL_ERROR("An internal error occured: %s", Category.CODE_ERROR, 3005),
		/**
		 * No recipient(s) has been defined for new message
		 */
		MISSING_RECIPIENTS("There are no recipient(s) for the new message.", Category.USER_INPUT, 3006),
		/**
		 * Message has been successfully sent, but a copy could not be placed in
		 * your sent folder
		 */
		COPY_TO_SENT_FOLDER_FAILED(MailException.Code.COPY_TO_SENT_FOLDER_FAILED),
		/**
		 * Receipt acknowledgment cannot be sent: missing header %s in message
		 * %s
		 */
		MISSING_NOTIFICATION_HEADER("Receipt acknowledgment cannot be sent: missing header %s in message %s",
				Category.CODE_ERROR, 3008),
		/**
		 * No send address could be found in user configuration
		 */
		NO_SEND_ADDRESS_FOUND("No send address could be found in user configuration", Category.CODE_ERROR, 3009),
		/**
		 * No content available in mail part
		 */
		NO_CONTENT("No content available in mail part", Category.CODE_ERROR, 3010),
		/**
		 * Message has been successfully sent, but a copy could not be placed in
		 * your sent folder due to exceeded quota.
		 */
		COPY_TO_SENT_FOLDER_FAILED_QUOTA(MailException.Code.COPY_TO_SENT_FOLDER_FAILED_QUOTA),
		/**
		 * No storage access because mail connection is not connected
		 */
		NOT_CONNECTED("No storage access because mail connection is not connected", Category.CODE_ERROR, 3012);

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
	 * @param cause
	 */
	public SMTPException(final AbstractOXException cause) {
		super(cause);
	}

	public SMTPException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	public SMTPException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.SMTP, code.category, code.detailNumber, code.message, cause);
		super.setMessageArgs(messageArgs);
	}

	public SMTPException(Code code) {
		this(code, EMPTY_ARGS);
	}

}
