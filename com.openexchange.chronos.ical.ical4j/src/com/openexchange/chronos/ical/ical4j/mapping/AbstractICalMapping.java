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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.tools.arrays.Arrays;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;

/**
 * {@link AbstractICalMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractICalMapping<T extends Component, U> implements ICalMapping<T, U> {

    /** A named logger reference */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractICalMapping.class);

    /**
     * Initializes a new {@link AbstractICalMapping}.
     */
    protected AbstractICalMapping() {
        super();
    }

    /**
     * Gets a value indicating whether a specific property is <i>ignored</i> based on the configured
     * {@link ICalParameters#IGNORED_PROPERTIES} array.
     *
     * @param parameters The iCal parameters
     * @param propertyName The property name to check
     * @return <code>true</code> if the property is ignored, <code>false</code>, otherwise
     */
    protected boolean isIgnored(ICalParameters parameters, String propertyName) {
        if (null != parameters && null != propertyName) {
            String[] ignoredProperties = parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class);
            if (null != ignoredProperties && Arrays.contains(ignoredProperties, propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether unset properties in the iCal data should be ignored during import or not.
     *
     * @param parameters The iCal paramters to evaluate
     * @return <code>true</code> if unset properties should be ignored, <code>false</code>, otherwise
     */
    protected boolean isIgnoreUnsetProperties(ICalParameters parameters) {
        return null != parameters && Boolean.TRUE.equals(parameters.get(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.class));
    }

    /**
     * Gets a value indicating whether a specific property is not <i>ignored</i> based on the configured
     * {@link ICalParameters#IGNORED_PROPERTIES} array.
     *
     * @param parameters The iCal parameters
     * @param propertyName The property name to check
     * @return <code>true</code> if the property is not ignored, <code>false</code>, otherwise
     */
    protected boolean isNotIgnored(ICalParameters parameters, String propertyName) {
        return false == isIgnored(parameters, propertyName);
    }

    /**
     * Removes all properties with a specific name from the supplied iCalendar component.
     *
     * @param component The component to remove the properties from
     * @param name The name of the properties to remove
     * @return <code>true</code> if at least one property has been removed, <code>false</code>, otherwise
     */
    protected boolean removeProperties(T component, String name) {
        int removed = 0;
        PropertyList properties = component.getProperties(name);
        for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
            Property property = (Property) iterator.next();
            if (property.getName().equalsIgnoreCase(name)) {
                iterator.remove();
                removed++;
            }
        }
        return 0 < removed;
    }

    /**
     * Optionally gets the value of a specific parameter.
     *
     * @param parameter The parameter to get the value for, or <code>null</code> to do nothing
     * @return The parameter value, or <code>null</code> if passed parameter instance was <code>null</code>
     */
    protected static String optParameterValue(Parameter parameter) {
        return null != parameter ? parameter.getValue() : null;
    }

    /**
     * Optionally gets the value of a specific parameter from a property.
     *
     * @param property The property to get the parameter from, or <code>null</code> to do nothing
     * @param parameterName The name of the parameter to get the value for
     * @return The parameter value, or <code>null</code> if passed parameter instance was <code>null</code>
     */
    protected static String optParameterValue(Property property, String parameterName) {
        return optParameterValue(property.getParameter(parameterName));
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
            LOG.debug("", warning);
            return warnings.add(warning);
        }
        return false;
    }

}
