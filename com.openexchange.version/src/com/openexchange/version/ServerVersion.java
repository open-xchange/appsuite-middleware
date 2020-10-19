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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

/**
 * {@link ServerVersion} - Data object storing the version and build number from the manifest of this bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a> - Original <code>Numbers</code> class
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Extended, renamed and published with 7.10.5
 */
public class ServerVersion implements Comparable<ServerVersion> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ServerVersion.class);

    private static final String EXPRESSION = "([0-9]+)\\.([0-9]+)\\.([0-9]+)";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION);

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
        this.versionString = version + "-Rev" + buildNumber;
    }

    /**
     * Initializes a new {@link ServerVersion}.
     * 
     * @param version The version
     * @param buildNumber The build number
     * @throws Exception In case version string can't be parsed
     */
    public ServerVersion(String version, String buildNumber) throws Exception {
        super();
        this.version = version;
        this.buildNumber = buildNumber;
        Matcher matcher = PATTERN.matcher(version);
        if (matcher.find()) {
            try {
                major = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new Exception("Can not parse major out of version \"" + version + "\".", e);
            }
            try {
                minor = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                throw new Exception("Can not parse minor out of version \"" + version + "\".", e);
            }
            try {
                patch = Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException e) {
                throw new Exception("Can not parse patch out of version \"" + version + "\".", e);
            }
        } else {
            throw new Exception("Version pattern does not match on version string \"" + version + "\".");
        }
        this.versionString = version + "-Rev" + buildNumber;
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
