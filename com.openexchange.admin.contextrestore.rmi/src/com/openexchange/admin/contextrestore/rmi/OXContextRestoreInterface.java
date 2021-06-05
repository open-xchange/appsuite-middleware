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

package com.openexchange.admin.contextrestore.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * This class defines the Open-Xchange API for restoring OX Contexts.<br><br>
 *
 * At the moment this API defines only one call
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXContextRestoreInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXContextRestore";

    /**
     * This method is used to restore one single context
     *
     * @param ctx Context object
     * @param filenames The file names of the MySQL dump files which contain the backup of the context. <b>Note</b> that these files
     *                  have to be available to the admin daemon, so they must reside on the machine on which the admin
     *                  daemon is running.
     * @param optConfigDbName The optional name of the ConfigDB schema
     * @param auth Credentials for authenticating against server.
     * @param dryrun <code>true</code> to perform a dry run; otherwise <code>false</code>
     * @return The restored context's URI
     * @throws RemoteException General RMI Exception
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXContextRestoreException
     * @throws DatabaseUpdateException
     * @throws PoolException
     * @throws NoSuchContextException
     * @throws
     */
    public String restore(final Context ctx, final String[] filenames, final String optConfigDbName, final Credentials auth, boolean dryrun) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXContextRestoreException, DatabaseUpdateException;

}
