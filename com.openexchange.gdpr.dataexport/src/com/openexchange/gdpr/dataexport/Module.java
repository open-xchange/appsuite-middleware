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

package com.openexchange.gdpr.dataexport;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link Module} - Represents a module that should be considered during a data export consisting of an obligatory module identifier and
 * optional additional properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class Module {

    private final static Map<String, Module> INSTANCES;
    static {
        ImmutableMap.Builder<String, Module> instances = ImmutableMap.builderWithExpectedSize(6);

        String id = "mail";
        instances.put(id, new Module(id));

        id = "calendar";
        instances.put(id, new Module(id));

        id = "contacts";
        instances.put(id, new Module(id));

        id = "tasks";
        instances.put(id, new Module(id));

        id = "drive";
        instances.put(id, new Module(id));

        INSTANCES = instances.build();
    }

    /**
     * Gets the module for specified identifier.
     *
     * @param id The module identifier
     * @return The module
     */
    public static Module valueOf(String id) {
        return valueOf(id, null);
    }

    /**
     * Gets the module for specified identifier and (optional) properties.
     *
     * @param id The module identifier
     * @param properties The optional module properties
     * @return The module
     */
    public static Module valueOf(String id, Map<String, Object> properties) {
        String lcid = Strings.asciiLowerCase(id);

        if (properties == null || properties.isEmpty()) {
            Module module = INSTANCES.get(lcid);
            return module == null ? new Module(lcid) : module;
        }

        return new Module(lcid, properties);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final Map<String, Object> properties;
    private int hash;

    /**
     * Initializes a new {@link Module}.
     *
     * @param id The module identifier
     */
    private Module(String id) {
        this(id, null);
    }

    /**
     * Initializes a new {@link Module}.
     *
     * @param id The module identifier
     * @param properties The optional module properties
     */
    private Module(String id, Map<String, Object> properties) {
        super();
        this.id = id;
        this.properties = properties == null || properties.isEmpty() ? null : ImmutableMap.copyOf(properties);
        hash = 0;
    }

    /**
     * Gets the module identifier.
     *
     * @return The module identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the optional properties.
     *
     * @return The properties
     */
    public Optional<Map<String, Object>> getProperties() {
        return properties == null ? Optional.empty() : Optional.of(properties);
    }

    @Override
    public int hashCode() {
        // Does not need to be thread-safe
        int h = hash;
        if (h == 0) {
            h = 31 * 1 + ((id == null) ? 0 : id.hashCode());
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Module other = (Module) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Module [");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (properties != null) {
            builder.append("properties=").append(properties);
        }
        builder.append("]");
        return builder.toString();
    }

}
