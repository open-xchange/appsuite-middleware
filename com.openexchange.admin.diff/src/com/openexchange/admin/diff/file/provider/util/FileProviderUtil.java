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

package com.openexchange.admin.diff.file.provider.util;

import com.openexchange.admin.diff.file.provider.IConfigurationFileProvider;


/**
 * Util methods that can be used from the {@link IConfigurationFileProvider} implementations
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class FileProviderUtil {

    /**
     * Removes the root folder from the given String
     * 
     * @param fileWithFullPath - String which includes the root folder
     * @param rootFolder - root folder that will be removed
     * @return String without the root folder or the given String in case rootFolder param is null
     */
    public static String removeRootFolder(String fileWithFullPath, String rootFolder) {
        if (rootFolder == null) {
            return fileWithFullPath;
        }
        return fileWithFullPath.substring(rootFolder.length());
    }

}
