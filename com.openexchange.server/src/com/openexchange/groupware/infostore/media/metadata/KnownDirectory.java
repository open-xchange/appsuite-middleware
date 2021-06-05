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

package com.openexchange.groupware.infostore.media.metadata;

import com.drew.metadata.Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.openexchange.java.Strings;

/**
 *
 * {@link KnownDirectory} contains known metadata directories
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public enum KnownDirectory {

    /**
     * The EXIF directory
     */
    EXIF("exif", null),
    /**
     * The GPS directory
     */
    GPS("gps", GpsDirectory.class),
    /**
     * The IPTC directory
     */
    IPTC("iptc", IptcDirectory.class);

    private final String id;
    private final Class<? extends Directory> optConcretetDirectoryType;

    private KnownDirectory(String id, Class<? extends Directory> directoryType) {
        this.id = id;
        this.optConcretetDirectoryType = directoryType;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the optional concrete directory type
     *
     * @return The concrete directory type or <code>null</code>
     */
    public Class<? extends Directory> getConcretetDirectoryType() {
        return optConcretetDirectoryType;
    }

    /**
     * Gets the known directory for given <code>Directory</code> instance.
     *
     * @param directory The <code>Directory</code> instance to look-up by
     * @return The associated known directory or <code>null</code>
     */
    public static KnownDirectory knownDirectoryFor(Directory directory) {
        if (null != directory) {
            String directoryName = Strings.asciiLowerCase(directory.getName());
            for (KnownDirectory knownDirectory : KnownDirectory.values()) {
                Class<? extends Directory> concretetDirectoryType = knownDirectory.optConcretetDirectoryType;
                if (null != concretetDirectoryType) {
                    // Check by type
                    if (concretetDirectoryType.equals(directory.getClass())) {
                        return knownDirectory;
                    }
                } else {
                    // Check by name
                    if (directoryName.indexOf(knownDirectory.id) >= 0) {
                        return knownDirectory;
                    }
                }
            }
        }
        return null;
    }
}