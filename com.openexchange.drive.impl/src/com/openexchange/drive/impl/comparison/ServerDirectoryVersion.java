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
