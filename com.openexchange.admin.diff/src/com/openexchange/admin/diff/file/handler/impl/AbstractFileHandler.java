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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.file.handler.IConfFileHandler;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.util.ConfigurationFileSearch;

/**
 * {@link AbstractFileHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public abstract class AbstractFileHandler implements IConfFileHandler {

    /**
     * List of files that will be ignored within the diff process
     */
    private final List<String> ignoredFiles = new ArrayList<String>(Arrays.asList(new String[] {
        "mpasswd",
        "secrets"
    }));

    private final List<String> ignoredDirectories = new ArrayList<String>(Arrays.asList(new String[] {
        "/languages/"
    }));

    /**
     * Registered installed files
     */
    protected List<ConfigurationFile> installedFiles = Collections.synchronizedList(new ArrayList<ConfigurationFile>());

    /**
     * Registered original files
     */
    protected List<ConfigurationFile> originalFiles = Collections.synchronizedList(new ArrayList<ConfigurationFile>());

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFile(DiffResult diffresult, ConfigurationFile configurationFile) {
        if (ignoredFiles.contains(configurationFile.getName()) || isIgnoredDirectory(configurationFile.getPathBelowRootDirectory())) {
            return;
        }

        if (configurationFile.isOriginal()) {
            for (ConfigurationFile orgFile : this.originalFiles) {
                final String fileName = orgFile.getName();
                if (fileName.equalsIgnoreCase(configurationFile.getName())) {
                    diffresult.getDuplicateFiles().add(configurationFile);
                }
            }
            originalFiles.add(configurationFile);
        } else {
            for (ConfigurationFile instFile : this.installedFiles) {
                final String fileName = instFile.getName();
                if (fileName.equalsIgnoreCase(configurationFile.getName())) {
                    diffresult.getDuplicateFiles().add(configurationFile);
                }
            }
            installedFiles.add(configurationFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diffResult) {
        getFileDiffs(diffResult, Collections.synchronizedList(new ArrayList<ConfigurationFile>(this.originalFiles)), Collections.synchronizedList(new ArrayList<ConfigurationFile>(this.installedFiles)));

        return getDiff(diffResult, this.originalFiles, this.installedFiles);
    }

    /**
     * Returns the diffs that belong to files. This method is called for each configuration file type.
     *
     * @param diff - the diff object to add file diff results
     * @param lOriginalFiles - original files that should be compared
     * @param lInstalledFiles - installed files the original ones should be compared with
     */
    protected void getFileDiffs(DiffResult diffResult, final List<ConfigurationFile> lOriginalFiles, final List<ConfigurationFile> lInstalledFiles) {

        for (ConfigurationFile origFile : lOriginalFiles) {
            final String fileName = origFile.getName();
            List<ConfigurationFile> result = new ConfigurationFileSearch().search(lInstalledFiles, fileName);

            // Not found in installation folder
            if (result.isEmpty()) {
                diffResult.getMissingFiles().add(origFile);
                continue;
            }

            if (lInstalledFiles.size() > 0) {
                lInstalledFiles.remove(result.get(0));
            }
        }

        if (lInstalledFiles.size() > 0) {
            diffResult.getAdditionalFiles().addAll(lInstalledFiles);
        }
    }

    private boolean isIgnoredDirectory(String folderPath) {

        for (String ignoredDirectory : ignoredDirectories) {
            if (folderPath.contains(ignoredDirectory)) {
                return true;
            }
        }
        return false;
    }
}
