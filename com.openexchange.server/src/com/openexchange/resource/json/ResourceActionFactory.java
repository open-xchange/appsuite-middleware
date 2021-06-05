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

package com.openexchange.resource.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.resource.json.actions.AbstractResourceAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ResourceActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@com.openexchange.ajax.requesthandler.Module(actions = {"get","all","list","search","updates"})
public class ResourceActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AbstractResourceAction> actions;

    /**
     * Initializes a new {@link ResourceActionFactory}.
     *
     * @param services The service look-up
     */
    public ResourceActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AbstractResourceAction>(5, 0.9f, 1);
        actions.put("get", new com.openexchange.resource.json.actions.GetAction(services));
        actions.put("all", new com.openexchange.resource.json.actions.AllAction(services));
        actions.put("list", new com.openexchange.resource.json.actions.ListAction(services));
        actions.put("search", new com.openexchange.resource.json.actions.SearchAction(services));
        actions.put("updates", new com.openexchange.resource.json.actions.UpdatesAction(services));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

}
