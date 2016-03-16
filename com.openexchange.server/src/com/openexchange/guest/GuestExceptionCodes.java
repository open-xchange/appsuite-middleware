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

package com.openexchange.guest;


import static com.openexchange.exception.OXExceptionStrings.SQL_ERROR_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 *
 * Exception codes for guest administration purposes.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public enum GuestExceptionCodes implements DisplayableOXExceptionCode {

    /** Unexpected database error: \"%1$s\" */
    DB_ERROR("Unexpected database error: \"%1$s\"", SQL_ERROR_MSG, Category.CATEGORY_WARNING, 1),

    /**
     * No connection to database.
     */
    NO_CONNECTION(GuestExceptionCodes.NO_CONNECTION_TO_GUEST_STORAGE_MSG, Category.CATEGORY_SERVICE_DOWN, 2),
    /**
     * No connection to database.
     */
    NO_CONNECTION_PROVIDED(GuestExceptionCodes.NO_CONNECTION_PROVIDED_TO_CONNECT_TO_GUEST_STORAGE_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * SQL problem: %1$s.
     */
    SQL_ERROR("SQL problem: %1$s.", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 4),

    INVALID_EMAIL_ADDRESS(GuestExceptionMessage.INVALID_EMAIL_ADDRESS_MSG, GuestExceptionMessage.INVALID_EMAIL_ADDRESS_MSG, Category.CATEGORY_USER_INPUT, 5),

    EMTPY_EMAIL_ADDRESS(GuestExceptionMessage.EMTPY_EMAIL_ADDRESS_MSG, GuestExceptionMessage.EMTPY_EMAIL_ADDRESS_MSG, Category.CATEGORY_USER_INPUT, 6),

    GUEST_CREATION_ERROR(GuestExceptionCodes.GUEST_CREATION_ERROR_MSG, Category.CATEGORY_ERROR, 7),

    GUEST_UPDATE_ERROR(GuestExceptionCodes.GUEST_UPDATE_ERROR_MSG, Category.CATEGORY_ERROR, 8),

    PASSWORD_RESET_ERROR(GuestExceptionCodes.PASSWORD_RESET_ERROR_MSG, Category.CATEGORY_ERROR, 9),

    TOO_MANY_GUESTS_REMOVED(GuestExceptionCodes.TOO_MANY_GUESTS_REMOVED_MSG, Category.CATEGORY_ERROR, 10),

    PASSWORD_EMPTY_ERROR(GuestExceptionCodes.PASSWORD_EMPTY_MSG, GuestExceptionMessage.PASSWORD_EMPTY_MSG, Category.CATEGORY_USER_INPUT, 11),

    GUEST_WITHOUT_ASSIGNMENT_ERROR(GuestExceptionCodes.GUEST_WITHOUT_ASSIGNMENT_MSG, Category.CATEGORY_ERROR, 12),

    CONTEXT_GUESTS_DELETION_ERROR(GuestExceptionCodes.CONTEXT_GUESTS_DELETION_ERROR_MSG, Category.CATEGORY_ERROR, 13),

    ;

    private final static String TOO_MANY_GUESTS_REMOVED_MSG = "There have been %1$s guests removed but there should max be 1. Executed SQL: %2$s.";

    private final static String PASSWORD_EMPTY_MSG = "The new password to for guest with mail address %1$s is empty. Password update not possible!";

    private final static String PASSWORD_RESET_ERROR_MSG = "Error while resetting password for user with mail address: %1$s.";

    private final static String NO_CONNECTION_TO_GUEST_STORAGE_MSG = "Could not connect to guest storage.";

    private final static String NO_CONNECTION_PROVIDED_TO_CONNECT_TO_GUEST_STORAGE_MSG = "No connection provided to connect to guest storage.";

    private final static String GUEST_CREATION_ERROR_MSG = "The guest cannot be created due to an internal server error.";

    private final static String GUEST_UPDATE_ERROR_MSG = "Guest contact/user that should update all related contacts/users in different contexts is null. Cannot update related guests.";

    private final static String GUEST_WITHOUT_ASSIGNMENT_MSG = "No assignment for the guest with mail address %1$s found. This might indicate incosistences as there is a guest user without assignments. Guest id: %2$s.";

    private final static String CONTEXT_GUESTS_DELETION_ERROR_MSG = "Inconsistences for deleting guest assignments: %1$s should be deleted but %2$s were deleted. Executed statement: %3$s";

    /**
     * (Log) Message of the exception.
     */
    private final String message;

    /**
     * Display message of the exception.
     */
    private final String displayMessage;

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
     * @param number detail number.
     */
    private GuestExceptionCodes(final String message, final Category category, final int number) {
        this(message, null, category, number);
    }

    /**
     * Default constructor.
     *
     * @param message message.
     * @param displayMessage message that might be displayed to the user
     * @param category category.
     * @param number detail number.
     */
    private GuestExceptionCodes(final String message, String displayMessage, final Category category, final int number) {
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return "GUE";
    }

    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * @return the category
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * @return the number
     */
    @Override
    public int getNumber() {
        return number;
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
