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

package com.openexchange.continuation.internal;

import java.util.UUID;
import com.google.common.cache.Cache;
import com.openexchange.caching.CacheElement;
import com.openexchange.caching.ElementEvent;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.continuation.Continuation;

/**
 * {@link ContinuationCacheElementEventHandler} - Closes elapsed {@link com.google.common.cache.Cache} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContinuationCacheElementEventHandler implements ElementEventHandler {

    private static final long serialVersionUID = -7746076251235016407L;

    /**
     * Initializes a new {@link ContinuationCacheElementEventHandler}.
     */
    public ContinuationCacheElementEventHandler() {
        super();
    }

    @SuppressWarnings("unchecked")
    private void doHandleElementEvent(final ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        ((Cache<UUID, Continuation<?>>) cacheElem.getVal()).invalidateAll(); // Notification send to associated RemovalListener
    }

    @Override
    public void handleElementEvent(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onExceededIdletimeBackground(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onExceededIdletimeOnRequest(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onExceededMaxlifeBackground(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onExceededMaxlifeOnRequest(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onSpooledDiskAvailable(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onSpooledDiskNotAvailable(final ElementEvent event) {
        doHandleElementEvent(event);
    }

    @Override
    public void onSpooledNotAllowed(final ElementEvent event) {
        doHandleElementEvent(event);
    }

}
