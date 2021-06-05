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

package com.openexchange.drive;


/**
 * {@link BrandedDriveVersionService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface BrandedDriveVersionService {

    /**
     * Adds or updates the soft and hard versions for a given branding.
     *
     * @param branding The branding
     * @param minSoftVersion The soft minimum version
     * @param minHardVersion The hard minimum version
     */
    public void putBranding(String branding, String minSoftVersion, String minHardVersion);

    /**
     * Remove all branding versions
     */
    public void clearAll();

    /**
     * Retrieves the soft minimum of the given brand
     *
     * @param branding The name of the branding
     * @return The soft minimum version or null
     */
    String getSoftMinimumVersion(String branding);

    /**
     * Retrieves the hard minimum of the given brand
     *
     * @param branding The name of the branding
     * @return The hard minimum version or null
     */
    String getHardMinimumVersion(String branding);

}
