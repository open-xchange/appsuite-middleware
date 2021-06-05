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

package com.openexchange.version.internal;

import java.util.Objects;
import com.openexchange.version.ServerVersion;
import com.openexchange.version.VersionService;

/**
 * Stores the version of the Middleware.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class VersionServiceImpl implements VersionService {

    private volatile ServerVersion serverVersion = null;
    private volatile String buildDate = null;

    /**
     * Initializes a new {@link VersionServiceImpl}.
     * 
     * @param buildDate The build date as {@link String}
     * @param serverVersion The server version
     */
    public VersionServiceImpl(String buildDate, ServerVersion serverVersion) {
        Objects.requireNonNull(buildDate, "The buildDate must not be null");
        Objects.requireNonNull(serverVersion, "The numbers must not be null");
        this.serverVersion = serverVersion;
        this.buildDate = buildDate;
    }

    @Override
    public String getBuildDate() {
        return buildDate;
    }

    @Override
    public int getMajor() {
        return serverVersion.getMajor();
    }

    @Override
    public int getMinor() {
        return serverVersion.getMinor();
    }

    @Override
    public int getPatch() {
        return serverVersion.getPatch();
    }

    @Override
    public String getVersionString() {
        return serverVersion.getVersionString();
    }

}
