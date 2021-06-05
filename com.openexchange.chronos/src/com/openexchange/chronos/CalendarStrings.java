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

package com.openexchange.chronos;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link CalendarStrings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarStrings implements LocalizableStrings {

    /** The summary that is inserted for <i>private</i> events the requesting user has no access to */
    public static final String SUMMARY_PRIVATE = "Private";

    /** The displayed name for the {@link EventField#SUMMARY} property of an event */
    public static final String FIELD_SUMMARY = "Subject";

    /** The displayed name for the {@link EventField#LOCATION} property of an event */
    public static final String FIELD_LOCATION = "Location";

    /** The displayed name for the {@link EventField#START_DATE} property of an event */
    public static final String FIELD_START_DATE = "Starts on";

    /** The displayed name for the {@link EventField#END_DATE} property of an event */
    public static final String FIELD_END_DATE = "Ends on";

    /** The displayed name for the {@link EventField#RECURRENCE_RULE} property of an event */
    public static final String FIELD_RECURRENCE_RULE = "Repeat";

    /** The displayed name for the {@link EventField#DESCRIPTION} property of an event */
    public static final String FIELD_DESCRIPTION = "Description";

    /** The displayed name for the {@link EventField#ATTENDEES} property of an event */
    public static final String FIELD_ATTENDEES = "Participants";

    /** The displayed name for the {@link EventField#ALARMS} property of an event */
    public static final String FIELD_ALARMS = "Reminder";

    /** The displayed name for the {@link EventField#CONFERENCES} property of an event */
    public static final String FIELD_CONFERENCES = "Conference";

    /** The displayed name for the {@link EventField#CLASSIFICATION} property of an event */
    public static final String FIELD_CLASSIFICATION = "Visibility";

    /** The displayed name for the {@link EventField#COLOR} property of an event */
    public static final String FIELD_COLOR = "Color";

    /** The displayed name for the {@link EventField#TRANSP} property of an event */
    public static final String FIELD_TRANSP = "Show as";

    /** The displayed name for the {@link EventField#ATTACHMENTS} property of an event */
    public static final String FIELD_ATTACHMENTS = "Attachments";

    /**
     * Prevent instantiation.
     */
    private CalendarStrings() {
        super();
    }

}
