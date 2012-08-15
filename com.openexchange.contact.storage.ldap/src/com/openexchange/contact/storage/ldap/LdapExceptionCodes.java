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

package com.openexchange.contact.storage.ldap;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link LdapExceptionCodes}
 * 
 * Exception codes for the LDAP contact storage.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum LdapExceptionCodes implements OXExceptionCode {

    /**
     * LDAP contacts cannot be deleted
     */
    DELETE_NOT_POSSIBLE(LdapExceptionMessages.DELETE_NOT_POSSIBLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 1),

    /**
     * An error occured while trying to read an LDAP attribute: %s
     */
    ERROR_GETTING_ATTRIBUTE(LdapExceptionMessages.ERROR_GETTING_ATTRIBUTE_MSG, Category.CATEGORY_ERROR, 2),

    /**
     * The given value "%s" is not possible for a sort field
     */
    SORT_FIELD_NOT_POSSIBLE(LdapExceptionMessages.SORT_FIELD_NOT_POSSIBLE_MSG, Category.CATEGORY_ERROR, 3),

    /**
     * Contacts cannot be inserted in LDAP
     */
    INSERT_NOT_POSSIBLE(LdapExceptionMessages.INSERT_NOT_POSSIBLE_MSG, Category.CATEGORY_PERMISSION_DENIED, 4),

    /**
     * The folderid object is null. This is an internal error. Please notify Open-Xchange
     */
    FOLDERID_OBJECT_NULL(LdapExceptionMessages.FOLDERID_OBJECT_NULL_MSG, Category.CATEGORY_ERROR, 5),

    /**
     * The search object contains more than one folder id. This is not supported by this implementation
     */
    TOO_MANY_FOLDERS(LdapExceptionMessages.TOO_MANY_FOLDERS_MSG, Category.CATEGORY_ERROR, 6),

    /**
     * The mapping table doesn't contain the string uid %s, so it has never been accessed before
     */
    NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND(LdapExceptionMessages.NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND_MSG, Category.CATEGORY_ERROR, 7),

    /**
     * Multi-values are not allowed for date attribute: %s
     */
    MULTIVALUE_NOT_ALLOWED_DATE(LdapExceptionMessages.MULTIVALUE_NOT_ALLOWED_DATE_MSG, Category.CATEGORY_ERROR, 8),

    /**
     * Multi-values are not allowed for int attribute: %s
     */
    MULTIVALUE_NOT_ALLOWED_INT(LdapExceptionMessages.MULTIVALUE_NOT_ALLOWED_INT_MSG, Category.CATEGORY_ERROR, 9),

    /**
     * Error while trying to create connection to LDAP server: %s
     */
    INITIAL_LDAP_ERROR(LdapExceptionMessages.INITIAL_LDAP_ERROR_MSG, Category.CATEGORY_ERROR, 10),

    /**
     * The LDAP search for the user contains too many results
     */
    TOO_MANY_USER_RESULTS(LdapExceptionMessages.TOO_MANY_USER_RESULTS_MSG, Category.CATEGORY_ERROR, 11),

    /**
     * The LDAP search for the user object "%s" gave no results
     */
    NO_USER_RESULTS(LdapExceptionMessages.NO_USER_RESULTS_MSG, Category.CATEGORY_ERROR, 12),

    /**
     * An error occurred while trying to get the user object from the database
     */
    ERROR_GETTING_USER_Object(LdapExceptionMessages.ERROR_GETTING_USER_Object_MSG, Category.CATEGORY_ERROR, 13),

    /**
     * The given userLoginSource is not possible: %s
     */
    GIVEN_USER_LOGIN_SOURCE_NOT_FOUND(LdapExceptionMessages.GIVEN_USER_LOGIN_SOURCE_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 14),

    /**
     * The imap login for user "%s" is null
     */
    IMAP_LOGIN_NULL(LdapExceptionMessages.IMAP_LOGIN_NULL_MSG, Category.CATEGORY_CONFIGURATION, 15),

    /**
     * The primary mail for user "%s" is null
     */
    PRIMARY_MAIL_NULL(LdapExceptionMessages.PRIMARY_MAIL_NULL_MSG, Category.CATEGORY_CONFIGURATION, 16),

    /**
     * The E-Mail address "%s" for distributionentry is invalid
     */
    MAIL_ADDRESS_DISTRI_INVALID(LdapExceptionMessages.MAIL_ADDRESS_DISTRI_INVALID_MSG, Category.CATEGORY_CONFIGURATION, 17),

    /**
     * The contact type "%s" is not known
     */
    UNKNOWN_CONTACT_TYPE(LdapExceptionMessages.UNKNOWN_CONTACT_TYPE_MSG, Category.CATEGORY_CONFIGURATION, 18),

    /**
     * The attribute "%s" is missing for object "%s"
     */
    MISSING_ATTRIBUTE(LdapExceptionMessages.MISSING_ATTRIBUTE_MSG, Category.CATEGORY_CONFIGURATION, 19),

    /**
     * No values mapping table found
     */
    NO_VALUES_MAPPING_TABLE_FOUND(LdapExceptionMessages.NO_VALUES_MAPPING_TABLE_FOUND_MSG, Category.CATEGORY_ERROR, 20),

    /**
     * No keys mapping table found
     */
    NO_KEYS_MAPPING_TABLE_FOUND(LdapExceptionMessages.NO_KEYS_MAPPING_TABLE_FOUND_MSG, Category.CATEGORY_ERROR, 21),

    /**
     * An error occurred while trying to get the defaultNamingContext attribute
     */
    ERROR_GETTING_DEFAULT_NAMING_CONTEXT(LdapExceptionMessages.ERROR_GETTING_DEFAULT_NAMING_CONTEXT_MSG, Category.CATEGORY_ERROR, 22),

    /**
     * The contact object with id "%s" could not be found in folder "%s"
     */
    CONTACT_NOT_FOUND(LdapExceptionMessages.CONTACT_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 23),
    
    /**
     * An error occurred: %1$s
     */
    ERROR(LdapExceptionMessages.ERROR_MSG, Category.CATEGORY_ERROR, 100),
    
    /**
     * This storage can't handle context %1$d
     */
    INVALID_CONTEXT(LdapExceptionMessages.INVALID_CONTEXT_MSG, CATEGORY_ERROR, 101),

    /**
     * This storage can't handle folder %1$s
     */
    INVALID_FOLDER(LdapExceptionMessages.INVALID_FOLDER_MSG, CATEGORY_ERROR, 102),

    /**
     * No mapping for object %1$s, folder %2$s, context %3$d found
     */
    NO_MAPPED_LDAP_ID(LdapExceptionMessages.NO_MAPPED_LDAP_ID_MSG, CATEGORY_ERROR, 103),
    
    /**
     * No mapping for LDAP identifier %1$s found
     */
//    NO_MAPPED_OX_ID(LdapExceptionMessages.NO_MAPPED_OX_ID_MSG, CATEGORY_ERROR, 104),
    
    /**
     * The configuration value %1$s is missing
     */
    MISSING_CONFIG_VALUE(LdapExceptionMessages.MISSING_CONFIG_VALUE_MSG, CATEGORY_CONFIGURATION, 105),

    /**
     * The configuration value %s is wrong or missing
     */
    WRONG_OR_MISSING_CONFIG_VALUE(LdapExceptionMessages.WRONG_OR_MISSING_CONFIG_VALUE_MSG, CATEGORY_CONFIGURATION, 106),

    /**
     * An internal LDAP error occurred: %1$s
     */
    LDAP_ERROR(LdapExceptionMessages.LDAP_ERROR_MSG, Category.CATEGORY_ERROR, 107),

    /**
     * Unable to map contact property \"%s\"
     */
    UNKNOWN_CONTACT_PROPERTY(LdapExceptionMessages.UNKNOWN_CONTACT_PROPERTY_MSG, Category.CATEGORY_CONFIGURATION, 108),

    /**
     * An internal LDAP sort error occurred: %1$s
     */
    LDAP_SORT_ERROR(LdapExceptionMessages.LDAP_SORT_ERROR_MSG, Category.CATEGORY_ERROR, 109),
    ;

    private static final String PREFIX = "LDAP";

    private final Category category;
    private final int number;
    private final String message;

    private LdapExceptionCodes(String message, Category category, int detailNumber) {
        this.message = message;
        number = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
