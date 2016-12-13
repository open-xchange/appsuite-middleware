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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

/**
 * {@link ICalParameters}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface ICalParameters {

    /**
     * {@link String[]} array denoting the names of the extended arbitrary properties to consider during import. Such properties will be
     * made available via {@link ComponentData#getProperties()} of the imported component.
     * <p/>
     * During export, any property preset in {@link ComponentData#getProperties()} will be considered implicitly.
     */
    String EXTRA_PROPERTIES = "EXTRA_PROPERTIES";


    String DEFAULT_TIMEZONE = "DEFAULT_TIMEZONE";

    String TIMEZONE_REGISTRY = "TIMEZONE_REGISTRY";

    String KEEP_COMPONENTS = "KEEP_COMPONENTS";

    String OUTLOOK_TIMEZONES = "OUTLOOK_TIMEZONES";

    //    /**
    //     * {@link Boolean} value to indicate whether attendee comments meant for the organizer (<code>X-CALENDARSERVER-ATTENDEE-COMMENT</code>)
    //     * should be exported or not.
    //     */
    //    String ATTENDEE_COMMENTS = "ATTENDEE_COMMENTS";
    //
    //    /**
    //     * {@link String} value holding the attendee comment to export into or import from the <code>X-CALENDARSERVER-PRIVATE-COMMENT</code>
    //     * property.
    //     */
    //    String PRIVATE_ATTENDEE_COMMENT = "PRIVATE_ATTENDEE_COMMENT";

    /**
     * Gets the value of an arbitrary extended parameter.
     *
     * @param name The parameter name
     * @param clazz The parameter value's class
     * @return The parameter's value, or <code>null</code> if not set
     */
    <T> T get(String name, Class<T> clazz);

    /**
     * Sets the value for an arbitrary extended parameter.
     *
     * @param name The parameter name
     * @param value The parameter value, or <code>null</code> to remove the parameter
     * @return A self reference
     */
    <T> ICalParameters set(String name, T value);

}
