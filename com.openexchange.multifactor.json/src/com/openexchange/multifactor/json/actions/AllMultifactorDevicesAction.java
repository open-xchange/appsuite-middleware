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

package com.openexchange.multifactor.json.actions;

import java.util.ArrayList;
import java.util.Collection;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.json.converter.MultifactorDevicesResultConverter;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AllMultifactorDevicesAction} gets a list of all registered {@link MultifactorDevice}s for the current session
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class AllMultifactorDevicesAction extends AbstractMultifactorAction {

    /**
     * Initializes a new {@link AllMultifactorDevicesAction}.
     * @param serviceLookup The {@link ServiceLookup} instance
     * @throws OXException
     */
    public AllMultifactorDevicesAction(ServiceLookup serviceLookup) throws OXException {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXMultifactorRequest request) throws OXException {
        final MultifactorProviderRegistry registry = serviceLookup.getServiceSafe(MultifactorProviderRegistry.class);
        MultifactorRequest multifactorRequest = request.getMultifactorRequest();
        Collection<MultifactorProvider> providers = registry.getProviders(multifactorRequest);
        ArrayList<MultifactorDevice> ret = new ArrayList<MultifactorDevice>();
        for(MultifactorProvider provider : providers) {
            ret.addAll(provider.getDevices(multifactorRequest));
        }

        return new AJAXRequestResult(ret, MultifactorDevicesResultConverter.INPUT_FORMAT);
    }
}
