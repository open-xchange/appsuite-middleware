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
 * Stores the version of the Middleware.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @deprecated Use {@link VersionService} instead
 */
@Deprecated
public class Version {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Version.class);

    public static final String CODENAME = "Hyperion";
    public static final String NAME = "Open-Xchange";

    public static final Version SINGLETON = new Version();

    /**
     * Gets the version instance
     *
     * @return The version instance
     */
    public static final Version getInstance() {
        return SINGLETON;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private volatile ServerVersion serverVersion = null;
    private volatile String buildDate = null;

    protected Version() {
        super();
    }

    /**
     * Sets the numbers for this version instance.
     *
     * @param serverVersion The number to set
     */
    public void setNumbers(ServerVersion serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * Sets the build date for this version instance.
     *
     * @param buildDate The build date to set
     */
    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    /**
     * Gets the build date.
     *
     * @return The build date
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public String getBuildDate() {
        if (null == buildDate) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return buildDate;
    }

    /**
     * Gets the major number.
     *
     * @return The major number
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public int getMajor() {
        if (null == serverVersion) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return serverVersion.getMajor();
    }

    /**
     * Gets the minor number.
     *
     * @return The minor number
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public int getMinor() {
        if (null == serverVersion) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return serverVersion.getMinor();
    }

    /**
     * Gets the patch number.
     *
     * @return The patch number
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public int getPatch() {
        if (null == serverVersion) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return serverVersion.getPatch();
    }

    /**
     * Gets the version string; e.g. <code>"7.8.3-Rev2"</code>.
     *
     * @return The version string
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public String getVersionString() {
        if (null == serverVersion) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return serverVersion.getVersionString();
    }

    /**
     * Optionally gets the version string.
     *
     * @return The version string, or <code>null</code> if it is not yet initialized
     */
    public String optVersionString() {
        return null == serverVersion ? null : serverVersion.getVersionString();
    }

}
