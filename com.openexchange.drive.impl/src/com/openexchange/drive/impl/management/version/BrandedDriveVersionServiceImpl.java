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

package com.openexchange.drive.impl.management.version;

import java.util.HashMap;
import com.openexchange.drive.BrandedDriveVersionService;


/**
 * {@link BrandedDriveVersionServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class BrandedDriveVersionServiceImpl implements BrandedDriveVersionService {

    private static BrandedDriveVersionServiceImpl instance = null;
    private HashMap<String, SoftHardVersionTuple> driveVersions;

    public static synchronized BrandedDriveVersionServiceImpl getInstance() {
        if (null == instance) {
            instance = new BrandedDriveVersionServiceImpl();
        }
        return instance;
    }

    @Override
    public String getSoftMinimumVersion(String branding) {
        if (driveVersions == null || !driveVersions.containsKey(branding)) {
            return null;
        }
        return driveVersions.get(branding).getSoftVersion();
    }

    @Override
    public String getHardMinimumVersion(String branding) {
        if (driveVersions == null || !driveVersions.containsKey(branding)) {
            return null;
        }
        return driveVersions.get(branding).getHardVersion();
    }

    @Override
    public void putBranding(String branding, String minSoftVersion, String minHardVersion) {
        if (null == driveVersions) {
            driveVersions = new HashMap<String, BrandedDriveVersionServiceImpl.SoftHardVersionTuple>();
        }
        if (null == branding || null == minSoftVersion || null == minHardVersion || branding.isEmpty() || minSoftVersion.isEmpty() || minHardVersion.isEmpty()) {
            return;
        }
        driveVersions.put(branding, new SoftHardVersionTuple(minSoftVersion, minHardVersion));
    }

    @Override
    public void clearAll() {
        if (driveVersions != null) {
            driveVersions.clear();
        }
    }

    /**
     *
     * {@link SoftHardVersionTuple}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.8.1
     */
    private static class SoftHardVersionTuple {

        private final String soft;
        private final String hard;

        public SoftHardVersionTuple(String soft, String hard) {
            super();
            this.soft = soft;
            this.hard = hard;
        }

        String getSoftVersion() {
            return soft;
        }

        String getHardVersion() {
            return hard;
        }
    }

}
