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

package com.openexchange.calendar.cache;

import java.util.jar.Attributes;
import com.openexchange.caching.ElementAttributes;

/**
 * {@link Attribute} - An attribute for an element which should be put into {@link CalendarVolatileCache cache}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Attribute<V> {
    /**
     * The attribute type.
     */
    public static enum Type {

        /**
         * The max. life seconds the element may reside in cache before it is removed regardless of idle time.
         */
        MAX_LIFE_SECONDS(Integer.class),
        /**
         * The number of seconds an element may reside idle (without being accessed) in cache before it is removed.
         */
        IDLE_TIME_SECONDS(Integer.class)

        ;

        private final Class<? extends Object> clazz;

        private <V> Type(final Class<V> clazz) {
            this.clazz = clazz;
        }

        /**
         * Gets the type.
         *
         * @return The type
         */
        protected Class<? extends Object> getClazz() {
            return clazz;
        }

    }

    /**
     * Gets the max-life attribute for specified <code>maxLifeSeconds</code>.
     *
     * @param maxLifeSeconds The max-life seconds
     * @return The max-life attribute
     */
    public static Attribute<Integer> getMaxLifeSecondsAttribute(final int maxLifeSeconds) {
        return new Attribute<Integer>(Type.MAX_LIFE_SECONDS, Integer.valueOf(maxLifeSeconds));
    }

    /**
     * Gets the idle-time attribute for specified <code>idleTimeSeconds</code>.
     *
     * @param idleTimeSeconds The idle-time seconds
     * @return The idle-time attribute
     */
    public static Attribute<Integer> getIdleTimeSecondsAttribute(final int idleTimeSeconds) {
        return new Attribute<Integer>(Type.IDLE_TIME_SECONDS, Integer.valueOf(idleTimeSeconds));
    }

    /**
     * Creates the attribute for specified arguments.
     *
     * @param type The type
     * @param value The value
     * @return The attribute
     * @throws IllegalArgumentException If type is not assignment-compatible with the value
     */
    public static <V> Attribute<V> getAttribute(final Type type, final V value) {
        if (type.getClazz().isInstance(value)) {
            throw new IllegalArgumentException("Invalid type.");
        }
        return new Attribute<V>(type, value);
    }

    /*-
     * ------------------------------------------------------------------------------------------
     */

    private final Type type;

    private final V value;

    /**
     * Initializes a new {@link Attribute}.
     */
    private Attribute(final Type type, final V value) {
        super();
        this.type = type;
        this.value = value;
    }

    /**
     * Applies this attribute to specified element attributes.
     *
     * @param elementAttributes The element {@link Attributes} to apply to
     */
    protected void applyToElementAttaributes(final ElementAttributes elementAttributes) {
        switch (type) {
        case MAX_LIFE_SECONDS:
            elementAttributes.setMaxLifeSeconds(((Integer) getValue()).intValue());
            break;
        case IDLE_TIME_SECONDS:
            elementAttributes.setIdleTime(((Integer) getValue()).intValue());
            break;
        default:
            break;
        }
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public V getValue() {
        return value;
    }

}
