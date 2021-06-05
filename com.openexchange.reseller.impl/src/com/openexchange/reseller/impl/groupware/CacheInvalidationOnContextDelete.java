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

package com.openexchange.reseller.impl.groupware;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.ContextDelete;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.reseller.impl.CachingResellerService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CacheInvalidationOnContextDelete} - Invalidates the {@link CachingResellerService#RESELLER_CONTEXT_NAME}
 * cache region upon context deletion.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class CacheInvalidationOnContextDelete extends ContextDelete {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInvalidationOnContextDelete.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CacheInvalidationOnContextDelete}.
     */
    public CacheInvalidationOnContextDelete(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (false == isContextDelete(event)) {
            return;
        }

        CacheService cacheService = services.getOptionalService(CacheService.class);
        if (null == cacheService) {
            return;
        }
        try {
            cacheService.getCache(CachingResellerService.RESELLER_CONTEXT_NAME).remove(I(event.getContext().getContextId()));
        } catch (OXException e) {
            LOGGER.error("", e);
        }
    }
}
