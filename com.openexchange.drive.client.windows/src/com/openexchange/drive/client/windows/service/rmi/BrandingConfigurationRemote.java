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

package com.openexchange.drive.client.windows.service.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link BrandingConfigurationRemote} is a rmi interface to reload the current branding configuration's.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface BrandingConfigurationRemote extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = BrandingConfigurationRemote.class.getSimpleName();

    /**
     * This method reloads the branding configurations and the corresponding update files.
     * It uses the last used path. In the major of cases this will be the path specified in <code>com.openexchange.drive.updater.path</code>.
     * 
     * @return A list of the loaded branding identifiers.
     * @throws OXException if the branding folder is missing
     * @throws RemoteException
     */
    public List<String> reload() throws OXException, RemoteException;

    /**
     * This method reloads the branding configurations and the corresponding update files for given path.
     * It must be pointed out that <code>com.openexchange.drive.updater.path</code> will be ignored and the given path is used instead.
     * 
     * @param path The path to be used.
     * @return A list of the loaded branding identifiers.
     * @throws OXException if the branding folder is missing
     * @throws RemoteException
     */
    public List<String> reload(String path) throws RemoteException, OXException;

    /**
     * Retrieves all available branding's.
     *
     * @param validate If true retrieves only the branding's which include all the necessary files.
     * @param invalid_only Retrieves only the invalid branding's. The parameter validate must be true for this to take effect.
     * @return a list of branding identifiers
     * @throws OXException if it is unable to validate the branding's
     * @throws RemoteException
     */
    public List<String> getBrandings(boolean validate, boolean invalid_only) throws RemoteException, OXException;

}
