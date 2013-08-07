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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public enum LdapExceptionCode implements OXExceptionCode {
    /**
     * LDAP contacts cannot be deleted
     */
    DELETE_NOT_POSSIBLE(LdapConfigurationExceptionMessage.DELETE_NOT_POSSIBLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1),

    /**
     * An error occured while trying to read an LDAP attribute: %s
     */
    ERROR_GETTING_ATTRIBUTE(LdapConfigurationExceptionMessage.ERROR_GETTING_ATTRIBUTE_MSG, Category.CATEGORY_ERROR, 2),

    /**
     * The given value "%s" is not possible for a sort field
     */
    SORT_FIELD_NOT_POSSIBLE(LdapConfigurationExceptionMessage.SORT_FIELD_NOT_POSSIBLE_MSG, Category.CATEGORY_ERROR, 3),

    /**
     * Contacts cannot be inserted in LDAP
     */
    INSERT_NOT_POSSIBLE(LdapConfigurationExceptionMessage.INSERT_NOT_POSSIBLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 4),

    /**
     * The folderid object is null. This is an internal error. Please notify Open-Xchange
     */
    FOLDERID_OBJECT_NULL(LdapConfigurationExceptionMessage.FOLDERID_OBJECT_NULL_MSG, Category.CATEGORY_ERROR, 5),

    /**
     * The search object contains more than one folder id. This is not supported by this implementation
     */
    TOO_MANY_FOLDERS(LdapConfigurationExceptionMessage.TOO_MANY_FOLDERS_MSG, Category.CATEGORY_ERROR, 6),

    /**
     * The mapping table doesn't contain the string uid %s, so it has never been accessed before
     */
    NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND(LdapConfigurationExceptionMessage.NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND_MSG, Category.CATEGORY_ERROR, 7),

    /**
     * Multi-values are not allowed for date attribute: %s
     */
    MULTIVALUE_NOT_ALLOWED_DATE(LdapConfigurationExceptionMessage.MULTIVALUE_NOT_ALLOWED_DATE_MSG, Category.CATEGORY_ERROR, 8),

    /**
     * Multi-values are not allowed for int attribute: %s
     */
    MULTIVALUE_NOT_ALLOWED_INT(LdapConfigurationExceptionMessage.MULTIVALUE_NOT_ALLOWED_INT_MSG, Category.CATEGORY_ERROR, 9),

    /**
     * Error while trying to create connection to LDAP server: %s
     */
    INITIAL_LDAP_ERROR(LdapConfigurationExceptionMessage.INITIAL_LDAP_ERROR_MSG, Category.CATEGORY_ERROR, 10),

    /**
     * The LDAP search for the user contains too many results
     */
    TOO_MANY_USER_RESULTS(LdapConfigurationExceptionMessage.TOO_MANY_USER_RESULTS_MSG, Category.CATEGORY_ERROR, 11),

    /**
     * The LDAP search for the user object "%s" gave no results
     */
    NO_USER_RESULTS(LdapConfigurationExceptionMessage.NO_USER_RESULTS_MSG, Category.CATEGORY_ERROR, 12),

    /**
     * An error occurred while trying to get the user object from the database
     */
    ERROR_GETTING_USER_Object(LdapConfigurationExceptionMessage.ERROR_GETTING_USER_Object_MSG, Category.CATEGORY_ERROR, 13),

    /**
     * The given userLoginSource is not possible: %s
     */
    GIVEN_USER_LOGIN_SOURCE_NOT_FOUND(LdapConfigurationExceptionMessage.GIVEN_USER_LOGIN_SOURCE_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 14),

    /**
     * The imap login for user "%s" is null
     */
    IMAP_LOGIN_NULL(LdapConfigurationExceptionMessage.IMAP_LOGIN_NULL_MSG, Category.CATEGORY_CONFIGURATION, 15),

    /**
     * The primary mail for user "%s" is null
     */
    PRIMARY_MAIL_NULL(LdapConfigurationExceptionMessage.PRIMARY_MAIL_NULL_MSG, Category.CATEGORY_CONFIGURATION, 16),

    /**
     * The E-Mail address "%s" for distributionentry is invalid
     */
    MAIL_ADDRESS_DISTRI_INVALID(LdapConfigurationExceptionMessage.MAIL_ADDRESS_DISTRI_INVALID_MSG, Category.CATEGORY_CONFIGURATION, 17),

    /**
     * The contact type "%s" is not known
     */
    UNKNOWN_CONTACT_TYPE(LdapConfigurationExceptionMessage.UNKNOWN_CONTACT_TYPE_MSG, Category.CATEGORY_CONFIGURATION, 18),

    /**
     * The attribute "%s" is missing for object "%s"
     */
    MISSING_ATTRIBUTE(LdapConfigurationExceptionMessage.MISSING_ATTRIBUTE_MSG, Category.CATEGORY_CONFIGURATION, 19),

    /**
     * No values mapping table found
     */
    NO_VALUES_MAPPING_TABLE_FOUND(LdapConfigurationExceptionMessage.NO_VALUES_MAPPING_TABLE_FOUND_MSG, Category.CATEGORY_ERROR, 20),

    /**
     * No keys mapping table found
     */
    NO_KEYS_MAPPING_TABLE_FOUND(LdapConfigurationExceptionMessage.NO_KEYS_MAPPING_TABLE_FOUND_MSG, Category.CATEGORY_ERROR, 21),

    /**
     * An error occurred while trying to get the defaultNamingContext attribute
     */
    ERROR_GETTING_DEFAULT_NAMING_CONTEXT(LdapConfigurationExceptionMessage.ERROR_GETTING_DEFAULT_NAMING_CONTEXT_MSG, Category.CATEGORY_ERROR, 22),

    /**
     * The contact object with id "%s" could not be found in folder "%s"
     */
    CONTACT_NOT_FOUND(LdapConfigurationExceptionMessage.CONTACT_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 23),

    ;



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
    private LdapExceptionCode(final String message, final Category category,
        final int detailNumber) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "LDAP";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
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
