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

package com.openexchange.admin.plugin.hosting.rmi.impl;

import static com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils.convertException;
import java.rmi.RemoteException;
import com.openexchange.admin.plugin.hosting.services.AdminServiceRegistry;
import com.openexchange.admin.plugin.hosting.storage.interfaces.OXContextGroupStorageInterface;
import com.openexchange.admin.rmi.OXContextGroupInterface;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.exception.OXException;

/**
 * {@link OXContextGroup}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OXContextGroup implements OXContextGroupInterface {

    /**
     * Initializes a new {@link OXContextGroup}.
     */
    public OXContextGroup() {
        super();
    }

    @Override
    public void deleteContextGroup(String contextGroupId) throws RemoteException, StorageException {
        OXContextGroupStorageInterface storage = AdminServiceRegistry.getInstance().getService(OXContextGroupStorageInterface.class);

        if (contextGroupId == null) {
            throw new IllegalArgumentException("The contextGroupId is null");
        }

        try {
            storage.deleteContextGroup(contextGroupId);
        } catch (OXException e) {
            throw StorageException.wrapForRMI(e);
        } catch (RuntimeException e) {
            throw convertException(e);
        }
    }

}