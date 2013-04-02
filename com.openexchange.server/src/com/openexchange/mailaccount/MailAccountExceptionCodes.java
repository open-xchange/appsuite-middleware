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

package com.openexchange.mailaccount;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link MailAccountExceptionCodes} - The error messages for mail account exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailAccountExceptionCodes implements OXExceptionCode {

    /**
     * Unexpected error: %1$s.
     */
    UNEXPECTED_ERROR(MailAccountExceptionStrings.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * Cannot find mail account with identifier %1$s for user %2$s in context %3$s.
     */
    NOT_FOUND(MailAccountExceptionStrings.NOT_FOUND_MSG, CATEGORY_USER_INPUT, 2),
    /**
     * Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.
     */
    CONFLICT(MailAccountExceptionStrings.CONFLICT_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * A SQL error occurred: %1$s.
     */
    SQL_ERROR(MailAccountExceptionStrings.SQL_ERROR_MSG, CATEGORY_ERROR, 4),
    /**
     * A host could not be resolved: %1$s.
     */
    UNKNOWN_HOST_ERROR(MailAccountExceptionStrings.UNKNOWN_HOST_ERROR_MSG, CATEGORY_ERROR, 5),
    /**
     * Denied deletion of default mail account of user %1$s in context %2$s.
     */
    NO_DEFAULT_DELETE(MailAccountExceptionStrings.NO_DEFAULT_DELETE_MSG, CATEGORY_PERMISSION_DENIED, 6),
    /**
     * Denied update of default mail account of user %1$s in context %2$s.
     */
    NO_DEFAULT_UPDATE(MailAccountExceptionStrings.NO_DEFAULT_UPDATE_MSG, CATEGORY_PERMISSION_DENIED, 7),
    /**
     * No duplicate default account allowed.
     */
    NO_DUPLICATE_DEFAULT(MailAccountExceptionStrings.NO_DUPLICATE_DEFAULT_MSG, CATEGORY_USER_INPUT, 8),
    /**
     * Password encryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).
     */
    PASSWORD_ENCRYPTION_FAILED(MailAccountExceptionStrings.PASSWORD_ENCRYPTION_FAILED_MSG, CATEGORY_ERROR, 9),
    /**
     * Password decryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).
     */
    PASSWORD_DECRYPTION_FAILED(MailAccountExceptionStrings.PASSWORD_DECRYPTION_FAILED_MSG, CATEGORY_ERROR, 10),
    /**
     * The Unified Mail account already exists for user %1$s in context %2$s.
     */
    DUPLICATE_UNIFIED_INBOX_ACCOUNT(MailAccountExceptionStrings.DUPLICATE_UNIFIED_INBOX_ACCOUNT_MSG, CATEGORY_USER_INPUT, 11),
    /**
     * Mail account creation failed.
     */
    CREATION_FAILED(MailAccountExceptionStrings.CREATION_FAILED_MSG, CATEGORY_USER_INPUT, 12),
    /**
     * Mail account validation failed.
     */
    VALIDATION_FAILED(MailAccountExceptionStrings.VALIDATION_FAILED_MSG, CATEGORY_USER_INPUT, 13),
    /**
     * Multiple mail accounts not enabled for user %1$s in context %2$s.
     */
    NOT_ENABLED(MailAccountExceptionStrings.NOT_ENABLED_MSG, CATEGORY_PERMISSION_DENIED, 14),
    /**
     * Found two mail accounts with same email address %1$s for user %2$s in context %3$s.
     */
    CONFLICT_ADDR(MailAccountExceptionStrings.CONFLICT_ADDR_MSG, CATEGORY_USER_INPUT, 15),
    /**
     * Invalid mail account name: %1$s
     */
    INVALID_NAME(MailAccountExceptionStrings.INVALID_NAME_MSG, CATEGORY_USER_INPUT, 16),
    /**
     * Duplicate mail account for user %1$s in context %2$s.
     */
    DUPLICATE_MAIL_ACCOUNT(MailAccountExceptionStrings.DUPLICATE_MAIL_ACCOUNT_MSG, CATEGORY_USER_INPUT, 17),
    /**
     * Duplicate transport account for user %1$s in context %2$s.
     */
    DUPLICATE_TRANSPORT_ACCOUNT(MailAccountExceptionStrings.DUPLICATE_TRANSPORT_ACCOUNT_MSG, CATEGORY_USER_INPUT, 17),
    /**
     * Unable to parse mail server URI "%1$s".
     */
    URI_PARSE_FAILED(MailAccountExceptionStrings.URI_PARSE_FAILED_MSG, CATEGORY_ERROR, 18),
    /**
     * Invalid host name: %1$s
     */
    INVALID_HOST_NAME(MailAccountExceptionStrings.INVALID_HOST_NAME_MSG, CATEGORY_USER_INPUT, 19),

    ;

    private static final String PREFIX = "ACC";

    /**
     * Checks if specified {@code OXException}'s prefix is equal to this {@code OXExceptionCode} enumeration.
     *
     * @param e The {@code OXException} to check
     * @return <code>true</code> if prefix is equal; otherwise <code>false</code>
     */
    public static boolean hasPrefix(final OXException e) {
        if (null == e) {
            return false;
        }
        return PREFIX.equals(e.getPrefix());
    }

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
    private final int number;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param number number.
     */
    private MailAccountExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
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
        return number;
    }

    public String getHelp() {
        return null;
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
