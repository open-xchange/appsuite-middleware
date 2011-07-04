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

package com.openexchange.api;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link OXPermissionException}
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class OXPermissionException extends OXException {

    private static final long serialVersionUID = 8893780895314747686L;

    /**
     * Initializes a new {@link OXPermissionException}.
     * 
     * @param code The code for the exception
     * @param messageArgs The arguments that will be formatted into the message
     */
    public OXPermissionException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provided by the code.
     * 
     * @param code The code for the exception
     * @param cause The cause of the exception
     * @param messageArgs The arguments that will be formatted into the message
     */
    public OXPermissionException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(
            EnumComponent.PERMISSION,
            code.getCategory(),
            code.getNumber(),
            null == code.getMessage() ? cause.getMessage() : code.getMessage(),
            cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Constructor with all parameters.
     * 
     * @param component The component.
     * @param category The category.
     * @param number The detail number.
     * @param message The message of the exception.
     * @param cause The cause.
     * @param messageArgs The arguments for the exception message.
     */
    public OXPermissionException(final EnumComponent component, final Category category, final int number, final String message, final Throwable cause, final Object... messageArgs) {
        super(component, category, number, message, cause);
        super.setMessageArgs(messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the cause.
     * 
     * @param cause The cause of the exception.
     */
    public OXPermissionException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Error codes for permission exceptions.
     */
    public enum Code {
        /**
         * No permission for module: %s.
         */
        NoPermissionForModul("No permission for module: %s.", Category.USER_INPUT, 1),

        /**
         * No folder permission.
         */
        NoFolderPermission("No folder permission.", Category.PERMISSION, 2);

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
        private final int number;

        /**
         * Default constructor.
         * 
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.number = detailNumber;
        }

        public Category getCategory() {
            return category;
        }

        public String getMessage() {
            return message;
        }

        public int getNumber() {
            return number;
        }
    }
}
