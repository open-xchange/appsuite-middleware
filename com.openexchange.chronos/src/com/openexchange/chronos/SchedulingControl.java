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

import com.openexchange.java.EnumeratedProperty;

/**
 * {@link SchedulingControl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 * @see <a href="https://datatracker.ietf.org/doc/draft-ietf-calext-caldav-scheduling-controls/">CalDAV SchedControl</a>
 */
public class SchedulingControl extends EnumeratedProperty {

    /**
     * Instructs the server to follow the behavior in <a href="https://tools.ietf.org/html/rfc6638#section-3.2">RFC 6638, section 3.2</a>.
     */
    public static final SchedulingControl ALL = new SchedulingControl("all");

    /**
     * Instructs the server to perform no scheduling at all, and to just store the event (useful for restoring from backup).
     */
    public static final SchedulingControl NONE = new SchedulingControl("none");

    /**
     * Instructs the server to update the events in other calendars within its system where that can be done silently, but not to send
     * visible notifications to users (where permitted by policy). This is useful when importing multiple related calendars into a new
     * system without flooding external parties with notifications.
     */
    public static final SchedulingControl INTERNAL_ONLY = new SchedulingControl("internal-only");

    /**
     * Instructs the server to import the data without updating local calendars, but to send notifications to external attendees so they
     * are aware of the event. This is useful when migrating calendar events to a new system where external parties need to have a way to
     * update their participation status in the new system.
     */
    public static final SchedulingControl EXTERNAL_ONLY = new SchedulingControl("external-only");

    /**
     * Initializes a new {@link SchedulingControl}.
     *
     * @param value The property value
     */
    public SchedulingControl(String value) {
        super(value);
    }

    @Override
    public String getDefaultValue() {
        return ALL.getValue();
    }

    @Override
    protected String[] getStandardValues() {
        return getValues(ALL, NONE, INTERNAL_ONLY, EXTERNAL_ONLY);
    }

}
