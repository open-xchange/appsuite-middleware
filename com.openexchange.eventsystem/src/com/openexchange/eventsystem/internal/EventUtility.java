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

package com.openexchange.eventsystem.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import com.openexchange.eventsystem.Event;

/**
 * {@link EventUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EventUtility {

    private static final String PROP_TOPIC = EventSystemConstants.PROP_TOPIC;
    private static final String PROP_UUID = EventSystemConstants.PROP_UUID;

    /**
     * Initializes a new {@link EventUtility}.
     */
    private EventUtility() {
        super();
    }

    /**
     * Wraps given event as a map.
     *
     * @param event The event
     * @return The wrapping map
     */
    public static Map<String, Object> wrap(final Event event) {
        final Map<String, Object> properties = event.getProperties();

        final Map<String, Object> retval = null == properties ? new LinkedHashMap<String, Object>(4) : new LinkedHashMap<String, Object>(properties);
        retval.put(PROP_TOPIC, event.getTopic());
        retval.put(PROP_UUID, event.getUuid());

        return retval;
    }

    /**
     * Unwraps given map as an event.
     *
     * @param map The map
     * @return The unwrapped event
     */
    public static Event unwrap(final Map<String, Object> map) {
        final UUID uuid = (UUID) map.get(PROP_UUID);
        final String topic = (String) map.get(PROP_TOPIC);
        final Event event = new Event(uuid, topic);

        for (final Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            if (!PROP_UUID.equals(key) && !PROP_TOPIC.equals(key)) {
                event.setProperty(key, entry.getValue());
            }
        }

        return event;
    }

}
