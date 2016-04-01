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

package com.openexchange.drive.impl.sync.optimize;

import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.comparison.VersionMapper;


/**
 * {@link FileActionOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class FileActionOptimizer extends AbstractActionOptimizer<FileVersion> {

    /**
     * Initializes a new {@link FileActionOptimizer}.
     *
     * @param mapper The file version mapper
     */
    public FileActionOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    protected static boolean matchesByNameAndChecksum(FileVersion v1, FileVersion v2) {
        return matchesByName(v1, v2) && matchesByChecksum(v1, v2);
    }

    protected static boolean matchesByName(FileVersion v1, FileVersion v2) {
        if (null == v1) {
            return null == v2;
        } else if (null == v2) {
            return null == v1;
        } else {
            return null == v1.getName() ? null == v2.getName() : v1.getName().equals(v2.getName());
        }
    }

    /**
     * Gets a value indicating whether the supplied file version represents the virtual <code>.drive-meta</code> file or not.
     *
     * @param session The sync session
     * @param fileVersion The file version to check
     * @return <code>true</code> if the file version represents a <code>.drive-meta</code> file, <code>false</code>, otherwise
     */
    protected static boolean isDriveMeta(FileVersion fileVersion) {
        return null != fileVersion && DriveConstants.METADATA_FILENAME.equals(fileVersion.getName());
    }

}