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

package com.openexchange.admin.storage.interfaces;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.fileStorage.OXAuthFileStorage;
import com.openexchange.admin.storage.mysqlStorage.OXAuthMySQLStorage;

/**
 *
 * @author cutmasta
 */
public abstract class OXAuthStorageInterface {
    public abstract boolean authenticate(final Credentials authdata);

    public abstract boolean authenticate(final Credentials authdata, final Context ctx) throws StorageException;

    public abstract boolean authenticateUser(final Credentials authdata, final Context ctx) throws StorageException;

    public static OXAuthStorageInterface getInstanceSQL() {
        synchronized (OXAuthStorageInterface.class) {
            return new OXAuthMySQLStorage();
        }
    }

    public static OXAuthStorageInterface getInstanceFile() {
        synchronized (OXAuthStorageInterface.class) {
            return new OXAuthFileStorage();
        }
    }
}
