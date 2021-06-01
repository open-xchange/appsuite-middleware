/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.user;

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

    // No permission to modify resources in context %1$s
    public final static String PERMISSION_DISPLAY = "No permission to modify resources in context \"%1$s\"";

    // No permission to access resources in context %1$s
    public static final String PERMISSION_ACCESS_DISPLAY = "No permission to access resources in context \"%1$s\"";

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
     * Invalid password length. The password must have a minimum length of %1$d characters.
     */
    public final static String INVALID_MIN_LENGTH_DISPLAY = "Invalid password length. The password must have a minimum length of %1$d characters.";

    /**
     * Invalid password length. The password must have a maximum length of %1$d characters.
     */
    public final static String INVALID_MAX_LENGTH_DISPLAY = "Invalid password length. The password must have a maximum length of %1$d characters.";

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
