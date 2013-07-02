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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.storage.StorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedRandomFileAccess;

/**
 * {@link DownloadHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DownloadHelper {

    private final DriveSession session;

    /**
     * Initializes a new {@link DownloadHelper}.
     *
     * @param session The drive session
     */
    public DownloadHelper(DriveSession session) {
        super();
        this.session = session;
    }

    /**
     * Performs a file download.
     *
     * @param path The directory path the file is located in
     * @param fileVersion The file version to download
     * @param offset The offset in bytes where to begin
     * @param length The length of the data in bytes to download
     * @return A file holder hosting the stream
     * @throws OXException
     */
    public IFileHolder perform(final String path, final FileVersion fileVersion, final long offset, final long length) throws OXException {

        return session.getStorage().wrapInTransaction(new StorageOperation<FileHolder>() {

            @Override
            public FileHolder call() throws OXException {
                /*
                 * get the file's input stream
                 */
                File file = session.getStorage().findFileByName(path, fileVersion.getName());
                if (null == file || false == ChecksumProvider.matches(session, file, fileVersion.getChecksum())) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(fileVersion.getName(), fileVersion.getChecksum(), path);
                }
                InputStream inputStream = getInputStream(file, offset, length);
                /*
                 * wrap stream into file holder and return
                 */
                if (null == inputStream) {
                    throw DriveExceptionCodes.FILE_NOT_FOUND.create(fileVersion.getName(), path);
                }
                String contentType = null != file.getFileMIMEType() ? file.getFileMIMEType() : "application/octet-stream";
                FileHolder fileHolder = new FileHolder(inputStream, -1, contentType, fileVersion.getName());
                fileHolder.setDelivery("download");
                return fileHolder;
            }
        });
    }

    private InputStream getInputStream(File file, long offset, long length) throws OXException {
        IDBasedFileAccess fileAccess = session.getStorage().getFileAccess();
        InputStream inputStream = null;
        if (0 < offset || 0 < length) {
            /*
             * offset or maximum length is requested, get partial stream
             */
            if (IDBasedRandomFileAccess.class.isInstance(fileAccess)) {
                inputStream = ((IDBasedRandomFileAccess)fileAccess).getDocument(file.getId(), file.getVersion(), offset, length);
            } else {
                try {
                    inputStream = new PartialInputStream(fileAccess.getDocument(file.getId(), file.getVersion()), offset, length);
                } catch (IOException e) {
                    throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        } else {
            /*
             * get complete stream by default
             */
            inputStream = fileAccess.getDocument(file.getId(), file.getVersion());
        }
        return inputStream;
    }

}
