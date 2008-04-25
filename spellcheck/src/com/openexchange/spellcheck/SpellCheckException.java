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

package com.openexchange.spellcheck;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link SpellCheckException}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpellCheckException extends AbstractOXException {

	private static final long serialVersionUID = -4936647856374250642L;

	public static enum Code {

		/**
		 * Spell check property '%s' not specified in configuration.
		 */
		MISSING_PROPERTY("Spell check property '%s' not specified in configuration.", Category.SETUP_ERROR, 1),
		/**
		 * Spell check directory '%s' not found or is not a directory
		 */
		MISSING_DIR("Spell check directory '%s' not found or is not a directory", Category.SETUP_ERROR, 2),
		/**
		 * Only one phonetic file is allowed per locale
		 */
		ONLY_ONE_PHON_FILE("Only one phonetic file is allowed per locale", Category.SETUP_ERROR, 3),
		/**
		 * At least one word list file per locale
		 */
		AT_LEAST_ONE_WL_FILE("At least one word list file per locale", Category.SETUP_ERROR, 4),
		/**
		 * An I/O error occurred: %s
		 */
		IO_ERROR("An I/O error occurred: %s", Category.CODE_ERROR, 5),
		/**
		 * No locale directory found
		 */
		NO_LOCALE_FOUND("No locale directory found", Category.SETUP_ERROR, 6),
		/**
		 * No dictionary available for locale %s
		 */
		MISSING_LOCALE_DIC("No dictionary available for locale %s", Category.SETUP_ERROR, 7),
		/**
		 * A SQL error occurred: %s
		 */
		SQL_ERROR("A SQL error occurred: %s", Category.CODE_ERROR, 8),
		/**
		 * Invalid format of user dictionary: %s
		 */
		INVALID_FORMAT("Invalid format of user dictionary: %s", Category.CODE_ERROR, 9),
		/**
		 * Spell check servlet cannot be registered: %s
		 */
		SERVLET_REGISTRATION_FAILED("Spell check servlet cannot be registered: %s", Category.CODE_ERROR, 10),
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAM("Missing parameter %s", Category.CODE_ERROR, 11),
		/**
		 * Unsupported value parameter %s: %s
		 */
		UNSUPPORTED_PARAM("Unsupported value parameter %s: %s", Category.CODE_ERROR, 12),
		/**
		 * A JSON error occurred: %s
		 */
		JSON_ERROR("A JSON error occurred: %s", Category.CODE_ERROR, 12),
		/**
		 * Invalid locale string: %s
		 */
		INVALID_LOCALE_STR("Invalid locale string: %s", Category.CODE_ERROR, 13);

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

	private static final Object[] EMPTY_ARGS = new Object[0];

	/**
	 * Initializes a new {@link SpellCheckException}
	 * 
	 * @param cause
	 *            The cause
	 */
	public SpellCheckException(final AbstractOXException cause) {
		super(cause);
	}

	/**
	 * Initializes a new {@link SpellCheckException}
	 * 
	 * @param code
	 *            The service error code
	 */
	public SpellCheckException(final Code code) {
		this(code, null, EMPTY_ARGS);
	}

	/**
	 * Initializes a new {@link SpellCheckException}
	 * 
	 * @param code
	 *            The service error code
	 * @param messageArgs
	 *            The message arguments
	 */
	public SpellCheckException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	/**
	 * Initializes a new {@link SpellCheckException}
	 * 
	 * @param code
	 *            The service error code
	 * @param cause
	 *            The init cause
	 * @param messageArgs
	 *            The message arguments
	 */
	public SpellCheckException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(EnumComponent.SERVICE, code.getCategory(), code.getNumber(), code.getMessage(), cause);
		super.setMessageArgs(messageArgs);
	}
}
