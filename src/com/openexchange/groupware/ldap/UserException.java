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

package com.openexchange.groupware.ldap;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.ldap.LdapException.Code;
import com.openexchange.groupware.ldap.LdapException.Detail;

/**
 * This exception is used if problems occur in the user storage component.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UserException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -5409503564218823806L;

    /**
     * Detailed information for this exception.
     */
    private final Detail detail;

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public UserException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public UserException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(Component.USER, code.category, code.detailNumber, code.message,
            cause);
        this.detail = code.detail;
        setMessageArgs(messageArgs);
    }

    /**
     * Initialize e new exception using the information from the nested abstract
     * OX exception.
     * @param cause the cause.
     */
    public UserException(final LdapException cause) {
        super(cause);
        detail = Detail.ERROR;
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
    public enum Detail {
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
     * Error codes for the database pooling exception.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * A property from the ldap.properties file is missing.
         */
        PROPERTY_MISSING("Cannot find property %s.", Category.SETUP_ERROR,
            Detail.ERROR, 1),
        /**
         * A problem with distinguished names occurred.
         */
        DN_PROBLEM("Cannot build distinguished name from %s.",
            Category.CODE_ERROR, Detail.ERROR, 2),
        /**
         * Class can not be found.
         */
        CLASS_NOT_FOUND("Class %s can not be loaded.", Category.SETUP_ERROR,
            Detail.ERROR, 3),
        /**
         * An implementation can not be instanciated.
         */
        INSTANCIATION_PROBLEM("Cannot instanciate class %s.",
            Category.SETUP_ERROR, Detail.ERROR, 4),
        /**
         * A database connection cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, Detail.ERROR, 5),
        /**
         * Cannot clone object %1$s.
         */
        NOT_CLONEABLE("Cannot clone object %1$s.", Category.CODE_ERROR,
            Detail.ERROR, 6),
        /**
         * SQL Problem: \"%s\".
         */
        SQL_ERROR("SQL Problem: \"%s\".", Category.CODE_ERROR,
            Detail.ERROR, 7),
        /**
         * Hash algorithm %s isn't found.
         */
        HASHING("Hash algorithm %s isn't found.", Category.CODE_ERROR, Detail
            .ERROR, 8),
        /**
         * Encoding %s cannot be used.
         */
        UNSUPPORTED_ENCODING("Encoding %s cannot be used.", Category.CODE_ERROR,
            Detail.ERROR, 9),
        /**
         * Cannot find user with identifier %1$s in context %2$d.
         */
        USER_NOT_FOUND("Cannot find user with identifier %1$s in context %2$d.",
            Category.CODE_ERROR, Detail.NOT_FOUND, 10),
        /**
         * Found two user with same identifier %1$s in context %2$d.
         */
        USER_CONFLICT("Found two user with same identifier %1$s in context "
            + "%2$d.", Category.CODE_ERROR, Detail.ERROR, 11),
        /**
         * Problem putting an object into the cache.
         */
        CACHE_PROBLEM("Problem putting/removing an object into/from the cache.",
            Category.CODE_ERROR, Detail.ERROR, 12);

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
        }

		public Category getCategory() {
			return category;
		}

		public Detail getDetail() {
			return detail;
		}

		public int getDetailNumber() {
			return detailNumber;
		}

		public String getMessage() {
			return message;
		}
    }
}
