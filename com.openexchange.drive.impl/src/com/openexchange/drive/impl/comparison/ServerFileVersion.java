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

import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;


/**
 * {@link ServerFileVersion}
 *
 * File version hosting the represented server file.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <V>
 */
public class ServerFileVersion implements FileVersion {

    private final File file;
    private final FileChecksum checksum;

    /**
     * Initializes a new {@link ServerFileVersion}.
     *
     * @param file The file
     * @param checksum The checksum
     */
    public ServerFileVersion(File file, FileChecksum checksum) {
        super();
        this.checksum = checksum;
        this.file = file;
    }

    @Override
    public String getChecksum() {
        return checksum.getChecksum();
    }

    @Override
    public String getName() {
        return file.getFileName();
    }

    /**
     * Gets the file
     *
     * @return The file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the file checksum
     *
     * @return The file checksum
     */
    public FileChecksum getFileChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return getName() + " | " + getChecksum() + " [" + file.getId() + ']';
    }

    /**
     * Gets the matching server file version to the supplied file version, throwing an exception if it not exists.
     *
     * @param fileVersion The file version to match
     * @param path The path the file version is located in
     * @param session The sync session
     * @return The matching server file version, never <code>null</code>
     * @throws OXException If the file version not exists
     */
    public static ServerFileVersion valueOf(FileVersion fileVersion, String path, SyncSession session) throws OXException {
        if (ServerFileVersion.class.isInstance(fileVersion)) {
            return (ServerFileVersion)fileVersion;
        }
        File file = session.getStorage().getFileByName(path, fileVersion.getName(), true);
        if (null != file) {
            FileChecksum fileChecksum = ChecksumProvider.getChecksum(session, file);
            if (fileVersion.getChecksum().equals(fileChecksum.getChecksum())) {
                return new ServerFileVersion(file, fileChecksum);
            }
        }
        throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
    }

}
