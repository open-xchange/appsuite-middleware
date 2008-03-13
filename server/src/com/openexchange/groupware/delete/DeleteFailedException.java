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

package com.openexchange.groupware.delete;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link DeleteFailedException} - Thrown if a delete event cannot be performed
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class DeleteFailedException extends AbstractOXException {

	private static final long serialVersionUID = -5601390102811112914L;

	public static enum Code {

		/**
		 * Unknown delete event type: %d
		 */
		UNKNOWN_TYPE("Unknown delete event type: %d", Category.CODE_ERROR, 1),
		/**
		 * A SQL error occurred: %s
		 */
		SQL_ERROR("A SQL error occurred: %s", Category.CODE_ERROR, 2),
		/**
		 * An error occurred: %s
		 */
		ERROR("An error occurred: %s", Category.CODE_ERROR, 3);

		private final String message;

		private final Category category;

		private final int detailNumber;

		private Code(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.category = category;
			this.detailNumber = detailNumber;
		}

		public final Category getCategory() {
			return category;
		}

		public final int getDetailNumber() {
			return detailNumber;
		}

		public final String getMessage() {
			return message;
		}
	}

	/**
	 * Initializes a new {@link DeleteFailedException} from specified cause
	 * exception
	 * 
	 * @param cause
	 *            The cause exception
	 */
	public DeleteFailedException(final AbstractOXException cause) {
		super(cause);
	}

	private static final transient Object[] EMPTY_ARGS = new Object[0];

	/**
	 * Initializes a new {@link DeleteFailedException}
	 * 
	 * @param code
	 *            The error code
	 */
	public DeleteFailedException(final DeleteFailedException.Code code) {
		this(code, null, EMPTY_ARGS);
	}

	/**
	 * Initializes a new {@link DeleteFailedException}
	 * 
	 * @param code
	 *            The error code
	 * @param messageArgs
	 *            The message arguments
	 */
	public DeleteFailedException(final DeleteFailedException.Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	/**
	 * Initializes a new {@link DeleteFailedException}
	 * 
	 * @param code
	 *            The error code
	 * @param cause
	 *            The cause exception
	 * @param messageArgs
	 *            The message arguments
	 */
	public DeleteFailedException(final DeleteFailedException.Code code, final Throwable cause,
			final Object... messageArgs) {
		super(Component.DELETE_EVENT, code.getCategory(), code.getDetailNumber(), code.getMessage(), cause);
		setMessageArgs(messageArgs);
	}

}
