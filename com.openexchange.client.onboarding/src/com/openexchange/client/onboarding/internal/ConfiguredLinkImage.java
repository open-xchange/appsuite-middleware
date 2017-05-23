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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.client.onboarding.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.java.util.UUIDs;

/**
 * {@link ConfiguredLinkImage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ConfiguredLinkImage {

    /** The type for a configured link image */
    public static enum Type {
        /**
         * The image is accessible via an HTTP URL like <code>"http://cdn.ox.io/images/apps/drive.gif"</code>
         */
        HTTP("http"),
        /**
         * The image is accessible via an HTTPS URL like <code>"https://cdn.ox.io/images/apps/drive.gif"</code>
         */
        HTTPS("https"),
        /**
         * The image is accessible through a file on HDD like <code>"file:///opt/open-xchange/images/drive.gif"</code>
         */
        FILE("file"),
        /**
         * The image is accessible through a resource <code>"resource://images/drive.gif"</code>
         */
        RESOURCE("resource");

        private final String scheme;

        private Type(String scheme) {
            this.scheme = scheme;
        }

        /**
         * Gets the scheme
         *
         * @return The scheme
         */
        public String getScheme() {
            return scheme;
        }

        /**
         * Gets the type for specified identifier.
         *
         * @param type The type identifier
         * @return The type or <code>null</code>
         */
        public static Type typeFor(String type) {
            if (null == type) {
                return null;
            }

            for (Type t : Type.values()) {
                if (type.equalsIgnoreCase(t.scheme)) {
                    return t;
                }
            }
            return null;
        }
    }

    private static final ConcurrentMap<String, String> NAME_MAP = new ConcurrentHashMap<>(16, 0.9F, 1);

    /**
     * Gets the real name for given UUID string.
     *
     * @param uuid The UUID string
     * @return The association real name or <code>null</code>
     */
    public static String getRealNameFor(String uuid) {
        return null == uuid ? null : NAME_MAP.get(uuid);
    }

    /**
     * Clears the UUID to real name associations.
     */
    public static void clear() {
        NAME_MAP.clear();
    }

    // ------------------------------------------------------------------------------------------------------------------

    private final String name;
    private final Type type;

    /**
     * Initializes a new {@link ConfiguredLinkImage}.
     */
    public ConfiguredLinkImage(String realName, Type type) {
        super();
        String uuid = UUIDs.getUnformattedStringFromRandom();
        while (null != NAME_MAP.putIfAbsent(uuid, realName)) {
            uuid = UUIDs.getUnformattedStringFromRandom();
        }
        this.name = uuid;
        this.type = type;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

}
