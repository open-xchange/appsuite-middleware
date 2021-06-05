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

import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.infostore.utils.FileMD5SumHelper;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link FileChecksumsRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class FileChecksumsRMIServiceImpl implements FileChecksumsRMIService {

    /**
     * Initialises a new {@link FileChecksumsRMIServiceImpl}.
     */
    public FileChecksumsRMIServiceImpl() {
        super();
    }

    @Override
    public List<String> listFilesWithoutChecksumInContext(int contextId) throws RemoteException {
        try {
            return FileMD5SumHelper.toString(contextId, getMD5SumHelper().listMissingInContext(contextId));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> listFilesWithoutChecksumInDatabase(int databaseId) throws RemoteException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().listMissingInDatabase(databaseId));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> listAllFilesWithoutChecksum() throws RemoteException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().listAllMissing());
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> calculateMissingChecksumsInContext(int contextId) throws RemoteException {
        try {
            return FileMD5SumHelper.toString(contextId, getMD5SumHelper().calculateMissingInContext(contextId));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> calculateMissingChecksumsInDatabase(int databaseId) throws RemoteException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().calculateMissingInDatabase(databaseId));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> calculateAllMissingChecksums() throws RemoteException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().calculateAllMissing());
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the MD5SumHelper
     * 
     * @return The MD5SumHelper
     * @throws OXException if the helper is absent
     */
    private FileMD5SumHelper getMD5SumHelper() throws OXException {
        DatabaseService dbService = ServerServiceRegistry.getServize(DatabaseService.class, true);
        QuotaFileStorageService fsService = ServerServiceRegistry.getServize(QuotaFileStorageService.class, true);
        return new FileMD5SumHelper(dbService, fsService);
    }
}
