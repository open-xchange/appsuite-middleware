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

package com.openexchange.chronos.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.chronos.provider.extensions.CTagAware;
import com.openexchange.chronos.provider.extensions.CachedAware;
import com.openexchange.chronos.provider.extensions.PermissionAware;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.QuotaAware;
import com.openexchange.chronos.provider.extensions.SearchAware;
import com.openexchange.chronos.provider.extensions.SubscribeAware;
import com.openexchange.chronos.provider.extensions.SyncAware;
import com.openexchange.chronos.provider.extensions.WarningsAware;

/**
 * {@link CalendarCapability}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum CalendarCapability {

    /**
     * Permissions for calendar folders.
     *
     * @see PermissionAware
     */
    PERMISSIONS("permissions", PermissionAware.class),
    /**
     * Search events based on specific criteria.
     *
     * @see SearchAware
     */
    SEARCH("search", SearchAware.class),
    /**
     * Provide quota information.
     *
     * @see QuotaAware
     */
    QUOTA("quota", QuotaAware.class),
    /**
     * Support for personal alarms on events.
     *
     * @see PersonalAlarmAware
     */
    ALARMS("alarms", PersonalAlarmAware.class),
    /**
     * Extended functionality to support incremental synchronization of folders.
     *
     * @see SyncAware
     */
    SYNC("sync", SyncAware.class),
    /**
     * Provides a collection of warnings that occurred during processing.
     *
     * @see WarningsAware
     */
    WARNINGS("warnings", WarningsAware.class),
    /**
     * Support for (un)subscribing calendars.
     *
     * @see SubscribeAware
     */
    SUBSCRIBE("subscribe", SubscribeAware.class),
    /**
     * Indicates that calendar data from an external source is cached internally for faster access.
     *
     * @see CachedAware
     */
    CACHED("cached", CachedAware.class),
    /**
     * Extended functionality to support synchronization of folders based on an etag like ctag.
     *
     * @see CTagAware
     */
    CTAG("ctag", CTagAware.class),

    ;

    /**
     * Gets a list of all capabilities implemented by a specific calendar access interface.
     *
     * @param accessInterface The calendar access interface to derive the capabilities from
     * @return The supported calendar capabilities, or an empty set if no extended functionality is available
     */
    public static EnumSet<CalendarCapability> getCapabilities(Class<? extends CalendarAccess> accessInterface) {
        EnumSet<CalendarCapability> capabilities = EnumSet.noneOf(CalendarCapability.class);
        for (CalendarCapability capability : CalendarCapability.values()) {
            if (capability.getAccessInterface().isAssignableFrom(accessInterface)) {
                capabilities.add(capability);
            }
        }
        return capabilities;
    }

    /**
     * Gets a list of all capabilities based on a collection of capability names.
     *
     * @param capabilityNames The capability names to derive the capabilities from
     * @return The corresponding calendar capabilities
     */
    public static EnumSet<CalendarCapability> getCapabilities(Set<String> capabilityNames) {
        EnumSet<CalendarCapability> capabilities = EnumSet.noneOf(CalendarCapability.class);
        if (null != capabilityNames) {
            for (String capabilityName : capabilityNames) {
                for (CalendarCapability capability : CalendarCapability.values()) {
                    if (capability.getName().equals(capabilityName)) {
                        capabilities.add(capability);
                        break;
                    }
                }
            }
        }
        return capabilities;
    }

    /**
     * Gets the names of a collection of calendar capabilities.
     *
     * @param capabilities The capabilities to get the corresponding capability names for
     * @return The capability names
     */
    public static Set<String> getCapabilityNames(Collection<CalendarCapability> capabilities) {
        if (null == capabilities) {
            return Collections.emptySet();
        }
        Set<String> capabilityNames = new HashSet<String>(capabilities.size());
        for (CalendarCapability capability : capabilities) {
            capabilityNames.add(capability.getName());
        }
        return capabilityNames;
    }

    private final String name;
    private final Class<? extends CalendarAccess> accessInterface;

    /**
     * Initializes a new {@link CalendarCapability}.
     *
     * @param name The capability name
     * @param accessInterface The corresponding calendar access interface defining the extended functionality
     */
    private CalendarCapability(String name, Class<? extends CalendarAccess> accessInterface) {
        this.name = name;
        this.accessInterface = accessInterface;
    }

    /**
     * Gets the capability's name.
     *
     * @return The capability name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the capability's calendar access interface defining the extended functionality.
     *
     * @param calendarAccess
     * @return The access interface
     */
    public Class<? extends CalendarAccess> getAccessInterface() {
        return accessInterface;
    }

    /**
     * Gets a value indicating whether a specific calendar access reference implements this capability's extended feature set or not.
     *
     * @param calendarAccess The calendar access to check
     * @return <code>true</code> if the capability is supported, <code>false</code>, otherwise
     */
    public boolean isSupported(CalendarAccess calendarAccess) {
        return accessInterface.isInstance(calendarAccess);
    }

    @Override
    public String toString() {
        return name;
    }

}
