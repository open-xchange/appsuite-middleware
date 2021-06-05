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

package com.openexchange.filestore.impl.groupware.unified;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link UnifiedQuotaDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class UnifiedQuotaDeleteListener implements DeleteListener {

    private final ServiceListing<UnifiedQuotaService> unifiedQuotaServices;

    /**
     * Initializes a new {@link UnifiedQuotaDeleteListener}.
     */
    public UnifiedQuotaDeleteListener(ServiceListing<UnifiedQuotaService> unifiedQuotaService) {
        super();
        this.unifiedQuotaServices = unifiedQuotaService;
    }

    @Override
    public void deletePerformed(DeleteEvent deleteEvent, Connection readCon, Connection writeCon) throws OXException {
        if (deleteEvent.getType() == DeleteEvent.TYPE_CONTEXT) {
            for (UnifiedQuotaService backendService : unifiedQuotaServices) {
                backendService.deleteEntryFor(deleteEvent.getContext().getContextId());
            }
        } else if (deleteEvent.getType() == DeleteEvent.TYPE_USER) {
            for (UnifiedQuotaService unifiedQuotaService : unifiedQuotaServices) {
                unifiedQuotaService.deleteEntryFor(deleteEvent.getId(), deleteEvent.getContext().getContextId());
            }
        }
    }

}
