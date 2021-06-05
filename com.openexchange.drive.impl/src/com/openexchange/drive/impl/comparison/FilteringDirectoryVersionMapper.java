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
import com.openexchange.drive.DirectoryPattern;
import com.openexchange.drive.DirectoryVersion;


/**
 * {@link FilteringDirectoryVersionMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FilteringDirectoryVersionMapper extends DirectoryVersionMapper {

    /**
     * Initializes a new {@link VersionMapper} using collections of original-, client- and server directories.
     *
     * @param directoryExclusions A list of directory patterns matching those directory versions that should be excluded from mapping
     * @param originalVersions The original, i.e. previously known versions
     * @param clientVersions The current client versions
     * @param serverVersions The current server versions
     */
    public FilteringDirectoryVersionMapper(List<DirectoryPattern> directoryExclusions, Collection<? extends DirectoryVersion> originalVersions,
        Collection<? extends DirectoryVersion> clientVersions, Collection<? extends DirectoryVersion> serverVersions) {
        super(filterExclusions(originalVersions, directoryExclusions), clientVersions,
            filterExclusions(serverVersions, directoryExclusions));
    }

    @Override
    protected String getKey(DirectoryVersion version) {
        return version.getPath();
    }

    /**
     * Removes all directory versions matching one of the supplied directory exclusion patterns from the given collection.
     *
     * @param directoryVersions The directory versions to apply the exclusion filters
     * @param directoryExclusions The directory exclusions, or <code>null</code> if there are none
     * @return A filtered collection
     */
    private static Collection<? extends DirectoryVersion> filterExclusions(Collection<? extends DirectoryVersion> directoryVersions, List<DirectoryPattern> directoryExclusions) {
        if (null == directoryExclusions || 0 == directoryExclusions.size()) {
            return directoryVersions;
        }
        Collection<DirectoryVersion> filteredVersions = new ArrayList<DirectoryVersion>(directoryVersions.size());
        for (DirectoryVersion directoryVersion : directoryVersions) {
            if (false == matchesAny(directoryVersion, directoryExclusions)) {
                filteredVersions.add(directoryVersion);
            }
        }
        return filteredVersions;
    }

    /**
     * Gets a value indicating whether a directory version in a specific path matches any of the supplied directory patterns.
     *
     * @param directoryVersion The directory version
     * @param patterns The patterns
     * @return <code>true</code> if any of the patterns matches the directory version, <code>false</code>, otherwise
     */
    private static boolean matchesAny(DirectoryVersion directoryVersion, List<DirectoryPattern> patterns) {
        String path = directoryVersion.getPath();
        for (DirectoryPattern directoryExclusion : patterns) {
            if (directoryExclusion.matches(path)) {
                return true;
            }
        }
        return false;
    }

}
