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

package com.openexchange.diagnostics.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.diagnostics.DiagnosticService;
import com.openexchange.diagnostics.internal.DiagnosticServiceImpl;
import com.openexchange.diagnostics.rmi.internal.RemoteDiagnosticServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link DiagnosticsActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DiagnosticsActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link DiagnosticsActivator}.
     */
    public DiagnosticsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = LoggerFactory.getLogger(DiagnosticsActivator.class);
        logger.info("Registering DiagnosticService");
        DiagnosticServiceImpl diagnosticService = new DiagnosticServiceImpl();
        registerService(DiagnosticService.class, diagnosticService);
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", RemoteDiagnosticServiceImpl.RMI_NAME);
        registerService(Remote.class, new RemoteDiagnosticServiceImpl(diagnosticService), serviceProperties);
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger logger = LoggerFactory.getLogger(DiagnosticsActivator.class);
        logger.info("Unregistering DiagnosticService");
        unregisterService(DiagnosticService.class);
        super.stopBundle();
    }
}
