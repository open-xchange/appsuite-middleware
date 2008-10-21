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

package com.openexchange.conversion;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link DataException} - The exception for data conversion
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class DataException extends AbstractOXException {

	public static enum Code {

		/**
		 * The given type of %1$s is not supported
		 */
		TYPE_NOT_SUPPORTED("The given type of %1$s is not supported", Category.CODE_ERROR, 1),
		/**
		 * Missing argument %1$s
		 */
		MISSING_ARGUMENT("Missing argument %1$s", Category.CODE_ERROR, 2),
		/**
		 * Invalid value for argument %1$s: %2$s
		 */
		INVALID_ARGUMENT("Invalid value for argument %1$s: %2$s", Category.CODE_ERROR, 3),
		/**
		 * Unknown data source identifier: %1$s
		 */
		UNKNOWN_DATA_SOURCE("Unknown data source identifier: %1$s", Category.CODE_ERROR, 4),
		/**
		 * Unknown data handler identifier: %1$s
		 */
		UNKNOWN_DATA_HANDLER("Unknown data handler identifier: %1$s", Category.CODE_ERROR, 5),
		/**
		 * No matching type could be found for data source %1$s and data handler
		 * %2$s
		 */
		NO_MATCHING_TYPE("No matching type could be found for data source %1$s and data handler %2$s",
				Category.CODE_ERROR, 6),
		/**
		 * An error occurred: %1$s
		 */
		ERROR("An error occurred: %1$s", Category.CODE_ERROR, 7),
		/**
		 * The following field(s) are too long: %1$s
		 */
		TRUNCATED("The following field(s) are too long: %1$s", Category.TRUNCATED, 8);

		private final Category category;

		private final int detailNumber;

		private final String message;

		private Code(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.detailNumber = detailNumber;
			this.category = category;
		}

		public Category getCategory() {
			return category;
		}

		public String getMessage() {
			return message;
		}

		public int getNumber() {
			return detailNumber;
		}
	}

	private static final Component DATA_COMPONENT = new Component() {
		public String getAbbreviation() {
			return STR_COMPONENT;
		}
	};

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static final long serialVersionUID = -1114834069849112275L;

	private static final String STR_COMPONENT = "DTA";

	/**
	 * Initializes a new {@link DataException}
	 * 
	 * @param cause
	 *            The cause
	 */
	public DataException(final AbstractOXException cause) {
		super(cause);
	}

	/**
	 * Initializes a new {@link DataException}
	 * 
	 * @param code
	 *            The data error code
	 */
	public DataException(final Code code) {
		this(code, null, EMPTY_ARGS);
	}

	/**
	 * Initializes a new {@link DataException}
	 * 
	 * @param code
	 *            The data error code
	 * @param messageArgs
	 *            The message arguments
	 */
	public DataException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	/**
	 * Initializes a new {@link DataException}
	 * 
	 * @param code
	 *            The data error code
	 * @param cause
	 *            The init cause
	 * @param messageArgs
	 *            The message arguments
	 */
	public DataException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(DATA_COMPONENT, code.getCategory(), code.getNumber(), code.getMessage(), cause);
		super.setMessageArgs(messageArgs);
	}
}
