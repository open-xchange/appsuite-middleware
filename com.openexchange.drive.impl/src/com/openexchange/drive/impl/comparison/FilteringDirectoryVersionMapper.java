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
