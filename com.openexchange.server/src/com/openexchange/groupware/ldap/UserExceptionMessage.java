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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link UserExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UserExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link UserExceptionMessage}.
     */
    private UserExceptionMessage() {
        super();
    }

    /**
     * Cannot clone object %1$s.
     */
    public final static String NOT_CLONEABLE_DISPLAY = "Cannot clone object \"%1$s\".";

    /**
     * Hash algorithm %s isn't found.
     */
    public final static String HASHING_DISPLAY = "Hash algorithm \"%s\" could not be found.";

    /**
     * Encoding %s cannot be used.
     */
    public final static String UNSUPPORTED_ENCODING_DISPLAY = "Encoding \"%s\" cannot be used.";

    /**
     * Cannot find user with identifier %1$s in context %2$d.
     */
    public final static String USER_NOT_FOUND_DISPLAY = "Cannot find user with identifier \"%1$s\" in context %2$d.";

    /**
     * Found two user with same identifier %1$s in context %2$d.
     */
    public final static String USER_CONFLICT_DISPLAY = "Two users with same identifier \"%1$s\" in context %2$d found.";

    /**
     * Problem putting an object into the cache.
     */
    public final static String CACHE_PROBLEM_DISPLAY = "Problem putting/removing an object into/from the cache.";

    /**
     * No permission to modify resources in context %1$s
     */
    public final static String PERMISSION_DISPLAY = "No permission to modify resources in context \"%1$s\"";

    /**
     * Missing or unknown password mechanism %1$s
     */
    public final static String MISSING_PASSWORD_MECH_DISPLAY = "Missing or unknown password mechanism \"%1$s\"";

    /**
     * New password contains invalid characters
     */
    public final static String INVALID_PASSWORD_DISPLAY = "New password contains invalid characters.";

    /**
     * Attributes of user %1$d in context %2$d have been erased.
     */
    public final static String ERASED_ATTRIBUTES_DISPLAY = "Attributes of user %1$d in context %2$d have been erased.";

    /**
     * Loading one or more users failed.
     */
    public final static String LOAD_FAILED_DISPLAY = "Loading one or more users failed.";

    /** Alias entries are missing for user %1$d in context %2$d. */
    public final static String ALIASES_MISSING_DISPLAY = "Alias entries are missing for user %1$d in context %2$d.";

    /** Updating attributes failed in context %1$d for user %2$d. */
    public final static String UPDATE_ATTRIBUTES_FAILED_DISPLAY = "Updating attributes failed in context %1$d for user %2$d.";

    /**
     * Invalid password length. The password must be of minimum length %1$d.
     */
    public final static String INVALID_MIN_LENGTH_DISPLAY = "Invalid password length. The password must have a minimum length of %1$d.";

    /**
     * Invalid password length. The password must be of maximum length %1$d.
     */
    public final static String INVALID_MAX_LENGTH_DISPLAY = "Invalid password length. The password must have a maximum length of %1$d.";

    /**
     * The parameter %s for this user is missing.
     */
    public final static String MISSING_PARAMETER_DISPLAY = "The parameter \"%s\" for this user is missing.";

    /**
     * %s is not a valid locale.
     */
    public final static String INVALID_LOCALE_DISPLAY = "\"%s\" is not a valid locale.";

    /**
     * %s is not a valid time zone.
     */
    public final static String INVALID_TIMEZONE_DISPLAY = "\"%s\" is not a valid timezone.";

    // This is an internal exception to detect coding problems. A user will never see this exception.
    // %1$d is replaced with the number of users that attributes should be locked in the database for updating them.
    public static final String LOCKING_NOT_ALLOWED_DISPLAY = "Locking attributes of multiple users is not allowed. You tried to lock %1$d user's attributes.";

    /**
     * The entered password is illegal and can't be saved. Allowed characters are: %1$s
     */
    public final static String NOT_ALLOWED_PASSWORD_DISPLAY = "The entered password is illegal and can't be saved. Allowed characters are: %1$s";

    /**
     * The current password is incorrect. Please enter your correct current password and try again.
     */
    public final static String INCORRECT_CURRENT_PASSWORD_DISPLAY = "The current password is incorrect. Please enter your correct current password and try again.";

    /**
     * The current password is missing. Please enter your current password and try again.
     */
    public final static String MISSING_CURRENT_PASSWORD_DISPLAY = "The current password is missing. Please enter your current password and try again.";

    /**
     * The new password is missing. Please enter your new password and try again.
     */
    public final static String MISSING_NEW_PASSWORD_DISPLAY = "The new password is missing. Please enter your new password and try again.";

    // This exception is triggered by concurrent requests of clients trying to modify the same user attributes in the same moment.
    // This should happen in very rare conditions and is not visible to the client.
    public static final String CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY = "Denied concurrent update of user attributes.";

}
