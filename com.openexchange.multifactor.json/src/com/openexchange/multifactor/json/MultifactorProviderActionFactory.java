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

package com.openexchange.multifactor.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.json.actions.AllMultifactorProvidersAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MultifactorProviderActionFactory} provides provider related multifactor API actions
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorProviderActionFactory implements AJAXActionServiceFactory {

    public static final String MODULE = "multifactor/provider";
    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link MultifactorProviderActionFactory}.
     *
     * @param serviceLookup  The {@link ServiceLookup}
     * @throws OXException
     */
    public MultifactorProviderActionFactory(ServiceLookup serviceLookup) throws OXException {
        actions = new ConcurrentHashMap<String, AJAXActionService>(4, 0.9f, 1);
        actions.put("all", new AllMultifactorProvidersAction(serviceLookup));
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        AJAXActionService actionService = actions.get(action);
        if (actionService == null) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, MODULE);
        }
        return actionService;
    }

}
