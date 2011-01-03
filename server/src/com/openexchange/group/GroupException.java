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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.group;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * This exception is used if problems occur in the groups component.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GroupException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -6738209089912459855L;

    /**
     * Initializes a new {@link GroupException}
     * 
     * @param cause The cause
     */
    public GroupException(final AbstractOXException cause) {
    	super(cause);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param component the component.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public GroupException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param component the component.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public GroupException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(EnumComponent.GROUP, code.category, code.detailNumber,
            code.message, cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for the ldap exception.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * A database connection Cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, 1),
        /**
         * SQL Problem: "%1$s".
         */
        SQL_ERROR("SQL Problem: \"%1$s\"", Category.CODE_ERROR,
            2),
        /**
         * No group given.
         */
        NULL("No group given.", Category.CODE_ERROR, 3),
        /**
         * The mandatory field %1$s is not defined.
         */
        MANDATORY_MISSING("The mandatory field %1$s is not defined.", Category
            .USER_INPUT, 4),
        /**
         * The simple name contains this not allowed characters: "%s".
         */
        NOT_ALLOWED_SIMPLE_NAME("The simple name contains this not allowed "
            + "characters: \"%1$s\".", Category.USER_INPUT, 5),
        /**
         * Another group with same identifier name exists: %1$d.
         */
        DUPLICATE("Another group with same identifier name exists: %1$d.",
            Category.USER_INPUT, 6),
        /**
         * Group contains a not existing member %1$d.
         */
        NOT_EXISTING_MEMBER("Group contains a not existing member %1$d.", Category
            .USER_INPUT, 7),
        /**
         * Group contains invalid data: "%1$s".
         */
        INVALID_DATA("Group contains invalid data: \"%1$s\".", Category
            .USER_INPUT, 8),
        /**
         * You are not allowed to create groups.
         */
        NO_CREATE_PERMISSION("You are not allowed to create groups.", Category
            .PERMISSION, 9),
        /**
         * Edit Conflict. Your change cannot be completed because somebody else
         * has made a conflicting change to the same item. Please refresh or
         * synchronize and try again.
         */
        MODIFIED("Edit Conflict. Your change cannot be completed because "
            + "somebody else has made a conflicting change to the same item. "
            + "Please refresh or synchronize and try again.", Category
            .CONCURRENT_MODIFICATION, 10),
        /**
         * You are not allowed to change groups.
         */
        NO_MODIFY_PERMISSION("You are not allowed to change groups.", Category
            .PERMISSION, 11),
        /**
         * You are not allowed to delete groups.
         */
        NO_DELETE_PERMISSION("You are not allowed to delete groups.", Category
            .PERMISSION, 12),
        /**
         * Group "%1$s" can not be deleted.
         */
        NO_GROUP_DELETE("Group \"%1$s\" can not be deleted.", Category
            .USER_INPUT, 13),
        /**
         * Group "%1$s" can not be changed.
         */
        NO_GROUP_UPDATE("Group \"%1$s\" can not be changed.", Category
            .USER_INPUT, 14);

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
        private Code(final String message, final Category category,
            final int detailNumber) {
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
