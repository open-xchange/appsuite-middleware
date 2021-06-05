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

package com.openexchange.chronos.ical;

import java.util.TimeZone;

/**
 * {@link ICalParameters}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface ICalParameters {

    /**
     * {@link Boolean}
     * <p/>
     * Configures whether iCal input should be pre-processed and sanitized so that known client quirks are corrected automatically.
     * <p/>
     * This currently includes all legacy workarounds, taken over from the previous parser implementation.
     * <p/>
     * Only effective during import, defaults to {@link Boolean#FALSE}.
     *
     * @see com.openexchange.data.conversion.ical.ical4j.ICal4JParser.parse(BufferedReader, Collection<Exception>)
     */
    String SANITIZE_INPUT = "SANITIZE_INPUT";

    /**
     * {@link String[]}
     * <p/>
     * Optional string array denoting the names of the extended arbitrary properties to consider during import. Such properties will be
     * made available via {@link ComponentData#getProperties()} of the imported component.
     * <p/>
     * Wildcards are allowed in the names, e.g. <code>X-MOZ-SNOOZE-TIME*</code>. Or, to track all iCal properties, <code>*</code> might
     * be used, too.
     * <p/>
     * During export, any property present in {@link ComponentData#getProperties()} will be considered implicitly.
     */
    String EXTRA_PROPERTIES = "EXTRA_PROPERTIES";

    /**
     * {@link TimeZone}
     * <p/>
     * Defines the default or fallback timezone to use in case of not parsable or mappable timezone references are encountered during
     * import.
     */
    String DEFAULT_TIMEZONE = "DEFAULT_TIMEZONE";

    /**
     * {@link String[]}
     * <p/>
     * Optional string array denoting the names of the properties to forcibly ignore during import or export.
     * <p/>
     * During export, the properties are removed after mapping, prior serialization. During import, the properties are removed after
     * deserialization, before mapping.
     * <p/>
     * Wildcards are allowed in the names, e.g. <code>X-MOZ-SNOOZE-TIME*</code>.
     */
    String IGNORED_PROPERTIES = "IGNORED_PROPERTIES";
    
    /**
     * {@link String[]}
     * <p/>
     * Optional string array denoting the names of specific property parameters to forcibly ignore during import or export.
     * <p/>
     * During export, the parameters are removed after mapping, prior serialization. During import, the parameters are removed after
     * deserialization, before mapping.
     * <p/>
     * To identify the correct property to remove the parameter for, use the property as key, delimited with a colon and followed by the
     * parameter name. E.g. <code>ATTENDEE:X-CALENDARSERVER-DTSTAMP</code>
     * <p/>
     * By default, the list contains <code>ATTENDEE:X-CALENDARSERVER-DTSTAMP</code> to not pollute the generated iCalendar files unless
     * really needed.
     */
    String IGNORED_PROPERTY_PARAMETERS = "IGNORED_PROPERTY_PARAMETERS";

    /**
     * {@link Boolean}
     * <p/>
     * Configures whether the <code>VALARM</code> component should be ignored during import or export.
     */
    String IGNORE_ALARM = "IGNORE_ALARM";

    /**
     * {@link Boolean}
     * <p/>
     * Configures whether properties that are not present in the iCalendar input should either be skipped, or if the mapped object
     * attribute(s) should be explicitly set to <code>null</code>. The latter option will make the typical <code>containsXXX</code>
     * methods return <code>true</code>, while ignoring such properties will keep the attributes appear unset.
     * <p/>
     * Only effective during import, defaults to {@link Boolean#FALSE}.
     */
    String IGNORE_UNSET_PROPERTIES = "IGNORE_EMPTY_PROPERTIES";

    /**
     * {@link Integer}
     * <p/>
     * Configures the maximum number of components to consider during import. If the limit is exceeded, the import result will contain an
     * appropriate warning ({@link ICalExceptionCodes#TRUNCATED_RESULTS}).
     * <p/>
     * Only effective during import, defaults to <code>-1</code>
     *
     * @see ICalExceptionCodes#TRUNCATED_RESULTS
     */
    String IMPORT_LIMIT = "IMPORT_LIMIT";

    /**
     * Gets the value of an arbitrary extended parameter.
     *
     * @param name The parameter name
     * @param clazz The parameter value's class
     * @return The parameter's value, or <code>null</code> if not set
     */
    <T> T get(String name, Class<T> clazz);

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
     * Sets the value for an arbitrary extended parameter.
     *
     * @param name The parameter name
     * @param value The parameter value, or <code>null</code> to remove the parameter
     * @return A self reference
     */
    <T> ICalParameters set(String name, T value);

}
