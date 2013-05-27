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


package com.openexchange.groupware.ldap;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link UserExceptionCode} - The user error codes.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UserExceptionCode implements OXExceptionCode {
    /**
     * A property from the ldap.properties file is missing.
     */
    PROPERTY_MISSING(UserExceptionMessage.PROPERTY_MISSING_MSG, Category.CATEGORY_CONFIGURATION, 1),
    /**
     * A problem with distinguished names occurred.
     */
    DN_PROBLEM(UserExceptionMessage.DN_PROBLEM_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Class can not be found.
     */
    CLASS_NOT_FOUND(UserExceptionMessage.CLASS_NOT_FOUND_MSG, Category.CATEGORY_CONFIGURATION, 3),
    /**
     * An implementation can not be instantiated.
     */
    INSTANTIATION_PROBLEM(UserExceptionMessage.INSTANTIATION_PROBLEM_MSG, Category.CATEGORY_CONFIGURATION, 4),
    /**
     * A database connection cannot be obtained.
     */
    NO_CONNECTION(UserExceptionMessage.NO_CONNECTION_MSG, Category.CATEGORY_SERVICE_DOWN, 5),
    /**
     * Cannot clone object %1$s.
     */
    NOT_CLONEABLE(UserExceptionMessage.NOT_CLONEABLE_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * SQL Problem: \"%s\".
     */
    SQL_ERROR(UserExceptionMessage.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * Hash algorithm %s isn't found.
     */
    HASHING(UserExceptionMessage.HASHING_MSG, Category.CATEGORY_ERROR, 8),
    /**
     * Encoding %s cannot be used.
     */
    UNSUPPORTED_ENCODING(UserExceptionMessage.UNSUPPORTED_ENCODING_MSG, Category.CATEGORY_ERROR, 9),
    /**
     * Cannot find user with identifier %1$s in context %2$d.
     */
    USER_NOT_FOUND(UserExceptionMessage.USER_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 10),
    /**
     * Found two user with same identifier %1$s in context %2$d.
     */
    USER_CONFLICT(UserExceptionMessage.USER_CONFLICT_MSG, Category.CATEGORY_ERROR, 11),
    /**
     * Problem putting an object into the cache.
     */
    CACHE_PROBLEM(UserExceptionMessage.CACHE_PROBLEM_MSG, Category.CATEGORY_ERROR, 12),
    /**
     * No CATEGORY_PERMISSION_DENIED to modify resources in context %1$s
     */
    PERMISSION(UserExceptionMessage.PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 13),
    /**
     * Missing or unknown password mechanism %1$s
     */
    MISSING_PASSWORD_MECH(UserExceptionMessage.MISSING_PASSWORD_MECH_MSG, Category.CATEGORY_ERROR, 14),
    /**
     * New password contains invalid characters
     */
    INVALID_PASSWORD(UserExceptionMessage.INVALID_PASSWORD_MSG, Category.CATEGORY_USER_INPUT, 15),
    /**
     * Attributes of user %1$d in context %2$d have been erased.
     */
    ERASED_ATTRIBUTES(UserExceptionMessage.ERASED_ATTRIBUTES_MSG, Category.CATEGORY_WARNING, 16),
    /**
     * Loading one or more users failed.
     */
    LOAD_FAILED(UserExceptionMessage.LOAD_FAILED_MSG, Category.CATEGORY_ERROR, 17),
    /** Alias entries are missing for user %1$d in context %2$d. */
    ALIASES_MISSING(UserExceptionMessage.ALIASES_MISSING_MSG, Category.CATEGORY_CONFIGURATION, 18),
    /** Updating attributes failed in context %1$d for user %2$d. */
    UPDATE_ATTRIBUTES_FAILED(UserExceptionMessage.UPDATE_ATTRIBUTES_FAILED_MSG, Category.CATEGORY_ERROR, 19),
    /**
     * Invalid password length. The password must be of minimum length %1$d.
     */
    INVALID_MIN_LENGTH(UserExceptionMessage.INVALID_MIN_LENGTH_MSG, Category.CATEGORY_USER_INPUT, 20),
    /**
     * Invalid password length. The password must be of maximum length %1$d.
     */
    INVALID_MAX_LENGTH(UserExceptionMessage.INVALID_MAX_LENGTH_MSG, Category.CATEGORY_USER_INPUT, 21),
    /**
     * The parameter %s for this user is missing.
     */
    MISSING_PARAMETER(UserExceptionMessage.MISSING_PARAMETER_MSG, Category.CATEGORY_USER_INPUT, 22),
    /**
     * %s is not a valid locale.
     */
    INVALID_LOCALE(UserExceptionMessage.INVALID_LOCALE_MSG, Category.CATEGORY_USER_INPUT, 23),
    /**
     * %s is not a valid time zone.
     */
    INVALID_TIMEZONE(UserExceptionMessage.INVALID_TIMEZONE_MSG, Category.CATEGORY_USER_INPUT, 24),
    /** Locking attributes of multiple users is not allowed. You tried to lock %1$d user's attributes. */
    LOCKING_NOT_ALLOWED(UserExceptionMessage.LOCKING_NOT_ALLOWED_MSG, Category.CATEGORY_ERROR, 25);

    private static final String PREFIX = "USR";

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
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private UserExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
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
