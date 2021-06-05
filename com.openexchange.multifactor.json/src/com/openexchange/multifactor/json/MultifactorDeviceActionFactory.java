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
import com.openexchange.multifactor.json.actions.AllMultifactorDevicesAction;
import com.openexchange.multifactor.json.actions.BeginMultifactorAuthenticationAction;
import com.openexchange.multifactor.json.actions.DeleteMultifactorRegistrationAction;
import com.openexchange.multifactor.json.actions.DoAuthenticationMultifactorAction;
import com.openexchange.multifactor.json.actions.FinishMultifactorRegistrationAction;
import com.openexchange.multifactor.json.actions.RenameMultifactorAction;
import com.openexchange.multifactor.json.actions.StartMultifactorRegistrationAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MultifactorDeviceActionFactory} provides device related multifactor  API actions
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorDeviceActionFactory implements AJAXActionServiceFactory {

    public static final String MODULE = "multifactor/device";
    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link MultifactorDeviceActionFactory}.
     *
     * @param serviceLookup  The {@link ServiceLookup}
     * @throws OXException
     */
    public MultifactorDeviceActionFactory(ServiceLookup serviceLookup) throws OXException {
        actions = new ConcurrentHashMap<String, AJAXActionService>(4, 0.9f, 1);
        actions.put("all", new AllMultifactorDevicesAction(serviceLookup));
        actions.put("startRegistration", new StartMultifactorRegistrationAction(serviceLookup));
        actions.put("finishRegistration", new FinishMultifactorRegistrationAction(serviceLookup));
        actions.put("startAuthentication", new BeginMultifactorAuthenticationAction(serviceLookup));
        actions.put("finishAuthentication", new DoAuthenticationMultifactorAction(serviceLookup));
        actions.put("rename", new RenameMultifactorAction(serviceLookup));
        actions.put("delete", new DeleteMultifactorRegistrationAction(serviceLookup));
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
