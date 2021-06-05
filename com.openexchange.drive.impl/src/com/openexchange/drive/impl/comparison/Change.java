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

import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.internal.PathNormalizer;


/**
 * {@link Change}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum Change {

    /**
     * No change was detected.
     */
    NONE,

    /**
     * The version is new, i.e. did not exist before.
     */
    NEW,

    /**
     * A previously existing version was deleted.
     */
    DELETED,

    /**
     * The version was modified.
     */
    MODIFIED
    ;

    /**
     * Determines the {@link Change} between an original and a current version.
     *
     * @param originalVersion The original version
     * @param currentVersion The current version
     * @return The change between the original and current version
     */
    public static Change get(DriveVersion originalVersion, DriveVersion currentVersion) {
        if (null == currentVersion && null == originalVersion) {
            return Change.NONE;
        } else if (null == currentVersion) {
            return Change.DELETED;
        } else if (null == originalVersion) {
            return Change.NEW;
        } else if (false == equalsByChecksum(originalVersion, currentVersion) ||
            false == equalsByName(originalVersion, currentVersion)) {
            return Change.MODIFIED;
        } else {
            return Change.NONE;
        }
    }

    private static boolean equalsByName(DriveVersion v1, DriveVersion v2) {
        String name1;
        String name2;
        if (FileVersion.class.isInstance(v1) && FileVersion.class.isInstance(v2)) {
            name1 = ((FileVersion)v1).getName();
            name2 = ((FileVersion)v2).getName();
        } else if (DirectoryVersion.class.isInstance(v1) && DirectoryVersion.class.isInstance(v2)) {
            name1 = ((DirectoryVersion)v1).getPath();
            name2 = ((DirectoryVersion)v2).getPath();
        } else {
            throw new UnsupportedOperationException("incompatible drive versions");
        }
        return PathNormalizer.equals(name1, name2);
    }

    private static boolean equalsByChecksum(DriveVersion v1, DriveVersion v2) {
        if (null == v1) {
            return null == v2;
        } else if (null == v2) {
            return false;
        } else {
            return null == v1.getChecksum() ? null == v2.getChecksum() : v1.getChecksum().equalsIgnoreCase(v2.getChecksum());
        }
    }

}
