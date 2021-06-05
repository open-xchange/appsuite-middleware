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

package com.openexchange.diagnostics.rmi.internal;

import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.diagnostics.DiagnosticService;
import com.openexchange.diagnostics.rmi.RemoteDiagnosticService;

/**
 * {@link RemoteDiagnosticServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RemoteDiagnosticServiceImpl implements RemoteDiagnosticService {

    private DiagnosticService delegate;

    /**
     * Initialises a new {@link RemoteDiagnosticServiceImpl}.
     */
    public RemoteDiagnosticServiceImpl(DiagnosticService diagnosticService) {
        super();
        this.delegate = diagnosticService;
    }

    @Override
    public List<String> getCharsets(boolean aliases) throws RemoteException {
        return delegate.getCharsets(aliases);
    }

    @Override
    public List<String> getProtocols() throws RemoteException {
        return delegate.getProtocols();
    }

    @Override
    public List<String> getCipherSuites() throws RemoteException {
        return delegate.getCipherSuites();
    }

    @Override
    public String getVersion() throws RemoteException {
        return delegate.getVersion();
    }
}
