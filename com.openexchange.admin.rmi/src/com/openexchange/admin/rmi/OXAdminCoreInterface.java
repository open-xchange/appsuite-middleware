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
package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * This interface defines a method for checking loaded plugins in the Open-Xchange Admin Daemon.<br><br>
 *
 * <b>Example:</b>
 * <pre>
 * final OXAdminCoreInterface iface = (OXAdminCoreInterface)Naming.lookup("rmi:///oxhost/"+OXAdminCoreInterface.RMI_NAME);
 *
 * if (iface.allPluginsLoaded()) {
 *	System.out.println("All plugins loaded");
 * }
 * </pre
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXAdminCoreInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXAdminCore";

    /**
     * This methods checks if all plugins have been loaded successfully
     *
     * @param credentials The credentials
     * @return true if all plugins are loaded successfully, false if not
     * @throws RemoteException
     */
    public boolean allPluginsLoaded() throws RemoteException;
}
