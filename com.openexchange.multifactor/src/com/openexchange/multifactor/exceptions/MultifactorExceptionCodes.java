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

package com.openexchange.multifactor.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link MulltifactorExceptionMessages}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * The client did not perform multifactor authentication but it is requried
     */
    ACTION_REQUIRES_AUTHENTICATION("Missing multifactor authentication", MultifactorExceptionMessages.MISSING_AUTHENTICATION_FACTOR_MESSAGE, Category.CATEGORY_USER_INPUT, 1),
    /**
     * The client did provice a wrong factor
     */
    INVALID_AUTHENTICATION_FACTOR("Invalid multifactor", MultifactorExceptionMessages.INVALID_AUTHENTICATION_FACTOR_MESSAGE, Category.CATEGORY_PERMISSION_DENIED, 2),
    /**
     * An error during factor creation
     */
    ERROR_CREATING_FACTOR("An error occured while creating/calculating the authentication factor: %1$s.", MultifactorExceptionMessages.ERROR_CREATING_FACTOR_MESSAGE, Category.CATEGORY_ERROR, 3),
    /**
     * Some service is missing
     */
    SERVICE_UNAVAILABLE("The required service %1$s is temporary not available. Please try again later.", MultifactorExceptionMessages.SERVICE_UNAVAILABLE_MESSAGE, Category.CATEGORY_TRY_AGAIN, 4),
    /**
     * Some error during JSON processing
     */
    JSON_ERROR("JSON error: %s", MultifactorExceptionMessages.JSON_ERROR_MESSAGE, Category.CATEGORY_ERROR, 5),
    /**
     * An unknown provider was requested
     */
    UNKNOWN_PROVIDER("The requested provider %s is unknown.", MultifactorExceptionMessages.UNKNOWN_PROVIDER_MESSAGE, Category.CATEGORY_ERROR, 6),
    /**
     * The requested provider is not available for the user
     */
    PROVIDER_NOT_AVAILABLE("The requested provider %s is not available for the user.", MultifactorExceptionMessages.PROVIDER_NOT_AVAILABLE_MESSAGE, Category.CATEGORY_ERROR, 7),
    /**
     * Parameter device ID missing
     */
    MISSING_DEVICE_ID("Multifactor authentication required but device identifier is missing", MultifactorExceptionMessages.MISSING_DEVICE_ID_MESSAGE, CATEGORY_USER_INPUT, 8),
    /**
     * The provided device ID is wrong
     */
    UNKNOWN_DEVICE_ID("The requested device is unknown", MultifactorExceptionMessages.UNKNOWN_DEVICE_ID_MESSAGE, CATEGORY_USER_INPUT, 9),
    /**
     * An unknown error occured
     */
    UNKNOWN_ERROR("An unknown error occured during multifactor authentication: %s", MultifactorExceptionMessages.UNKNOWN_ERROR_MESSAGE, CATEGORY_ERROR, 10),
    /**
     * The provider Name is missing
     */
    MISSING_PROVIDER_NAME("Multifactor authentication required but provider name is missing", MultifactorExceptionMessages.MISSING_PROVIDER_NAME_MESSAGE, CATEGORY_USER_INPUT, 11),
    /**
     * DB/SQL error
     */
    SQL_EXCEPTION("SQL error: %1$s", MultifactorExceptionMessages.SQL_EXCEPTION_MESSAGE, CATEGORY_ERROR, 12),
    /**
     * Missing parameter: %1$s
     */
    MISSING_PARAMETER("Missing parameter: %1$s", MultifactorExceptionMessages.MISSING_PARAMETER_MESSAGE, CATEGORY_ERROR, 13),
    /**
     * The device is already registered
     */
    DEVICE_ALREADY_REGISTERED("The device is already registered", MultifactorExceptionMessages.DEVICE_ALREADY_REGISTERED_MESSAGE, CATEGORY_ERROR, 14),
    /**
     * The requested action cannot be performed because it requires recent multifactor RE-authentication
     */
    ACTION_REQUIRES_REAUTHENTICATION("This action requires recent multifactor authentication.  Missing multifactor parameters for this action", MultifactorExceptionMessages.ACTION_REQUIRES_REAUTHENTICATION_MESSAGE, CATEGORY_ERROR, 15),
    /**
     * The user has no multi factor devices registered for a specific provider
     */
    NO_DEVICES("No devices in this provider", MultifactorExceptionMessages.NO_DEVICES_MESSAGE, CATEGORY_ERROR, 17),
    /**
     * An argument exceeded the allowed length
     */
    INVALID_ARGUMENT_LENGTH("The provided agument \"%1$s\" of length %2$s exceeded the allowed length of %3$s", MultifactorExceptionMessages.INVALID_ARGUMENT_LENGTH, CATEGORY_USER_INPUT, 18),
    /**
     * The registration failed
     */
    REGISTRATION_FAILED("The registration failed", MultifactorExceptionMessages.REGISTRATION_FAILED_MESSAGE, CATEGORY_USER_INPUT, 19),
    /**
     * Unable to remove the device
     */
    DEVICE_REMOVAL_FAILED("Unable to remove the device", MultifactorExceptionMessages.DEVICE_REMOVAL_FAILED_MESSAGE, CATEGORY_USER_INPUT, 20),
    /**
     * The authentication failed
     */
    AUTHENTICATION_FAILED("The authentication failed.", MultifactorExceptionMessages.AUTHENTICATION_FAILED, CATEGORY_USER_INPUT, 21),
    /**
     * The authentication failed
     */
    AUTHENTICATION_FAILED_EXT("The authentication failed: %1$s", MultifactorExceptionMessages.AUTHENTICATION_FAILED_EXT, CATEGORY_USER_INPUT, 22),

    /**
     * Authentication failed.  Lockout data provided
     */
    AUTHENTICATION_FAILED_WITH_LOCKOUT("The authentication failed.  A total of %1$d attempts allowed before temporary lockout.", MultifactorExceptionMessages.AUTHENTICATION_WITH_LOCKOUT, CATEGORY_USER_INPUT, 23),

    ;

    private static final String PREFIX = "MFA";
    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param number detail number.
     */
    private MultifactorExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    private MultifactorExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
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

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}