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
