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

package com.openexchange.chronos.exception;

import static com.openexchange.chronos.exception.CalendarExceptionMessages.ATTENDEE_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.CONCURRENT_MODIFICATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.DATA_TRUNCATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.END_BEFORE_START_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.EVENT_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INCORRECT_STRING_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_CALENDAR_USER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_RRULE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MANDATORY_FIELD_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MOVE_OCCURRENCE_NOT_SUPPORTED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MOVE_SERIES_NOT_SUPPORTED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.NO_PERMISSION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UID_CONFLICT_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_CLASSIFICATION_MSG;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link CalendarExceptionCodes}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum CalendarExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * <li>The requested event was not found.</li>
     * <li>Event not found [id %1$d]</li>
     */
    EVENT_NOT_FOUND("Event not found [id %1$d]", EVENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4040),
    /**
     * <li>The requested event was not found.</li>
     * <li>Event not found in folder [folder %1$d, id %2$d]</li>
     */
    EVENT_NOT_FOUND_IN_FOLDER("Event not found in folder [folder %1$d, id %2$d]", EVENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4041),
    /**
     * <li>The requested event was not found.</li>
     * <li>Event recurrence not found [series id %1$d, recurrence id %2$s]</li>
     */
    EVENT_RECURRENCE_NOT_FOUND("Event recurrence not found [series id %1$d, recurrence id %2$s]", EVENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4042),
    /**
     * <li>The requested attendee was not found.</li>
     * <li>Attendee not found [attendee %1$s, event %1$d]</li>
     */
    ATTENDEE_NOT_FOUND("Attendee not found [attendee %1$s, event %1$d]", ATTENDEE_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4043),
    /**
     * <li>The targeted occurrence is not part of the event series. Please select a valid recurrence identifier and try again.</li>
     * <li>Invalid recurrence id [id %1$s, rule %2$s]</li>
     */
    INVALID_RECURRENCE_ID("Invalid recurrence id [id %1$s, rule %2$s]", INVALID_RRULE_MSG, Category.CATEGORY_USER_INPUT, 4044),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Insufficient read permissions in folder [folder %1$d]</li>
     */
    NO_READ_PERMISSION("Insufficient read permissions in folder [folder %1$d]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4030),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Insufficient write permissions in folder [folder %1$d]</li>
     */
    NO_WRITE_PERMISSION("Insufficient write permissions in folder [folder %1$d]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4031),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Insufficient delete permissions in folder [folder %1$d]</li>
     */
    NO_DELETE_PERMISSION("Insufficient delete permissions in folder [folder %1$d]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4032),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Forbidden attendee change [id %1$d, attendee %2$s, field %3$s]</li>
     */
    FORBIDDEN_ATTENDEE_CHANGE("Forbidden attendee change [id %1$d, attendee %2$s, field %3$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4033),
    /**
     * <li>The calendar user \"%1$s\" is invalid.</li>
     * <li>Invalid calendar user [uri %1$s, id %1$d, type %1$s]</li>
     */
    INVALID_CALENDAR_USER("Invalid calendar user [uri %1$s, id %1$d, type %1$s]", INVALID_CALENDAR_USER_MSG, Category.CATEGORY_USER_INPUT, 4034),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Forbidden change [id %1$d, field %2$s]</li>
     */
    FORBIDDEN_CHANGE("Forbidden change [id %1$d, field %2$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4035),
    /**
     * <li>The operation could not be completed due to a concurrent modification. Please reload the data and try again.</li>
     * <li>Concurrent modification [id %1$d, client timestamp %2$d, actual timestamp %3$d]</li>
     */
    CONCURRENT_MODIFICATION("Concurrent modification [id %1$d, client timestamp %2$d, actual timestamp %3$d]", CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 4120),
    /**
     * <li>The event could not be created due to another conflicting event with the same unique identifier.</li>
     * <li>UID conflict [uid %1$s, conflicting id %2$d]</li>
     */
    UID_CONFLICT("UID conflict [uid %1$s, conflicting id %2$d]", UID_CONFLICT_MSG, Category.CATEGORY_CONFLICT, 4090),
    /**
     * <li>The field \"%1$s\" is mandatory. Please supply a valid value and try again.</li>
     * <li>Mandatory field missing [field %1$s]</li>
     */
    MANDATORY_FIELD("Mandatory field missing [field %1$s]", MANDATORY_FIELD_MSG, Category.CATEGORY_USER_INPUT, 4220),
    /**
     * <li>The end date lies before the start date. Please correct the event times and try again.</li>
     * <li>End before start date [start %1$d, end %2$d]</li>
     */
    END_BEFORE_START("End before start date [start %1$d, end %2$d]", END_BEFORE_START_MSG, Category.CATEGORY_USER_INPUT, 4221),
    /**
     * <li>Events in non-personal folders must not be classified as \"private\" or \"confidential\".</li>
     * <li>Unsupported classification [classification %1$s, folder %2$d, type %3$s]</li>
     */
    UNSUPPORTED_CLASSIFICATION("Unsupported classification [classification %1$s, folder %2$d, type %3$s]", UNSUPPORTED_CLASSIFICATION_MSG, Category.CATEGORY_USER_INPUT, 4222),
    /**
     * <li>The supplied recurrence rule is invalid. Please correct the rule and try again.</li>
     * <li>Invalid recurrence rule [rule %1$s]</li>
     */
    INVALID_RRULE("Invalid recurrence rule [rule %1$s]", INVALID_RRULE_MSG, Category.CATEGORY_USER_INPUT, 4223),
    /**
     * <li>Moving an event series into another folder is not supported.</li>
     * <li>Unsupported series move [id %1$d, folder %2$d, target folder %3$d]</li>
     */
    MOVE_SERIES_NOT_SUPPORTED("Unsupported series move [id %1$d, folder %2$d, target folder %3$d]", MOVE_SERIES_NOT_SUPPORTED_MSG, Category.CATEGORY_USER_INPUT, 4224),
    /**
     * <li>Moving an occurrence of an event series into another folder is not supported.</li>
     * <li>Unsupported occurrence move [id %1$d, folder %2$d, target folder %3$d]</li>
     */
    MOVE_OCCURRENCE_NOT_SUPPORTED("Unsupported occurrence move [id %1$d, folder %2$d, target folder %3$d]", MOVE_OCCURRENCE_NOT_SUPPORTED_MSG, Category.CATEGORY_USER_INPUT, 4225),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Unexpected database error [%1$s]</li>
     */
    DB_ERROR("Unexpected database error [%1$s]", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 5000),
    /**
     * <li>Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$d, current: %3$d) and try again.</li>
     * <li>Data truncation [field %1$s, limit %2$d, current %3$d]</li>
     */
    DATA_TRUNCATION("Data truncation [field %1$s, limit %2$d, current %3$d]", DATA_TRUNCATION_MSG, Category.CATEGORY_CAPACITY, 5070),
    /**
     * <li>The character \"%1$s\" in field \"%2$s\" can't be saved. Please remove the problematic character and try again.</li>
     * <li>Incorrect string [string %1$s, field %2$s, column %3$s]</li>
     */
    INCORRECT_STRING("Incorrect string [string %1$s, field %2$s, column %3$s]", INCORRECT_STRING_MSG, Category.CATEGORY_USER_INPUT, 4227),

    ;

    public static final String PREFIX = "CAL".intern();

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    private CalendarExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = null != displayMessage ? displayMessage : MESSAGE;
        this.category = category;
        this.number = number;
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
    public String getDisplayMessage() {
        return displayMessage;
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
    public boolean equals(OXException e) {
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
