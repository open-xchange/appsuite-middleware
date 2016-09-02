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

package com.openexchange.subscribe;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;


/**
 * {@link SubscriptionErrorMessage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public enum SubscriptionErrorMessage implements DisplayableOXExceptionCode {

    /**
     * Error while reading/writing data from/to the database.
     */
    SQLException(CATEGORY_ERROR, 1, SubscriptionErrorMessage.SQL_ERROR, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * A parsing error occurred: %1$s.
     */
    ParseException(CATEGORY_ERROR, 2, SubscriptionErrorMessage.PARSING_ERROR),
    /**
     * Can not save a given ID.
     */
    IDGiven(CATEGORY_ERROR, 3, SubscriptionErrorMessage.CANT_SAVE_ID),
    /**
     * Parsing error.
     */
    ParsingError(CATEGORY_ERROR, 6, SubscriptionErrorMessage.PARSING_ERROR),
    /**
     * Could not find Subscription (according ID and Context).
     */
    SubscriptionNotFound(CATEGORY_USER_INPUT, 5, SubscriptionErrorMessage.CANT_FIND_SUBSCRIPTION, SubscriptionErrorStrings.CANT_FIND_SUBSCRIPTION_DISPLAY),
    /**
     * Inserted login or password have been wrong.
     */
    INVALID_LOGIN(CATEGORY_USER_INPUT, 7, SubscriptionErrorMessage.WRONG_PASSWORD, SubscriptionErrorStrings.WRONG_PASSWORD_DISPLAY),
    /**
     *
     */
    COMMUNICATION_PROBLEM(CATEGORY_SERVICE_DOWN, 8, SubscriptionErrorMessage.SERVICE_UNAVAILABLE, SubscriptionErrorStrings.SERVICE_UNAVAILABLE_DISPLAY),
    /**
     *
     */
    INVALID_WORKFLOW(CATEGORY_CONFIGURATION, 9, SubscriptionErrorMessage.INCONSISTENT_WORKFLOW),
    /**
     *
     */
    INACTIVE_SOURCE(CATEGORY_CONFIGURATION, 10, SubscriptionErrorMessage.INACTIVE_SOURCE_MSG, SubscriptionErrorStrings.INACTIVE_SOURCE_DISPLAY),
    /**
     *
     */
    MISSING_ARGUMENT(CATEGORY_USER_INPUT, 11, SubscriptionErrorMessage.MISSING_ARGUMENT_MSG, SubscriptionErrorStrings.MISSING_ARGUMENT_DISPLAY),
    /**
     *
     */
    PERMISSION_DENIED(CATEGORY_WARNING, 12, SubscriptionErrorMessage.PERMISSION_DENIED_MSG, SubscriptionErrorStrings.PERMISSION_DENIED_DISPLAY),
    /**
     * Please specify your full E-Mail address as login name.
     */
    EMAIL_ADDR_LOGIN(CATEGORY_TRY_AGAIN, 13, SubscriptionErrorMessage.EMAIL_ADDR_LOGIN_MSG, SubscriptionErrorStrings.EMAIL_ADDR_LOGIN_DISPLAY),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(CATEGORY_ERROR, 14, "An I/O error occurred: %1$s"),
    /**
     * The user '%1$s' in context '%2$s' has not authorised the '%3$s' provider to gain access to '%4%s'
     */
    NO_SCOPE_PERMISSION(CATEGORY_WARNING, 15, "The user needs to authorise the '%1$s' provider to gain access to '%2%s'", SubscriptionErrorStrings.NO_SCOPE_PERMISSION),
    /**
     * An unexpected error occurred: %1$s.
     */
    UNEXPECTED_ERROR(CATEGORY_ERROR, 9999, SubscriptionErrorMessage.UNEXPECTED_ERROR_MSG),
    /**
     * User does not have an OAuth-account to access this service.
     */
    NO_OAUTH_ACCOUNT_GIVEN(CATEGORY_USER_INPUT, 90111, SubscriptionErrorMessage.NO_OAUTH_ACCOUNT_GIVEN_MSG, SubscriptionErrorStrings.NO_OAUTH_ACCOUNT_GIVEN_DISPLAY),
    /**
     * Your account needs to be verified: %1$s
     */
    NEED_VERIFICATION(CATEGORY_USER_INPUT, 90112, SubscriptionErrorStrings.NEED_VERIFICATION_DISPLAY, SubscriptionErrorStrings.NEED_VERIFICATION_DISPLAY),
    /**
     * The service provider asked for an identity confirmation. This happens for some accounts and cannot fixed by us. It is in the provider's responsibility. For this reason, the subscription cannot be completed.
     */
    ABORT_IDENTITY_CONFIRMATION(CATEGORY_USER_INPUT, 90113, SubscriptionErrorStrings.ABORT_IDENTITY_CONFIRMATION_DISPLAY, SubscriptionErrorStrings.ABORT_IDENTITY_CONFIRMATION_DISPLAY),
    /**
     * Such a subscription from source %1$s does already exist for user %2$s in context %3$s
     */
    DUPLICATE_SUBSCRIPTION(CATEGORY_USER_INPUT, 90113, SubscriptionErrorStrings.DUPLICATE_SUBSCRIPTION_DISPLAY, SubscriptionErrorStrings.DUPLICATE_SUBSCRIPTION_DISPLAY),

    ;

    private static final String SQL_ERROR = "A SQL error occurred.";

    private static final String PARSING_ERROR = "A parsing error occurred: %1$s.";

    private static final String CANT_SAVE_ID = "Unable to save a given ID.";

    private static final String CANT_FIND_SUBSCRIPTION = "Not able to find the requested subscription";

    private static final String WRONG_PASSWORD = "Inserted login or password have been wrong.";

    private static final String SERVICE_UNAVAILABLE = "Subscription or an involved service is currently not available.";

    private static final String INCONSISTENT_WORKFLOW = "The steps of the crawling workflow do not fit together.";

    private static final String INACTIVE_SOURCE_MSG = "Cannot access subscription source.";

    private static final String MISSING_ARGUMENT_MSG = "The argument %1$s is missing to process the subscription.";

    private static final String PERMISSION_DENIED_MSG = "User do not have appropriate permissions to complete the operation.";

    private static final String EMAIL_ADDR_LOGIN_MSG = "The user has to specify full E-Mail address as login name.";

    private static final String UNEXPECTED_ERROR_MSG = "An unexpected error occurred: %1$s.";

    private static final String NO_OAUTH_ACCOUNT_GIVEN_MSG = "User does not have an OAuth-account to access this service.";

    // ---------------------------------------------------------------------------------------------------------------------------- //

    private static final String PREFIX = "SUB";

    /**
     * Gets the static prefix for this error code.
     *
     * @return The prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    // ---------------------------------------------------------------------------------------------------------------------------- //

    private final Category category;
    private final int errorCode;
    private final String message;
    private final String displayMessage;

    /**
     * Initializes a new {@link SubscriptionErrorMessage}.
     *
     * @param category
     * @param errorCode
     * @param help
     * @param message
     * @param displayMessage
     */
    private SubscriptionErrorMessage(Category category, int errorCode, String message, String displayMessage) {
        this.category = category;
        this.errorCode = errorCode;
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    /**
     * Initializes a new {@link SubscriptionErrorMessage}.
     *
     * @param category
     * @param errorCode
     * @param help
     * @param message
     */
    private SubscriptionErrorMessage(final Category category, final int errorCode, final String message) {
        this(category, errorCode, message, null);
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
    public int getNumber() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
