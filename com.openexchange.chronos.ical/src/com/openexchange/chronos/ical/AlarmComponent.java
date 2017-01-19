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

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.DelegatingAlarm;

/**
 * {@link AlarmComponent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmComponent extends DelegatingAlarm implements ComponentData {

    private List<ICalProperty> properties;

    /**
     * Initializes a new {@link AlarmComponent}.
     */
    public AlarmComponent() {
        this(new Alarm());
    }

    /**
     * Initializes a new {@link AlarmComponent}.
     *
     * @param delegate The underlying alarm delegate
     * @param iCalHolder A file holder storing the original iCal component, or <code>null</code> if not available
     */
    public AlarmComponent(Alarm delegate) {
        super(delegate);
    }

    /**
     * Gets the extended iCal properties matching the supplied name.
     *
     * @param propertyName The name of the properties to get
     * @return The properties, or an empty list if not found
     */
    public List<ICalProperty> getProperties(String propertyName) {
        return ComponentUtils.getProperties(this, propertyName);
    }

    /**
     * Gets the first extended iCal property matching the supplied name.
     *
     * @param propertyName The name of the property to get
     * @return The property, or <code>null</code> if not found
     */
    public ICalProperty getProperty(String propertyName) {
        return ComponentUtils.getProperty(this, propertyName);
    }

    @Override
    public List<ICalProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the list of further arbitrary iCalendar properties associated with the component.
     *
     * @param properties The extra properties to set
     */
    public void setProperties(List<ICalProperty> properties) {
        this.properties = properties;
    }

}
