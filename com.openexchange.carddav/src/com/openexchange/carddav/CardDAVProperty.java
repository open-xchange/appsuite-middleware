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
