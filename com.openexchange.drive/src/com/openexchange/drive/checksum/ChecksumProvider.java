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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import jonelo.jacksum.algorithm.MD;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
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
        FileID fileID = new FileID(file.getId());
        FolderID folderID = new FolderID(file.getFolderId());
        if (null == fileID.getFolderId()) {
            // TODO: check
            fileID.setFolderId(folderID.getFolderId());
        }
        FileChecksum fileChecksum = session.getChecksumStore().getFileChecksum(fileID, file.getVersion(), file.getSequenceNumber());
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
        List<FileChecksum> storedChecksums = session.getChecksumStore().getFileChecksums(new FolderID(folderID));
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






        try {
            List<File> files = new ArrayList<File>();
            List<String> lines = new ArrayList<String>();
            FileInputStream is = new FileInputStream("d:/lines.txt");
            DataInputStream in = new DataInputStream(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                lines.add(strLine);
            }
            in.close();

            for (String line : lines) {
                String[] splitted = line.split("\\|");
                File file = new DefaultFile();
                file.setFileName(splitted[0].trim());
                file.setFileMD5Sum(splitted[1].trim());
                files.add(file);
            }

            Collections.sort(files, FILENAME_COMPARATOR);
            MD md5 = session.newMD5();
            for (File file : files) {

                md5.update(file.getFileName().getBytes(Charsets.UTF_8));
                md5.update(file.getFileMD5Sum().getBytes(Charsets.UTF_8));
            }

            String formattedValue = md5.getFormattedValue();


            System.out.println(formattedValue);

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }









        StringAllocator trace = session.isTraceEnabled() ? new StringAllocator("Directory checksums:\n") : null;
        List<FolderID> fids = new ArrayList<FolderID>(folderIDs.size());
        for (String folderID : folderIDs) {
            fids.add(new FolderID(folderID));
        }
        List<DirectoryChecksum> checksums;
        if (false == session.getStorage().supportsFolderSequenceNumbers()) {
            if (null != trace) {
                trace.append(" No folder sequence numbers supported.\n");
            }
            checksums = calculateDirectoryChecksums(session, fids);
        } else {
            checksums = new ArrayList<DirectoryChecksum>(folderIDs.size());
            List<DirectoryChecksum> storedChecksums = session.getChecksumStore().getDirectoryChecksums(fids);
            List<DirectoryChecksum> updatedChecksums = new ArrayList<DirectoryChecksum>();
            List<DirectoryChecksum> newChecksums = new ArrayList<DirectoryChecksum>();
            Map<String, Long> sequenceNumbers = session.getStorage().getSequenceNumbers(folderIDs);
            for (FolderID folderID : fids) {
                DirectoryChecksum directoryChecksum = find(storedChecksums, folderID);
                Long value = sequenceNumbers.get(folderID.toUniqueID());
                long sequenceNumber = null != value ? value.longValue() : 0;
                if (null != directoryChecksum) {
                    if (sequenceNumber != directoryChecksum.getSequenceNumber()) {
                        if (null != trace) {
                            trace.append(" Stored, invalid ( != " + sequenceNumber + " ): ").append(directoryChecksum).append('\n');
                        }
                        directoryChecksum.setChecksum(calculateMD5(session, folderID));
                        directoryChecksum.setSequenceNumber(sequenceNumber);
                        updatedChecksums.add(directoryChecksum);
                        if (null != trace) {
                            trace.append(" Re-calculated: ").append(directoryChecksum).append('\n');
                        }
                    } else {
                        if (null != trace) {
                            trace.append(" Stored, valid: ").append(directoryChecksum).append('\n');
                        }
                    }
                } else {
                    directoryChecksum = new DirectoryChecksum(folderID, sequenceNumber, calculateMD5(session, folderID));
                    if (null != trace) {
                        trace.append(" Newly calculated: ").append(directoryChecksum).append('\n');
                    }
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
        }
        if (null != trace) {
            session.trace(trace);
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

    private static List<DirectoryChecksum> calculateDirectoryChecksums(DriveSession session, List<FolderID> folderIDs) throws OXException {
        List<DirectoryChecksum> checksums = new ArrayList<DirectoryChecksum>(folderIDs.size());
        for (FolderID folderID : folderIDs) {
            checksums.add(new DirectoryChecksum(folderID, -1, calculateMD5(session, folderID)));
        }
        return checksums;
    }

    private static String calculateMD5(DriveSession session, FolderID folderID) throws OXException {
        StringAllocator trace = session.isTraceEnabled() ? new StringAllocator("File checksums in folder " + folderID + ":\n") : null;
        String checksum;
        List<File> filesInFolder = session.getStorage().getFilesInFolder(folderID.toUniqueID());
        if (null == filesInFolder || 0 == filesInFolder.size()) {
            checksum = DriveConstants.EMPTY_MD5;
            if (null != trace) {
                trace.append(" no files in folder, using empty MD5");
            }
        } else {
            Set<File> files = new TreeSet<File>(FILENAME_COMPARATOR);
            files.addAll(filesInFolder);
            MD md5 = session.newMD5();
            List<FileChecksum> knownChecksums = session.getChecksumStore().getFileChecksums(folderID);
            List<FileChecksum> calculatedChecksums = new ArrayList<FileChecksum>();
            for (File file : files) {
                FileChecksum fileChecksum = find(knownChecksums, file);
                if (null == fileChecksum) {
                    fileChecksum = calculateFileChecksum(session, file);
                    if (null != trace) {
                        trace.append(' ' + file.getFileName()).append(" - Newly calculated: ").append(fileChecksum).append('\n');
                    }
                    calculatedChecksums.add(fileChecksum);
                } else {
                    if (null != trace) {
                        trace.append(' ' + file.getFileName()).append(" - Stored, valid: ").append(fileChecksum).append('\n');
                    }
                }
                md5.update(file.getFileName().getBytes(Charsets.UTF_8));
                md5.update(fileChecksum.getChecksum().getBytes(Charsets.UTF_8));
            }
            if (0 < calculatedChecksums.size()) {
                session.getChecksumStore().insertFileChecksums(calculatedChecksums);
            }
            checksum = md5.getFormattedValue();
        }
        if (null != trace) {
            trace.append("Directory checksum: ").append(checksum).append('\n');
            session.trace(trace);
        }
        return checksum;
    }

    private static FileChecksum calculateFileChecksum(DriveSession session, File file) throws OXException {
        String md5 = Strings.isEmpty(file.getFileMD5Sum()) ? calculateMD5(session, file) : file.getFileMD5Sum();
        if (null == md5) {
            throw DriveExceptionCodes.NO_CHECKSUM_FOR_FILE.create(file);
        }
        FileID fileID = new FileID(file.getId());
        FolderID folderID = new FolderID(file.getFolderId());
        if (null == fileID.getFolderId()) {
            // TODO: check
            fileID.setFolderId(folderID.getFolderId());
        }
        FileChecksum fileChecksum = new FileChecksum();
        fileChecksum.setFileID(fileID);
        fileChecksum.setSequenceNumber(file.getSequenceNumber());
        fileChecksum.setVersion(file.getVersion());
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
            // TODO: check
            String fileID = checksum.getFileID().toUniqueID();
            String folderID = new FolderID(
                checksum.getFileID().getService(), checksum.getFileID().getAccountId(), checksum.getFileID().getFolderId()).toUniqueID();
            if (fileID.equals(file.getId()) && folderID.equals(file.getFolderId()) &&
//            if (checksum.getFileID().equals(new FileID(file.getId())) &&
                (null == checksum.getVersion() ? null == file.getVersion() : checksum.getVersion().equals(file.getVersion())) &&
                checksum.getSequenceNumber() == file.getSequenceNumber()) {
                return checksum;
            }
        }
        return null;
    }

    private static DirectoryChecksum find(Collection<? extends DirectoryChecksum> checksums, FolderID folderID) {
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
                int result = (fileName1[i] & 0xFF) - (fileName2[i] & 0xFF);
                if (result != 0) {
                    return result;
                }
            }
            return fileName1.length - fileName2.length;
        }
    };

}
