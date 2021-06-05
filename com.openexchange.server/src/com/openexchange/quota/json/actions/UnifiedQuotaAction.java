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

package com.openexchange.quota.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.filestore.unified.UsageResult;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.quota.json.QuotaAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UnifiedQuotaAction} - The quota action for unified quota.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class UnifiedQuotaAction extends AbstractUnifiedQuotaAction {

    /**
     * Initializes a new {@link UnifiedQuotaAction}.
     *
     * @param unifiedQuotaServices The tracked unified quota services
     * @param services The service look-up
     */
    public UnifiedQuotaAction(ServiceListing<UnifiedQuotaService> unifiedQuotaServices, ServiceLookup services) {
        super(unifiedQuotaServices, services);
    }

    @Override
    protected AJAXRequestResult perform(QuotaAJAXRequest req) throws OXException, JSONException {
        ServerSession session = req.getSession();
        int userId = session.getUserId();
        int contextId = session.getContextId();

        UnifiedQuotaService unifiedQuotaService = getHighestRankedBackendService(userId, contextId);
        if (unifiedQuotaService == null) {
            throw ServiceExceptionCode.absentService(UnifiedQuotaService.class);
        }

        long limit = unifiedQuotaService.getLimit(userId, contextId);
        UsageResult usage = unifiedQuotaService.getUsage(userId, contextId);

        JSONObject data = new JSONObject(4).put("quota", limit).put("use", usage.getTotal());
        return new AJAXRequestResult(data, "json");
    }

}
