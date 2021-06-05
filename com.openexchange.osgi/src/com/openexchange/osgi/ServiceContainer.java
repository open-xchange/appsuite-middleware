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

package com.openexchange.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.Constants;

/**
 * {@link ServiceContainer} - Container for a service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServiceContainer<S> {

    private static final String SERVICE_RANKING = Constants.SERVICE_RANKING;

    private final S service;
    private int ranking;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link ServiceContainer}.
     *
     * @param service The service
     */
    public ServiceContainer(S service) {
        this(service, 0);
    }

    /**
     * Initializes a new {@link ServiceContainer}.
     *
     * @param service The service
     * @param ranking The service ranking
     */
    public ServiceContainer(final S service, final int ranking) {
        super();
        this.service = service;
        this.ranking = ranking;
        properties = new HashMap<String, Object>(6);
        properties.put(SERVICE_RANKING, Integer.valueOf(ranking));
    }

    /**
     * Adds specified properties.
     *
     * @param properties The properties to add
     */
    public void addProperties(final Dictionary<String, Object> properties) {
        if (null == properties || properties.isEmpty()) {
            return;
        }
        final Map<String, Object> thisProperties = this.properties;
        for (final Enumeration<String> keys = properties.keys(); keys.hasMoreElements();) {
            final String key = keys.nextElement();
            final Object value = properties.get(key);
            if (null != value) {
                if (SERVICE_RANKING.equals(key)) {
                    ranking = ((Integer) value).intValue();
                }
                thisProperties.put(key, value);
            }
        }
    }

    /**
     * Adds specified properties.
     *
     * @param properties The properties to add
     */
    public void addProperties(final Map<String, Object> properties) {
        if (null == properties || properties.isEmpty()) {
            return;
        }
        if (properties.containsKey(SERVICE_RANKING)) {
            ranking = getRanking(properties);
        }
        this.properties.putAll(properties);
    }

    /**
     * Removes specified properties.
     *
     * @param keys The named properties to remove
     */
    public void removeProperties(final Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        final Map<String, Object> thisProperties = this.properties;
        for (final String key : keys) {
            if (SERVICE_RANKING.equals(key)) {
                ranking = 0;
            }
            thisProperties.remove(key);
        }
    }

    /**
     * Gets the <i>unmodifiable</i> properties
     *
     * @return The <i>unmodifiable</i> properties
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Gets the service
     *
     * @return The service
     */
    public S getService() {
        return service;
    }

    /**
     * Gets the service ranking
     *
     * @return The service ranking
     */
    public int getRanking() {
        return ranking;
    }

    /**
     * Sets the service ranking
     *
     * @param ranking The service ranking to set
     */
    public void setRanking(int ranking) {
        this.ranking = ranking;
        this.properties.put(SERVICE_RANKING, Integer.valueOf(ranking));
    }

    /** Gets the ranking */
    private static int getRanking(final Map<String, Object> properties) {
        final Object property = properties.get(SERVICE_RANKING);
        if (null == property) {
            return 0;
        }
        return ((Integer) property).intValue();
    }

}
