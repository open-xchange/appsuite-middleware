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

package com.openexchange.mailaccount;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link MailAccountExceptionCodes} - The error messages for mail account exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailAccountExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Unexpected error: %1$s.
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s.", CATEGORY_ERROR, 1),
    /**
     * Cannot find mail account with identifier %1$s for user %2$s in context %3$s.
     */
    NOT_FOUND("Cannot find mail account with identifier %1$s for user %2$s in context %3$s.", CATEGORY_ERROR, 2, MailAccountExceptionStrings.NOT_FOUND_MSG_DISPLAY),
    /**
     * Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.
     */
    CONFLICT("Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.", CATEGORY_CONFLICT, 3, MailAccountExceptionStrings.CONFLICT_MSG_DISPLAY),
    /**
     * A SQL error occurred: %1$s.
     */
    SQL_ERROR("A SQL error occurred: %1$s.", CATEGORY_ERROR, 4, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * A host could not be resolved: %1$s.
     */
    UNKNOWN_HOST_ERROR("A host could not be resolved: %1$s.", CATEGORY_ERROR, 5, MailAccountExceptionStrings.UNKNOWN_HOST_ERROR_MSG_DISPLAY),
    /**
     * Denied deletion of default mail account of user %1$s in context %2$s.
     */
    NO_DEFAULT_DELETE("Denied deletion of default mail account of user %1$s in context %2$s.", CATEGORY_PERMISSION_DENIED, 6, MailAccountExceptionStrings.NO_DEFAULT_DELETE_MSG_DISPLAY),
    /**
     * Denied update of default mail account of user %1$s in context %2$s.
     */
    NO_DEFAULT_UPDATE("Denied update of default mail account of user %1$s in context %2$s.", CATEGORY_PERMISSION_DENIED, 7, MailAccountExceptionStrings.NO_DEFAULT_UPDATE_MSG_DISPLAY),
    /**
     * Denied update of attribute %1$s for default mail account of user %2$s in context %3$s.
     */
    NO_DEFAULT_UPDATE_ATTR("Denied update of attribute %1$s for default mail account of user %2$s in context %3$s.", CATEGORY_PERMISSION_DENIED, 7, MailAccountExceptionStrings.NO_DEFAULT_UPDATE_MSG_DISPLAY),
    /**
     * No duplicate default account allowed.
     */
    NO_DUPLICATE_DEFAULT("No duplicate default account allowed.", CATEGORY_USER_INPUT, 8, MailAccountExceptionStrings.NO_DUPLICATE_DEFAULT_MSG_DISPLAY),
    /**
     * Password encryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).
     */
    PASSWORD_ENCRYPTION_FAILED("Password encryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).", CATEGORY_ERROR, 9),
    /**
     * Password decryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).
     */
    PASSWORD_DECRYPTION_FAILED("Password decryption failed for login %1$s on server %2$s (user=%3$s, context=%4$s).", CATEGORY_ERROR, 10),
    /**
     * The Unified Mail account already exists for user %1$s in context %2$s.
     */
    DUPLICATE_UNIFIED_INBOX_ACCOUNT("The Unified Mail account already exists for user %1$s in context %2$s.", CATEGORY_USER_INPUT, 11, MailAccountExceptionStrings.DUPLICATE_UNIFIED_INBOX_ACCOUNT_MSG_DISPLAY),
    /**
     * It is not allowed to create a new mail account for Unified Mail with id %1$s.
     */
    UNIFIED_INBOX_ACCOUNT_CREATION_FAILED("It is not allowed to create a new mail account for Unified Mail with id %1$s.", CATEGORY_USER_INPUT, 12, MailAccountExceptionStrings.UNIFIED_INBOX_ACCOUNT_CREATION_FAILED_MSG_DISPLAY),
    /**
     * Validation for Unified Mail account failed.
     */
    UNIFIED_INBOX_ACCOUNT_VALIDATION_FAILED("Validation for Unified Mail account failed.", CATEGORY_USER_INPUT, 13, MailAccountExceptionStrings.UNIFIED_INBOX_ACCOUNT_VALIDATION_FAILED_MSG_DISPLAY),
    /**
     * Multiple mail accounts not enabled for user %1$s in context %2$s.
     */
    NOT_ENABLED("Multiple mail accounts not enabled for user %1$s in context %2$s.", CATEGORY_PERMISSION_DENIED, 14, MailAccountExceptionStrings.NOT_ENABLED_MSG_DISPLAY),
    /**
     * Found two mail accounts with same email address %1$s for user %2$s in context %3$s.
     */
    CONFLICT_ADDR("Found two mail accounts with same E-Mail address %1$s for user %2$s in context %3$s.", CATEGORY_USER_INPUT, 15, MailAccountExceptionStrings.CONFLICT_ADDR_MSG_DISPLAY),
    /**
     * Invalid mail account name: %1$s
     */
    INVALID_NAME("Invalid mail account name: %1$s", CATEGORY_USER_INPUT, 16, MailAccountExceptionStrings.INVALID_NAME_MSG_DISPLAY),
    /**
     * Duplicate mail account for user %1$s in context %2$s.
     */
    DUPLICATE_MAIL_ACCOUNT("Duplicate mail account for user %1$s in context %2$s.", CATEGORY_USER_INPUT, 17, MailAccountExceptionStrings.DUPLICATE_MAIL_ACCOUNT_MSG_DISPLAY),
    /**
     * Duplicate transport account for user %1$s in context %2$s.
     */
    DUPLICATE_TRANSPORT_ACCOUNT("Duplicate transport account for user %1$s in context %2$s.", CATEGORY_ERROR, 17, MailAccountExceptionStrings.DUPLICATE_TRANSPORT_ACCOUNT_MSG_DISPLAY),
    /**
     * Unable to parse mail server URI "%1$s".
     */
    URI_PARSE_FAILED("Unable to parse mail server URI \"%1$s\".", CATEGORY_ERROR, 18, MailAccountExceptionStrings.INVALID_HOST_NAME_MSG_DISPLAY),
    /**
     * Invalid host name: %1$s
     */
    INVALID_HOST_NAME("Invalid host name: %1$s", CATEGORY_USER_INPUT, 19, MailAccountExceptionStrings.INVALID_HOST_NAME_MSG_DISPLAY),
    /**
     * Could not connect to mail server "%1$s" for login %2$s
     */
    VALIDATE_FAILED_MAIL("Could not connect to mail server \"%1$s\" for login %2$s", CATEGORY_WARNING, 20, MailAccountExceptionStrings.VALIDATE_FAILED_MAIL_MSG_DISPLAY),
    /**
     * Could not connect to transport server "%1$s" for login %2$s
     */
    VALIDATE_FAILED_TRANSPORT("Could not connect to transport server \"%1$s\" for login %2$s", CATEGORY_WARNING, 21, MailAccountExceptionStrings.VALIDATE_FAILED_TRANSPORT_MSG_DISPLAY),
    /**
     * Default port specified for mail protocol "%1$s", but SSL is enabled. Please review if appropriate.
     */
    DEFAULT_BUT_SECURE_MAIL("Default port specified for mail protocol \"%1$s\", but SSL is enabled. Please review if appropriate.", CATEGORY_WARNING, 22, MailAccountExceptionStrings.DEFAULT_BUT_SECURE_MAIL_MSG_DISPLAY),
    /**
     * Secure port specified for mail protocol "%1$s", but SSL is not enabled. Please review if appropriate.
     */
    SECURE_BUT_DEFAULT_MAIL("Secure port specified for mail protocol \"%1$s\", but SSL is not enabled. Please review if appropriate.", CATEGORY_WARNING, 23, MailAccountExceptionStrings.SECURE_BUT_DEFAULT_MAIL_MSG_DISPLAY),
    /**
     * Default port specified for transport protocol "%1$s", but SSL is enabled. Please review if appropriate.
     */
    DEFAULT_BUT_SECURE_TRANSPORT("Default port specified for transport protocol \"%1$s\", but SSL is enabled. Please review if appropriate.", CATEGORY_WARNING, 24, MailAccountExceptionStrings.DEFAULT_BUT_SECURE_TRANSPORT_MSG_DISPLAY),
    /**
     * Secure port specified for transport protocol "%1$s", but SSL is not enabled. Please review if appropriate.
     */
    SECURE_BUT_DEFAULT_TRANSPORT("Secure port specified for transport protocol \"%1$s\", but SSL is not enabled. Please review if appropriate.", CATEGORY_WARNING, 25, MailAccountExceptionStrings.SECURE_BUT_DEFAULT_TRANSPORT_MSG_DISPLAY),
    /**
     * Mail account creation denied. The host/server name "%1$s" is covered by specified IP range back-list.
     */
    BLACKLISTED_SERVER("Mail account creation denied. The host/server name \"%1$s\" is covered by specified IP range back-list.", CATEGORY_WARNING, 25, MailAccountExceptionStrings.BLACKLISTED_SERVER_MSG_DISPLAY),
    /**
     * The protocol must not changed from %1$s to %2$s for user %3$s in context %4$s.
     */
    PROTOCOL_CHANGE("The protocol must not changed from %1$s to %2$s for user %3$s in context %4$s.", CATEGORY_USER_INPUT, 26, MailAccountExceptionStrings.PROTOCOL_CHANGE_MSG_DISPLAY),

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
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param number number.
     */
    private MailAccountExceptionCodes(final String message, final Category category, final int number) {
        this(message, category, number, null);
    }

    /**
     * Default constructor.
     *
     * @param message
     * @param category
     * @param number
     * @param displayMessage
     */
    private MailAccountExceptionCodes(final String message, final Category category, final int number, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
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
