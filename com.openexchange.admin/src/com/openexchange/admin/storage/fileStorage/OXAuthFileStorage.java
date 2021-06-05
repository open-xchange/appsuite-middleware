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

package com.openexchange.admin.storage.fileStorage;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXAuthStorageInterface;
import com.openexchange.exception.OXException;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;

/**
 * Default file implementation for admin auth.
 *
 * @author choeger
 */
public class OXAuthFileStorage extends OXAuthStorageInterface {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXAuthFileStorage.class);

    /** */
    public OXAuthFileStorage() {
        super();
    }

    /**
     * Authenticates against a textfile
     */
    @Override
    public boolean authenticate(final Credentials authdata) {
        final Credentials master = ClientAdminThread.cache.getMasterCredentials();
        if (master != null && authdata != null &&
           master.getLogin() != null && authdata.getLogin() != null &&
           master.getPassword() != null && authdata.getPassword() != null &&
           master.getLogin().equals(authdata.getLogin())) {
            try {
                PasswordMechRegistry factory = AdminServiceRegistry.getInstance().getService(PasswordMechRegistry.class);
                if (factory != null) {
                    PasswordMech passwordMech = factory.get(master.getPasswordMech());
                    return passwordMech.check(authdata.getPassword(), master.getPassword(), master.getSalt());
                }
            } catch (OXException e) {
                log.error("", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean authenticate(final Credentials authdata, final Context ctx) throws StorageException {
        return false;
    }

    @Override
    public boolean authenticateUser(final Credentials authdata, final Context ctx) throws StorageException {
        return false;
    }

}
