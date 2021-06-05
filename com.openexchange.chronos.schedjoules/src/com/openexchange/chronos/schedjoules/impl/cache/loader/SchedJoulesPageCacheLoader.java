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

import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesAPICache;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesCachedItemKey;
import com.openexchange.exception.OXException;

/**
 * {@link SchedJoulesPageCacheLoader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesPageCacheLoader extends AbstractSchedJoulesCacheLoader {

    /**
     * Initialises a new {@link SchedJoulesPageCacheLoader}.
     */
    public SchedJoulesPageCacheLoader(SchedJoulesAPICache apiCache) {
        super(apiCache);
    }

    @Override
    boolean isModified(SchedJoulesCachedItemKey key, SchedJoulesPage page) throws OXException {
        SchedJoulesAPI api = apiCache.getAPI(key.getContextId());
        return api.pages().isModified(key.getItemId(), key.getLocale(), page.getEtag(), page.getLastModified());
    }

    @Override
    public SchedJoulesPage load(SchedJoulesCachedItemKey key) throws Exception {
        SchedJoulesAPI api = apiCache.getAPI(key.getContextId());
        return api.pages().getPage(key.getItemId(), key.getLocale());
    }
}
