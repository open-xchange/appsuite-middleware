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

package com.openexchange.drive.impl.comparison;

import java.util.Arrays;
import java.util.List;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;


/**
 * {@link ServerDirectoryVersion}
 *
 * Directory version hosting the represented userized folder.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ServerDirectoryVersion implements DirectoryVersion {

    private final String normalizedPath;
    private final DirectoryChecksum checksum;

    /**
     * Initializes a new {@link ServerDirectoryVersion}.
     *
     * @param path The path
     * @param checksum The checksum
     */
    public ServerDirectoryVersion(String path, DirectoryChecksum checksum) {
        super();
        this.normalizedPath = PathNormalizer.normalize(path);
        this.checksum = checksum;
    }

    @Override
    public String getChecksum() {
        return checksum.getChecksum();
    }

    @Override
    public String getPath() {
        return normalizedPath;
    }

    /**
     * Gets the directory checksum
     *
     * @return The directory checksum
     */
    public DirectoryChecksum getDirectoryChecksum() {
        return checksum;
    }

    /**
     * Gets the matching server directory version to the supplied directory version, throwing an exception if it not exists.
     *
     * @param directoryVersion The directory version to match
     * @param path The path to the directory
     * @param session The sync session
     * @return The matching server directory version, never <code>null</code>
     * @throws OXException If the directory version not exists
     */
    public static ServerDirectoryVersion valueOf(DirectoryVersion directoryVersion, SyncSession session) throws OXException {
        if (ServerDirectoryVersion.class.isInstance(directoryVersion)) {
            return (ServerDirectoryVersion)directoryVersion;
        }
        String folderID = session.getStorage().getFolderID(directoryVersion.getPath());
        List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, Arrays.asList(new String[] { folderID }));
        if (null == checksums || 0 == checksums.size() || false == directoryVersion.getChecksum().equals(checksums.get(0).getChecksum())) {
            throw DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.create(directoryVersion.getPath(), directoryVersion.getChecksum());
        }
        return new ServerDirectoryVersion(directoryVersion.getPath(), checksums.get(0));
    }

    @Override
    public String toString() {
        return getPath() + " | " + getChecksum() + " [" + getDirectoryChecksum().getFolderID() + ']';
    }

}
