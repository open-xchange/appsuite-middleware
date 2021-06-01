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

package com.openexchange.chronos.schedjoules.impl.cache.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesAPICache;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesCachedItemKey;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.timer.TimerService;

/**
 * {@link AbstractSchedJoulesCacheLoader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractSchedJoulesCacheLoader extends CacheLoader<SchedJoulesCachedItemKey, SchedJoulesPage> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchedJoulesCacheLoader.class);
    final SchedJoulesAPICache apiCache;

    /**
     * Initialises a new {@link AbstractSchedJoulesCacheLoader}.
     * 
     * @param apiCache The {@link SchedJoulesAPICache}
     */
    public AbstractSchedJoulesCacheLoader(SchedJoulesAPICache apiCache) {
        super();
        this.apiCache = apiCache;
    }

    @Override
    public ListenableFuture<SchedJoulesPage> reload(SchedJoulesCachedItemKey key, SchedJoulesPage oldValue) throws Exception {
        TimerService timerService = Services.getService(TimerService.class);
        ListenableFutureTask<SchedJoulesPage> task = ListenableFutureTask.create(() -> {
            if (isModified(key, oldValue)) {
                LOG.debug("The entry with key: '{}' was modified since last fetch. Reloading...", key.toString());
                return load(key);
            }
            LOG.debug("The entry with key: '{}' was not modified since last fetch.", key.toString());
            return oldValue;
        });
        timerService.getExecutor().execute(task);
        return task;
    }

    /**
     * Checks if the specified {@link SchedJoulesPage} is modified
     * 
     * @param page The {@link SchedJoulesPage}
     * @return <code>true</code> if the page was modified; <code>false</code> otherwise
     */
    abstract boolean isModified(SchedJoulesCachedItemKey key, SchedJoulesPage page) throws OXException;
}
