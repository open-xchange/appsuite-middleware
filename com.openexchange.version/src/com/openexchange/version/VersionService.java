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

package com.openexchange.version;

/**
 * 
 * {@link VersionService} provides the version of the Middleware
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public interface VersionService {

    public static final String CODENAME = "Hyperion";
    public static final String NAME = "Open-Xchange";

    /**
     * Gets the build date.
     *
     * @return The build date
     */
    public String getBuildDate();

    /**
     * Gets the major number.
     *
     * @return The major number
     */
    public int getMajor();

    /**
     * Gets the minor number.
     *
     * @return The minor number
     */
    public int getMinor();

    /**
     * Gets the patch number.
     *
     * @return The patch number
     */
    public int getPatch();

    /**
     * Gets the version string; e.g. <code>"7.8.3-Rev2"</code>.
     *
     * @return The version string
     */
    public String getVersionString();

}
