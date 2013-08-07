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

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link LdapConfigurationExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class LdapConfigurationExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link LdapConfigurationExceptionMessage}.
     */
    private LdapConfigurationExceptionMessage() {
        super();
    }

    /**
     * The value given for authtype "%s" is invalid
     */
    public final static String AUTH_TYPE_WRONG_MSG = "The value given for authtype \"%s\" is invalid";

    /**
     * The value given for sorting "%s" is invalid
     */
    public final static String SORTING_WRONG_MSG = "The value given for sorting \"%s\" is invalid";

    /**
     * The value given for searchScope "%s" is invalid
     */
    public final static String SEARCH_SCOPE_WRONG_MSG = "The value given for searchScope \"%s\" is invalid";

    /**
     * The parameter "%s" is not set in the property file %s
     */
    public final static String PARAMETER_NOT_SET_MSG = "The parameter \"%s\" is not set in property file \"%s\"";

    /**
     * Value for context "%s" is no integer value
     */
    public final static String NO_INTEGER_VALUE_MSG = "Value for context \"%s\" is no integer value";

    /**
     * The configured mapping file "%1$s" is empty.
     */
    public final static String INVALID_MAPPING_FILE_MSG = "The configured mapping file \"%1$s\" is empty.";

    /**
     * The given value for pagesize "%s" is no integer value
     */
    public final static String INVALID_PAGESIZE_MSG = "The given value for pagesize \"%s\" is no integer value";

    /**
     * The value given for userauthtype "%s" is invalid
     */
    public final static String USER_AUTH_TYPE_WRONG_MSG = "The value given for userauthtype \"%s\" is invalid";

    /**
     * The value given for userSearchScope "%s" is invalid
     */
    public final static String USER_SEARCH_SCOPE_WRONG_MSG = "The value given for userSearchScope \"%s\" is invalid";

    /**
     * The value given for userLoginSource "%s" is invalid
     */
    public final static String USER_LOGIN_SOURCE_WRONG_MSG = "The value given for userLoginSource \"%s\" is invalid";

    /**
     * The value given for contactTypes "%s" is invalid
     */
    public final static String CONTACT_TYPES_WRONG_MSG = "The value given for contactTypes \"%s\" is invalid";

    /**
     * The value given for searchScope_distributionlist "%1$s" is invalid
     */
    public final static String SEARCH_SCOPE_DISTRI_WRONG_MSG = "The given value for searchScope_distributionlist \"%1$s\" is invalid";

    /**
     * Abstract pathname "%1$s" does not denote a directory.
     */
    public final static String NOT_DIRECTORY_MSG = "Abstract pathname \"%1$s\" does not denote a directory.";

    /**
     * The directory "%1$s" is not a context identifier.
     */
    public final static String DIRECTORY_IS_NOT_A_CONTEXT_ID_MSG = "The directory \"%1$s\" is not a context identifier.";

    /**
     * The value given for referrals "%s" is not a possible one
     */
    public final static String REFERRALS_WRONG_MSG = "The value given for referrals \"%s\" is not a possible one";

    /**
     * The value given for refreshinterval "%s" is no integer value
     */
    public final static String INVALID_REFRESHINTERVAL_MSG = "The value given for refreshinterval \"%s\" is no integer value";

    /**
     * The value given for pooltimeout "%s" in file "%s" is no integer value
     */
    public final static String INVALID_POOLTIMEOUT_MSG = "The value given for pooltimeout \"%s\" in file \"%s\" is no integer value";

    /**
     * The value given for derefAliases "%s" in file "%s" is invalid
     */
    public final static String DEREF_ALIASES_WRONG_MSG = "The value given for derefAliases \"%s\" in file \"%s\" is invalid";

    /**
     * LDAP contacts cannot be deleted
     */
    public final static String DELETE_NOT_POSSIBLE_MSG = "LDAP contacts cannot be deleted";

    /**
     * An error occured while trying to read an LDAP attribute: %s
     */
    public final static String ERROR_GETTING_ATTRIBUTE_MSG = "An error occured while trying to read an LDAP attribute: %s";

    /**
     * The given value \"%s\" is not possible for a sort field
     */
    public final static String SORT_FIELD_NOT_POSSIBLE_MSG = "The given value \"%s\" is not possible for a sort field";

    /**
     * Contacts cannot be inserted in LDAP
     */
    public final static String INSERT_NOT_POSSIBLE_MSG = "Contacts cannot be inserted in LDAP";

    /**
     * The folderid object is null. This is an internal error. Please notify Open-Xchange
     */
    public final static String FOLDERID_OBJECT_NULL_MSG = "The folderid object is null. This is an internal error. Please notify Open-Xchange.";

    /**
     * The search object contains more than one folder id. This is not supported by this implementation
     */
    public final static String TOO_MANY_FOLDERS_MSG = "The search object contains more than one folder id. This is not supported by this implementation.";

    /**
     * The mapping table doesn't contain the string uid %s, so it has never been accessed before
     */
    public final static String NO_SUCH_LONG_UID_IN_MAPPING_TABLE_FOUND_MSG = "The mapping table does not contain the long uid %s i.e., it has never been accessed before";

    /**
     * Multi-values are not allowed for date attribute: %s
     */
    public final static String MULTIVALUE_NOT_ALLOWED_DATE_MSG = "Multi-values are not allowed for date attribute: %s";

    /**
     * Multi-values are not allowed for int attribute: %s
     */
    public final static String MULTIVALUE_NOT_ALLOWED_INT_MSG = "Multi-values are not allowed for int attribute: %s";

    /**
     * Error while trying to create connection to LDAP server: %s
     */
    public final static String INITIAL_LDAP_ERROR_MSG = "Error while trying to create connection to LDAP server: %s";

    /**
     * The LDAP search for the user contains too many results
     */
    public final static String TOO_MANY_USER_RESULTS_MSG = "The LDAP search for the user contains too many results";

    /**
     * The LDAP search for the user object \"%s\" gave no results
     */
    public final static String NO_USER_RESULTS_MSG = "The LDAP search for the user object \"%s\" gave no results";

    /**
     * An error occurred while trying to get the user object from the database
     */
    public final static String ERROR_GETTING_USER_Object_MSG = "An error occurred while trying to get the user object from the database";

    /**
     * The given userLoginSource is not possible: %s
     */
    public final static String GIVEN_USER_LOGIN_SOURCE_NOT_FOUND_MSG = "The given userLoginSource is not possible: %s";

    /**
     * The imap login for user "%s" is null
     */
    public final static String IMAP_LOGIN_NULL_MSG = "The imap login for user \"%s\" is null";

    /**
     * The primary mail for user "%s" is null
     */
    public final static String PRIMARY_MAIL_NULL_MSG = "The primary mail for user \"%s\" is null";

    /**
     * The E-Mail address "%s" for distributionentry is invalid
     */
    public final static String MAIL_ADDRESS_DISTRI_INVALID_MSG = "The E-Mail address \"%s\" for distributionentry is invalid";

    /**
     * The contact type "%s" is not known
     */
    public final static String UNKNOWN_CONTACT_TYPE_MSG = "The contact type \"%s\" is not known";

    /**
     * The attribute "%s" is missing for object "%s"
     */
    public final static String MISSING_ATTRIBUTE_MSG = "The attribute \"%s\" is missing for object \"%s\"";

    /**
     * No values mapping table found
     */
    public final static String NO_VALUES_MAPPING_TABLE_FOUND_MSG = "No values mapping table found";

    /**
     * No keys mapping table found
     */
    public final static String NO_KEYS_MAPPING_TABLE_FOUND_MSG = "No keys mapping table found";

    /**
     * An error occurred while trying to get the defaultNamingContext attribute
     */
    public final static String ERROR_GETTING_DEFAULT_NAMING_CONTEXT_MSG = "An error occurred while trying to get the defaultNamingContext attribute";

    // The contact object with id "%s" could not be found in folder "%s"
    public static final String CONTACT_NOT_FOUND_MSG = "The contact object with id \"%s\" could not be found in folder \"%s\"";

    /**
     * The value given for storagePriority "%s" in file "%s" is no integer value
     */
    public static final String INVALID_STORAGE_PRIORITY = "The value given for storagePriority \"%s\" in file \"%s\" is no integer value";

}
