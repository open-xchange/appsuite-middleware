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

package com.openexchange.carddav;

import com.openexchange.config.lean.Property;

/**
 * {@link CardDAVProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public enum CardDAVProperty implements Property {

    /**
     * Whether CardDAV is enabled or not.
     */
    ENABLED("enabled", Boolean.TRUE),

    /**
     * A comma-separated list of folder IDs to exclude from the synchronization.
     * Use this to disable syncing of very large folders (e.g. the global address
     * list in large contexts, which always has ID 6).
     */
    IGNORE_FOLDERS("ignoreFolders", null),

    /**
     * Configures the ID of the folder tree used by the CardDAV interface.
     */
    TREE("tree", "0"),

    /**
     * Controls which collections are exposed via the CardDAV interface. Possible
     * values are <code>0</code>, <code>1</code> and <code>2</code>. A value of <code>1</code>
     * makes each visible folder available as a resource collection, while <code>2</code> only
     * exposes an aggregated collection containing all contact resources from all visible folders. The
     * value <code>0</code> exposes either an aggregated collection or individual
     * collections for each folder, depending on the client's user-agent that is
     * matched against the pattern in [[com.openexchange.carddav.userAgentForAggregatedCollection]].
     */
    EXPOSED_COLLECTIONS("exposedCollections", "0"),

    /**
     * Regular expression to match against the client's user-agent to decide
     * whether the aggregated collection is exposed or not. The default pattern
     * matches all known varieties of the Mac OS Addressbook client, that doesn't
     * support multiple collections. Only used if [[com.openexchange.carddav.exposedCollections]]
     * is set to <code>0</code>. The pattern is used case insensitive.
     */
    USERAGENT_FOR_AGGREGATED_COLLECTION("userAgentForAggregatedCollection", ".*CFNetwork.*Darwin.*|.*AddressBook.*CardDAVPlugin.*Mac_OS_X.*|.*Mac OS X.*AddressBook.*|.*macOS.*AddressBook.*"),

    /**
     * Specifies if all visible folders are used to create the aggregated
     * collection, or if a reduced set of folders only containing the global
     * addressbook and the personal contacts folders should be used. This setting
     * only influences the aggregated collection that is used for clients that
     * don't support multiple collections. Possible values are <code>true</code> and <code>false</code>.
     */
    REDUCED_AGGREGATED_COLLECTION("reducedAggregatedCollection", Boolean.FALSE),

    /**
     * Configures which contact folders are exposed through the special aggregated collection. This setting
     * only influences the aggregated collection that is used for clients that don't support multiple collections.
     * <p/>
     * Possible values are:
     * <ul>
     * <li><code>all</code>: All visible contact folders are exposed</li>
     * <li><code>all_synced</code>: All folders that are marked to be <i>used for sync</i> are exposed</li>
     * <li><code>reduced</code>: Only the default personal and the global addressbook folders are exposed</li>
     * <li><code>reduced_synced</code>: Only the default personal and the global addressbook folders are exposed, if marked to be <i>used for sync</i></li>
     * <li><code>default_only</code>: Only the default personal contact folder is exposed</li>
     * </ul>
     * Defaults to <code>all_synced</code>.
     */
    AGGREGATED_COLLECTION_FOLDERS("aggregatedCollectionFolders", "all_synced"),

    /**
     * Option to configure whether the value of the <code>PHOTO</code>-property in vCards should be preferably exported as URI or not.
     * Possible values are <code>binary</code> to generate Base64-encoded inline data, or <code>uri</code> to generate a link pointing to
     * the <code>/photos</code>-collection on the server.
     * <p/>
     * May still be overridden by clients on a per-request basis using the <code>Prefer</code>-header.
     */
    PREFERRED_PHOTO_ENCODING("preferredPhotoEncoding", "binary"),
    ;

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link CardDAVProperty}.
     *
     * @param suffix The property name suffix
     * @param defaultValue The property's default value
     */
    private CardDAVProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = "com.openexchange.carddav." + suffix;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
