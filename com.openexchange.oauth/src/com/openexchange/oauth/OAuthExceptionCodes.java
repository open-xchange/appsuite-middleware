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

package com.openexchange.oauth;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.LogLevelAwareOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link OAuthExceptionCodes} - Enumeration of all {@link OXException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public enum OAuthExceptionCodes implements LogLevelAwareOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(OAuthExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1, LogLevel.ERROR),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(OAuthExceptionMessages.IO_ERROR_MSG, CATEGORY_ERROR, 2, LogLevel.ERROR),
    /**
     * A JSON occurred: %1$s
     */
    JSON_ERROR(OAuthExceptionMessages.JSON_ERROR_MSG, CATEGORY_ERROR, 3, LogLevel.ERROR),
    /**
     * Unknown OAuth service meta data: %1$s
     */
    UNKNOWN_OAUTH_SERVICE_META_DATA(OAuthExceptionMessages.UNKNOWN_OAUTH_SERVICE_META_DATA_MSG, CATEGORY_ERROR, 4, LogLevel.ERROR),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(OAuthExceptionMessages.SQL_ERROR_MSG, CATEGORY_ERROR, 5, LogLevel.ERROR),
    /**
     * Account not found with identifier %1$s for user %2$s in context %3$s.
     */
    ACCOUNT_NOT_FOUND(OAuthExceptionMessages.ACCOUNT_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 6, LogLevel.ERROR),
    /**
     * Unsupported OAuth service: %1$s
     */
    UNSUPPORTED_SERVICE(OAuthExceptionMessages.UNSUPPORTED_SERVICE_MSG, CATEGORY_ERROR, 7, LogLevel.ERROR),
    /**
     * Missing argument: %1$s
     */
    MISSING_ARGUMENT(OAuthExceptionMessages.MISSING_ARGUMENT_MSG, CATEGORY_USER_INPUT, 8, LogLevel.ERROR),
    /**
     * Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.
     */
    TOKEN_EXPIRED(OAuthExceptionMessages.TOKEN_EXPIRED_MSG, CATEGORY_TRY_AGAIN, 9, LogLevel.ERROR),
    /**
     * An OAuth error occurred: %1$s
     */
    OAUTH_ERROR(OAuthExceptionMessages.OAUTH_ERROR_MSG, CATEGORY_ERROR, 10, LogLevel.ERROR),
    /**
     * "The address %1 is not white-listed as for the %2 OAuth API"
     */
    NOT_A_WHITELISTED_URL(OAuthExceptionMessages.NOT_A_WHITELISTED_URL_MSG, CATEGORY_PERMISSION_DENIED, 11, LogLevel.ERROR),
    /**
     * The request sent was missing its body
     */
    MISSING_BODY(OAuthExceptionMessages.MISSING_BODY_MSG, CATEGORY_USER_INPUT, 12, LogLevel.ERROR),
    /**
     * The account is invalid, please recreate it.
     */
    INVALID_ACCOUNT(OAuthExceptionMessages.INVALID_ACCOUNT_MSG, CATEGORY_TRY_AGAIN, 13, LogLevel.ERROR),
    /**
     * Please provide a display name.
     */
    MISSING_DISPLAY_NAME(OAuthExceptionMessages.MISSING_DISPLAY_NAME_MSG, CATEGORY_USER_INPUT, 14, LogLevel.ERROR),
    /**
     * The associated OAuth provider denied the request: %1$s.
     */
    DENIED_BY_PROVIDER(OAuthExceptionMessages.DENIED_BY_PROVIDER_MSG, CATEGORY_USER_INPUT, 15, LogLevel.ERROR),
    /**
     * The OAuth authentication process has been canceled.
     */
    CANCELED_BY_USER(OAuthExceptionMessages.CANCELED_BY_USER_MSG, CATEGORY_USER_INPUT, 16, LogLevel.ERROR),
    ;

    private final Category category;
    private final int number;
    private final String message;
    private final LogLevel logLevel;

    private OAuthExceptionCodes(final String message, final Category category, final int detailNumber, final LogLevel logLevel) {
        this.message = message;
        this.number = detailNumber;
        this.category = category;
        this.logLevel = logLevel;
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
    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getHelp() {
        return null;
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
