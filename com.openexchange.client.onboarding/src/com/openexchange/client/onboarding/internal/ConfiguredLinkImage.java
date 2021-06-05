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
