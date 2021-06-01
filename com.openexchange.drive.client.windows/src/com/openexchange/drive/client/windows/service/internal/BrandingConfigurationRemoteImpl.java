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

package com.openexchange.drive.client.windows.service.internal;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.drive.client.windows.files.UpdateFilesProviderImpl;
import com.openexchange.drive.client.windows.service.rmi.BrandingConfigurationRemote;
import com.openexchange.exception.OXException;

/**
 * {@link BrandingConfigurationRemoteImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class BrandingConfigurationRemoteImpl implements BrandingConfigurationRemote {

    @Override
    public List<String> reload() throws OXException, RemoteException {
        UpdateFilesProviderImpl.getInstance().reload();
        return UpdateFilesProviderImpl.getInstance().getAvailableBrandings();
    }

    @Override
    public List<String> reload(String path) throws RemoteException, OXException {
        UpdateFilesProviderImpl.getInstance().reload(path);
        return UpdateFilesProviderImpl.getInstance().getAvailableBrandings();
    }

    private static final Pattern EXE_PATTERN = Pattern.compile(".*\\.exe");
    private static final Pattern MSI_PATTERN = Pattern.compile(".*\\.msi");
    private static final Pattern BRANDING_PATTERN = Pattern.compile(".*\\.branding");

    @Override
    public List<String> getBrandings(boolean validate, boolean invalidate_only) throws RemoteException, OXException {
        UpdateFilesProviderImpl provider = UpdateFilesProviderImpl.getInstance();
        List<String> brandings = provider.getAvailableBrandings();
        if (validate) {
            List<String> tmp = new ArrayList<String>();
            for (String brand : brandings) {
                if (provider.getFileName(brand, EXE_PATTERN) != null &&
                    provider.getFileName(brand, MSI_PATTERN) != null &&
                    provider.getFileName(brand, BRANDING_PATTERN) != null) {
                    
                    tmp.add(brand);
                }
            }
            if (invalidate_only){
                brandings.removeAll(tmp);
            } else {
                brandings = tmp;
            }
        }
        return brandings;
    }

}
