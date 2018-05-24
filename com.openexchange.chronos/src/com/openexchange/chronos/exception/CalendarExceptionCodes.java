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

import static com.openexchange.chronos.exception.CalendarExceptionMessages.ACCOUNT_DISABLED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.ACCOUNT_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.ATTACHMENT_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.ATTENDEE_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.AUTH_FAILED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.BAD_AUTH_CONFIGURATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.CONCURRENT_MODIFICATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.DATA_TRUNCATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.END_BEFORE_START_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.EVENT_CONFLICTS_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.EVENT_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.FOLDER_NOT_FOUND_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.IGNORED_INVALID_DATA_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INCOMPATIBLE_DATE_TYPES_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INCORRECT_STRING_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_ALARM_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_CALENDAR_USER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_CONFIGURATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_GEO_LOCATION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_RECURRENCE_ID_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_RRULE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_SPLIT_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.INVALID_TIMEZONE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.IO_ERROR_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MANDATORY_FIELD_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MAX_ACCOUNTS_EXCEEDED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MISSING_CAPABILITY_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MISSING_ORGANIZER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MOVE_OCCURRENCE_NOT_SUPPORTED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.MOVE_SERIES_NOT_SUPPORTED_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.NOT_ORGANIZER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.NO_PERMISSION_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.PROVIDER_NOT_AVAILABLE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.QUERY_TOO_SHORT_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.TOO_MANY_ALARMS_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.TOO_MANY_ATTENDEES_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.TOO_MANY_EVENTS_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UID_CONFLICT_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_CLASSIFICATION_FOR_FOLDER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_CLASSIFICATION_FOR_MOVE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_CLASSIFICATION_FOR_OCCURRENCE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_CLASSIFICATION_FOR_RESOURCE_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_DATA_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_FOLDER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_OPERATION_FOR_PROVIDER_MSG;
import static com.openexchange.chronos.exception.CalendarExceptionMessages.UNSUPPORTED_RRULE_MSG;
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
     * <li>The requested appointment was not found.</li>
     * <li>Event not found [id %1$s]</li>
     */
    EVENT_NOT_FOUND("Event not found [id %1$s]", EVENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4040),
    /**
     * <li>The requested appointment was not found.</li>
     * <li>Event not found in folder [folder %1$s, id %2$s]</li>
     */
    EVENT_NOT_FOUND_IN_FOLDER("Event not found in folder [folder %1$s, id %2$s]", EVENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4041),
    /**
     * <li>The requested appointment was not found.</li>
     * <li>Event recurrence not found [series id %1$s, recurrence id %2$s]</li>
     */
    EVENT_RECURRENCE_NOT_FOUND("Event recurrence not found [series id %1$s, recurrence id %2$s]", EVENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4042),
    /**
     * <li>The requested participant was not found.</li>
     * <li>Attendee not found [attendee %1$d, event %2$s]</li>
     */
    ATTENDEE_NOT_FOUND("Attendee not found [attendee %1$d, event %2$s]", ATTENDEE_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4043),
    /**
     * <li>The requested calendar account was not found.</li>
     * <li>Account not found [account %1$d]</li>
     */
    ACCOUNT_NOT_FOUND("Account not found [account %1$d]", ACCOUNT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4044),
    /**
     * <li>The requested folder was not found.</li>
     * <li>Folder not found [folder %1$s]</li>
     */
    FOLDER_NOT_FOUND("Folder not found [folder %1$s]", FOLDER_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4045),
    /**
     * <li>The requested attachment was not found.</li>
     * <li>Attachment not found [attachment %1$d, event %2$s, folder %3$s]</li>
     */
    ATTACHMENT_NOT_FOUND("Attachment not found [attachment %1$d, event %2$s, folder %3$s]", ATTACHMENT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 4047),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Insufficient read permissions in folder [folder %1$s]</li>
     */
    NO_READ_PERMISSION("Insufficient read permissions in folder [folder %1$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4030),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Insufficient write permissions in folder [folder %1$s]</li>
     */
    NO_WRITE_PERMISSION("Insufficient write permissions in folder [folder %1$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4031),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Insufficient delete permissions in folder [folder %1$s]</li>
     */
    NO_DELETE_PERMISSION("Insufficient delete permissions in folder [folder %1$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4032),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Forbidden attendee change [id %1$s, attendee %2$s, field %3$s]</li>
     */
    FORBIDDEN_ATTENDEE_CHANGE("Forbidden attendee change [id %1$s, attendee %2$s, field %3$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4033),
    /**
     * <li>The calendar user \"%1$s\" is invalid.</li>
     * <li>Invalid calendar user [uri %1$s, id %2$d, type %3$s]</li>
     */
    INVALID_CALENDAR_USER("Invalid calendar user [uri %1$s, id %2$d, type %3$s]", INVALID_CALENDAR_USER_MSG, Category.CATEGORY_USER_INPUT, 4034),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Forbidden change [id %1$s, field %2$s]</li>
     */
    FORBIDDEN_CHANGE("Forbidden change [id %1$s, field %2$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4035),
    /**
     * <li>The operation could not be completed due to insufficient capabilities.</li>
     * <li>Missing capability [%1$s]</li>
     */
    MISSING_CAPABILITY("Missing capability [%1$s]", MISSING_CAPABILITY_MSG, Category.CATEGORY_PERMISSION_DENIED, 4036),
    /**
     * <li>The operation could not be completed due to insufficient permissions.</li>
     * <li>Access to event restricted by classification [folder %1$s, id %2$s, classification %3$s]</li>
     */
    RESTRICTED_BY_CLASSIFICATION("Access to event restricted by classification [folder %1$s, id %2$s, classification %3$s]", NO_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 4037),
    /**
     * <li>This modification can only be performed by the organizer of the appointment.</li>
     * <li>Modification restricted to organizer [folder %1$s, id %2$s, organizerUri %3$s, organizerCn %4$s]</li>
     */
    NOT_ORGANIZER("Modification restricted to organizer [folder %1$s, id %2$s, organizerUri %3$s, organizerCn %4$s]", NOT_ORGANIZER_MSG, Category.CATEGORY_PERMISSION_DENIED, 4038),
    /**
     * <li>Organizer must also be an attendee.</li>
     * <li>Missing organizer.</li>
     */
    MISSING_ORGANIZER("Missing organizer.", MISSING_ORGANIZER_MSG, Category.CATEGORY_USER_INPUT, 4039),
    /**
     * <li>The supplied folder is not supported. Please select a valid folder and try again.</li>
     * <li>Unsupported folder [folder %1$s, content type %2$s]</li>
     */
    UNSUPPORTED_FOLDER("Unsupported folder [folder %1$s, content type %2$s]", UNSUPPORTED_FOLDER_MSG, Category.CATEGORY_USER_INPUT, 4060),
    /**
     * <li>The targeted occurrence is not part of the appointment series. Please select a valid recurrence identifier and try again.</li>
     * <li>Invalid recurrence id [id %1$s, rule %2$s]</li>
     */
    INVALID_RECURRENCE_ID("Invalid recurrence id [id %1$s, rule %2$s]", INVALID_RECURRENCE_ID_MSG, Category.CATEGORY_USER_INPUT, 4061),
    /**
     * <li>In order to accomplish the search, %1$d or more characters are required.</li>
     * <li>Query too short [minimum %1$d, query %2$s]</li>
     */
    QUERY_TOO_SHORT("Query too short [minimum %1$d, query %2$s]", QUERY_TOO_SHORT_MSG, Category.CATEGORY_USER_INPUT, 4062),
    /**
     * <li>The split of the appointment series cannot be performed. Please select a valid split point and try again.</li>
     * <li>Invalid split [id %1$s, split point %2$s]</li>
     */
    INVALID_SPLIT("Invalid split [id %1$s, split point %2$s]", INVALID_SPLIT_MSG, Category.CATEGORY_USER_INPUT, 4063),
    /**
     * <li>The operation could not be completed due to a concurrent modification. Please reload the data and try again.</li>
     * <li>Concurrent modification [id %1$s, client timestamp %2$d, actual timestamp %3$d]</li>
     */
    CONCURRENT_MODIFICATION("Concurrent modification [id %1$s, client timestamp %2$d, actual timestamp %3$d]", CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 4120),
    /**
     * <li>The appointment could not be created due to another conflicting appointment with the same unique identifier.</li>
     * <li>UID conflict [uid %1$s, conflicting id %2$s]</li>
     */
    UID_CONFLICT("UID conflict [uid %1$s, conflicting id %2$s]", UID_CONFLICT_MSG, Category.CATEGORY_CONFLICT, 4090),
    /**
     * <li>The appointment conflicts with one or more other appointments.</li>
     * <li>Event conflicts detected [see problematics]</li>
     */
    EVENT_CONFLICTS("Event conflicts detected [see problematics]", EVENT_CONFLICTS_MSG, Category.CATEGORY_CONFLICT, 4091),
    /**
     * <li>The appointment conflicts with one or more other appointments.</li>
     * <li>(Hard) event conflicts detected [see problematics]</li>
     */
    HARD_EVENT_CONFLICTS("(Hard) event conflicts detected [see problematics]", EVENT_CONFLICTS_MSG, Category.CATEGORY_CONFLICT, 4092),
    /**
     * <li>The field \"%1$s\" is mandatory. Please supply a valid value and try again.</li>
     * <li>Mandatory field missing [field %1$s]</li>
     */
    MANDATORY_FIELD("Mandatory field missing [field %1$s]", MANDATORY_FIELD_MSG, Category.CATEGORY_USER_INPUT, 4220),
    /**
     * <li>The end date lies before the start date. Please correct the appointment times and try again.</li>
     * <li>End before start date [start %1$s, end %2$s]</li>
     */
    END_BEFORE_START("End before start date [start %1$s, end %2$s]", END_BEFORE_START_MSG, Category.CATEGORY_USER_INPUT, 4221),
    /**
     * <li>Appointments in non-personal folders must not be classified as \"private\" or \"secret\".</li>
     * <li>Unsupported classification [classification %1$s, folder %2$d, type %3$s]</li>
     */
    UNSUPPORTED_CLASSIFICATION_FOR_FOLDER("Unsupported classification [classification %1$s, folder %2$d, type %3$s]", UNSUPPORTED_CLASSIFICATION_FOR_FOLDER_MSG, Category.CATEGORY_USER_INPUT, 4222),
    /**
     * <li>Appointments with resources must not be classified as \"secret\".".</li>
     * <li>Unsupported classification [classification %1$s, attendee %2$d]</li>
     */
    UNSUPPORTED_CLASSIFICATION_FOR_RESOURCE("Unsupported classification [classification %1$s, attendee %2$d]", UNSUPPORTED_CLASSIFICATION_FOR_RESOURCE_MSG, Category.CATEGORY_USER_INPUT, 42210),
    /**
     * <li>The supplied recurrence rule is not supported. Please use adjust the rule and try again.</li>
     * <li>Unsupported recurrence rule [rule %1$s, part %2$s, error %3$s]</li>
     */
    UNSUPPORTED_RRULE("Unsupported recurrence rule [rule %1$s, part %2$s, error %3$s]", UNSUPPORTED_RRULE_MSG, Category.CATEGORY_USER_INPUT, 4223),
    /**
     * <li>Moving an appointment series into another folder is not supported.</li>
     * <li>Unsupported series move [id %1$s, folder %2$s, target folder %3$s]</li>
     */
    MOVE_SERIES_NOT_SUPPORTED("Unsupported series move [id %1$s, folder %2$s, target folder %3$s]", MOVE_SERIES_NOT_SUPPORTED_MSG, Category.CATEGORY_USER_INPUT, 4224),
    /**
     * <li>Moving an occurrence of an appointment series into another folder is not supported.</li>
     * <li>Unsupported occurrence move [id %1$s, folder %2$s, target folder %3$s]</li>
     */
    MOVE_OCCURRENCE_NOT_SUPPORTED("Unsupported occurrence move [id %1$s, folder %2$s, target folder %3$s]", MOVE_OCCURRENCE_NOT_SUPPORTED_MSG, Category.CATEGORY_USER_INPUT, 4225),
    /**
     * <li>Appointments classified as \"private\" or \"confidential\" cannot be moved to this type of folder.</li>
     * <li>Unsupported classification for move [classification %1$s, folder %2$s, type %3$s, target folder %4$s, target type %5$s]</li>
     */
    UNSUPPORTED_CLASSIFICATION_FOR_MOVE("Unsupported classification for move [classification %1$s, folder %2$s, type %3$s, target folder %4$s, target type %5$s]", UNSUPPORTED_CLASSIFICATION_FOR_MOVE_MSG, Category.CATEGORY_USER_INPUT, 4226),
    /**
     * <li>Occurrences of appointment series must not be classified differently.</li>
     * <li>Unsupported classification for occurrence [classification %1$s, series id %2$s, recurrence id %3$s]</li>
     */
    UNSUPPORTED_CLASSIFICATION_FOR_OCCURRENCE("Unsupported classification for occurrence [classification %1$s, series id %2$s, recurrence id %3$s]", UNSUPPORTED_CLASSIFICATION_FOR_OCCURRENCE_MSG, Category.CATEGORY_USER_INPUT, 4227),
    /**
     * <li>The requested operation is not supported for calendar provider \"%1$s\".</li>
     * <li>Unsupported operation for calendar provider [provider %1$s]</li>
     */
    UNSUPPORTED_OPERATION_FOR_PROVIDER("Unsupported operation for calendar provider [provider %1$s]", UNSUPPORTED_OPERATION_FOR_PROVIDER_MSG, Category.CATEGORY_USER_INPUT, 4228),
    /**
     * <li>The supplied types of start and end date are incompatible. Please correct the appointment times and try again.</li>
     * <li>Incompatible date types [start %1$s, end %2$s]</li>
     */
    INCOMPATIBLE_DATE_TYPES("Incompatible date types [start %1$s, end %2$s]", INCOMPATIBLE_DATE_TYPES_MSG, Category.CATEGORY_USER_INPUT, 4229),
    /**
     * <li>The supplied timezone is invalid. Please select a valid timezone and try again.</li>
     * <li>Invalid timezone [timezone id %1$s]</li>
     */
    INVALID_TIMEZONE("Invalid timezone [timezone id %1$s]", INVALID_TIMEZONE_MSG, Category.CATEGORY_USER_INPUT, 4001),
    /**
     * <li>The supplied recurrence rule is invalid. Please correct the rule and try again.</li>
     * <li>Invalid recurrence rule [rule %1$s]</li>
     */
    INVALID_RRULE("Invalid recurrence rule [rule %1$s]", INVALID_RRULE_MSG, Category.CATEGORY_USER_INPUT, 4002),
    /**
     * <li>The supplied geographical location is invalid. Please select valid coordinates and try again.</li>
     * <li>Invalid geo location [geo %1$s]</li>
     */
    INVALID_GEO_LOCATION("Invalid geo location [geo %1$s]", INVALID_GEO_LOCATION_MSG, Category.CATEGORY_USER_INPUT, 4003),
    /**
     * <li>The supplied configuration is invalid. Please correct the configuration and try again.</li>
     * <li>Invalid configuration [configuration %1$s]</li>
     */
    INVALID_CONFIGURATION("Invalid configuration [configuration %1$s]", INVALID_CONFIGURATION_MSG, Category.CATEGORY_USER_INPUT, 4004),
    /**
     * <li>The supplied reminder is invalid. Please correct the reminder and try again.</li>
     * <li>Invalid alarm [alarm %1$s]</li>
     */
    INVALID_ALARM("Invalid alarm [alarm %1$s]", INVALID_ALARM_MSG, Category.CATEGORY_USER_INPUT, 4005),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>Unexpected error [%1$s]</li>
     */
    UNEXPECTED_ERROR("Unexpected error [%1$s]", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 5000),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Unexpected database error [%1$s]</li>
     */
    DB_ERROR("Unexpected database error [%1$s]", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 5001),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Unexpected database error, try again [%1$s]</li>
     */
    DB_ERROR_TRY_AGAIN("Unexpected database error, try again [%1$s]", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_TRY_AGAIN, 5002),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Data not modified in storage</li>
     */
    DB_NOT_MODIFIED("Data not modified in storage", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_WARNING, 3040),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Calendar account data not written in storage</li>
     */
    ACCOUNT_NOT_WRITTEN("Calendar account data not written in storage", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_WARNING, 3041),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", IO_ERROR_MSG, Category.CATEGORY_ERROR, 5003),
    /**
     * <li>The calendar provider \"%1$s\" is not available.</li>
     * <li>Missing calendar provider [provider: %1$s]</li>
     */
    PROVIDER_NOT_AVAILABLE("Missing calendar provider [provider: %1$s]", PROVIDER_NOT_AVAILABLE_MSG, Category.CATEGORY_SERVICE_DOWN, 5030),
    /**
     * <li>The requested calendar account is currently disabled. Please enable the account and try again.</li>
     * <li>Calendar account disabled [provider: %1$s, account %2$d]</li>
     */
    ACCOUNT_DISABLED("Calendar account disabled [provider: %1$s, account %2$d]", ACCOUNT_DISABLED_MSG, Category.CATEGORY_SERVICE_DOWN, 5031),
    /**
     * <li>Some data entered exceeded the field limit. Please shorten the value for \"%1$s\" (limit: %2$d, current: %3$d) and try again.</li>
     * <li>Data truncation [field %1$s, limit %2$d, current %3$d]</li>
     */
    DATA_TRUNCATION("Data truncation [field %1$s, limit %2$d, current %3$d]", DATA_TRUNCATION_MSG, Category.CATEGORY_TRUNCATED, 5070),
    /**
     * <li>The character \"%1$s\" in field \"%2$s\" can't be saved. Please remove the problematic character and try again.</li>
     * <li>Incorrect string [string %1$s, field %2$s, column %3$s]</li>
     */
    INCORRECT_STRING("Incorrect string [string %1$s, field %2$s, column %3$s]", INCORRECT_STRING_MSG, Category.CATEGORY_USER_INPUT, 5071),
    /**
     * <li>Too many appointments are queried. Please choose a shorter timeframe.</li>
     */
    TOO_MANY_EVENT_RESULTS("Too many events are queried. Please choose a shorter timeframe.", TOO_MANY_EVENTS_MSG, Category.CATEGORY_USER_INPUT, 5072),
    /**
     * <li>The appointment contains too many attendees.</li>
     */
    TOO_MANY_ATTENDEES("The event contains too many attendees.", TOO_MANY_ATTENDEES_MSG, Category.CATEGORY_USER_INPUT, 5073),
    /**
     * <li>The appointment contains too many alarms.</li>
     */
    TOO_MANY_ALARMS("The event contains too many alarms.", TOO_MANY_ALARMS_MSG, Category.CATEGORY_USER_INPUT, 5074),
    /**
     * <li>The maximum number of calendar subscriptions is exceeded.</li>
     * <li>Maximum number of accounts exceeded [provider %1$s, limit %2$d, current %3$d]</li>
     */
    MAX_ACCOUNTS_EXCEEDED("Maximum number of accounts exceeded [provider %1$s, limit %2$d, current %3$d]", MAX_ACCOUNTS_EXCEEDED_MSG, Category.CATEGORY_USER_INPUT, 5075),
    /**
     * <li>The value for \"%2$s\" is invalid and wasn't applied.</li>
     * <li>Ignored invalid data [id %1$s, field %2$s, severity %3$s, message %4$s]</li>
     */
    IGNORED_INVALID_DATA("Ignored invalid data [id %1$s, field %2$s, severity %3$s, message %4$s]", IGNORED_INVALID_DATA_MSG, Category.CATEGORY_WARNING, 1990),
    /**
     * <li>The value for \"%2$s\" is not supported and wasn't applied.</li>
     * <li>Unsupported data [id %1$s, field %2$s, severity %3$s, message %4$s]</li>
     */
    UNSUPPORTED_DATA("Unsupported data [id %1$s, field %2$s, severity %3$s, message %4$s]", UNSUPPORTED_DATA_MSG, Category.CATEGORY_TRUNCATED, 1991),

    //TODO: check if needed/useful, check code
    /**
     * <li>Unable to create account. There have been issues with the provided authentication.</li>
     * <li>Account cannot be created. Too many auth mechanisms provided by client: %1$s</li>
     */
    BAD_AUTH_CONFIGURATION("Account cannot be created. Too many auth mechanisms provided by client: %1$s", BAD_AUTH_CONFIGURATION_MSG, Category.CATEGORY_USER_INPUT, 4046),

    /**
     * <li>Authentication failed.</li>
     * <li>Authentication failed to access the resource at %1$s</li>
     */
    AUTH_FAILED("Authentication failed to access the resource at %1$s", AUTH_FAILED_MSG, Category.CATEGORY_USER_INPUT, 4010),

    /**
     * <li>Authentication failed.</li>
     * <li>Authentication failed to access a shared calendar at %1$s</li>
     */
    AUTH_FAILED_FOR_SHARE("Authentication failed to access a shared calendar at %1$s", AUTH_FAILED_MSG, Category.CATEGORY_USER_INPUT, 4011),

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
