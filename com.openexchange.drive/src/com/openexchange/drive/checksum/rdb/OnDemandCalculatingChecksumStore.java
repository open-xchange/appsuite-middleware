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

package com.openexchange.drive.checksum.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import jonelo.jacksum.algorithm.MD;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.storage.DriveStorage;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.java.Streams;

/**
 * {@link OnDemandCalculatingChecksumStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OnDemandCalculatingChecksumStore implements ChecksumStore {

    private final ChecksumStore delegate;
    private final DriveStorage storage;

    /**
     * Initializes a new {@link OnDemandCalculatingChecksumStore}.
     *
     * @param delegate The underlying checksum store
     * @param storage The drive storage
     */
    public OnDemandCalculatingChecksumStore(ChecksumStore delegate, DriveStorage storage) {
        super();
        this.delegate = delegate;
        this.storage = storage;
    }

    @Override
    public void addChecksum(File file, String checksum) throws OXException {
        delegate.addChecksum(file, checksum);
    }

    @Override
    public String getChecksum(File file) throws OXException {
        /*
         * try available metadata first
         */
        String md5sum = file.getFileMD5Sum();
        if (null == md5sum) {
            /*
             * query checksum store
             */
            md5sum = delegate.getChecksum(file);
            if (null == md5sum) {
                /*
                 * calculate and store checksum
                 */
                md5sum = calculateMD5(file);
                if (null != md5sum) {
                    delegate.addChecksum(file, md5sum);
                } else {
                    throw DriveExceptionCodes.IO_ERROR.create("Unable to calculate md5 checksum for file " + file);
                }
            }
        }
        return md5sum;
    }

    @Override
    public void removeChecksums(File file) throws OXException {
        delegate.removeChecksums(file);
    }

    @Override
    public Collection<File> getFiles(String checksum) throws OXException {
        return delegate.getFiles(checksum);
    }

    @Override
    public Map<File, String> getFilesInFolder(String folderID) throws OXException {
        return delegate.getFilesInFolder(folderID);
    }

    @Override
    public void updateFolderIDs(String currentFolderID, String newFolderID) throws OXException {
        delegate.updateFolderIDs(currentFolderID, newFolderID);
    }

    private String calculateMD5(File file) throws OXException {
        InputStream document = null;
        try {
            document =  storage.getDocument(file);
            return calculateMD5(document);
        } catch (IOException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(document);
        }
    }

    private static String calculateMD5(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        try {
            MD md5 = new MD("MD5");
            int read;
            do {
                read = inputStream.read(buffer);
                if (0 < read) {
                    md5.update(buffer, 0, read);
                }
            } while (-1 != read);
            return md5.getFormattedValue();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

}

