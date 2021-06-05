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

package com.openexchange.chronos.common;

import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.exception.OXException;

/**
 * {@link DataHandlers}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum DataHandlers {
    ;

    /** The identifier of the data handler to convert from an event's JSON representation to {@link Event}s. */
    public static final String JSON2EVENT = "com.openexchange.chronos.json2event";

    /** The identifier of the data handler to convert from an alarm's JSON representation to {@link Alarm}s. */
    public static final String JSON2ALARM = "com.openexchange.chronos.json2alarm";

    /** The identifier of the data handler to convert from an {@link Alarm} to its JSON representation. */
    public static final String ALARM2JSON = "com.openexchange.chronos.alarm2json";

    /** The identifier of the data handler to convert from an ox exception's JSON representation to {@link OXException}s. */
    public static final String JSON2OXEXCEPTION = "com.openexchange.chronos.json2oxexception";

    /** The identifier of the data handler to convert from an {@link OXException} to its JSON representation. */
    public static final String OXEXCEPTION2JSON = "com.openexchange.chronos.oxexception2json";

    /** The identifier of the data handler to convert from an available's JSON representation to {@link Available}s. */
    public static final String JSON2AVAILABLE = "com.openexchange.chronos.json2available";

    /** The identifier of the data handler to convert from an {@link Available} to its JSON representation. */
    public static final String AVAILABLE2JSON = "com.openexchange.chronos.available2json";

    /** The identifier of the data handler to convert from an extended properties JSON representation to {@link ExtendedProperties}. */
    public static final String JSON2XPROPERTIES = "com.openexchange.chronos.json2xproperties";

    /** The identifier of the data handler to convert from a string array to an event field array. */
    public static final String STRING_ARRAY_TO_EVENT_FIELDS = "com.openexchange.chronos.stringArray2EventField";

    /** The identifier of the data handler to convert from an {@link ExtendedProperties} to its JSON representation. */
    public static final String XPROPERTIES2JSON = "com.openexchange.chronos.xproperties2json";

    /** The identifier of the data handler to convert from an alarm's iCalendar representation to an {@link Alarm}. */
    public static final String ICAL2ALARM = "com.openexchange.chronos.ical2alarm";

    /** The identifier of the data handler to convert from one or more alarm's iCalendar representation to {@link Alarm}s. */
    public static final String ICAL2ALARMS = "com.openexchange.chronos.ical2alarms";

    /** The identifier of the data handler to convert from an {@link Event} to its iCalendar representation. */
    public static final String EVENT2ICAL = "com.openexchange.chronos.event2ical";
    
    /** The identifier of the data handler to convert from an {@link Event} to its {@link org.json.JSONObject} representation. */
    public static final String EVENT2JSON = "com.openexchange.chronos.event2json";

    /** The identifier of the data handler to convert from an event's iCalendar representation to an {@link Event}. */
    public static final String ICAL2EVENT = "com.openexchange.chronos.ical2event";

    /** The identifier of the data handler to convert from one or more event's iCalendar representation to {@link Event}s. */
    public static final String ICAL2EVENTS = "com.openexchange.chronos.ical2events";

    /** The identifier of the data handler to convert from an {@link Alarm} to its iCalendar representation. */
    public static final String ALARM2ICAL = "com.openexchange.chronos.alarm2ical";

    /** The identifier of the data handler to convert from an timezone's iCalendar representation to {@link TimeZone}s. */
    public static final String ICAL2TIMEZONE = "com.openexchange.chronos.ical2timezone";

    /** The identifier of the data handler to convert from an {@link TimeZone} to its iCalendar representation. */
    public static final String TIMEZONE2ICAL = "com.openexchange.chronos.timezone2ical";

}
