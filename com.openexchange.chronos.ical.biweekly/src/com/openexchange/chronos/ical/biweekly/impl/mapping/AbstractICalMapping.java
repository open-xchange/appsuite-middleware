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

package com.openexchange.chronos.ical.biweekly.impl.mapping;

import java.util.List;
import java.util.TimeZone;
import biweekly.component.ICalComponent;
import biweekly.io.TimezoneInfo;
import biweekly.property.ICalProperty;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractICalMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractICalMapping<T extends ICalComponent, U> implements ICalMapping<T, U> {

    /**
     * Initializes a new {@link AbstractICalMapping}.
     */
    protected AbstractICalMapping() {
        super();
    }

    /**
     * Initializes and adds a new conversion warning to the warnings collection in the supplied iCal parameters reference.
     *
     * @param warnings A reference to the warnings collection, or <code>null</code> if not used
     * @param cause The underlying exception
     * @param propertyName The iCal property name where the warning occurred
     * @param message The warning message
     * @return <code>true</code> if the warning was added, <code>false</code>, otherwise
     */
    protected static boolean addConversionWarning(List<OXException> warnings, Throwable cause, String propertyName, String message) {
        return addWarning(warnings, ICalExceptionCodes.CONVERSION_FAILED.create(cause, propertyName, message));
    }

    /**
     * Initializes and adds a new conversion warning to the warnings collection in the supplied iCal parameters reference.
     *
     * @param warnings A reference to the warnings collection, or <code>null</code> if not used
     * @param propertyName The iCal property name where the warning occurred
     * @param message The warning message
     * @return <code>true</code> if the warning was added, <code>false</code>, otherwise
     */
    protected static boolean addConversionWarning(List<OXException> warnings, String propertyName, String message) {
        return addWarning(warnings, ICalExceptionCodes.CONVERSION_FAILED.create(propertyName, message));
    }

    /**
     * Adds a conversion warning to the supplied warnings collection.
     *
     * @param warnings A reference to the warnings collection, or <code>null</code> if not used
     * @param warning The warning to add
     * @return <code>true</code> if the warning was added, <code>false</code>, otherwise
     */
    protected static boolean addWarning(List<OXException> warnings, OXException warning) {
        if (null != warnings) {
            return warnings.add(warning);
        }
        return false;
    }

    protected static TimeZone getTimeZone(ICalParameters parameters, ICalProperty property) {
        TimezoneInfo timezoneInfo = parameters.get("TIMEZONE_INFO", TimezoneInfo.class);
        if (null != timezoneInfo) {
            return timezoneInfo.getTimeZone(property);
        }
        return null;
    }

    protected static void trackTimeZone(ICalParameters parameters, ICalProperty property, TimeZone timeZone) {
        TimezoneInfo tzInfo = parameters.get("TIMEZONE_INFO", TimezoneInfo.class);
        if (null == tzInfo) {
            tzInfo = new TimezoneInfo();
            parameters.set("TIMEZONE_INFO", tzInfo);
        }
        if (null == timeZone) {
            tzInfo.setFloating(property, true);
        } else {
            tzInfo.setTimeZone(property, timeZone);
        }
    }

}
