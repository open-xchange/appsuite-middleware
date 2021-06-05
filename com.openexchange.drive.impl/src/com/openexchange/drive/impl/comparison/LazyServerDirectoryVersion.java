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

import com.openexchange.drive.impl.checksum.ChecksumSupplier;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.exception.OXException;

/**
 * {@link LazyServerDirectoryVersion}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class LazyServerDirectoryVersion extends ServerDirectoryVersion {

    private final String normalizedPath;
    private final String folderID;
    private final ChecksumSupplier checksumSupplier;

    private DirectoryChecksum knownChecksum;

    /**
     * Initializes a new {@link LazyServerDirectoryVersion}.
     *
     * @param path The path
     * @param folderID The folder identifier
     * @param checksumSupplier The checksum supplier
     */
    public LazyServerDirectoryVersion(String path, String folderID, ChecksumSupplier checksumSupplier) {
        super(path, null);
        this.normalizedPath = PathNormalizer.normalize(path);
        this.folderID = folderID;
        this.checksumSupplier = checksumSupplier;
    }

    @Override
    public String getChecksum() {
        return getDirectoryChecksum().getChecksum();
    }

    @Override
    public String getPath() {
        return normalizedPath;
    }

    @Override
    public DirectoryChecksum getDirectoryChecksum() {
        if (null == knownChecksum) {
            try {
                knownChecksum = checksumSupplier.getChecksum(folderID);
            } catch (OXException e) {
                throw new IllegalStateException(e);
            }
        }
        return knownChecksum;
    }

    /**
     * Optionally gets the known directory checksum, if already available.
     *
     * @return The directory checksum, or <code>null</code> if not yet retrieved
     */
    public DirectoryChecksum optDirectoryChecksum() {
        if (null == knownChecksum) {
            knownChecksum = checksumSupplier.optChecksum(folderID);
        }
        return knownChecksum;
    }

    @Override
    public String toString() {
        return getPath() + " | " + (null != knownChecksum ? knownChecksum.getChecksum() : "<pending>") + " [" + folderID + ']';
    }

}
