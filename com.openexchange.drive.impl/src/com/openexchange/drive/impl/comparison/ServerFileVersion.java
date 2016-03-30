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
