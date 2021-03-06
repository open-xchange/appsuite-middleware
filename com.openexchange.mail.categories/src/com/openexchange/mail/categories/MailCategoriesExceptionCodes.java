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

package com.openexchange.mail.categories;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link MailCategoriesExceptionCodes}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public enum MailCategoriesExceptionCodes implements DisplayableOXExceptionCode {
    
    /**
     * The user category %1$s does not exist.
     */
    USER_CATEGORY_DOES_NOT_EXIST("The user category %1$s does not exist.", CATEGORY_USER_INPUT, 1, MailCategoriesExceptionStrings.USER_CATEGORY_DOES_NOT_EXIST),

    /**
     * Invalid configuration: %1$s
     */
    INVALID_CONFIGURATION_EXTENDED("Invalid configuration: %1$s", CATEGORY_USER_INPUT, 2, MailCategoriesExceptionStrings.INVALID_CONFIGURATION_EXTENDED),

    /**
     * Invalid configuration.
     */
    INVALID_CONFIGURATION("Invalid configuration.", CATEGORY_USER_INPUT, 3, MailCategoriesExceptionStrings.INVALID_CONFIGURATION),

    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 4, null),

    /**
     * The required service %1$s is temporary not available. Please try again later.
     */
    SERVICE_UNAVAILABLE("The required service %1$s is temporary not available. Please try again later.", Category.CATEGORY_TRY_AGAIN, 5, MailCategoriesExceptionStrings.SERVICE_UNAVAILABLE_MSG),
    
    /**
     * You are not allowed to change the name of the category %1$s.
     */
    CHANGE_NAME_NOT_ALLOWED("You are not allowed to change the name of the category %1$s.", Category.CATEGORY_USER_INPUT, 6, MailCategoriesExceptionStrings.CHANGE_NAME_NOT_ALLOWED),

    /**
     * You are not allowed to enable or disable the category %1$s.
     */
    SWITCH_NOT_ALLOWED("You are not allowed to enable or disable the category %1$s.", Category.CATEGORY_USER_INPUT, 8, MailCategoriesExceptionStrings.SWITCH_NOT_ALLOWED),

    ;

    private static final String PREFIX = "CATEGORIES";

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
    private MailCategoriesExceptionCodes(final String message, final Category category, final int number) {
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
    private MailCategoriesExceptionCodes(final String message, final Category category, final int number, final String displayMessage) {
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
