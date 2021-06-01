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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.openexchange.java.Strings;

/**
 * {@link ServerVersion} - Data object storing the version and build number from the manifest of this bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a> - Original <code>Numbers</code> class
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Extended, renamed and published with 7.10.5
 */
public class ServerVersion implements Comparable<ServerVersion> {

    /**
     * Parses the supplied server version string.
     * 
     * @param versionString The version string to parse, e.g. <code>7.10.5-Rev1</code>
     * @return The parsed server version
     * @throws IllegalArgumentException If string cannot be parsed
     */
    public static ServerVersion parse(String versionString) throws IllegalArgumentException {
        if (Strings.isEmpty(versionString)) {
            throw new IllegalArgumentException(versionString);
        }
        String[] splitBy = Strings.splitBy(versionString, '-', true);
        if (null == splitBy || 2 != splitBy.length) {
            return null;
        }
        String buildNumber = splitBy[1];
        if (Strings.isNotEmpty(buildNumber) && buildNumber.startsWith(REVISION_ID) && buildNumber.length() > REVISION_ID.length()) {
            buildNumber = buildNumber.substring(REVISION_ID.length());
        }
        return new ServerVersion(splitBy[0], buildNumber);
    }

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ServerVersion.class);

    private static final String EXPRESSION = "([0-9]+)\\.([0-9]+)\\.([0-9]+)";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION);
    private static final String REVISION_ID = "Rev";

    private final String versionString;
    private final String version;
    private final String buildNumber;
    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Initializes a new {@link ServerVersion}.
     * 
     * @param major The major version, e.g <code>7</code>
     * @param minor The minor version, e.g. <code>10</code>
     * @param patch The patch version, e.g. <code>5</code>
     * @param buildNumber The build number aka. the revision, e.g. <code>32</code>
     */
    public ServerVersion(int major, int minor, int patch, String buildNumber) {
        super();
        this.buildNumber = buildNumber;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.version = String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(patch);
        this.versionString = version + '-' + REVISION_ID + buildNumber;
    }

    /**
     * Initializes a new {@link ServerVersion}.
     * 
     * @param version The version
     * @param buildNumber The build number
     * @throws Exception In case version string can't be parsed
     */
    public ServerVersion(String version, String buildNumber) throws IllegalArgumentException {
        super();
        this.version = version;
        this.buildNumber = buildNumber;
        Matcher matcher = PATTERN.matcher(version);
        if (matcher.find()) {
            try {
                major = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Can not parse major out of version \"" + version + "\".", e);
            }
            try {
                minor = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Can not parse minor out of version \"" + version + "\".", e);
            }
            try {
                patch = Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Can not parse patch out of version \"" + version + "\".", e);
            }
        } else {
            throw new IllegalArgumentException("Version pattern does not match on version string \"" + version + "\".");
        }
        this.versionString = version + '-' + REVISION_ID + buildNumber;
    }

    /**
     * The version
     * 
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * The build number
     *
     * @return The build number aka. the revision
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * The major version
     *
     * @return The major version, e.g <code>7</code>
     */
    public int getMajor() {
        return major;
    }

    /**
     * The minor version
     *
     * @return The minor version, e.g. <code>10</code>
     */
    public int getMinor() {
        return minor;
    }

    /**
     * The patch version
     *
     * @return The patch version, e.g. <code>5</code>
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Get the full qualified server version
     *
     * @return The server version, e.g. <code>7.10.5-Rev1</code>
     */
    public String getVersionString() {
        return versionString;
    }

    @Override
    public String toString() {
        return "ServerVersion [" + getVersionString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((buildNumber == null) ? 0 : buildNumber.hashCode());
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((versionString == null) ? 0 : versionString.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        /* version string contains everything */
        ServerVersion other = (ServerVersion) obj;
        if (versionString == null) {
            if (other.versionString != null)
                return false;
        } else if (!versionString.equals(other.versionString)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ServerVersion other) {
        if (major > other.major) {
            return 1;
        } else if (major < other.major) {
            return -1;
        }
        if (minor > other.minor) {
            return 1;
        } else if (minor < other.minor) {
            return -1;
        }
        if (patch > other.patch) {
            return 1;
        } else if (patch < other.patch) {
            return -1;
        }
        try {
            Integer bn = Integer.valueOf(buildNumber);
            Integer otherBn = Integer.valueOf(other.buildNumber);

            return Integer.compare(bn.intValue(), otherBn.intValue());
        } catch (NumberFormatException e) {
            LOGGER.warn("Unable to parse build number of OX server version", e);
        }

        return 0;
    }

}
