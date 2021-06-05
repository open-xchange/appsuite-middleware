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

package com.openexchange.net.ssl.management.json.action;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SSLCertificateManagementActionFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SSLCertificateManagementActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    private final Collection<AJAXActionService> supportedServices;

    /**
     * Initialises a new {@link SSLCertificateManagementActionFactory}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public SSLCertificateManagementActionFactory(ServiceLookup services) {
        super();
        Map<String, AJAXActionService> a = new HashMap<>(8);
        a.put("examine", new ExamineSSLCertificateAction(services));
        a.put("get", new GetSSLCertificateAction(services));
        a.put("store", new StoreSSLCertificateAction(services));
        a.put("delete", new DeleteSSLCertificateAction(services));
        a.put("deleteAll", new DeleteAllSSLCertificateAction(services));
        a.put("update", new UpdateSSLCertificateAction(services));
        a.put("all", new AllSSLCertificateAction(services));

        actions = Collections.unmodifiableMap(a);
        supportedServices = Collections.unmodifiableCollection(actions.values());
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }
}
