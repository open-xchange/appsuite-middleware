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

package com.openexchange.version.osgi;

import java.util.Dictionary;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.version.ServerVersion;
import com.openexchange.version.Version;
import com.openexchange.version.VersionService;
import com.openexchange.version.internal.VersionServiceImpl;

/**
 * Reads version and build number from the bundle manifest.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@SuppressWarnings("deprecation")
public class VersionActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VersionActivator.class);

    public VersionActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.version");
        Dictionary<String, String> headers = context.getBundle().getHeaders();
        String version = headers.get("OXVersion");
        if (null == version) {
            throw new Exception("Can not read version from bundle manifest " + context.getBundle().getSymbolicName());
        }
        String buildNumber = headers.get("OXRevision");
        if (null == buildNumber) {
            throw new Exception("Can not read buildNumber from bundle manifest.");
        }
        String date = headers.get("OXBuildDate");
        if (null == date) {
            throw new Exception("Can not read build date from bundle manifest.");
        }
        Version instance = Version.getInstance();
        instance.setNumbers(new ServerVersion(version, buildNumber));
        instance.setBuildDate(date);
        VersionService versionService = new VersionServiceImpl(date, new ServerVersion(version, buildNumber));
        registerService(VersionService.class, versionService);
        LOG.info(VersionServiceImpl.NAME + ' ' + versionService.getVersionString());
        LOG.info("(c) OX Software GmbH , Open-Xchange GmbH");

    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle com.openexchange.version");
        super.stopBundle();
    }
}
