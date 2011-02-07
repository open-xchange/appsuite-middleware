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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.oauth.exception.OAuthExceptionFactory;

/**
 * {@link OAuthExceptionCodes} - Enumeration of all {@link OAuthException}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public enum OAuthExceptionCodes implements OXErrorMessage {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(OAuthExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CODE_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(OAuthExceptionMessages.IO_ERROR_MSG, Category.CODE_ERROR, 2),
    /**
     * An I/O error occurred: %1$s
     */
    JSON_ERROR(OAuthExceptionMessages.JSON_ERROR_MSG, Category.CODE_ERROR, 3),
    /**
     * Unknown OAuth service meta data: %1$s
     */
    UNKNOWN_OAUTH_SERVICE_META_DATA(OAuthExceptionMessages.UNKNOWN_OAUTH_SERVICE_META_DATA_MSG, Category.CODE_ERROR, 4),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(OAuthExceptionMessages.SQL_ERROR_MSG, Category.CODE_ERROR, 5),
    /**
     * Account not found with identifier %1$s for user %2$s in context %3$s.
     */
    ACCOUNT_NOT_FOUND(OAuthExceptionMessages.ACCOUNT_NOT_FOUND_MSG, Category.CODE_ERROR, 6),
    /**
     * Unsupported OAuth service: %1$s
     */
    UNSUPPORTED_SERVICE(OAuthExceptionMessages.UNSUPPORTED_SERVICE_MSG, Category.CODE_ERROR, 7),
    /**
     * Missing argument: %1$s
     */
    MISSING_ARGUMENT(OAuthExceptionMessages.MISSING_ARGUMENT_MSG, Category.CODE_ERROR, 8),
    
    /**
     * Token has expired
     */
    TOKEN_EXPIRED(OAuthExceptionMessages.TOKEN_EXPIRED, Category.TRY_AGAIN, 9);

    ;

    private final Category category;

    private final int detailNumber;

    private final String message;

    private OAuthExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getHelp() {
        return null;
    }

    private static final Object[] EMPTY = new Object[0];

    /**
     * Creates a new OAuth exception of this error type with no message arguments.
     * 
     * @return A new OAuth exception
     */
    public OAuthException create() {
        return OAuthExceptionFactory.getInstance().create(this, EMPTY);
    }

    /**
     * Creates a new OAuth exception of this error type with specified message arguments.
     * 
     * @param messageArgs The message arguments
     * @return A new OAuth exception
     */
    public OAuthException create(final Object... messageArgs) {
        return OAuthExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new OAuth exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new OAuth exception
     */
    public OAuthException create(final Throwable cause, final Object... messageArgs) {
        return OAuthExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
