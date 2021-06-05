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

package com.openexchange.contact.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.contact.provider.extensions.SearchAware;
import com.openexchange.contact.provider.extensions.WarningsAware;

/**
 * {@link ContactsAccessCapability}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public enum ContactsAccessCapability {

    /**
     * Search contacts based on specific criteria.
     *
     * @see SearchAware
     */
    SEARCH("search", SearchAware.class),
    /**
     * Provides a collection of warnings that occurred during processing.
     *
     * @see WarningsAware
     */
    WARNINGS("warnings", WarningsAware.class),
    ;

    /**
     * Gets a list of all capabilities implemented by a specific contacts access interface.
     *
     * @param accessInterface The contacts access interface to derive the capabilities from
     * @return The supported contacts capabilities, or an empty set if no extended functionality is available
     */
    public static EnumSet<ContactsAccessCapability> getCapabilities(Class<? extends ContactsAccess> accessInterface) {
        EnumSet<ContactsAccessCapability> capabilities = EnumSet.noneOf(ContactsAccessCapability.class);
        for (ContactsAccessCapability capability : ContactsAccessCapability.values()) {
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
     * @return The corresponding contacts capabilities
     */
    public static EnumSet<ContactsAccessCapability> getCapabilities(Set<String> capabilityNames) {
        EnumSet<ContactsAccessCapability> capabilities = EnumSet.noneOf(ContactsAccessCapability.class);
        if (null == capabilityNames) {
            return capabilities;
        }
        for (String capabilityName : capabilityNames) {
            for (ContactsAccessCapability capability : ContactsAccessCapability.values()) {
                if (capability.getName().equals(capabilityName)) {
                    capabilities.add(capability);
                    break;
                }
            }
        }
        return capabilities;
    }

    /**
     * Gets the names of a collection of contacts capabilities.
     *
     * @param capabilities The capabilities to get the corresponding capability names for
     * @return The capability names
     */
    public static Set<String> getCapabilityNames(Collection<ContactsAccessCapability> capabilities) {
        if (null == capabilities) {
            return Collections.emptySet();
        }
        Set<String> capabilityNames = new HashSet<>(capabilities.size());
        for (ContactsAccessCapability capability : capabilities) {
            capabilityNames.add(capability.getName());
        }
        return capabilityNames;
    }

    private final String name;
    private final Class<? extends ContactsAccess> accessInterface;

    /**
     * Initializes a new {@link ContactsAccessCapability}.
     *
     * @param name The capability name
     * @param accessInterface The corresponding contacts access interface defining the extended functionality
     */
    private ContactsAccessCapability(String name, Class<? extends ContactsAccess> accessInterface) {
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
     * Gets the capability's contacts access interface defining the extended functionality.
     *
     * @param contactsAccess
     * @return The access interface
     */
    public Class<? extends ContactsAccess> getAccessInterface() {
        return accessInterface;
    }

    /**
     * Gets a value indicating whether a specific contacts access reference implements this capability's extended feature set or not.
     *
     * @param contactsAccess The contacts access to check
     * @return <code>true</code> if the capability is supported, <code>false</code>, otherwise
     */
    public boolean isSupported(ContactsAccess contactsAccess) {
        return accessInterface.isInstance(contactsAccess);
    }

    @Override
    public String toString() {
        return name;
    }
}
