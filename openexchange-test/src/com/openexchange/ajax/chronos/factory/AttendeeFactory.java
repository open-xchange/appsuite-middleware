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

package com.openexchange.ajax.chronos.factory;

import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CalendarUser;

/**
 * {@link AttendeeFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class AttendeeFactory {

    /**
     * Creates a new {@link Attendee} object with the specified user identifier, email address and {@link CuTypeEnum}
     *
     * @param userId The user identifier
     * @param cuType the {@link CuTypeEnum}
     * @return The new {@link Attendee}
     */
    public static Attendee createAttendee(Integer userId, CuTypeEnum cuType) {
        Attendee attendee = new Attendee();
        attendee.entity(userId);
        attendee.cuType(cuType);
        attendee.setMember(null); //set member explicitly to null
        return attendee;
    }

    /**
     * Creates an {@link Attendee} of type {@link CuTypeEnum#INDIVIDUAL}
     *
     * @param userId The user identifier
     * @return The new {@link Attendee}
     */
    public static Attendee createIndividual(Integer userId) {
        return createAttendee(userId, CuTypeEnum.INDIVIDUAL);
    }

    /**
     * Creates an external {@link Attendee} of type {@link CuTypeEnum#INDIVIDUAL}
     *
     * @param emailAddress The e-mail address
     * @return The new {@link Attendee}
     */
    public static Attendee createIndividual(String emailAddress) {
        Attendee attendee = new Attendee();
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + emailAddress);
        return attendee;
    }

    /**
     * Converts an {@link Attendee} to an organizer.
     * 
     * @param attendee The attendee to convert
     * @return THe organizer as {@link CalendarUser} object
     */
    public static CalendarUser createOrganizerFrom(Attendee attendee) {
        CalendarUser c = new CalendarUser();
        c.cn(attendee.getCn());
        c.email(attendee.getEmail());
        c.entity(attendee.getEntity());
        c.uri(attendee.getUri());
        return c;
    }

}
