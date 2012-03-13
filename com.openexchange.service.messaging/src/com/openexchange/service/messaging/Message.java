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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.messaging;

import java.io.Serializable;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link Message} - Messages are delivered to <code>MessageHandler</code> services which subscribe to the topic of the message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.22
 */
public class Message {

    /**
     * The topic of this message.
     */
    private final String topic;

    /**
     * The properties carried by this message. Keys are strings and values are objects
     */
    private final Map<String, Serializable> properties;

    /**
     * The hash code.
     */
    private final int hash;

    /**
     * Constructs an message.
     *
     * @param topic The topic of the message.
     * @param properties The message's properties (may be <code>null</code>). A property whose key is not of type <code>String</code> will
     *            be ignored.
     * @throws IllegalArgumentException If topic is not a valid topic name.
     */
    public Message(final String topic, final Map<String, Serializable> properties) {
        super();
        validateTopicName(topic);
        this.topic = topic;
        final int size = (properties == null) ? 1 : (properties.size() + 1);
        final Map<String, Serializable> p = new HashMap<String, Serializable>(size);
        if (properties != null) {
            for (final Entry<String, Serializable> entry : properties.entrySet()) {
                p.put(entry.getKey(), entry.getValue());
            }
        }
        p.put(MessagingServiceConstants.MESSAGE_TOPIC, topic);
        /*
         * Safely publish the map
         */
        this.properties = p;
        hash = calcHashCode();
    }

    /**
     * Constructs an message.
     *
     * @param topic The topic of the message.
     * @param properties The message's properties (may be <code>null</code>). A property whose key is not of type <code>String</code> will
     *            be ignored.
     * @throws IllegalArgumentException If topic is not a valid topic name.
     */
    public Message(final String topic, final Dictionary<String, Serializable> properties) {
        super();
        validateTopicName(topic);
        this.topic = topic;
        final int size = (properties == null) ? 1 : (properties.size() + 1);
        final Map<String, Serializable> p = new HashMap<String, Serializable>(size);
        if (properties != null) {
            for (final Enumeration<String> e = properties.keys(); e.hasMoreElements();) {
                final String key = e.nextElement();
                p.put(key, properties.get(key));
            }
        }
        p.put(MessagingServiceConstants.MESSAGE_TOPIC, topic);
        /*
         * Safely publish the map
         */
        this.properties = p;
        hash = calcHashCode();
    }

    private int calcHashCode() {
        int h = 31 * 17 + topic.hashCode();
        h = 31 * h + properties.hashCode();
        return h;
    }

    /**
     * Retrieves a property.
     *
     * @param name the name of the property to retrieve
     * @return The value of the property, or <code>null</code> if not found.
     */
    public final Serializable getProperty(final String name) {
        return properties.get(name);
    }

    /**
     * Returns a list of this message's property names.
     *
     * @return A non-empty array with one element per property.
     */
    public final String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    /**
     * Gets an unmodifiable view on this message's properties.
     *
     * @return An unmodifiable view on this message's properties
     */
    public final Map<String, Serializable> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns the topic of this message.
     *
     * @return The topic of this message.
     */
    public final String getTopic() {
        return topic;
    }

    /**
     * Tests this message's properties against the given filter using a case sensitive match.
     *
     * @param filter The filter to test.
     * @return true If this message's properties match the filter, false otherwise.
     */
    public final boolean matches(final org.osgi.framework.Filter filter) {
        return filter.matchCase(new UnmodifiableDictionary(properties));
    }

    /**
     * Compares this <code>Message</code> object to another object.
     * <p>
     * An message is considered to be <b>equal to</b> another message if the topic is equal and the properties are equal.
     *
     * @param object The <code>Message</code> object to be compared.
     * @return <code>true</code> if <code>object</code> is a <code>Message</code> and is equal to this object; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) { // quick test
            return true;
        }

        if (!(object instanceof Message)) {
            return false;
        }

        final Message message = (Message) object;
        return topic.equals(message.topic) && properties.equals(message.properties);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return An integer which is a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Returns the string representation of this message.
     *
     * @return The string representation of this message.
     */
    @Override
    public String toString() {
        return getClass().getName() + " [topic=" + topic + ']';
    }

    /**
     * Called by the constructor to validate the topic name.
     *
     * @param topic The topic name to validate.
     * @throws IllegalArgumentException If the topic name is invalid.
     */
    private static void validateTopicName(final String topic) {
        if (null == topic) {
            throw new IllegalArgumentException("topic is null");
        }
        final int length = topic.length();
        if (length == 0) {
            throw new IllegalArgumentException("empty topic");
        }
        final char[] chars = topic.toCharArray();
        for (int i = 0; i < length; i++) {
            final char ch = chars[i];
            if (ch == '/') {
                /*
                 * Can't start or end with a '/' but anywhere else is okay
                 */
                if (i == 0 || (i == length - 1)) {
                    throw new IllegalArgumentException("invalid topic: " + topic);
                }
                /*
                 * Can't have "//" as that implies empty token
                 */
                if (chars[i - 1] == '/') {
                    throw new IllegalArgumentException("invalid topic: " + topic);
                }
                continue;
            }
            if (('A' <= ch) && (ch <= 'Z')) {
                continue;
            }
            if (('a' <= ch) && (ch <= 'z')) {
                continue;
            }
            if (('0' <= ch) && (ch <= '9')) {
                continue;
            }
            if ((ch == '_') || (ch == '-')) {
                continue;
            }
            throw new IllegalArgumentException("invalid topic: " + topic);
        }
    }

    /**
     * Unmodifiable wrapper for Dictionary.
     */
    private static class UnmodifiableDictionary extends Dictionary<String, Serializable> {

        private final Map<String, Serializable> wrapped;

        UnmodifiableDictionary(final Map<String, Serializable> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Enumeration<Serializable> elements() {
            return Collections.enumeration(wrapped.values());
        }

        @Override
        public Serializable get(final Object key) {
            return wrapped.get(key);
        }

        @Override
        public boolean isEmpty() {
            return wrapped.isEmpty();
        }

        @Override
        public Enumeration<String> keys() {
            return Collections.enumeration(wrapped.keySet());
        }

        @Override
        public Serializable put(final String key, final Serializable value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Serializable remove(final Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return wrapped.size();
        }
    }

}
