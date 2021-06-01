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

package com.openexchange.tools.oxfolder;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link GABRestorerRMIService} - The RMI service for restoring default permissions on global address book folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface GABRestorerRMIService extends Remote {

    public static final String RMI_NAME = GABRestorerRMIService.class.getSimpleName();

    /**
     * Restores default permissions on global address book folder.
     *
     * @param cid The context ID
     * @param gabMode The mode the global address book shall operate on
     * @throws RemoteException If an error is occurred
     */
    void restoreDefaultPermissions(int cid, GABMode gabMode) throws RemoteException;
}
