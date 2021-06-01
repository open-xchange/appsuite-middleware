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

import java.util.Arrays;

/**
 * {@link DriveClientVersion}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveClientVersion implements Comparable<DriveClientVersion> {

    /**
     * The version "0".
     */
    public static final DriveClientVersion VERSION_0 = new DriveClientVersion("0");

    private final String version;
    private final int[] versionParts;
    private final int hashCode;

    /**
     * Initializes a new {@link DriveClientVersion}.
     *
     * @param version The version string, matching the pattern <code>^[0-9]+(\.[0-9]+)*$</code>.
     * @throws IllegalArgumentException If the version has an unexpected format
     */
    public DriveClientVersion(String version) throws IllegalArgumentException {
        super();
        if (null == version || false == version.matches("^[0-9]+(\\.[0-9]+)*$")) {
            throw new IllegalArgumentException(version);
        }
        this.version = version;
        String[] parts = version.split("\\.");
        versionParts = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                versionParts[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(version, e);
        }

        final int prime = 31;
        int result = 1;
        result = prime * result + version.hashCode();
        result = prime * result + Arrays.hashCode(versionParts);
        hashCode = result;
    }

    /**
     * Gets the version string
     *
     * @return The version string
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int compareTo(DriveClientVersion other) {
        if (null == other) {
            return 1;
        }
        int maxLength = Math.max(versionParts.length, other.versionParts.length);
        for (int i = 0; i < maxLength; i++) {
            int thisPart = i < versionParts.length ? versionParts[i] : 0;
            int otherPart = i < other.versionParts.length ? other.versionParts[i] : 0;
            if (thisPart < otherPart) {
                return -1;
            } else if (thisPart > otherPart) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DriveClientVersion other = (DriveClientVersion) obj;
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (!Arrays.equals(versionParts, other.versionParts)) {
            return false;
        }
        return true;
    }
}
