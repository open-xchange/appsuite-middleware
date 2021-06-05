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

package com.openexchange.contact.storage.rdb.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link ContactStorageRMIService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface ContactStorageRMIService extends Remote {

    public static final String RMI_NAME = ContactStorageRMIService.class.getSimpleName();

    /**
     * De-duplicates contacts in a folder.
     *
     * @param contextID The context ID
     * @param folderID The folder ID
     * @param limit The maximum number of contacts to process, or <code>0</code> for no limits
     * @param dryRun <code>true</code> to analyse the folder for duplicates only, without actually performing the de-duplication,
     *            <code>false</code>, otherwise
     * @return The identifiers of the contacts identified (and deleted in case <code>dryRun</code> is <code>false</code>) as duplicates
     * @throws RemoteException
     */
    int[] deduplicateContacts(int contextID, int folderID, long limit, boolean dryRun) throws RemoteException;
}
