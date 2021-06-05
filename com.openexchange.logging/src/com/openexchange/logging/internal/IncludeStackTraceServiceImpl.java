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

package com.openexchange.logging.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.exception.OXException;
import com.openexchange.session.UserAndContext;


/**
 * {@link IncludeStackTraceServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IncludeStackTraceServiceImpl implements IncludeStackTraceService {

    /** The map for tuples */
    private final ConcurrentMap<UserAndContext, Boolean> map;

    /** The has-any flag */
    private volatile boolean hasAny;

    /**
     * Initializes a new {@link IncludeStackTraceServiceImpl}.
     */
    public IncludeStackTraceServiceImpl() {
        super();
        map = new ConcurrentHashMap<UserAndContext, Boolean>(32, 0.9f, 1);
        hasAny = false;
    }

    @Override
    public boolean includeStackTraceOnError(int userId, int contextId) throws OXException {
        return hasAny && map.containsKey(UserAndContext.newInstance(userId, contextId));
    }

    @Override
    public boolean isEnabled() {
        return hasAny;
    }

    /**
     * Adds specified user / context identifier pair.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param enable <code>true</code> to enable stack traces; otherwise <code>false</code>
     */
    public void addTuple(int userId, int contextId, boolean enable) {
        if (enable) {
            hasAny = true;
            map.put(UserAndContext.newInstance(userId, contextId), Boolean.TRUE);
        } else {
            map.remove(UserAndContext.newInstance(userId, contextId));
            hasAny = !map.isEmpty();
        }
    }

}
