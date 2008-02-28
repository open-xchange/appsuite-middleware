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

package com.openexchange.caching;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link CacheException} - Represents a cache exception occurred in underlying
 * caching system
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CacheException extends AbstractOXException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 4444285314027442420L;

	public static enum Code {

		/**
		 * A cache error occurred: %s
		 */
		CACHE_ERROR("A cache error occurred: %s", Category.CODE_ERROR, 1),
		/**
		 * Missing cache config file at location: %s
		 */
		MISSING_CACHE_CONFIG_FILE("Missing cache config file at location: %s", Category.SETUP_ERROR, 2),
		/**
		 * An I/O error occurred: %s
		 */
		IO_ERROR("An I/O error occurred: %s", Category.CODE_ERROR, 3),
		/**
		 * Missing configuration property: %s
		 */
		MISSING_CONFIGURATION_PROPERTY("Missing configuration property: %s", Category.SETUP_ERROR, 4),
		/**
		 * The default element attributes could not be retrieved
		 */
		FAILED_ATTRIBUTE_RETRIEVAL("The default element attributes could not be retrieved", Category.CODE_ERROR, 5),
		/**
		 * A put into the cache failed.
		 */
		FAILED_PUT("Put into cache failed.", Category.CODE_ERROR, 6),
		/**
		 * Safe put into cache failed. An object bound to given key already
		 * exists.
		 */
		FAILED_SAFE_PUT("Safe put into cache failed. An object bound to given key already exists.",
				Category.CODE_ERROR, 7),
		/**
		 * Remove on cache failed
		 */
		FAILED_REMOVE("Remove on cache failed", Category.CODE_ERROR, 8),
		/**
		 * The default element attributes could not be assigned
		 */
		FAILED_ATTRIBUTE_ASSIGNMENT("The default element attributes could not be assigned", Category.CODE_ERROR, 5);

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
	 * Initializes a new {@link CacheException}
	 * 
	 * @param cause
	 *            The cause
	 */
	public CacheException(final AbstractOXException cause) {
		super(cause);
	}

	/**
	 * Initializes a new {@link CacheException}
	 * 
	 * @param code
	 *            The service error code
	 */
	public CacheException(final Code code) {
		this(code, null, EMPTY_ARGS);
	}

	/**
	 * Initializes a new {@link CacheException}
	 * 
	 * @param code
	 *            The service error code
	 * @param messageArgs
	 *            The message arguments
	 */
	public CacheException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	/**
	 * Initializes a new {@link CacheException}
	 * 
	 * @param code
	 *            The service error code
	 * @param cause
	 *            The init cause
	 * @param messageArgs
	 *            The message arguments
	 */
	public CacheException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.CACHE, code.getCategory(), code.getNumber(), code.getMessage(), cause);
		super.setMessageArgs(messageArgs);
	}

}
