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


public class LdapConfigurationException extends OXException {

    /**
     * Error codes for permission exceptions.
     * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
     */
    public enum Code {
        /**
         * The given value for authtype "%s" is not a possible one
         */
        AUTH_TYPE_WRONG("The given value for authtype \"%s\" is not a possible one", Category.SETUP_ERROR, 1),

        /**
         * The given value for authtype "%s" is not a possible one
         */
        SORTING_WRONG("The given value for sorting \"%s\" is not a possible one", Category.SETUP_ERROR, 2),

        /**
         * The given value for searchScope "%s" is not a possible one
         */
        SEARCH_SCOPE_WRONG("The given value for searchScope \"%s\" is not a possible one", Category.SETUP_ERROR, 3),

        /**
         * The parameter "%s" is not set in the property file
         */
        PARAMETER_NOT_SET("The parameter \"%s\" is not set in property file \"%s\"", Category.SETUP_ERROR, 4),

        /**
         * Value for context "%s" is no integer value
         */
        NO_INTEGER_VALUE("Value for context \"%s\" is no integer value", Category.SETUP_ERROR, 5),

        /**
         * Mapping file %s not valid
         */
        INVALID_MAPPING_FILE("Mapping file \"%s\" not valid", Category.SETUP_ERROR, 6),
        
        /**
         * The given value for pagesize "%s" is no integer value
         */
        INVALID_PAGESIZE("The given value for pagesize \"%s\" is no integer value", Category.SETUP_ERROR, 7),

        /**
         * The given value for userauthtype "%s" is not a possible one
         */
        USER_AUTH_TYPE_WRONG("The given value for userauthtype \"%s\" is not a possible one", Category.SETUP_ERROR, 8),

        /**
         * The given value for userSearchScope "%s" is not a possible one
         */
        USER_SEARCH_SCOPE_WRONG("The given value for userSearchScope \"%s\" is not a possible one", Category.SETUP_ERROR, 9);

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

    public LdapConfigurationException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }
    
    /**
     * Initializes a new exception using the information provided by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public LdapConfigurationException(final Code code, final Throwable cause,
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
    public LdapConfigurationException(final EnumComponent component, final Category category,
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
    public LdapConfigurationException(final AbstractOXException cause) {
        super(cause);
    }

}
