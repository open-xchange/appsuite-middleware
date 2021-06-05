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

package com.openexchange.admin.diff.file.handler.impl;

import java.util.List;
import com.openexchange.admin.diff.ConfigDiff;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;



/**
 * Handler for files in configuration folders that are not defined as configuration files (per file extension)
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class NoConfigFileHandler extends AbstractFileHandler {

    private volatile static NoConfigFileHandler instance;

    private NoConfigFileHandler() {
        ConfigDiff.register(this);
    }

    public static synchronized NoConfigFileHandler getInstance() {
        if (instance == null) {
            synchronized (NoConfigFileHandler.class) {
                if (instance == null) {
                    instance = new NoConfigFileHandler();
                }
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {
        diffResult.getNonConfigurationFiles().addAll(lInstalledFiles);

        return diffResult;
    }

    /**
     * {@inheritDoc<br>
     * <br>
     * Files indicated as non-configuration-files do not need to get compared.
     */
    @Override
    protected void getFileDiffs(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {
        // nothing to to. already added by com.openexchange.admin.diff.file.handler.impl.NoConfigFileHandler.getDiff(DiffResult,
        // List<ConfigurationFile>, List<ConfigurationFile>)
    }
}
