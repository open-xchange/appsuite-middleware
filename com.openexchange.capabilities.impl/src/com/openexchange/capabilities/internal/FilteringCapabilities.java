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
