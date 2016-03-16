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

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link UserExceptionCode} - The user error codes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UserExceptionCode implements DisplayableOXExceptionCode {

    /**
     * A property from the ldap.properties file is missing.
     */
    PROPERTY_MISSING("Cannot find property %s.", null, Category.CATEGORY_CONFIGURATION, 1),

    /**
     * A problem with distinguished names occurred.
     */
    DN_PROBLEM("Cannot build distinguished name from %s.", null, Category.CATEGORY_ERROR, 2),

    /**
     * Class can not be found.
     */
    CLASS_NOT_FOUND("Class %s can not be loaded.", null, Category.CATEGORY_CONFIGURATION, 3),

    /**
     * An implementation can not be instantiated.
     */
    INSTANTIATION_PROBLEM("Cannot instantiate class %s.", null, Category.CATEGORY_CONFIGURATION, 4),

    /**
     * A database connection cannot be obtained.
     */
    NO_CONNECTION("Cannot get database connection.", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 5),

    /**
     * Cannot clone object %1$s.
     */
    NOT_CLONEABLE("Cannot clone object %1$s.", UserExceptionMessage.NOT_CLONEABLE_DISPLAY, Category.CATEGORY_ERROR, 6),

    /**
     * SQL Problem: \"%s\".
     */
    SQL_ERROR("SQL problem: \"%s\".", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 7),

    /**
     * Hash algorithm %s isn't found.
     */
    HASHING("Hash algorithm %s could not be found.", UserExceptionMessage.HASHING_DISPLAY, Category.CATEGORY_ERROR, 8),

    /**
     * Encoding %s cannot be used.
     */
    UNSUPPORTED_ENCODING("Encoding %s cannot be used.", UserExceptionMessage.UNSUPPORTED_ENCODING_DISPLAY, Category.CATEGORY_ERROR, 9),

    /**
     * Cannot find user with identifier %1$s in context %2$d.
     */
    USER_NOT_FOUND("Cannot find user with identifier %1$s in context %2$d.", UserExceptionMessage.USER_NOT_FOUND_DISPLAY,
        Category.CATEGORY_ERROR, 10),

    /**
     * Found two user with same identifier %1$s in context %2$d.
     */
    USER_CONFLICT("Two users with same identifier %1$s in context %2$d found.", UserExceptionMessage.USER_CONFLICT_DISPLAY,
        Category.CATEGORY_ERROR, 11),

    /**
     * Problem putting an object into the cache.
     */
    CACHE_PROBLEM("Problem putting/removing an object into/from the cache.", UserExceptionMessage.CACHE_PROBLEM_DISPLAY,
        Category.CATEGORY_ERROR, 12),

    /**
     * No CATEGORY_PERMISSION_DENIED to modify resources in context %1$s
     */
    PERMISSION("No permission to modify resources in context %1$s", UserExceptionMessage.PERMISSION_DISPLAY,
        Category.CATEGORY_PERMISSION_DENIED, 13),

    /**
     * Missing or unknown password mechanism %1$s
     */
    MISSING_PASSWORD_MECH("Missing or unknown password mechanism %1$s", UserExceptionMessage.MISSING_PASSWORD_MECH_DISPLAY,
        Category.CATEGORY_ERROR, 14),

    /**
     * New password contains invalid characters
     */
    INVALID_PASSWORD("New password contains invalid characters", UserExceptionMessage.INVALID_PASSWORD_DISPLAY,
        Category.CATEGORY_USER_INPUT, 15),

    /**
     * Attributes of user %1$d in context %2$d have been erased.
     */
    ERASED_ATTRIBUTES("Attributes of user %1$d in context %2$d have been erased.", UserExceptionMessage.ERASED_ATTRIBUTES_DISPLAY,
        Category.CATEGORY_WARNING, 16),

    /**
     * Loading one or more users failed.
     */
    LOAD_FAILED("Loading one or more users failed.", UserExceptionMessage.LOAD_FAILED_DISPLAY, Category.CATEGORY_ERROR, 17),

    /**
     * Alias entries are missing for user %1$d in context %2$d.
     */
    ALIASES_MISSING("Alias entries are missing for user %1$d in context %2$d.", UserExceptionMessage.ALIASES_MISSING_DISPLAY,
        Category.CATEGORY_CONFIGURATION, 18),

    /**
     * Updating attributes failed in context %1$d for user %2$d. Likely due to a concurrent modification.
     */
    UPDATE_ATTRIBUTES_FAILED("Updating attributes failed in context %1$d for user %2$d.",
        UserExceptionMessage.UPDATE_ATTRIBUTES_FAILED_DISPLAY, Category.CATEGORY_ERROR, 19),

    /**
     * Invalid password length. The password must be of minimum length %1$d.
     */
    INVALID_MIN_LENGTH("Invalid password length. The password must have a minimum length of %1$d.",
        UserExceptionMessage.INVALID_MIN_LENGTH_DISPLAY, Category.CATEGORY_USER_INPUT, 20),

    /**
     * Invalid password length. The password must be of maximum length %1$d.
     */
    INVALID_MAX_LENGTH("Invalid password length. The password must have a maximum length of %1$d.",
        UserExceptionMessage.INVALID_MAX_LENGTH_DISPLAY, Category.CATEGORY_USER_INPUT, 21),

    /**
     * The parameter %s for this user is missing.
     */
    MISSING_PARAMETER("The parameter %s for this user is missing.", UserExceptionMessage.MISSING_PARAMETER_DISPLAY,
        Category.CATEGORY_USER_INPUT, 22),

    /**
     * %s is not a valid locale.
     */
    INVALID_LOCALE("%s is not a valid locale.", UserExceptionMessage.INVALID_LOCALE_DISPLAY, Category.CATEGORY_USER_INPUT, 23),

    /**
     * %s is not a valid time zone.
     */
    INVALID_TIMEZONE("%s is not a valid timezone.", UserExceptionMessage.INVALID_TIMEZONE_DISPLAY, Category.CATEGORY_USER_INPUT, 24),

    /**
     * Locking attributes of multiple users is not allowed. You tried to lock %1$d user's attributes.
     */
    LOCKING_NOT_ALLOWED("Locking attributes of multiple users is not allowed. You tried to lock %1$d user's attributes.",
        UserExceptionMessage.LOCKING_NOT_ALLOWED_DISPLAY, Category.CATEGORY_ERROR, 25),

    /**
     * The entered password is illegal and can't be saved. Allowed characters are: %1$s
     */
    NOT_ALLOWED_PASSWORD("The entered password is illegal and can't be saved. Allowed characters are: %1$s",
        UserExceptionMessage.NOT_ALLOWED_PASSWORD_DISPLAY, Category.CATEGORY_USER_INPUT, 26),

    /**
     * The current password is incorrect. Please enter your correct current password and try again.
     */
    INCORRECT_CURRENT_PASSWORD("The current password is incorrect.",
        UserExceptionMessage.INCORRECT_CURRENT_PASSWORD_DISPLAY, Category.CATEGORY_USER_INPUT, 27),

    /**
     * The current password is incorrect. Please enter your correct current password and try again.
     */
    MISSING_CURRENT_PASSWORD("The current password is missing.",
        UserExceptionMessage.MISSING_CURRENT_PASSWORD_DISPLAY, Category.CATEGORY_USER_INPUT, 28),

    /**
     * The current password is incorrect. Please enter your correct current password and try again.
     */
    MISSING_NEW_PASSWORD("The new password is incorrect.",
    UserExceptionMessage.MISSING_NEW_PASSWORD_DISPLAY, Category.CATEGORY_USER_INPUT, 29),

    /**
     * Denied concurrent update for user attributes in context %1$d for user %2$d.
     */
    CONCURRENT_ATTRIBUTES_UPDATE("Denied concurrent update for user attributes in context %1$d for user %2$d.",
        UserExceptionMessage.CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY, Category.CATEGORY_ERROR, 30);

    private static final String PREFIX = "USR";

    /**
     * Message of the exception.
     */
    private final String message;

    private final String displayMessage;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int detailNumber;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private UserExceptionCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
