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

package com.openexchange.mobile.configuration.json.osgi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.action.ActionTypes;
import com.openexchange.osgi.ServiceRegistry;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public final class MobilityProvisioningServiceRegistry extends ServiceRegistry {

	private final static MobilityProvisioningServiceRegistry instance = new MobilityProvisioningServiceRegistry();

	private final Map<ActionTypes, ActionService> actionServices = new ConcurrentHashMap<ActionTypes, ActionService>();

    public static MobilityProvisioningServiceRegistry getInstance() {
        return instance;
    }

    public void putActionService(final ActionTypes identifier, final ActionService actionService) {
        actionServices.put(identifier, actionService);
    }

    public ActionService getActionService(final ActionTypes identifier) {
        return actionServices.get(identifier);
    }

    public ActionService removeActionService(final ActionTypes identifier) {
        return actionServices.remove(identifier);
    }

    public boolean containsService(final ActionTypes identifier) {
    	return actionServices.containsKey(identifier);
    }

    public void clearActionServices() {
        actionServices.clear();
    }

	/**
	 * Initializes a new {@link MobilityProvisioningServiceRegistry}
	 */
	private MobilityProvisioningServiceRegistry() {
		super();
	}

}
