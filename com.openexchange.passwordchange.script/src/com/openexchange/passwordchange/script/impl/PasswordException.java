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

package com.openexchange.passwordchange.script.impl;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * This exception is used if specific problems occur in the password change script.
 * @author <a href="mailto:samuel.kvasnica@ims.co.at">Sameul Kvasnica</a>
 */
public class PasswordException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -5409503564218867467L;

    /**
     * Detailed information for this exception.
     */
    private final Detail detail;

    public PasswordException(final AbstractOXException cause) {
        super(cause);
        detail = Detail.ERROR;
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public PasswordException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public PasswordException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.USER, code.getCategory(), code.getDetailNumber(), code.getMessage(), cause);
        this.detail = code.getDetail();
        setMessageArgs(messageArgs);
    }

    /**
     * Initialize e new exception using the information from the nested abstract
     * OX exception.
     * @param cause the cause.
     */
    /*    public PasswordException(final OXException cause) {
        super(cause);
        detail = Detail.ERROR;
    }
     */
    /**
     * @return the detail
     */
    public Detail getDetail() {
        return detail;
    }

    /**
     * Detail information for the OXException.
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
    /**
     * {@link Code}
     *
     * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Cannot change password for any reason.
         */
        PASSWORD_FAILED("Cannot change password < %s >, see logfiles for details.", Category.PERMISSION, Detail.ERROR, 1),
        /**
         * New password too short.
         */
        PASSWORD_SHORT("New password is too short.", CATEGORY_USER_INPUT, Detail.ERROR, 2),
        /**
         * New password too weak.
         */
        PASSWORD_WEAK("New password is too weak.", CATEGORY_USER_INPUT, Detail.ERROR, 3),
        /**
         * User not found.
         */
        PASSWORD_NOUSER("Cannot find user.", CATEGORY_CONFIGURATION, Detail.ERROR, 4),
        /**
         * User not found.
         */
        LDAP_ERROR("LDAP error.", CATEGORY_CONFIGURATION, Detail.ERROR, 5),
        /**
         * A database connection cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.", CATEGORY_SERVICE_DOWN, Detail.ERROR, 6),

        /**
         * No permission to modify resources in context %1$s
         */
        PERMISSION("No permission to modify resources in context %1$s",
            Category.PERMISSION, Detail.ERROR, 7);

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
