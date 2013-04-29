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

package com.openexchange.drive.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jonelo.jacksum.algorithm.MD;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link ChecksumProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ChecksumProvider {

    /**
     * Gets the MD5 checksum for the current document version of the supplied file. The checksum is retrieved by first querying the
     * checksum storage, and, if not yet known, by calculating the checksum on demand. Newly calculated checksums are pushed back
     * automatically to the storage.
     *
     * @param session The drive session
     * @param file The file
     * @return The checksum, never <code>null</code>
     * @throws OXException
     */
    public static FileChecksum getChecksum(DriveSession session, File file) throws OXException {
        FileChecksum fileChecksum = session.getChecksumStore().getFileChecksum(
            file.getFolderId(), file.getId(), file.getVersion(), file.getSequenceNumber());
        if (null == fileChecksum) {
            fileChecksum = session.getChecksumStore().insertFileChecksum(calculateFileChecksum(session, file));
        }
        return fileChecksum;
    }

    /**
     * Gets the MD5 checksums for a list of files in a folder. The checksums are retrieved by first querying the checksum storage, and, if
     * not yet known, by calculating the missing checksums on demand. Newly calculated checksums are pushed back automatically to the
     * storage.
     *
     * @param session The drive session
     * @param folderID The ID of all file's parent folder
     * @param files The list of files to get the checksum for
     * @return The list of checksums, in an equal order as the passed files
     * @throws OXException
     */
    public static List<FileChecksum> getChecksums(DriveSession session, String folderID, List<File> files) throws OXException {
        List<FileChecksum> checksums = new ArrayList<FileChecksum>(files.size());
        List<FileChecksum> storedChecksums = session.getChecksumStore().getFileChecksums(folderID);
        List<FileChecksum> newChecksums = new ArrayList<FileChecksum>();
        for (File file : files) {
            if (false == folderID.equals(file.getFolderId())) {
                throw new IllegalArgumentException("files must all have the same folder ID");
            }
            FileChecksum fileChecksum = find(storedChecksums, file);
            if (null == fileChecksum) {
                fileChecksum = calculateFileChecksum(session, file);
                newChecksums.add(fileChecksum);
            }
            checksums.add(fileChecksum);
        }
        if (0 < newChecksums.size()) {
            session.getChecksumStore().insertFileChecksums(newChecksums);
        }
        return checksums;
    }

    /**
     * Gets the MD5 checksums for the supplied folders. The checksum is retrieved by first querying the checksum storage, and, if not yet
     * known or no longer up-to-date, by calculating the checksum on demand. Newly calculated checksums are pushed back automatically to
     * the storage, outdated ones are removed.
     *
     * @param session The drive session
     * @param folderIDs The list of folder IDs to get the checksum for
     * @return The list of checksums, in an equal order as the passed folder IDs
     * @throws OXException
     */
    public static List<DirectoryChecksum> getChecksums(DriveSession session, List<String> folderIDs) throws OXException {
        if (false == session.getStorage().supportsFolderSequenceNumbers()) {
            return calculateDirectoryChecksums(session, folderIDs);
        }
        List<DirectoryChecksum> checksums = new ArrayList<DirectoryChecksum>(folderIDs.size());
        List<DirectoryChecksum> storedChecksums = session.getChecksumStore().getDirectoryChecksums(folderIDs);
        List<DirectoryChecksum> updatedChecksums = new ArrayList<DirectoryChecksum>();
        List<DirectoryChecksum> newChecksums = new ArrayList<DirectoryChecksum>();
        for (String folderID : folderIDs) {
            DirectoryChecksum directoryChecksum = find(storedChecksums, folderID);
            if (null != directoryChecksum) {
                long sequenceNumber = session.getStorage().getSequenceNumber(
                    session.getStorage().getPath(folderID), directoryChecksum.getSequenceNumber());
                if (sequenceNumber != directoryChecksum.getSequenceNumber()) {
                    directoryChecksum.setChecksum(calculateMD5(session, folderID));
                    directoryChecksum.setSequenceNumber(sequenceNumber);
                    updatedChecksums.add(directoryChecksum);
                }
            } else {
                long sequenceNumber = session.getStorage().getSequenceNumber(session.getStorage().getPath(folderID), 0);
                directoryChecksum = new DirectoryChecksum(folderID, sequenceNumber, calculateMD5(session, folderID));
                newChecksums.add(directoryChecksum);
            }
            checksums.add(directoryChecksum);
        }
        if (0 < updatedChecksums.size()) {
            session.getChecksumStore().updateDirectoryChecksums(updatedChecksums);
        }
        if (0 < newChecksums.size()) {
            session.getChecksumStore().insertDirectoryChecksums(newChecksums);
        }
        return checksums;
    }

    /**
     * Checks whether the supplied file's checksum matches a given checksum.
     *
     * @param session The drive session
     * @param file The file to check
     * @param checksum The checksum to match
     * @return <code>true</code>, if the checksum matches, <code>false</code>, otherwise
     * @throws OXException
     */
    public static boolean matches(DriveSession session, File file, String checksum) throws OXException {
        return checksum.equals(getChecksum(session, file).getChecksum());
    }

    private static List<DirectoryChecksum> calculateDirectoryChecksums(DriveSession session, List<String> folderIDs) throws OXException {
        List<DirectoryChecksum> checksums = new ArrayList<DirectoryChecksum>(folderIDs.size());
        for (String folderID : folderIDs) {
            checksums.add(new DirectoryChecksum(folderID, -1, calculateMD5(session, folderID)));
        }
        return checksums;
    }

    private static String calculateMD5(DriveSession session, String folderID) throws OXException {
        List<File> files = session.getStorage().getFilesInFolder(folderID);
        if (null == files || 0 == files.size()) {
            return DriveConstants.EMPTY_MD5;
        } else if (0 < files.size()) {
            Collections.sort(files, FILENAME_COMPARATOR);
        }
        MD md5 = session.newMD5();
        List<FileChecksum> knownChecksums = session.getChecksumStore().getFileChecksums(folderID);
        List<FileChecksum> calculatedChecksums = new ArrayList<FileChecksum>();
        for (File file : files) {
            FileChecksum fileChecksum = find(knownChecksums, file);
            if (null == fileChecksum) {
                fileChecksum = calculateFileChecksum(session, file);
                calculatedChecksums.add(fileChecksum);
            }
            md5.update(file.getFileName().getBytes(Charsets.UTF_8));
            md5.update(fileChecksum.getChecksum().getBytes(Charsets.UTF_8));
        }
        if (0 < calculatedChecksums.size()) {
            session.getChecksumStore().insertFileChecksums(calculatedChecksums);
        }
        return md5.getFormattedValue();
    }

    private static FileChecksum calculateFileChecksum(DriveSession session, File file) throws OXException {
        String md5 = Strings.isEmpty(file.getFileMD5Sum()) ? calculateMD5(session, file) : file.getFileMD5Sum();
        if (null == md5) {
            throw DriveExceptionCodes.NO_CHECKSUM_FOR_FILE.create(file);
        }
        FileChecksum fileChecksum = new FileChecksum();
        fileChecksum.setFileID(file.getId());
        fileChecksum.setFolderID(file.getFolderId());
        fileChecksum.setVersion(file.getVersion());
        fileChecksum.setSequenceNumber(file.getSequenceNumber());
        fileChecksum.setChecksum(md5);
        return fileChecksum;
    }

    private static String calculateMD5(DriveSession session, File file) throws OXException {
        InputStream document = null;
        try {
            document = session.getStorage().getDocument(file);
            return calculateMD5(document);
        } catch (IOException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(document);
        }
    }

    private static String calculateMD5(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[2048];
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

    private static FileChecksum find(Collection<? extends FileChecksum> checksums, File file) {
        for (FileChecksum checksum : checksums) {
            if (checksum.matches(file)) {
                return checksum;
            }
        }
        return null;
    }

    private static DirectoryChecksum find(Collection<? extends DirectoryChecksum> checksums, String folderID) {
        for (DirectoryChecksum checksum : checksums) {
            if (checksum.getFolderID().equals(folderID)) {
                return checksum;
            }
        }
        return null;
    }

    private static final Comparator<File> FILENAME_COMPARATOR = new Comparator<File>() {

        @Override
        public int compare(File o1, File o2) {
            byte[] fileName1 = o1.getFileName().getBytes(Charsets.UTF_8);
            byte[] fileName2 = o2.getFileName().getBytes(Charsets.UTF_8);
            int minLength = Math.min(fileName1.length, fileName2.length);
            for (int i = 0; i < minLength; i++) {
                int result = fileName1[i] - fileName2[i];
                if (result != 0) {
                    return result;
                }
            }
            return fileName1.length - fileName2.length;
        }
    };

}
