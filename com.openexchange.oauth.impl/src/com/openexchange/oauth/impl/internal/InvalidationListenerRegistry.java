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

package com.openexchange.oauth.impl.internal;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccountInvalidationListener;


/**
 * {@link InvalidationListenerRegistry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InvalidationListenerRegistry {
    private static volatile InvalidationListenerRegistry instance;

    /**
     * Initializes the registry instance.
     */
    public static void initInstance() {
        instance = new InvalidationListenerRegistry();
    }

    /**
     * Releases the registry instance.
     */
    public static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static InvalidationListenerRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<Class<? extends OAuthAccountInvalidationListener>, OAuthAccountInvalidationListener> registry;

    /**
     * Initializes a new {@link InvalidationListenerRegistry}.
     */
    public InvalidationListenerRegistry() {
        super();
        registry = new ConcurrentHashMap<Class<? extends OAuthAccountInvalidationListener>, OAuthAccountInvalidationListener>();
    }

    /**
     * Adds specified listener to this registry.
     *
     * @param listener The listener to add
     * @return <code>true</code> if listener could be successfully added; otherwise <code>false</code>
     */
    public boolean addInvalidationListener(final OAuthAccountInvalidationListener listener) {
        return (null == registry.putIfAbsent(listener.getClass(), listener));
    }

    /**
     * Removes specified listener from this registry.
     *
     * @param listener The listener to remove
     */
    public void removeInvalidationListener(final OAuthAccountInvalidationListener listener) {
        registry.remove(listener.getClass());
    }

    /**
     * Triggers the {@link OAuthAccountInvalidationListene#onAfterOAuthAccountInvalidation()} event for registered listeners.
     */
    public void onAfterOAuthAccountInvalidation(final int id, final Map<String, Object> properties, final int user, final int cid, final Connection con) throws OXException {
        for (final OAuthAccountInvalidationListener listener : registry.values()) {
            listener.onAfterOAuthAccountInvalidation(id, properties, user, cid, con);
        }
    }

}
