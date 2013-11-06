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

package com.openexchange.eventsystem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * {@link Event} - An event.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class Event {

    private final Map<String, Object> properties;
    private final String topic;
    private final UUID uuid;

    /**
     * Initializes a new {@link EventImpl} with a random UUID.
     *
     * @param topic The topic
     */
    public Event(final String topic) {
        this(UUID.randomUUID(), topic);
    }

    /**
     * Initializes a new {@link EventImpl}.
     *
     * @param uuid The event's UUID
     * @param topic The topic
     */
    public Event(final UUID uuid, final String topic) {
        super();
        this.uuid = uuid;
        this.topic = topic;
        properties = new LinkedHashMap<String, Object>(6);
    }

    /**
     * Gets the UUID
     *
     * @return The UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the topic
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the properties
     *
     * @return The properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Checks for presence of denoted property
     *
     * @param name The property name
     * @return <code>true</code> if present; otherwise <code>false</code>
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Gets denoted property.
     *
     * @param name The property name
     * @return The property value or <code>null</code>
     */
    public Object getProperty(final String name) {
        return properties.get(name);
    }

    /**
     * Sets given property.
     *
     * @param name The property name
     * @param value The property value
     */
    public void setProperty(final String name, final Object value) {
        properties.put(name, value);
    }

    /**
     * Removes denoted property.
     *
     * @param name The property name
     * @return The removed property value or <code>null</code> if absent
     */
    public Object removeProperty(final String name) {
        return properties.remove(name);
    }

    /**
     * Clears the properties.
     */
    public void clearProperties() {
        properties.clear();
    }

    /**
     * Gets the names of available properties.
     *
     * @return The property names
     */
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

}
