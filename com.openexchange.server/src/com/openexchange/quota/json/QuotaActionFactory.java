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

package com.openexchange.quota.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.quota.json.actions.GetAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link QuotaActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class QuotaActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link QuotaActionFactory}.
     *
     * @param unifiedQuotaServices The tracked unified quota services
     * @param services The service look-up
     */
    public QuotaActionFactory(ServiceListing<UnifiedQuotaService> unifiedQuotaServices, ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AJAXActionService> actions = ImmutableMap.builder();
        GetAction getAction = new com.openexchange.quota.json.actions.GetAction(unifiedQuotaServices, services);
        actions.put("get", getAction);
        actions.put("GET", getAction);
        actions.put("filestore", new com.openexchange.quota.json.actions.FilestoreAction(services));
        actions.put("mail", new com.openexchange.quota.json.actions.MailAction(services));
        actions.put(UnifiedQuotaService.MODE, new com.openexchange.quota.json.actions.UnifiedQuotaAction(unifiedQuotaServices, services));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}
