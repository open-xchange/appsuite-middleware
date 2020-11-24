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
