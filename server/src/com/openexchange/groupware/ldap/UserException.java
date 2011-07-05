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

package com.openexchange.groupware.ldap;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionStrings;

/**
 * This exception is used if problems occur in the user storage component.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UserException extends OXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -5409503564218823806L;

    /**
     * Detailed information for this exception.
     */
    private final Detail detail;

    protected UserException(final int code, final String displayMessage, final Throwable cause, final Detail detail, final Object... displayArgs) {
        super(code, displayMessage, cause, displayArgs);
        this.detail = detail;
    }

    /**
     * @return the detail
     */
    public Detail getDetail() {
        return detail;
    }

    /**
     * Detail information for the LdapException.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public static enum Detail {
        /**
         * The requested data can not be found.
         */
        NOT_FOUND,
        /**
         * Internal error.
         */
        ERROR
    }

    /**
     * {@link Code} - The user error codes.
     *
     * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
     */
    public static enum Code implements OXExceptionCode {
        /**
         * A property from the ldap.properties file is missing.
         */
        PROPERTY_MISSING("Cannot find property %s.", Category.CATEGORY_CONFIGURATION,
            Detail.ERROR, 1),
        /**
         * A problem with distinguished names occurred.
         */
        DN_PROBLEM("Cannot build distinguished name from %s.",
            Category.CATEGORY_ERROR, Detail.ERROR, 2),
        /**
         * Class can not be found.
         */
        CLASS_NOT_FOUND("Class %s can not be loaded.", Category.CATEGORY_CONFIGURATION,
            Detail.ERROR, 3),
        /**
         * An implementation can not be instantiated.
         */
        INSTANTIATION_PROBLEM("Cannot instantiate class %s.",
            Category.CATEGORY_CONFIGURATION, Detail.ERROR, 4),
        /**
         * A database connection cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.",
            Category.CATEGORY_SERVICE_DOWN, Detail.ERROR, 5),
        /**
         * Cannot clone object %1$s.
         */
        NOT_CLONEABLE("Cannot clone object %1$s.", Category.CATEGORY_ERROR,
            Detail.ERROR, 6),
        /**
         * SQL Problem: \"%s\".
         */
        SQL_ERROR("SQL Problem: \"%s\".", Category.CATEGORY_ERROR,
            Detail.ERROR, 7),
        /**
         * Hash algorithm %s isn't found.
         */
        HASHING("Hash algorithm %s isn't found.", Category.CATEGORY_ERROR, Detail
            .ERROR, 8),
        /**
         * Encoding %s cannot be used.
         */
        UNSUPPORTED_ENCODING("Encoding %s cannot be used.", Category.CATEGORY_ERROR,
            Detail.ERROR, 9),
        /**
         * Cannot find user with identifier %1$s in context %2$d.
         */
        USER_NOT_FOUND("Cannot find user with identifier %1$s in context %2$d.",
            Category.CATEGORY_ERROR, Detail.NOT_FOUND, 10),
        /**
         * Found two user with same identifier %1$s in context %2$d.
         */
        USER_CONFLICT("Found two user with same identifier %1$s in context "
            + "%2$d.", Category.CATEGORY_ERROR, Detail.ERROR, 11),
        /**
         * Problem putting an object into the cache.
         */
        CACHE_PROBLEM("Problem putting/removing an object into/from the cache.",
            Category.CATEGORY_ERROR, Detail.ERROR, 12),
        /**
         * No CATEGORY_PERMISSION_DENIED to modify resources in context %1$s
         */
         CATEGORY_PERMISSION_DENIED("No CATEGORY_PERMISSION_DENIED to modify resources in context %1$s",
            Category.CATEGORY_PERMISSION_DENIED, Detail.ERROR, 13),
         /**
          * Missing or unknown password mechanism %1$s
          */
         MISSING_PASSWORD_MECH("Missing or unknown password mechanism %1$s", Category.CATEGORY_ERROR, Detail.ERROR, 14),
         /**
          * New password contains invalid characters
          */
         INVALID_PASSWORD("New password contains invalid characters", Category.CATEGORY_USER_INPUT, Detail.ERROR, 15),
         /**
          * Attributes of user %1$d in context %2$d have been erased.
          */
         ERASED_ATTRIBUTES("Attributes of user %1$d in context %2$d have been erased.", Category.CATEGORY_WARNING, Detail.ERROR, 16),
         /**
          * Loading one or more users failed.
          */
         LOAD_FAILED("Loading one or more users failed.", Category.CATEGORY_ERROR, Detail.ERROR, 17),
         /** Alias entries are missing for user %1$d in context %2$d. */
         ALIASES_MISSING("Alias entries are missing for user %1$d in context %2$d.", Category.CATEGORY_CONFIGURATION, Detail.ERROR, 18),
         /** Updating attributes failed in context %1$d for user %2$d. */
         UPDATE_ATTRIBUTES_FAILED("Updating attributes failed in context %1$d for user %2$d.", Category.CATEGORY_ERROR, Detail.ERROR, 19);

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
         * Detail information for the exception.
         */
        private final Detail detail;

        private final boolean display;

        /**
         * Default constructor.
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final Detail detail, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detail = detail;
            this.detailNumber = detailNumber;
            display = category.getLogLevel().implies(LogLevel.DEBUG);
        }

        public Category getCategory() {
            return category;
        }

        public Detail getDetail() {
            return detail;
        }

        public int getNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }
        
        public String getPrefix() {
            return "USR";
        }

        public boolean equals(final OXException e) {
            return getPrefix().equals(e.getPrefix()) && e.getCode() == getNumber();
        }
        
        /**
         * Creates a new {@link UserException} instance pre-filled with this code's attributes.
         * 
         * @return The newly created {@link UserException} instance
         */
        public UserException create() {
            return create(new Object[0]);
        }

        /**
         * Creates a new {@link UserException} instance pre-filled with this code's attributes.
         * 
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link UserException} instance
         */
        public UserException create(final Object... args) {
            return create((Throwable) null, args);
        }

        /**
         * Creates a new {@link UserException} instance pre-filled with this code's attributes.
         * 
         * @param cause The optional initial cause
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link UserException} instance
         */
        public UserException create(final Throwable cause, final Object... args) {
            final UserException ret;
            if (display) {
                ret = new UserException(detailNumber, message, cause, detail, args);
            } else {
                final String msg = Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE;
                ret = new UserException(detailNumber, msg, null, detail, new Object[0]);
                ret.setLogMessage(message, args);
            }
            ret.addCategory(category);
            ret.setPrefix(getPrefix());
            return ret;
        }
    }
}
