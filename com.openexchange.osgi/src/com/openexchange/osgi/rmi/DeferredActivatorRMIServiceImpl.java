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

package com.openexchange.osgi.rmi;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.console.ServiceState;

/**
 * {@link DeferredActivatorRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class DeferredActivatorRMIServiceImpl implements DeferredActivatorRMIService {

    /**
     * Initialises a new {@link DeferredActivatorRMIServiceImpl}.
     */
    public DeferredActivatorRMIServiceImpl() {
        super();
    }

    @Override
    public List<String> listMissingServices(String name) throws RemoteException {
        ServiceState serviceState = DeferredActivator.getLookup().determineState(name);
        return serviceState.getMissingServices();
    }

    @Override
    public Map<String, List<String>> listAllMissingServices() throws RemoteException {
        Map<String, List<String>> res = new ConcurrentHashMap<String, List<String>>();
        for (String bundleName : DeferredActivator.getLookup().getNames()) {
            ServiceState serviceState = DeferredActivator.getLookup().determineState(bundleName);
            List<String> list = serviceState.getMissingServices();
            if (!list.isEmpty()) {
                res.put(bundleName, list);
            }
        }
        return res;
    }

    @Override
    public boolean isActive(String name) throws RemoteException {
        ServiceState serviceState = DeferredActivator.getLookup().determineState(name);
        return serviceState.getMissingServices().isEmpty();
    }

    @Override
    public List<String> listAvailableBundles() throws RemoteException {
        return DeferredActivator.getLookup().getNames();
    }
}
