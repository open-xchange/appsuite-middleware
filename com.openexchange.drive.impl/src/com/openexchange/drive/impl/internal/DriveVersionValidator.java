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

package com.openexchange.drive.impl.internal;

import java.util.Collection;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link DriveVersionValidator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveVersionValidator {

    /**
     * Performs basic checks to verify that the supplied file versions are valid, i.e. are not <code>null</code> or empty, and their
     * checksum consists of a 32 characters long lowercase hexadecimal number string.
     *
     * @param fileVersions The file versions to check
     * @throws OXException If validation fails
     */
    public static void validateFileVersions(FileVersion...fileVersions) throws OXException {
        if (null != fileVersions && 0 < fileVersions.length) {
            for (FileVersion fileVersion : fileVersions) {
                validateFileVersion(fileVersion);
            }
        }
    }

    /**
     * Performs basic checks to verify that the supplied file versions are valid, i.e. are not <code>null</code> or empty, and their
     * checksum consists of a 32 characters long lowercase hexadecimal number string.
     *
     * @param fileVersions The file versions to check
     * @throws OXException If validation fails
     */
    public static void validateFileVersions(Collection<FileVersion> fileVersions) throws OXException {
        if (null != fileVersions && 0 < fileVersions.size()) {
            for (FileVersion fileVersion : fileVersions) {
                validateFileVersion(fileVersion);
            }
        }
    }

    /**
     * Performs basic checks to verify that the supplied file version is valid, i.e. is not <code>null</code> or empty, and it's
     * checksum consists of a 32 characters long lowercase hexadecimal number string.
     *
     * @param fileVersion The file version to check
     * @throws OXException If validation fails
     */
    public static void validateFileVersion(FileVersion fileVersion) throws OXException {
        if (null == fileVersion) {
            throw DriveExceptionCodes.INVALID_FILEVERSION.create("", "");
        }
        if (Strings.isEmpty(fileVersion.getChecksum()) || Strings.isEmpty(fileVersion.getName())) {
            throw DriveExceptionCodes.INVALID_FILEVERSION.create(fileVersion.getName(), fileVersion.getChecksum());
        }
        if (false == DriveConstants.CHECKSUM_VALIDATION_PATTERN.matcher(fileVersion.getChecksum()).matches()) {
            throw DriveExceptionCodes.INVALID_FILEVERSION.create(fileVersion.getName(), fileVersion.getChecksum());
        }
    }

    /**
     * Performs basic checks to verify that the supplied directory versions are valid, i.e. are not <code>null</code> or empty, and their
     * checksum consists of a 32 characters long lowercase hexadecimal number string.
     *
     * @param directoryVersions The directory versions to check
     * @throws OXException If validation fails
     */
    public static void validateDirectoryVersions(DirectoryVersion...directoryVersions) throws OXException {
        if (null != directoryVersions && 0 < directoryVersions.length) {
            for (DirectoryVersion directoryVersion : directoryVersions) {
                validateDirectoryVersion(directoryVersion);
            }
        }
    }

    /**
     * Performs basic checks to verify that the supplied directory versions are valid, i.e. are not <code>null</code> or empty, and their
     * checksum consists of a 32 characters long lowercase hexadecimal number string.
     *
     * @param directoryVersions The directory versions to check
     * @throws OXException If validation fails
     */
    public static void validateDirectoryVersions(Collection<DirectoryVersion> directoryVersions) throws OXException {
        if (null != directoryVersions && 0 < directoryVersions.size()) {
            for (DirectoryVersion directoryVersion : directoryVersions) {
                validateDirectoryVersion(directoryVersion);
            }
        }
    }

    /**
     * Performs basic checks to verify that the supplied directory version is valid, i.e. is not <code>null</code> or empty, and it's
     * checksum consists of a 32 characters long lowercase hexadecimal number string.
     *
     * @param directoryVersion The directory version to check
     * @throws OXException If validation fails
     */
    public static void validateDirectoryVersion(DirectoryVersion directoryVersion) throws OXException {
        if (null == directoryVersion) {
            throw DriveExceptionCodes.INVALID_DIRECTORYVERSION.create("", "");
        }
        if (Strings.isEmpty(directoryVersion.getChecksum()) || Strings.isEmpty(directoryVersion.getPath())) {
            throw DriveExceptionCodes.INVALID_DIRECTORYVERSION.create(directoryVersion.getPath(), directoryVersion.getChecksum());
        }
        if (false == DriveConstants.CHECKSUM_VALIDATION_PATTERN.matcher(directoryVersion.getChecksum()).matches()) {
            throw DriveExceptionCodes.INVALID_DIRECTORYVERSION.create(directoryVersion.getPath(), directoryVersion.getChecksum());
        }
    }

    private DriveVersionValidator() {
        super();
    }
}
