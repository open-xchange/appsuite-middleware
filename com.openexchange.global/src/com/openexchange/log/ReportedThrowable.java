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

package com.openexchange.log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link ReportedThrowable} - A reported {@link Throwable} instance along with optional properties.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReportedThrowable {

    private final Throwable throwable;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link ReportedThrowable}.
     * 
     * @param throwable The {@code Throwable} instance
     */
    public ReportedThrowable(final Throwable throwable) {
        this(throwable, null);
    }

    /**
     * Initializes a new {@link ReportedThrowable}.
     * 
     * @param throwable The {@code Throwable} instance
     * @param properties The optional properties; pass <code>null</code> to ignore
     */
    public ReportedThrowable(final Throwable throwable, final Map<String, Object> properties) {
        super();
        this.throwable = throwable;
        this.properties = null == properties ? null : new HashMap<String, Object>(properties);
    }

    /**
     * Gets the <code>Throwable</code> instance.
     * 
     * @return The <code>Throwable</code> instance
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Gets the property associated with given name.
     * 
     * @param name The name
     * @return The property value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V getProperty(final String name) {
        final Map<String, Object> properties = this.properties;
        return null == properties ? null : (V) properties.get(name);
    }

    /**
     * Gets the property names.
     * 
     * @return The property names
     */
    public Collection<String> getPropertyNames() {
        final Map<String, Object> properties = this.properties;
        return null == properties ? Collections.<String> emptyList() : properties.keySet();
    }

    /**
     * A {@link Set} view of the properties. The set is backed by the properties' map, so changes to the map are reflected in the set, and
     * vice-versa.
     * 
     * @return The entry set
     */
    public Set<Entry<String, Object>> entrySet() {
        final Map<String, Object> properties = this.properties;
        return null == properties ? Collections.<Entry<String, Object>> emptySet() : properties.entrySet();
    }

}
