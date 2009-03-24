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

package com.openexchange.contacts.ldap.exceptions;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;


/**
 * An exception class for all non-config-related exceptions
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class LdapException extends OXException {

    /**
     * Error codes
     * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
     */
    public enum Code {
        /**
         * LDAP contacts cannot be deleted
         */
        DELETE_NOT_POSSIBLE("LDAP contacts cannot be deleted", Category.PERMISSION, 1),

        /**
         * LDAP contacts cannot be deleted
         */
        ERROR_GETTING_ATTRIBUTE("An error occured while trying to read an LDAP attribute: %s", Category.INTERNAL_ERROR, 2),

        /**
         * The given value "%s" is not possible for a sort field
         */
        SORT_FIELD_NOT_POSSIBLE("The given value \"%s\" is not possible for a sort field", Category.INTERNAL_ERROR, 3),
    
        /**
         * Contacts cannot be inserted in LDAP
         */
        INSERT_NOT_POSSIBLE("Contacts cannot be inserted in LDAP", Category.PERMISSION, 4),
        
        /**
         * The folderid object is null. This is an internal error. Please notify Open-Xchange
         */
        FOLDERID_OBJECT_NULL("The folderid object is null. This is an internal error. Please notify Open-Xchange", Category.INTERNAL_ERROR, 5),
        
        /**
         * The search object contains more than one folder id. This is not supported by this implementation
         */
        TOO_MANY_FOLDERS("The search object contains more than one folder id. This is not supported by this implementation", Category.INTERNAL_ERROR, 6),

        /**
         * The mapping table doesn't contain the string uid %s, so it has never been accessed before
         */
        NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND("The mapping table doesn't contain the long uid %s, so it has never been accessed before", Category.CODE_ERROR, 7),

        /**
         * Multi-values are not allowed for date attribute: %s
         */
        MULTIVALUE_NOT_ALLOWED_DATE("Multi-values are not allowed for date attribute: %s", Category.CODE_ERROR, 8),

        /**
         * Multi-values are not allowed for int attribute: %s
         */
        MULTIVALUE_NOT_ALLOWED_INT("Multi-values are not allowed for int attribute: %s", Category.CODE_ERROR, 9),

        /**
         * Error while trying to create connection to LDAP server: %s
         */
        INITIAL_LDAP_ERROR("Error while trying to create connection to LDAP server: %s", Category.CODE_ERROR, 10),
        
        /**
         * The LDAP search for the user contains too many results
         */
        TOO_MANY_USER_RESULTS("The LDAP search for the user contains too many results", Category.CODE_ERROR, 11),

        /**
         * The LDAP search for the user object contains no results
         */
        NO_USER_RESULTS("The LDAP search for the user object contains no results", Category.CODE_ERROR, 12);

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
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final int detailNumber) {
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

    /**
     * 
     */
    private static final long serialVersionUID = 3828591312217664226L;

    public LdapException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }
    
    /**
     * Initializes a new exception using the information provided by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public LdapException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(EnumComponent.PERMISSION, code.category, code.number,
            null == code.message ? cause.getMessage() : code.message, cause);
        setMessageArgs(messageArgs);
    }
    
    /**
     * Constructor with all parameters.
     * @param component Component.
     * @param category Category.
     * @param number detail number.
     * @param message message of the exception.
     * @param cause the cause.
     * @param messageArgs arguments for the exception message.
     */
    public LdapException(final EnumComponent component, final Category category,
        final int number, final String message, final Throwable cause, final Object... messageArgs) {
        super(component, category, number, message, cause);
        super.setMessageArgs(messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the cause.
     * 
     * @param cause
     *            the cause of the exception.
     */
    public LdapException(final AbstractOXException cause) {
        super(cause);
    }

}
