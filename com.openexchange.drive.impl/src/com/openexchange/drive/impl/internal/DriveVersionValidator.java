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
 *    trademarks of the OX Software GmbH group of companies.
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
