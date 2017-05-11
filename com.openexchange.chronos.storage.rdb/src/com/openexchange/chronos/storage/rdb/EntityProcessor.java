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

package com.openexchange.chronos.storage.rdb;

import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.exception.OXException;

/**
 * {@link EntityProcessor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EntityProcessor {

    private final EntityResolver entityResolver;

    /**
     * Initializes a new {@link EntityProcessor}.
     *
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     */
    public EntityProcessor(EntityResolver entityResolver) {
        super();
        this.entityResolver = entityResolver;
    }

    /**
     * Gets the underlying entity resolver.
     *
     * @return The entity resolver
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Adjusts certain properties of an event prior inserting it into the database.
     *
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    public Event adjustPriorSave(Event event) throws OXException {
        if (null == entityResolver) {
            return event;
        }
        if (event.containsOrganizer() && null != event.getOrganizer() && 0 < event.getOrganizer().getEntity()) {
            /*
             * store internal static resource identifier for internal organizer
             */
            final Organizer storedOrganizer = new Organizer();
            storedOrganizer.setEntity(event.getOrganizer().getEntity());
            storedOrganizer.setUri(ResourceId.forUser(entityResolver.getContextID(), event.getOrganizer().getEntity()));
            event = new DelegatingEvent(event) {

                @Override
                public Organizer getOrganizer() {
                    return storedOrganizer;
                }
            };
        }
        return event;
    }

    /**
     * Adjusts certain properties of an attendee prior inserting it into the database.
     *
     * @param attendee The attendee to adjust
     * @return The (possibly adjusted) attendee reference
     */
    public Attendee adjustPriorSave(Attendee attendee) throws OXException {
        if (null == entityResolver) {
            return attendee;
        }
        if (0 < attendee.getEntity()) {
            Attendee savedAttendee = AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null);
            savedAttendee.removeCn();
            if (savedAttendee.containsUri()) {
                ResourceId resourceId = new ResourceId(entityResolver.getContextID(), savedAttendee.getEntity(), savedAttendee.getCuType());
                savedAttendee.setUri(resourceId.getURI());
            }
            return savedAttendee;
        }
        return attendee;
    }

    /**
     * Adjusts certain properties of an event after loading it from the database.
     *
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    public Event adjustAfterLoad(Event event) throws OXException {
        if (null == entityResolver) {
            return event;
        }
        if (null != event.getOrganizer()) {
            /*
             * apply entity data for internal organizers
             */
            ResourceId resourceId = ResourceId.parse(event.getOrganizer().getUri());
            if (null != resourceId && CalendarUserType.INDIVIDUAL.equals(resourceId.getCalendarUserType())) {
                event.setOrganizer(entityResolver.applyEntityData(new Organizer(), resourceId.getEntity()));
            }
        }
        return event;
    }

    /**
     * Adjusts certain properties of an attendee after loading it from the database.
     *
     * @param attendee The attendee to adjust
     * @return The (possibly adjusted) attendee reference
     */
    public Attendee adjustAfterLoad(Attendee attendee) throws OXException {
        if (null == entityResolver) {
            return attendee;
        }
        if (0 < attendee.getEntity()) {
            /*
             * apply entity data for internal attendees
             */
            attendee = entityResolver.applyEntityData(attendee);
        }
        return attendee;
    }

}
