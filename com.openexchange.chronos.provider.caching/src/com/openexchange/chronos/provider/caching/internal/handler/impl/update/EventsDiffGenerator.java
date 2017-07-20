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

package com.openexchange.chronos.provider.caching.internal.handler.impl.update;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;

/**
 * {@link EventsDiffGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class EventsDiffGenerator {

    /**
     * @param externalEvents
     * @param persistedEvents
     * @return
     */
    public static EventsDiff extractDiff(Map<String, List<Event>> externalEvents, Map<String, List<Event>> persistedEvents) {
        EventsDiff diff = new EventsDiff();

        for (Map.Entry<String, List<Event>> entry : externalEvents.entrySet()) {
            List<Event> existingEvents = persistedEvents.get(entry.getKey());
            if (null == existingEvents) {
                diff.addEventToCreate(entry);
            } else {
                if (needsUpdate(existingEvents, entry.getValue())) {
                    diff.addEventToUpdate(entry);
                }
            }
        }
        for (Map.Entry<String, List<Event>> entry : persistedEvents.entrySet()) {
            if (false == externalEvents.containsKey(entry.getKey())) {
                diff.addEventToDelete(entry);
            }
        }
        return diff;
    }

    private static boolean needsUpdate(List<Event> existingEvents, List<Event> updatedEvents) {
        if (1 == existingEvents.size() && 1 == updatedEvents.size()) {
            return needsUpdate(existingEvents.get(0), updatedEvents.get(0));
        }
        //FIXME handle series
        return true;
    }

    private static boolean needsUpdate(Event existingEvent, Event updatedEvent) {
        if (existingEvent.containsSequence() && updatedEvent.containsSequence()) {
            if (existingEvent.getSequence() != updatedEvent.getSequence()) {
                return true;
            }
        }
        if (existingEvent.containsLastModified() && updatedEvent.containsLastModified()) {
            if (existingEvent.getLastModified().getTime() < updatedEvent.getLastModified().getTime()) {
                return true;
            }
        }

        return false;
    }

}
