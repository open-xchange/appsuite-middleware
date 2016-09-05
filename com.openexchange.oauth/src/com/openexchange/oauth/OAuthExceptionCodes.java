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

package com.openexchange.oauth;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link OAuthExceptionCodes} - Enumeration of all {@link OXException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public enum OAuthExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1, null),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 2, null),
    /**
     * A JSON occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 3, null),
    /**
     * Unknown OAuth service meta data: %1$s
     */
    UNKNOWN_OAUTH_SERVICE_META_DATA("Unknown OAuth service meta data: %1$s", CATEGORY_ERROR, 4, null),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", CATEGORY_ERROR, 5, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * Account not found with identifier %1$s for user %2$s in context %3$s.
     */
    ACCOUNT_NOT_FOUND("Account not found with identifier %1$s for user %2$s in context %3$s.", CATEGORY_USER_INPUT, 6,
        OAuthExceptionMessages.ACCOUNT_NOT_FOUND_MSG),
    /**
     * Unsupported OAuth service: %1$s
     */
    UNSUPPORTED_SERVICE("Unsupported OAuth service: %1$s", CATEGORY_ERROR, 7, null),
    /**
     * Missing argument: %1$s
     */
    MISSING_ARGUMENT("Missing argument: %1$s", CATEGORY_USER_INPUT, 8, OAuthExceptionMessages.MISSING_ARGUMENT_MSG),
    /**
     * Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.
     */
    TOKEN_EXPIRED("Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, "
        + "go to Configuration -> My Social Configuration -> Accounts. Then try again.", CATEGORY_TRY_AGAIN, 9,
        OAuthExceptionMessages.TOKEN_EXPIRED_MSG),
    /**
     * An OAuth error occurred: %1$s
     */
    OAUTH_ERROR("An OAuth error occurred: %1$s", CATEGORY_ERROR, 10, null),
    /**
     * "The address %1 is not white-listed as for the %2 OAuth API"
     */
    NOT_A_WHITELISTED_URL("The address %1$s is not white-listed as for the %2$s OAuth API", CATEGORY_PERMISSION_DENIED, 11,
        OAuthExceptionMessages.NOT_A_WHITELISTED_URL_MSG),
    /**
     * The request sent was missing its body
     */
    MISSING_BODY("The request sent was missing its body", CATEGORY_USER_INPUT, 12, OAuthExceptionMessages.MISSING_BODY_MSG),
    /**
     * The account is invalid, please recreate it.
     */
    INVALID_ACCOUNT("The account is invalid, please recreate it.", CATEGORY_TRY_AGAIN, 13, OAuthExceptionMessages.INVALID_ACCOUNT_MSG),
    /**
     * The account "%1$s" (id=%2$s) is invalid, please recreate it.
     */
    INVALID_ACCOUNT_EXTENDED("The account \"%1$s\" (id=%2$s) is invalid, please recreate it.", CATEGORY_TRY_AGAIN, 13, OAuthExceptionMessages.INVALID_ACCOUNT_EXTENDED_MSG), // Yapp, the same error code
    /**
     * Please provide a display name.
     */
    MISSING_DISPLAY_NAME("Please provide a display name.", CATEGORY_USER_INPUT, 14, OAuthExceptionMessages.MISSING_DISPLAY_NAME_MSG),
    /**
     * The associated OAuth provider denied the request: %1$s.
     */
    DENIED_BY_PROVIDER("The associated OAuth provider denied the request: %1$s.", CATEGORY_USER_INPUT, 15,
        OAuthExceptionMessages.DENIED_BY_PROVIDER_MSG),
    /**
     * The OAuth authentication process has been canceled.
     */
    CANCELED_BY_USER("The OAuth authentication process has been canceled.", CATEGORY_USER_INPUT, 16, OAuthExceptionMessages.CANCELED_BY_USER_MSG),
    /**
     * Could not get a valid response from the associated OAuth provider.
     */
    NOT_A_VALID_RESPONSE("Could not get a valid response from the associated OAuth provider.", CATEGORY_ERROR, 17, OAuthExceptionMessages.NOT_A_VALID_RESPONSE_MSG),
    /**
     * There was a problem while creating a connection to the remote service.
     */
    CONNECT_ERROR("There was a problem while creating a connection to the remote service.", CATEGORY_CONNECTIVITY, 18, OAuthExceptionMessages.CONNECT_ERROR_MSG),

    /**
     * The user needs to give additional %1$s oauth permissions before the consumer is allowed access to the resource.
     */
    OAUTH_PROBLEM_ADDITIONAL_AUTHORIZATION_REQUIRED("The user needs to give additional %1$s oauth permissions before the consumer is allowed access to the resource.", CATEGORY_TRY_AGAIN, 19, OAuthExceptionMessages.OAUTH_PROBLEM_ADDITIONAL_AUTHORIZATION_REQUIRED_MSG),

    /**
     * The %1$s oauth consumer key is temporarily unacceptable to the service provider.
     */
    OAUTH_PROBLEM_CONSUMER_KEY_REFUSED("The %1$s oauth consumer key is temporarily unacceptable to the Service Provider.", CATEGORY_ERROR, 20, null),

    /**
     * The %1$s oauth consumer key is permanently unacceptable to the service provider.
     */
    OAUTH_PROBLEM_CONSUMER_KEY_REJECTED("The %1$s oauth consumer key is permanently unacceptable to the service provider.", CATEGORY_ERROR, 21, null),

    /**
     * The %1$s oauth consumer key is unknown to the service provider.
     */
    OAUTH_PROBLEM_CONSUMER_KEY_UNKNOWN("The %1$s oauth consumer key is unknown to the service provider.", CATEGORY_ERROR, 22, null),

    /**
     * The oauth nonce value was used in a previous request, and consequently can't be used now.
     */
    OAUTH_PROBLEM_NONCE_USED("The oauth nonce value was used in a previous request, and consequently can't be used now.", CATEGORY_ERROR, 23, null),

    /**
     * A required parameter wasn't received. Required parameter: %1$s.
     */
    OAUTH_PROBLEM_PARAMETER_ABSENT("A required parameter wasn't received. Required parameter: %1$s.", CATEGORY_ERROR, 24, null),

    /**
     * An unexpected parameter was received. Rejected parameter: %1$s.
     */
    OAUTH_PROBLEM_PARAMETER_REJECTED("An unexpected parameter was received. Rejected parameter: %1$s.", CATEGORY_ERROR, 25, null),

    /**
     * The user refused to permit this Consumer to access the protected resources.
     */
    OAUTH_PROBLEM_PERMISSION_DENIED("The user refused to permit this Consumer to access the protected resources.", CATEGORY_PERMISSION_DENIED, 26, null),

    /**
     * The user hasn't decided whether to permit this consumer to access protected resources.
     */
    OAUTH_PROBLEM_PERMISSION_UNKNOWN("The user hasn't decided whether to permit this consumer to access protected resources", CATEGORY_PERMISSION_DENIED, 27, null),

    /**
     * The oauth signature doesn't match the one computed by the service provider.
     */
    OAUTH_PROBLEM_SIGNATURE_INVALID("The oauth signature doesn't match the one computed by the service provider.", CATEGORY_ERROR, 28, null),

    /**
     * The oauth signature method is unacceptable to the service provider
     */
    OAUTH_PROBLEM_SIGNATURE_METHOD_REJECTED("The oauth signature method is unacceptable to the service provider", CATEGORY_ERROR, 29, null),

    /**
     * The oauth timestamp value is unacceptable to the service provider. Acceptable timestamp: %1$s.
     */
    OAUTH_PROBLEM_TIMESTAMP_REFUSED("The oauth timestamp value is unacceptable to the service provider. Acceptable timestamp: %1$s.",CATEGORY_ERROR, 30, null),

    /**
     * The %1$s oauth token has expired.
     */
    OAUTH_PROBLEM_TOKEN_EXPIRED("The %1$s oauth token has expired.", CATEGORY_TRY_AGAIN, 31, OAuthExceptionMessages.TOKEN_EXPIRED_MSG),

    /**
     * The %1$s oauth token is unacceptable to the service provider.
     */
    OAUTH_PROBLEM_TOKEN_REJECTED("The %1$s oauth token is unacceptable to the service provider.", CATEGORY_TRY_AGAIN, 32, OAuthExceptionMessages.OAUTH_PROBLEM_TOKEN_REJECTED_MSG),

    /**
     * The %1$s oauth token has been revoked.
     */
    OAUTH_PROBLEM_TOKEN_REVOKED("The %1$s oauth token has been revoked.", CATEGORY_TRY_AGAIN, 33, OAuthExceptionMessages.OAUTH_PROBLEM_TOKEN_REVOKED_MSG),

    /**
     * The oauth token has been consumed and can't be used any more.
     */
    OAUTH_PROBLEM_TOKEN_USED("The %1$s oauth token has been consumed and can't be used any more.", CATEGORY_TRY_AGAIN, 34, null),

    /**
     * The user is temporarily unacceptable to the service provider.
     */
    OAUTH_PROBLEM_USER_REFUSED("The user is temporarily unacceptable to %1$s.", CATEGORY_TRY_AGAIN, 35, null),

    /**
     * The oauth verifier is incorrect.
     */
    OAUTH_PROBLEM_VERIFIER_INVALID("The oauth verifier is incorrect.", CATEGORY_ERROR, 36, null),

    /**
     * The oauth version isn't supported by the service provider. Acceptable versions: %1$s.
     */
    OAUTH_PROBLEM_VERSION_REJECTED("The oauth version isn't supported by the service provider. Acceptable versions: %1$s", CATEGORY_ERROR, 37, null),

    /**
     * An unexpected OAuth problem occurred: %1$s
     */
    OAUTH_PROBLEM_UNEXPECTED("An unexpected OAuth problem occurred: %1$s", CATEGORY_ERROR, 38, null),

    /**
     * The OAuth provider refused to permit this Consumer to access the protected resources.
     */
    OAUTH_PROBLEM_ACCESS_DENIED("The OAuth provider refused to permit this Consumer to access the protected resources.", CATEGORY_PERMISSION_DENIED, 39, null),
    
    /**
     * The OAuth access token for the %1$s OAuth account with id '%2$s' of user '%3$s' in context '%4$s' is invalid. Re-authorisation is required. URL: %5$s
     */
    OAUTH_ACCESS_TOKEN_INVALID("The OAuth access token for the %1$s OAuth account with id '%2$s' of user '%3$s' in context '%4$s' is invalid. Re-authorisation is required. URL: %5$s", CATEGORY_ERROR, 40, OAuthExceptionMessages.OAUTH_TOKEN_INVALID),

    ;

    private final Category category;
    private final int number;
    private final String message;
    private String displayMessage;

    private OAuthExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    private OAuthExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        this.number = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "OAUTH";
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
    public String getDisplayMessage() {
        return displayMessage;
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
