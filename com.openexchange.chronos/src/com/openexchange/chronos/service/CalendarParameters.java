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

package com.openexchange.chronos.service;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;

/**
 * {@link CalendarParameters}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarParameters {

    /**
     * {@link Boolean}
     * <p/>
     * Configures if only the <i>master</i> event of a series should be returned, or if recurring events should be resolved into their
     * individual instances.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-9.6.5">RFC 4791, section 9.6.5</a>
     */
    static final String PARAMETER_EXPAND_OCCURRENCES = "expand";

    /**
     * {@link Date}
     * <p/>
     * Specifies the lower inclusive limit of the queried range, i.e. only events which start on or after this date should be returned.
     */
    static final String PARAMETER_RANGE_START = "rangeStart";

    /**
     * {@link Date}
     * <p/>
     * Specifies the upper exclusive limit of the queried range, i.e. only appointments which end before this date should be returned.
     */
    static final String PARAMETER_RANGE_END = "rangeEnd";

    /**
     * Array of {@link EventField}
     * <p/>
     * Allows to restrict the returned properties of retrieved event data.
     */
    static final String PARAMETER_FIELDS = "fields";

    /**
     * {@link EventField}
     * <p/>
     * Specifies the field for sorting the results.
     */
    static final String PARAMETER_ORDER_BY = "sort";

    /**
     * {@link String}
     * <p/>
     * The sort order to apply, either <code>ASC</code> for ascending, or <code>DESC</code> for descending.
     */
    static final String PARAMETER_ORDER = "order";

    /**
     * {@link TimeZone}
     * <p/>
     * Provides a (possibly overridden) timezone used on a per-request basis.
     * <p/>
     * The timezone is used to resolve <i>floating</i> date-times to concrete timestamps when determining if an event intersects with a
     * given range.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4791#section-9.8">RFC 4791, section 9.8</a>
     */
    static final String PARAMETER_TIMEZONE = "timezone";

    /**
     * {@link Boolean}
     * <p/>
     * Indicates whether an event should only be saved when there are (soft) conflicts of attendees or not.
     */
    static final String PARAMETER_CHECK_CONFLICTS = "checkConflicts";

    /**
     * {@link Boolean}
     * <p/>
     * Signals that the checks of (external) attendee URIs should be disabled when storing event data.
     */
    static final String PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS = "skipExternalAttendeeURIChecks";

    /**
     * {@link Boolean}
     * <p/>
     * Specifies that attendees should be notified about the changes when saving a meeting or not.
     * The value <code>true</code> means that notifications should be send.
     */
    static final String PARAMETER_NOTIFICATION = "notification";

    /**
     * {@link Integer}
     * <p/>
     * A positive integer number to specify the "left-hand" limit of the range to return.
     */
    static final String PARAMETER_LEFT_HAND_LIMIT = "left_hand_limit";

    /**
     * {@link Integer}
     * <p/>
     * A positive integer number to specify the "right-hand" limit of the range to return.
     */
    static final String PARAMETER_RIGHT_HAND_LIMIT = "right_hand_limit";

    /**
     * {@link String}
     * <p/>
     * A collection of values that should be "ignored" when retrieving results, currently known values are <code>deleted</code> and
     * <code>changed</code> when serving the "updates" request.
     */
    static final String PARAMETER_IGNORE = "ignore";

    /**
     * {@link Boolean}
     * <p/>
     * Indicates whether the current calendar user should be added as default attendee to events implicitly or not, independently of the
     * event being <i>group-scheduled</i> or not.
     * <p/>
     * If set to <code>true</code>, an attendee representing the current calendar user as well as a corresponding organizer will be
     * implicitly added by the service during event creation, and an attempt to remove this default attendee will be ignored silently.
     */
    static final String PARAMETER_DEFAULT_ATTENDEE = "default_attendee";

    /**
     * {@link String}
     * <p/>
     * The identifier of an existing event or event series to ignore when calculating free/busy information.
     * <p/>
     * If set, existing events with this identifier are implicitly excluded during free/busy lookups, which aids to ignore the event
     * itself when it is about to be re-scheduled. If the identifier of an event series is specified, all regular occurrences of the
     * series, as well as any overridden instance will be excluded, too.
     *
     * @see <a href="https://raw.githubusercontent.com/apple/ccs-calendarserver/master/doc/Extensions/icalendar-maskuids.txt">icalendar-maskuids-03, section 4.1</a>
     */
    static final String PARAMETER_MASK_ID = "maskId";

    /**
     * {@link String}
     * <p/>
     * The push token identifier used by the client to allow filtering of push events for modified calendar data, i.e. to avoid that push
     * notifications generated from an operation performed within the session are sent back to the acting client.
     *
     * @see <a href="https://tools.ietf.org/html/draft-gajda-dav-push-00#section-7.1">draft-gajda-dav-push-00, section 7.1</a>
     */
    static final String PARAMETER_PUSH_TOKEN = "pushToken";

    /**
     * {@link UIDConflictStrategy}
     * <p/>
     * Configures what to do in case an event cannot be saved due to another existing event with the same unique identifier. By default, an
     * appropriate exception is thrown (as per {@link UIDConflictStrategy#THROW}.
     */
    static final String UID_CONFLICT_STRATEGY = "uidConflictStrategy";

    /**
     * {@link Boolean}
     * <p/>
     * Configures whether possible warnings that occurred in the storage layer during write operations should be handled automatically or
     * not. This includes
     * <ul>
     * <li><b>Auto-handling data truncations:</b><br/>
     * Strings exceeding the storage capacity in the affected calendar data are truncated, and another attempt to store the data is
     * performed implicitly, up to a fixed number of retry attempts.
     * </li>
     * <li><b>Auto-handling incorrect strings:</b><br/>
     * Incorrect strings in the affected calendar data are replaced, and another attempt to store the data is performed implicitly, up to
     * a fixed number of retry attempts.
     * </li>
     * <li><b>Skip unsupported data:</b><br/>
     * Specific properties or property values that are not supported by the underlying storage are ignored silently.
     * </li>
     * </ul>
     * Defaults to <code>false</code>, so that an appropriate exception bubbles up when the data cannot be stored as expected.
     *
     * @see CalendarExceptionCodes#DATA_TRUNCATION
     * @see CalendarExceptionCodes#INCORRECT_STRING
     * @see CalendarExceptionCodes#UNSUPPORTED_DATA
     */
    static final String PARAMETER_IGNORE_STORAGE_WARNINGS = "ignoreStorageWarnings";

    /**
     * {@link Boolean}
     * <p/>
     * Indicates whether cached calendar data should forcibly be updated prior performing the operation or not.
     */
    static final String PARAMETER_UPDATE_CACHE = "updateCache";

    /**
     * {@link Boolean}
     * <p/>
     * Configures whether newly added attendees from creations and updates should be tracked automatically, which includes adding new
     * entries in the collected contacts folder for new external calendar users (utilizing the contact collector service), as well as
     * incrementing the use counts for already known internal and external entities (using the object use count service). Defaults to
     * <code>false</code>, hence needs to be enabled explicitly.
     */
    static final String PARAMETER_TRACK_ATTENDEE_USAGE = "trackAttendeeUsage";

    /**
     * {@link Boolean}
     * <p/>
     * Indicates whether forbidden changes in a scheduling object resource performed by an attendee should be ignored during update
     * operations or not.
     * <p/>
     * If set to <code>true</code>, only changes in certain properties (as per
     * <a href="https://tools.ietf.org/html/rfc6638#section-3.2.2.1">RFC 6638, section 3.2.2.1</a>) are considered when updating an
     * attendee scheduling resource, while changes on properties that are under the control of the organizer are ignored implicitly (which
     * may be suitable for updates performed by non-interactive synchronization clients). Defaults to <code>false</code>, so that an
     * appropriate exception is thrown when forbidden changes are detected.
     *
     * @see CalendarExceptionCodes#NOT_ORGANIZER
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.2">RFC 6638, section 3.2.2</a>
     */
    static final String PARAMETER_IGNORE_FORBIDDEN_ATTENDEE_CHANGES = "ignoreForbiddenAttendeeChanges";

    /**
     * {@link Integer}
     * <p/>
     * The principal id in an itip context
     * <p/>
     */
    static final String PARAMETER_PRINCIPAL_ID = "itip.principalId";

    /**
     * {@link String}
     * <p/>
     * The principal email address in an itip context
     * <p/>
     */
    static final String PARAMETER_PRINCIPAL_EMAIL = "itip.principalEmail";

    /**
     * {@link Boolean}
     * <p/>
     * Boolean to suppress ITip roundtrip. Necessary when already inside an itip handling and additional actions are performed.
     * <p/>
     */
    static final String PARAMETER_SUPPRESS_ITIP = "itip.suppress";

    /**
     * {@link String}
     * <p/>
     * A comment set by the user when updating/deleting events.
     * <p/>
     */
    static final String PARAMETER_COMMENT = "comment";

    /**
     * {@link java.sql.Connection}
     * <p/>
     * The (dynamic) parameter name where the underlying database connection is held during transactions, or when slipstreaming a
     * surrounding connection to the storage. Empty by default.
     * 
     * @return The parameter name where the underlying database connection is held during transactions
     */
    static String PARAMETER_CONNECTION() {
        return new StringBuilder(java.sql.Connection.class.getName()).append('@').append(Thread.currentThread().getId()).toString();
    }

    /**
     * Sets a parameter.
     * <p/>
     * A value of <code>null</code> removes the parameter.
     *
     * @param parameter The parameter name to set
     * @param value The value to set, or <code>null</code> to remove the parameter
     * @return A self reference
     */
    <T> CalendarParameters set(String parameter, T value);

    /**
     * Gets a parameter.
     *
     * @param parameter The parameter name
     * @param clazz The value's target type
     * @return The parameter value, or <code>null</code> if not set
     */
    <T> T get(String parameter, Class<T> clazz);

    /**
     * Gets a parameter, falling back to a custom default value if not set.
     *
     * @param parameter The parameter name
     * @param clazz The value's target type
     * @param defaultValue The default value to use as fallback if the parameter is not set
     * @return The parameter value, or the passed default value if not set
     */
    <T> T get(String parameter, Class<T> clazz, T defaultValue);

    /**
     * Gets a value indicating whether a specific parameter is set.
     *
     * @param parameter The parameter name
     * @return <code>true</code> if the parameter is set, <code>false</code>, otherwise
     */
    boolean contains(String parameter);

    /**
     * Gets a set of all configured parameters.
     *
     * @return All parameters as set
     */
    Set<Entry<String, Object>> entrySet();

}
