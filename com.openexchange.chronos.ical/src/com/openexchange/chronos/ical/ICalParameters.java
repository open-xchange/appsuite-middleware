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
