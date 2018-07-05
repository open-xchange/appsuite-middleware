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

package com.openexchange.chronos.ical.ical4j;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ValidationException;

/**
 * {@link VCalendar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class VCalendar extends Component {

    private static final long serialVersionUID = 6337610616448759858L;

    private final net.fortuna.ical4j.model.Calendar calendar;

    /**
     * Initializes a new {@link VCalendar}.
     */
    public VCalendar() {
        this(new net.fortuna.ical4j.model.Calendar());
    }

    /**
     * Initializes a new {@link VCalendar}.
     *
     * @param calendar The underlying iCal4j calendar
     */
    public VCalendar(net.fortuna.ical4j.model.Calendar calendar) {
        super(net.fortuna.ical4j.model.Calendar.VCALENDAR, calendar.getProperties());
        this.calendar = calendar;
    }

    /**
     * Gets the underlying iCal4j calendar
     *
     * @return The underlying iCal4j calendar
     */
    public net.fortuna.ical4j.model.Calendar getCalendar() {
        return calendar;
    }

    /**
     * Gets the contained <code>VEVENT</code> components.
     *
     * @return The event components
     */
    public ComponentList getEvents() {
        return calendar.getComponents(Component.VEVENT);
    }

    /**
     * Gets the contained <code>VFREEBUSY</code> components.
     *
     * @return The free/busy components
     */
    public ComponentList getFreeBusys() {
        return calendar.getComponents(Component.VFREEBUSY);
    }

    /**
     * Gets the contained <code>VAVAILABILITY</code> component.
     *
     * @return The availability component
     */
    public Component getAvailability() {
        return calendar.getComponent(Component.VAVAILABILITY);
    }

    /**
     * Adds an additional component.
     *
     * @param component The component to add
     */
    public void add(Component component) {
        calendar.getComponents().add(component);
    }

    /**
     * Adds an additional component.
     *
     * @param index The index to add the component at
     * @param component The component to add
     */
    @SuppressWarnings("unchecked")
    public void add(int index, Component component) {
        calendar.getComponents().add(index, component);
    }

    @Override
    public void validate(boolean recurse) throws ValidationException {
        // no
    }

}
