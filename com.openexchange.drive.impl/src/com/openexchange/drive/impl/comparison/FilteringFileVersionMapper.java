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

package com.openexchange.drive.impl.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.drive.FilePattern;
import com.openexchange.drive.FileVersion;


/**
 * {@link FilteringFileVersionMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FilteringFileVersionMapper extends FileVersionMapper {

    /**
     * Initializes a new {@link VersionMapper} using collections of original-, client- and server files.
     *
     * @param path The path
     * @param fileExclusions A list of file patterns matching those file versions that should be excluded from mapping
     * @param originalVersions The original, i.e. previously known versions
     * @param clientVersions The current client versions
     * @param serverVersions The current server versions
     */
    public FilteringFileVersionMapper(String path, List<FilePattern> fileExclusions, Collection<? extends FileVersion> originalVersions,
        Collection<? extends FileVersion> clientVersions, Collection<? extends FileVersion> serverVersions) {
        super(filterExclusions(path, originalVersions, fileExclusions), clientVersions,
            filterExclusions(path, serverVersions, fileExclusions));
    }

    @Override
    protected String getKey(FileVersion version) {
        return version.getName();
    }

    /**
     * Removes all file versions matching one of the supplied file exclusion patterns from the given collection.
     *
     * @param path The path
     * @param fileVersions The file versions to apply the exclusion filters
     * @param fileExclusions The file exclusions, or <code>null</code> if there are none
     * @return A filtered collection
     */
    private static Collection<? extends FileVersion> filterExclusions(String path, Collection<? extends FileVersion> fileVersions, List<FilePattern> fileExclusions) {
        if (null == fileExclusions || 0 == fileExclusions.size()) {
            return fileVersions;
        }
        Collection<FileVersion> filteredVersions = new ArrayList<FileVersion>(fileVersions.size());
        for (FileVersion fileVersion : fileVersions) {
            if (false == matchesAny(path, fileVersion, fileExclusions)) {
                filteredVersions.add(fileVersion);
            }
        }
        return filteredVersions;
    }

    /**
     * Gets a value indicating whether a file version in a specific path matches any of the supplied file patterns.
     *
     * @param path The path
     * @param fileVersion The file version
     * @param patterns The patterns
     * @return <code>true</code> if any of the patterns matches the file version, <code>false</code>, otherwise
     */
    private static boolean matchesAny(String path, FileVersion fileVersion, List<FilePattern> patterns) {
        String name = fileVersion.getName();
        for (FilePattern fileExclusion : patterns) {
            if (fileExclusion.matches(path, name)) {
                return true;
            }
        }
        return false;
    }

}
