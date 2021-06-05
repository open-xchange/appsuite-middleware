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

package com.openexchange.groupware.infostore.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link FileChecksumsRMIService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface FileChecksumsRMIService extends Remote {

    public static final String RMI_NAME = FileChecksumsRMIService.class.getSimpleName();

    /**
     * Provides a listing of all files in a context with a missing checksum property.
     *
     * @param contextId The identifier of the context to list the files for
     * @return A listing of all files in the context with a missing checksum property
     */
    List<String> listFilesWithoutChecksumInContext(int contextId) throws RemoteException;

    /**
     * Provides a listing of all files in a database with a missing checksum property.
     *
     * @param databaseId The read- or write-pool identifier of the database to list the files for
     * @return A listing of all files in the database with a missing checksum property
     */
    List<String> listFilesWithoutChecksumInDatabase(int databaseId) throws RemoteException;

    /**
     * Provides a listing of all files in all contexts with a missing checksum property.
     *
     * @return A listing of all files with a missing checksum property
     */
    List<String> listAllFilesWithoutChecksum() throws RemoteException;

    /**
     * Calculates and stores missing checksums for all files of a specific context.
     *
     * @param contextId The identifier of the context to calculate the missing checksums for
     * @return A listing of all files in the context where the missing checksum was calculated for
     */
    List<String> calculateMissingChecksumsInContext(int contextId) throws RemoteException;

    /**
     * Calculates and stores missing checksums for all files of a database.
     *
     * @param databaseId The read- or write-pool identifier of the database to calculate the missing checksums for
     * @return A listing of all files in the database where the missing checksum was calculated for
     */
    List<String> calculateMissingChecksumsInDatabase(int databaseId) throws RemoteException;

    /**
     * Calculates and stores missing checksums for all files in all contexts.
     *
     * @return A listing of all files where the missing checksum was calculated for
     */
    List<String> calculateAllMissingChecksums() throws RemoteException;
}
