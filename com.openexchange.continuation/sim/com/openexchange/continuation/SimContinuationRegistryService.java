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

package com.openexchange.continuation;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link SimContinuationRegistryService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class SimContinuationRegistryService implements ContinuationRegistryService {

    private final ConcurrentMap<UUID, Continuation<?>> map;

    /**
     * Initializes a new {@link SimContinuationRegistryService}.
     */
    public SimContinuationRegistryService() {
        super();
        map = new ConcurrentHashMap<UUID, Continuation<?>>();
    }

    @Override
    public <V> Continuation<V> getContinuation(final UUID uuid, final Session session) throws OXException {
        return (Continuation<V>) map.get(uuid);
    }

    @Override
    public <V> void putContinuation(final Continuation<V> continuation, final Session session) throws OXException {
        map.put(continuation.getUuid(), continuation);
    }

    @Override
    public void removeContinuation(final UUID uuid, final Session session) throws OXException {
        map.remove(uuid);
    }

}
