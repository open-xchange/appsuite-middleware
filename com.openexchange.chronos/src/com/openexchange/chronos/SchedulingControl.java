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

package com.openexchange.chronos;

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
