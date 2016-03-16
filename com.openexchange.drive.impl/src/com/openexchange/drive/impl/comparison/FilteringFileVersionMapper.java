/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
