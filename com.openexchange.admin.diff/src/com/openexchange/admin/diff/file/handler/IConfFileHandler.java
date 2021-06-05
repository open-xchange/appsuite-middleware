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

package com.openexchange.admin.diff.file.handler;

import java.util.List;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * This interface defines all method to implement for new configurationFileHandler that deals with the given configuration file formats.
 * Each handler is responsible for one file format.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public interface IConfFileHandler {

    /**
     * This method is used from internal files
     * 
     * @param diffResult - The {@link DiffResult} the processing results of the current handler will be attached.
     * @return The {@link DiffResult} with all results of the previously given and the results of this handler.
     */
    public DiffResult getDiff(DiffResult diffResult);

    /**
     * This method might be used from different handlers<br>
     * <br>
     * Hint: only use provided objects for processing within this method and do not work on singleton members!
     * 
     * @param diffResult - the object that will be aerated with the results
     * @param lOriginalFiles - original files to diff
     * @param lInstalledFiles - installed files to diff
     * @return The {@link DiffResult} with all results of the previously given and the results of this handler.
     */
    public DiffResult getDiff(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles);

    /**
     * Add a file for processing to get a diff result.
     * 
     * @param diffResult - the object that will be aerated with the results
     * @param configurationFile - the file that should be processed
     */
    public void addFile(DiffResult diffResult, ConfigurationFile configurationFile);
}
