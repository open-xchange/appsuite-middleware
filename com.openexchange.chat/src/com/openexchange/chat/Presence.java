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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link Presence}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Presence extends Packet {

    /**
     * A enumeration to represent the presence type.
     * <p>
     * <b>Note</b> that presence type is often confused with presence mode.<br>
     * Generally, if a user is signed into a server, they have a presence type of {@link #available available}, even if the mode is
     * {@link Mode#away away}, {@link Mode#dnd dnd}, etc. The presence type is only {@link #unavailable unavailable} when the user is
     * signing out of the server.
     */
    public static enum Type {

        /**
         * The user is available to receive messages (default).
         */
        AVAILABLE,
        /**
         * The user is unavailable to receive messages.
         */
        UNAVAILABLE,
        /**
         * Request subscription to recipient's presence.
         */
        SUBSCRIBE,
        /**
         * Grant subscription to sender's presence.
         */
        SUBSCRIBED,
        /**
         * Request removal of subscription to sender's presence.
         */
        UNSUBSCRIBE,
        /**
         * Grant removal of subscription to sender's presence.
         */
        UNSUBSCRIBED,
        /**
         * The presence packet contains an error message.
         */
        ERROR;

        private static final TIntObjectMap<Type> ORDINAL_MAP;

        static {
            final TIntObjectMap<Type> map = new TIntObjectHashMap<Type>(6);
            for (final Type type : Type.values()) {
                map.put(type.ordinal(), type);
            }
            ORDINAL_MAP = map;
        }

        /**
         * Gets the type for specified ordinal or <code>null</code>.
         */
        public static Type typeOf(final int ordninal) {
            return ORDINAL_MAP.get(ordninal);
        }

        /**
         * Gets the type for specified string or <code>null</code>.
         */
        public static Type typeOf(final String type) {
            for (final Type t : Type.values()) {
                if (t.name().equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * An enumeration to represent the presence mode.
     */
    public static enum Mode {

        /**
         * Free to chat.
         */
        CHAT,
        /**
         * Available (the default).
         */
        AVAILABLE,
        /**
         * Away.
         */
        AWAY,
        /**
         * Away for an extended period of time.
         */
        XA,
        /**
         * Do not disturb.
         */
        DND;

        private static final TIntObjectMap<Mode> ORDINAL_MAP;

        static {
            final TIntObjectMap<Mode> map = new TIntObjectHashMap<Mode>(6);
            for (final Mode mode : Mode.values()) {
                map.put(mode.ordinal(), mode);
            }
            ORDINAL_MAP = map;
        }

        /**
         * Gets the mode for specified ordinal or <code>null</code>.
         */
        public static Mode modeOf(final int ordninal) {
            return ORDINAL_MAP.get(ordninal);
        }

        /**
         * Gets the mode for specified string or <code>null</code>.
         */
        public static Mode modeOf(final String mode) {
            for (final Mode m : Mode.values()) {
                if (m.name().equalsIgnoreCase(mode)) {
                    return m;
                }
            }
            return null;
        }
    }

    /**
     * Gets the status message.
     *
     * @return The status message
     */
    String getStatus();

    /**
     * Gets the type of this presence packet.
     *
     * @return The type
     */
    Type getType();

    /**
     * Gets the mode of this presence packet.
     *
     * @return The mode
     */
    Mode getMode();

}
