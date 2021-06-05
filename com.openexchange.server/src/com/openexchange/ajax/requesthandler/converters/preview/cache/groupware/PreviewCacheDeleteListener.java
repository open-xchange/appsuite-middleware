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

package com.openexchange.ajax.requesthandler.converters.preview.cache.groupware;

import java.sql.Connection;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.converters.preview.cache.rmi.ResourceCacheRMIServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link PreviewCacheDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PreviewCacheDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link PreviewCacheDeleteListener} instance.
     */
    public PreviewCacheDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            deleteUserEntriesFromDB(event, writeCon);
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            deleteContextEntries(event.getContext().getContextId(), writeCon);
        }
    }

    protected void deleteContextEntries(final int contextId, final Connection writeCon) throws OXException {
        // Cleanse by instance
        final ResourceCache resourceCache = ResourceCacheRMIServiceImpl.CACHE_REF.get();
        if (null == resourceCache) {
            return;
        }
        try {
            resourceCache.clearFor(contextId);
        } catch (Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(PreviewCacheDeleteListener.class);
            logger.warn("Failed to clean resource cache for deleted context {}", Integer.valueOf(contextId), e);
        }
    }

    private void deleteUserEntriesFromDB(final DeleteEvent event, final Connection writeCon) throws OXException {
        final int contextId = event.getContext().getContextId();
        final int userId = event.getId();

        // Cleanse by instance
        final ResourceCache resourceCache = ResourceCacheRMIServiceImpl.CACHE_REF.get();
        if (null == resourceCache) {
            return;
        }
        try {
            resourceCache.remove(userId, contextId);
        } catch (Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(PreviewCacheDeleteListener.class);
            logger.warn("Failed to clean resource cache for deleted user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId), e);
        }
    }

}
