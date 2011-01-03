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

package com.openexchange.resource;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link ResourceException} - This exception is used if problems occur in the resource component.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ResourceException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -6738209089912459855L;

    /**
     * Detail information for the LdapException.
     * 
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
     * Detailed information for this exception.
     */
    private final Detail detail;

    /**
     * Initializes a new {@link ResourceException}
     * 
     * @param cause The cause
     */
    public ResourceException(final AbstractOXException cause) {
        super(cause);
        detail = Detail.ERROR;
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param component the component.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public ResourceException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param component the component.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public ResourceException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.RESOURCE, code.category, code.detailNumber, code.message, cause);
        detail = code.detail;
        setMessageArgs(messageArgs);
    }

    /**
     * @return the detail
     */
    public Detail getDetail() {
        return detail;
    }

    /**
     * Error codes for the ldap exception.
     * 
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * A database connection Cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.", Category.SUBSYSTEM_OR_SERVICE_DOWN, Detail.ERROR, 1),
        /**
         * SQL Problem: "%1$s".
         */
        SQL_ERROR("SQL Problem: \"%1$s\"", Category.CODE_ERROR, Detail.ERROR, 2),
        /**
         * Cannot find resource group with identifier %1$d.
         */
        RESOURCEGROUP_NOT_FOUND("Cannot find resource group with identifier %1$d.", Category.CODE_ERROR, Detail.ERROR, 3),
        /**
         * Found resource groups with same identifier %1$d.
         */
        RESOURCEGROUP_CONFLICT("Found resource groups with same identifier %1$d.", Category.CODE_ERROR, Detail.ERROR, 4),
        /**
         * Cannot find resource with identifier %1$d.
         */
        RESOURCE_NOT_FOUND("Cannot find resource with identifier %1$d.", Category.CODE_ERROR, Detail.ERROR, 5),
        /**
         * Found resource(s) with same identifier %1$s.
         */
        RESOURCE_CONFLICT("Found resource(s) with same identifier %1$s.", Category.CODE_ERROR, Detail.ERROR, 6),
        /**
         * No resource given.
         */
        NULL("No resource given.", Category.CODE_ERROR, Detail.ERROR, 7),
        /**
         * Missing mandatory field(s) in given resource.
         */
        MANDATORY_FIELD("Missing mandatory field(s) in given resource.", Category.CODE_ERROR, Detail.ERROR, 8),
        /**
         * No permission to modify resources in context %1$s
         */
        PERMISSION("No permission to modify resources in context %1$s", Category.PERMISSION, Detail.ERROR, 9),
        /**
         * Found resource(s) with same email address %1$s.
         */
        RESOURCE_CONFLICT_MAIL("Found resource(s) with same email address %1$s.", Category.CODE_ERROR, Detail.ERROR, 10),
        /**
         * Invalid resource identifier: %1$s
         */
        INVALID_RESOURCE_IDENTIFIER("Invalid resource identifier: %1$s", Category.USER_INPUT, Detail.ERROR, 11),
        /**
         * Invalid resource email address: %1$s
         */
        INVALID_RESOURCE_MAIL("Invalid resource email address: %1$s", Category.USER_INPUT, Detail.ERROR, 12),
        /**
         * The resource has been changed in the meantime
         */
        CONCURRENT_MODIFICATION("The resource has been changed in the meantime", Category.CONCURRENT_MODIFICATION, Detail.ERROR, 13);

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
         * 
         * @param message message.
         * @param category category.
         * @param detail detailed information for the exception.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category, final Detail detail, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
            this.detail = detail;
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
