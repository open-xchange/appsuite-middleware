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
import com.openexchange.oauth.OAuthAccountDeleteListener;

/**
 * {@link DeleteListenerRegistry} - Registry for OAuth account delete listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteListenerRegistry {

    private static volatile DeleteListenerRegistry instance;

    /**
     * Initializes the registry instance.
     */
    public static void initInstance() {
        instance = new DeleteListenerRegistry();
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
    public static DeleteListenerRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<Class<? extends OAuthAccountDeleteListener>, OAuthAccountDeleteListener> registry;

    /**
     * Initializes a new {@link DeleteListenerRegistry}.
     */
    public DeleteListenerRegistry() {
        super();
        registry = new ConcurrentHashMap<Class<? extends OAuthAccountDeleteListener>, OAuthAccountDeleteListener>();
    }

    /**
     * Adds specified delete listener to this registry.
     *
     * @param deleteListener The delete listener to add
     * @return <code>true</code> if listener could be successfully added; otherwise <code>false</code>
     */
    public boolean addDeleteListener(final OAuthAccountDeleteListener deleteListener) {
        return (null == registry.putIfAbsent(deleteListener.getClass(), deleteListener));
    }

    /**
     * Removes specified delete listener from this registry.
     *
     * @param deleteListener The delete listener to add
     */
    public void removeDeleteListener(final OAuthAccountDeleteListener deleteListener) {
        registry.remove(deleteListener.getClass());
    }

    /**
     * Triggers the {@link OAuthAccountDeleteListener#onBeforeOAuthAccountDeletion()} event for registered listeners.
     */
    public void triggerOnBeforeDeletion(final int id, final Map<String, Object> properties, int user, int cid, final Connection con) throws OXException {
        for (final OAuthAccountDeleteListener listener : registry.values()) {
            listener.onBeforeOAuthAccountDeletion(id, properties, user, cid, con);
        }
    }

    /**
     * Triggers the {@link OAuthAccountDeleteListener#onAfterOAuthAccountDeletion()} event for registered listeners.
     */
    public void triggerOnAfterDeletion(final int id, final Map<String, Object> properties, final int user, final int cid, final Connection con) throws OXException {
        for (final OAuthAccountDeleteListener listener : registry.values()) {
            listener.onAfterOAuthAccountDeletion(id, properties, user, cid, con);
        }
    }

}
