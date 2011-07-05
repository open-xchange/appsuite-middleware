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
import com.openexchange.exception.OXExceptionFactory;

/**
 * This exception is used if problems occur in the ldap DAOs.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LdapException extends OXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -6738209089912459855L;

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
     * Detailed information for this exception.
     */
    private final Detail detail;

    protected LdapException(final int code, final String displayMessage, final Throwable cause, final Detail detail, final Object... displayArgs) {
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
     * Error codes for the ldap exception.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
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
         * A database connection Cannot be obtained.
         */
        NO_CONNECTION("Cannot get database connection.",
            Category.CATEGORY_SERVICE_DOWN, Detail.ERROR, 5),
        /**
         * SQL Problem: "%s".
         */
        SQL_ERROR("SQL Problem: \"%s\"", Category.CATEGORY_ERROR,
            Detail.ERROR, 6),
        /**
         * Problem putting an object into the cache.
         */
        CACHE_PROBLEM("Problem putting/removing an object into/from the cache.",
            Category.CATEGORY_ERROR, Detail.ERROR, 7),
        /**
         * Hash algorithm %s isn't found.
         */
        HASH_ALGORITHM("Hash algorithm %s isn't found.",
            Category.CATEGORY_ERROR, Detail.ERROR, 8),
        /**
         * Encoding %s cannot be used.
         */
        UNSUPPORTED_ENCODING("Encoding %s cannot be used.",
            Category.CATEGORY_ERROR, Detail.ERROR, 9),
        /**
         * Cannot find resource group with identifier %d.
         */
        RESOURCEGROUP_NOT_FOUND(
            "Cannot find resource group with identifier %d.",
            Category.CATEGORY_ERROR, Detail.ERROR, 10),
        /**
         * Found resource groups with same identifier %d.
         */
        RESOURCEGROUP_CONFLICT("Found resource groups with same identifier %d.",
            Category.CATEGORY_ERROR, Detail.ERROR, 11),
        /**
         * Cannot find resource with identifier %d.
         */
        RESOURCE_NOT_FOUND("Cannot find resource with identifier %d.",
            Category.CATEGORY_ERROR, Detail.ERROR, 12),
        /**
         * Found resources with same identifier %d.
         */
        RESOURCE_CONFLICT("Found resources with same identifier %d.",
            Category.CATEGORY_ERROR, Detail.ERROR, 13),
        /**
         * Cannot find user with email %s.
         */
        NO_USER_BY_MAIL("Cannot find user with email %s.",
            Category.CATEGORY_ERROR, Detail.ERROR, 14),
        /**
         * Cannot find user with identifier %1$s in context %2$d.
         */
        USER_NOT_FOUND("Cannot find user with identifier %1$s in context %2$d.",
            Category.CATEGORY_ERROR, Detail.NOT_FOUND, 15),
        /**
         * Cannot find group with identifier %1$s in context %2$d.
         */
        GROUP_NOT_FOUND(
            "Cannot find group with identifier %1$s in context %2$d.",
            Category.CATEGORY_ERROR, Detail.ERROR, 17),
        /**    
         * Unexpected error: %1$s
         */
        UNEXPECTED_ERROR(
            "Unexpected error: %1$s",
            Category.CATEGORY_ERROR, Detail.ERROR, 18);

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
         * @param detail detailed information for the exception.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final Detail detail, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
            this.detail = detail;
            display = category.getLogLevel().implies(LogLevel.DEBUG);
        }

        public String getPrefix() {
            return null;
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
		
		public boolean equals(final OXException e) {
	        return getPrefix().equals(e.getPrefix()) && e.getCode() == getNumber();
	    }

	    /**
	     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
	     * 
	     * @return The newly created {@link OXException} instance
	     */
	    public OXException create() {
	        return OXExceptionFactory.getInstance().create(this, new Object[0]);
	    }

	    /**
	     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
	     * 
	     * @param args The message arguments in case of printf-style message
	     * @return The newly created {@link OXException} instance
	     */
	    public OXException create(final Object... args) {
	        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
	    }

	    /**
	     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
	     * 
	     * @param cause The optional initial cause
	     * @param args The message arguments in case of printf-style message
	     * @return The newly created {@link OXException} instance
	     */
	    public OXException create(final Throwable cause, final Object... args) {
	        return OXExceptionFactory.getInstance().create(this, cause, args);
	    }
    }
}
