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

package com.openexchange.contact;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.contact.common.ContactsParameters;

/**
 * {@link DefaultContactsParameters}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DefaultContactsParameters implements ContactsParameters {

    private final ConcurrentMap<String, Object> parameters;

    /**
     * Initializes a new {@link DefaultContactsParameters}.
     */
    public DefaultContactsParameters() {
        super();
        this.parameters = new ConcurrentHashMap<String, Object>();
    }

    /**
     * Initializes a new {@link DefaultContactsParameters}.
     *
     * @param parameters The contacts parameters to use for initialization
     */
    public DefaultContactsParameters(ContactsParameters parameters) {
        this();
        if (null != parameters) {
            for (Entry<String, Object> entry : parameters.entrySet()) {
                set(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public <T> ContactsParameters set(String parameter, T value) {
        if (null != value) {
            parameters.put(parameter, value);
        } else {
            parameters.remove(parameter);
        }
        return this;
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.containsKey(parameter);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(parameters.entrySet());
    }
}
