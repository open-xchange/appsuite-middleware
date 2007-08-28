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

package com.openexchange.groupware.reminder;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * ReminderException
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class ReminderException extends OXException {
	
	/**
     * For serialization.
     */
    private static final long serialVersionUID = 3162824095925586553L;

    public ReminderException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}
	
	public ReminderException(final Code code, final Throwable throwable,
        final Object... messageArgs) {
		super(Component.REMINDER, code.category, code.detailNumber,
            code.message, throwable);
		setMessageArgs(messageArgs);
	}
	
	public enum Code {
		MANDATORY_FIELD_USER("Required value \"user\" was not supplied.", 1,
            AbstractOXException.Category.CODE_ERROR),
		MANDATORY_FIELD_TARGET_ID(
            "Required value \"End Date\" was not supplied.", 2,
            AbstractOXException.Category.CODE_ERROR),
		MANDATORY_FIELD_ALARM("Required value \"Title\" was not supplied.", 3,
            AbstractOXException.Category.CODE_ERROR),
		INSERT_EXCEPTION("Unable to insert reminder", 4,
            AbstractOXException.Category.CODE_ERROR),
		UPDATE_EXCEPTION("Unable to update reminder", 5,
            AbstractOXException.Category.CODE_ERROR),
		DELETE_EXCEPTION("Unable to delete reminder", 6,
            AbstractOXException.Category.CODE_ERROR),
		LOAD_EXCEPTION("Unable to load reminder", 7,
            AbstractOXException.Category.CODE_ERROR),
		LIST_EXCEPTION("Unable to list reminder", 8,
            AbstractOXException.Category.CODE_ERROR),
        NOT_FOUND("Cannot find reminder (identifier %d). Context %d.", 9,
            Category.CODE_ERROR);
		
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
		 * @param message message.
		 * @param category category.
		 * @param detailNumber detail number.
		 */
		private Code(final String message,
				final int detailNumber,
				final Category category)  {
			this.message = message;
			this.category = category;
			this.detailNumber = detailNumber;
		}

		public Category getCategory() {
			return category;
		}

		public int getDetailNumber() {
			return detailNumber;
		}

		public String getMessage() {
			return message;
		}
	}
}
