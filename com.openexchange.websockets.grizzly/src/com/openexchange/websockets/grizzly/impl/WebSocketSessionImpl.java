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

package com.openexchange.websockets.grizzly.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.websockets.WebSocketSession;

/**
 * {@link WebSocketSessionImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketSessionImpl implements WebSocketSession {

    private final ConcurrentMap<String, Object> attributes;

    /**
     * Initializes a new {@link WebSocketSessionImpl}.
     */
    public WebSocketSessionImpl() {
        super();
        attributes = new ConcurrentHashMap<>(8, 0.9F, 1);
    }

    @Override
    public <V> V getAttribute(String name) {
        try {
            return (V) (null == name ? null : attributes.get(name));
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Set<String> getAttributeNames() {
        return new LinkedHashSet<>(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (null == name) {
            return;
        }
        if (null == value) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (null == name) {
            return;
        }
        attributes.remove(name);
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

}
