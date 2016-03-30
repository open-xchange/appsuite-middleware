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


package com.openexchange.groupware.reminder;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

public enum ReminderExceptionCode implements DisplayableOXExceptionCode {
    /**
     * User is missing for the reminder.
     */
    MANDATORY_FIELD_USER("User is missing for the reminder.", ReminderExceptionMessage.MANDATORY_FIELD_USER_DISPLAY, 1,
        CATEGORY_USER_INPUT),
        
    /**
     * Identifier of the object is missing.
     */
    MANDATORY_FIELD_TARGET_ID("Object identifier is missing.", ReminderExceptionMessage.MANDATORY_FIELD_TARGET_ID_DISPLAY, 2,
        CATEGORY_USER_INPUT),
        
    /**
     * Alarm date for the reminder is missing.
     */
    MANDATORY_FIELD_ALARM("Alarm date for the reminder is missing.", ReminderExceptionMessage.MANDATORY_FIELD_ALARM_DISPLAY, 3,
        CATEGORY_USER_INPUT),
        
    INSERT_EXCEPTION("Unable to insert reminder", ReminderExceptionMessage.INSERT_EXCEPTION_DISPLAY, 4, CATEGORY_ERROR),
    
    UPDATE_EXCEPTION("Unable to update reminder", ReminderExceptionMessage.UPDATE_EXCEPTION_DISPLAY, 5, CATEGORY_ERROR),
    
    DELETE_EXCEPTION("Unable to delete reminder", ReminderExceptionMessage.DELETE_EXCEPTION_DISPLAY, 6, CATEGORY_ERROR),
    
    LOAD_EXCEPTION("Unable to load reminder", ReminderExceptionMessage.LOAD_EXCEPTION_DISPLAY, 7, CATEGORY_ERROR),
    
    LIST_EXCEPTION("Unable to list reminder", ReminderExceptionMessage.LIST_EXCEPTION_DISPLAY, 8, CATEGORY_ERROR),
    
    /** Can not find reminder with identifier %1$d in context %2$d. */
    NOT_FOUND("Reminder with identifier %1$d can not be found in context %2$d.", ReminderExceptionMessage.NOT_FOUND_DISPLAY, 9,
        CATEGORY_ERROR),
    
    /**
     * Folder of the object is missing.
     */
    MANDATORY_FIELD_FOLDER("Object folder is missing", ReminderExceptionMessage.MANDATORY_FIELD_FOLDER_DISPLAY, 10, CATEGORY_USER_INPUT),
    
    /**
     * Module type of the object is missing.
     */
    MANDATORY_FIELD_MODULE("Object's module type is missing", ReminderExceptionMessage.MANDATORY_FIELD_MODULE_DISPLAY, 11,
        CATEGORY_USER_INPUT),
    
    /**
     * Updated too many reminders.
     */
    TOO_MANY("Updated too many reminders.", ReminderExceptionMessage.TOO_MANY_DISPLAY, 12, CATEGORY_ERROR),
    
    /** SQL Problem: %1$s. */
    SQL_ERROR("SQL Problem: \"%1$s\"", OXExceptionStrings.SQL_ERROR_MSG, 13, CATEGORY_ERROR),
    
    /** No target service is registered for module %1$d. */
    NO_TARGET_SERVICE("No target service is registered for module %1$d.", ReminderExceptionMessage.NO_TARGET_SERVICE_DISPLAY, 14,
        CATEGORY_ERROR),
    
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", null, 15, CATEGORY_ERROR),
    
    /**
     * Reminder identifier is missing.
     */
    MANDATORY_FIELD_ID("Reminder identifier is missing.", ReminderExceptionMessage.MANDATORY_FIELD_ID_DISPLAY, 16, CATEGORY_USER_INPUT);

    /**
     * Message of the exception.
     */
    private final String message;
    
    private final String displayMessage;

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
    private ReminderExceptionCode(final String message, final String displayMessage, final int detailNumber, final Category category) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "REM";
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
