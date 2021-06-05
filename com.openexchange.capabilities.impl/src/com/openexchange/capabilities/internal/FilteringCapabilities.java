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

package com.openexchange.capabilities.internal;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.java.Strings;

/**
 * {@link FilteringCapabilities}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class FilteringCapabilities {

    private final Map<Capability, ValueAndScope> forced;
    private final CapabilitySetImpl caps;
    private final Function<String, Boolean> filter;

    /**
     * Initializes a new {@link FilteringCapabilities}.
     *
     * @param forced the map with forced capabilities
     * @param caps The capability set
     * @param filter A function which returns <code>true</code> or <code>false</code> depending on whether the capability should be applied or not.
     */
    public FilteringCapabilities(Map<Capability, ValueAndScope> forced, CapabilitySetImpl caps, Function<String, Boolean> filter) {
        super();
        this.forced = forced;
        this.caps = caps;
        this.filter = filter;
    }

    /**
     * Adds the given capability to the {@link CapabilitySet} if it is accepted by the filter of this {@link FilteringCapabilities}
     *
     * @param name The name of the capability
     * @param sup The {@link Supplier} which provides the capability if it is accepted
     * @param source The capability source
     * @param optionalReason The optional reason string; may be <code>null</code>
     */
    public void add(String name, Supplier<Capability> sup, CapabilitySource capabilitySource, String optReason) {
        if (Strings.isEmpty(name) || sup == null) {
            return;
        }
        if (filter.apply(name).booleanValue()) {
            Capability capability = sup.get();
            if (capability != null) {
                caps.add(capability, capabilitySource, optReason);
            }
        }
    }

    /**
     * Removes the capability with the given name from the {@link CapabilitySet} if it is accepted by the filter of this {@link FilteringCapabilities}
     *
     * @param name The name of the capability
     * @param source The capability source
     * @param optionalReason The optional reason string; may be <code>null</code>
     */
    public void remove(String name, CapabilitySource capabilitySource, String optReason) {
        if (Strings.isEmpty(name)) {
            return;
        }
        if (filter.apply(name).booleanValue()) {
            caps.remove(name, capabilitySource, optReason);
        }
    }

    /**
     * Puts the given capability and value to the map of forced capabilities in case it is accepted by the filter of this {@link FilteringCapabilities}
     *
     * @param name The name of the capability
     * @param sup The {@link Supplier} which provides the capability if it is accepted
     * @param value The value of the capability
     */
    public void addForced(String name, Supplier<Capability> sup, ValueAndScope value) {
        if (Strings.isEmpty(name) || sup == null || value == null) {
            return;
        }
        if (filter.apply(name).booleanValue()) {
            Capability capability = sup.get();
            if (capability != null) {
                forced.put(capability, value);
            }
        }
    }

    /**
     * Gets the underlying {@link CapabilitySet}
     *
     * @return The {@link CapabilitySet}
     */
    public CapabilitySetImpl getCapabilitySet() {
        return caps;
    }

}
