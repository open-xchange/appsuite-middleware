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

package com.openexchange.groupware.infostore.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.Lock;

/**
 * {@link LockedUntilLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LockedUntilLoader extends MetadataLoader<List<Lock>> {

    private final EntityLockManager lockManager;

    /**
     * Initializes a new {@link LockedUntilLoader}.
     *
     * @param lockManager The underlying lock manager
     */
    public LockedUntilLoader(EntityLockManager lockManager) {
        super();
        this.lockManager = lockManager;
    }

    @Override
    protected DocumentMetadata set(DocumentMetadata document, List<Lock> metadata) {
        if (null != metadata && 0 < metadata.size()) {
            long maximumTimeout = 0;
            for (Lock lock : metadata) {
                maximumTimeout = Math.max(maximumTimeout, lock.getTimeout());
            }
            document.setLockedUntil(new Date(System.currentTimeMillis() + maximumTimeout));
        }
        return document;
    }

    @Override
    public Map<Integer, List<Lock>> load(Collection<Integer> ids, Context context) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyMap();
        }
        return lockManager.findLocks(new ArrayList<Integer>(ids), context);
    }

}
