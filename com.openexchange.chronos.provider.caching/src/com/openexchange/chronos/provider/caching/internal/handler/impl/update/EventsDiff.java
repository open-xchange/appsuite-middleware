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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.Event;

/**
 * {@link EventsDiff} - Contains the {@link Event} changes based on their UID
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class EventsDiff {

    private List<Map.Entry<String, List<Event>>> eventsToCreate = new ArrayList<Map.Entry<String, List<Event>>>();
    private List<Map.Entry<String, List<Event>>> eventsToUpdate = new ArrayList<Map.Entry<String, List<Event>>>();
    private List<Map.Entry<String, List<Event>>> eventsToDelete = new ArrayList<Map.Entry<String, List<Event>>>();

    public EventsDiff() {}

    public List<Entry<String, List<Event>>> getEventsToCreate() {
        return eventsToCreate;
    }

    public void addEventToCreate(Map.Entry<String, List<Event>> eventToCreate) {
        this.eventsToCreate.add(eventToCreate);
    }

    public List<Entry<String, List<Event>>> getEventsToUpdate() {
        return eventsToUpdate;
    }

    public void addEventToUpdate(Map.Entry<String, List<Event>> eventToUpdate) {
        this.eventsToUpdate.add(eventToUpdate);
    }

    public List<Entry<String, List<Event>>> getEventsToDelete() {
        return eventsToDelete;
    }

    public void addEventToDelete(Map.Entry<String, List<Event>> eventToDelete) {
        this.eventsToDelete.add(eventToDelete);
    }

    public static List<Event> getAllEvents(List<Entry<String, List<Event>>> source) {
        List<Event> events = new ArrayList<>();
        for (Entry<String, List<Event>> entry : source) {
            events.addAll(entry.getValue());
        }
        return events;
    }
}
