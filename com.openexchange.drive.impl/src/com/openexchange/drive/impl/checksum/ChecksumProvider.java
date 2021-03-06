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

package com.openexchange.drive.impl.checksum;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.comparison.MappingProblems;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.metadata.DriveMetadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import jonelo.jacksum.algorithm.MD;

/**
 * {@link ChecksumProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ChecksumProvider {

    /**
     * Gets the MD5 checksum for the current document version of the supplied file. Unless already present in the file's metadata,
     * the checksum is retrieved by first querying the checksum storage, and, if not yet known, by calculating the checksum on demand.
     * Newly calculated checksums are pushed back automatically to the storage.
     *
     * @param session The sync session
     * @param file The file
     * @return The checksum, never <code>null</code>
     * @throws OXException
     */
    public static FileChecksum getChecksum(SyncSession session, File file) throws OXException {
        FileChecksum fileChecksum = optFileChecksum(file);
        if (null == fileChecksum) {
            fileChecksum = session.getChecksumStore().getFileChecksum(DriveUtils.getFileID(file), file.getVersion(), file.getSequenceNumber());
            if (null == fileChecksum) {
                fileChecksum = session.getChecksumStore().insertFileChecksum(calculateFileChecksum(session, file));
            }
        }
        return fileChecksum;
    }

    /**
     * Gets the MD5 checksums for a list of files in a folder. Unless already present in the file's metadata, the checksums are
     * retrieved by first querying the checksum storage, and, if not yet known, by calculating the missing checksums on demand.
     * Newly calculated checksums are pushed back automatically to the storage.
     *
     * @param session The sync session
     * @param folderID The ID of all file's parent folder
     * @param files The list of files to get the checksum for
     * @return The list of checksums, in an equal order as the passed files
     * @throws OXException
     */
    public static List<FileChecksum> getChecksums(SyncSession session, String folderID, List<File> files) throws OXException {
        List<File> filesWithoutMD5Sum = new ArrayList<File>();
        for (File file : files) {
            if (Strings.isEmpty(optMD5(file))) {
                filesWithoutMD5Sum.add(file);
            }
        }
        List<FileChecksum> checksums = new ArrayList<FileChecksum>(files.size());
        List<FileChecksum> storedChecksums = filesWithoutMD5Sum.isEmpty() ? Collections.emptyList() : session.getChecksumStore().getFileChecksums(new FolderID(folderID));
        List<FileChecksum> newChecksums = new ArrayList<FileChecksum>();
        for (File file : files) {
            if (false == folderID.equals(file.getFolderId())) {
                throw new IllegalArgumentException("files must all have the same folder ID");
            }
            FileChecksum fileChecksum = optFileChecksum(file);
            if (null == fileChecksum) {
                fileChecksum = find(storedChecksums, file);
                if (null == fileChecksum) {
                    fileChecksum = calculateFileChecksum(session, file);
                    newChecksums.add(fileChecksum);
                }
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
     * @param session The sync session
     * @param folderIDs The list of folder IDs to get the checksum for
     * @return The list of checksums, in an equal order as the passed folder IDs
     */
    public static List<DirectoryChecksum> getChecksums(SyncSession session, List<String> folderIDs) throws OXException {
        return getChecksums(session, folderIDs, DriveUtils.calculateView(session.getDriveSession()), session.getDriveSession().useDriveMeta());
    }

    /**
     * Gets the MD5 checksum for a specific folder. The checksum is retrieved by first querying the checksum storage, and, if not yet
     * known or no longer up-to-date, by calculating the checksum on demand.
     *
     * @param session The sync session
     * @param folderID The folder ID to get the checksum for
     * @param view The client view to use when determining the directory checksum
     * @param useDriveMeta <code>true</code> to consider <code>.drive-meta</code> files, <code>false</code>, otherwise
     * @return The checksum
     */
    public static DirectoryChecksum getChecksum(SyncSession session, String folderID, int view, boolean useDriveMeta) throws OXException {
        List<DirectoryChecksum> checksums = getChecksums(session, Collections.singletonList(folderID), view, useDriveMeta);
        return null != checksums && 0 < checksums.size() ? checksums.get(0) : null;
    }

    /**
     * Gets the MD5 checksums for the supplied folders. The checksum is retrieved by first querying the checksum storage, and, if not yet
     * known or no longer up-to-date, by calculating the checksum on demand. Newly calculated checksums are pushed back automatically to
     * the storage, outdated ones are removed.
     *
     * @param session The sync session
     * @param folderIDs The list of folder IDs to get the checksum for
     * @param view The client view to use when determining the directory checksums
     * @param useDriveMeta <code>true</code> to consider <code>.drive-meta</code> files, <code>false</code>, otherwise
     * @return The list of checksums, in an equal order as the passed folder IDs
     */
    public static List<DirectoryChecksum> getChecksums(SyncSession session, List<String> folderIDs, int view, boolean useDriveMeta) throws OXException {
        StringBuilder trace = session.isTraceEnabled() ? new StringBuilder("Directory checksums:\n") : null;
        /*
         * probe for optimized checksum retrieval
         */
        List<DirectoryChecksum> checksums;
        List<String> foldersSupportingSequenceNumbers = DriveUtils.filterByCapabilities(session, folderIDs, FileStorageCapability.SEQUENCE_NUMBERS);
        if (0 == foldersSupportingSequenceNumbers.size()) {
            /*
             * no sequence numbers supported at all, fallback to manual calculation
             */
            if (null != trace) {
                trace.append(" No folder sequence numbers supported.\n");
            }
            checksums = calculateDirectoryChecksums(session, DriveUtils.getFolderIDs(folderIDs), view, useDriveMeta);
        } else {
            /*
             * sequence numbers (at least partially) supported
             */
            List<FolderID> folderIDsSupportingSequenceNumbers = DriveUtils.getFolderIDs(foldersSupportingSequenceNumbers);
            int userID = session.getServerSession().getUserId();
            checksums = new ArrayList<DirectoryChecksum>(folderIDs.size());
            HashMap<Integer, DirectoryChecksum> storedChecksums = getChecksumHelperMap(session.getChecksumStore().getDirectoryChecksums(userID, folderIDsSupportingSequenceNumbers, view));
            List<DirectoryChecksum> updatedChecksums = new ArrayList<DirectoryChecksum>();
            List<DirectoryChecksum> newChecksums = new ArrayList<DirectoryChecksum>();
            Map<String, Long> sequenceNumbers = session.getStorage().getSequenceNumbers(foldersSupportingSequenceNumbers);
            for (FolderID folderID : DriveUtils.getFolderIDs(folderIDs)) {
                if (false == folderIDsSupportingSequenceNumbers.contains(folderID)) {
                    /*
                     * calculate checksum as fallback
                     */
                    DirectoryChecksum directoryChecksum = new DirectoryChecksum(session.getServerSession().getUserId(), folderID, -1, calculateMD5(session, folderID, useDriveMeta), view);
                    if (null != trace) {
                        trace.append(" Calculated: ").append(directoryChecksum).append('\n');
                    }
                    checksums.add(directoryChecksum);
                    continue;
                }
                /*
                 * use stored checksum if available
                 */
                DirectoryChecksum directoryChecksum = find(storedChecksums, folderID, view);
                Long value = sequenceNumbers.get(folderID.toUniqueID());
                long sequenceNumber = null != value ? value.longValue() : 0;
                if (null != directoryChecksum) {
                    if (sequenceNumber != directoryChecksum.getSequenceNumber()) {
                        if (null != trace) {
                            trace.append(" Stored, invalid ( != " + sequenceNumber + " ): ").append(directoryChecksum).append('\n');
                        }
                        directoryChecksum.setChecksum(calculateMD5(session, folderID, useDriveMeta));
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
                    directoryChecksum = new DirectoryChecksum(userID, folderID, sequenceNumber, calculateMD5(session, folderID, useDriveMeta), view);
                    if (null != trace) {
                        trace.append(" Newly calculated: ").append(directoryChecksum).append('\n');
                    }
                    newChecksums.add(directoryChecksum);
                }
                checksums.add(directoryChecksum);
                /*
                 * flush so-far calculated checksums to storage
                 */
                if (100 < newChecksums.size() + updatedChecksums.size()) {
                    storeCalculatedChecksums(session, newChecksums, updatedChecksums);
                    newChecksums.clear();
                    updatedChecksums.clear();
                }
            }
            storeCalculatedChecksums(session, newChecksums, updatedChecksums);
        }
        if (null != trace) {
            session.trace(trace);
        }
        return checksums;
    }

    private static void storeCalculatedChecksums(SyncSession session, List<DirectoryChecksum> newChecksums, List<DirectoryChecksum> updatedChecksums) throws OXException {
        if (0 < updatedChecksums.size()) {
            session.getChecksumStore().updateDirectoryChecksums(updatedChecksums);
        }
        if (0 < newChecksums.size()) {
            session.getChecksumStore().insertDirectoryChecksums(newChecksums);
        }
    }

    /**
     * Checks whether the supplied file's checksum matches a given checksum.
     *
     * @param session The sync session
     * @param file The file to check
     * @param checksum The checksum to match
     * @return <code>true</code>, if the checksum matches, <code>false</code>, otherwise
     * @throws OXException
     */
    public static boolean matches(SyncSession session, File file, String checksum) throws OXException {
        return checksum.equals(getChecksum(session, file).getChecksum());
    }

    private static List<DirectoryChecksum> calculateDirectoryChecksums(SyncSession session, List<FolderID> folderIDs, int view, boolean useDriveMeta) throws OXException {
        List<DirectoryChecksum> checksums = new ArrayList<DirectoryChecksum>(folderIDs.size());
        for (FolderID folderID : folderIDs) {
            checksums.add(new DirectoryChecksum(session.getServerSession().getUserId(), folderID, -1, calculateMD5(session, folderID, useDriveMeta), view));
        }
        return checksums;
    }

    private static String calculateMD5(SyncSession session, FolderID folderID, boolean useDriveMeta) throws OXException {
        StringBuilder trace = session.isTraceEnabled() ? new StringBuilder("File checksums in folder " + folderID + ":\n") : null;
        String checksum;
        List<File> filesInFolder = session.getStorage().getFilesInFolder(folderID.toUniqueID(), useDriveMeta);
        if (null == filesInFolder || 0 == filesInFolder.size()) {
            checksum = DriveConstants.EMPTY_MD5;
            if (null != trace) {
                trace.append(" no files in folder, using empty MD5\n");
            }
        } else {
            List<File> filesWithoutMD5Sum = new ArrayList<File>();
            Set<File> files = new TreeSet<File>(FILENAME_COMPARATOR);
            {
                MappingProblems<File> mappingProblems = new MappingProblems<File>();
                Map<String, File> filesByName = new TreeMap<String, File>(String.CASE_INSENSITIVE_ORDER);
                for (File file : filesInFolder) {
                    String normalizedKey = PathNormalizer.normalize(file.getFileName());
                    File existingFile = filesByName.get(normalizedKey);
                    if (null != existingFile) {
                        /*
                         * case / normalization conflict - choose file to use
                         */
                        String existingKey = PathNormalizer.normalize(existingFile.getFileName());
                        file = mappingProblems.chooseServerVersion(existingFile, existingKey, file, normalizedKey);
                    }
                    if (Strings.isEmpty(optMD5(file))) {
                        filesWithoutMD5Sum.add(file);
                    }
                    filesByName.put(normalizedKey, file);
                }
                if (null != trace && false == mappingProblems.isEmpty()) {
                    trace.append(mappingProblems);
                }
                files.addAll(filesByName.values());
            }
            MD md5 = session.newMD5();
            List<FileChecksum> knownChecksums = filesWithoutMD5Sum.isEmpty() ? Collections.emptyList() : session.getChecksumStore().getFileChecksums(folderID);
            List<FileChecksum> newChecksums = new ArrayList<FileChecksum>();
            List<FileChecksum> updatedChecksums = new ArrayList<FileChecksum>();
            for (File file : files) {
                FileChecksum fileChecksum = optFileChecksum(file);
                if (null != fileChecksum) {
                    if (null != trace) {
                        trace.append(" From metadata: ").append(fileChecksum).append('\n');
                    }
                } else {
                    fileChecksum = find(knownChecksums, file, false);
                    long sequenceNumber = file.getSequenceNumber();
                    if (null != fileChecksum) {
                        if (sequenceNumber != fileChecksum.getSequenceNumber()) {
                            if (null != trace) {
                                trace.append(" Stored, invalid ( != " + sequenceNumber + " ): ").append(fileChecksum).append('\n');
                            }
                            fileChecksum.setChecksum(calculateFileChecksum(session, file).getChecksum());
                            fileChecksum.setSequenceNumber(sequenceNumber);
                            updatedChecksums.add(fileChecksum);
                            if (null != trace) {
                                trace.append(" Re-calculated: ").append(fileChecksum).append('\n');
                            }
                        } else {
                            if (null != trace) {
                                trace.append(" Stored, valid: ").append(fileChecksum).append('\n');
                            }
                        }
                    } else {
                        fileChecksum = calculateFileChecksum(session, file);
                        if (null != trace) {
                            trace.append(" Newly calculated: ").append(fileChecksum).append('\n');
                        }
                        newChecksums.add(fileChecksum);
                    }
                }
                md5.update(PathNormalizer.normalize(file.getFileName()).getBytes(Charsets.UTF_8));
                md5.update(fileChecksum.getChecksum().getBytes(Charsets.UTF_8));
            }
            checksum = md5.getFormattedValue();
            if (0 < newChecksums.size()) {
                session.getChecksumStore().insertFileChecksums(newChecksums);
            }
            if (0 < updatedChecksums.size()) {
                session.getChecksumStore().updateFileChecksums(updatedChecksums);
            }
        }
        if (null != trace) {
            trace.append("Directory checksum: ").append(checksum).append('\n');
            session.trace(trace);
        }
        return checksum;
    }

    private static FileChecksum calculateFileChecksum(SyncSession session, File file) throws OXException {
        String md5 = file.getFileMD5Sum();
        if (Strings.isEmpty(md5)) {
            md5 = calculateMD5(session, file);
            if (Strings.isEmpty(md5)) {
                throw DriveExceptionCodes.NO_CHECKSUM_FOR_FILE.create(file);
            }
        }
        FileChecksum fileChecksum = new FileChecksum();
        fileChecksum.setFileID(DriveUtils.getFileID(file));
        fileChecksum.setSequenceNumber(file.getSequenceNumber());
        fileChecksum.setVersion(file.getVersion());
        fileChecksum.setChecksum(md5);
        return fileChecksum;
    }

    /**
     * Optionally gets the file checksum if the md5 sum is present in the file's metadata.
     *
     * @param file The file to optionally get the checksum for
     * @return The file checksum, or <code>null</code> if not available in the file's metadata
     */
    private static FileChecksum optFileChecksum(File file) {
        String md5 = optMD5(file);
        if (Strings.isEmpty(md5)) {
            return null;
        }
        FileChecksum fileChecksum = new FileChecksum();
        fileChecksum.setFileID(DriveUtils.getFileID(file));
        fileChecksum.setSequenceNumber(file.getSequenceNumber());
        fileChecksum.setVersion(file.getVersion());
        fileChecksum.setChecksum(md5);
        return fileChecksum;
    }

    /**
     * Optionally gets a file's md5 sum if present in the file's metadata.
     *
     * @param file The file to optionally get the md5 sum for
     * @return The md5 checksum, or <code>null</code> if not available in the file's metadata
     */
    private static String optMD5(File file) {
        return DriveMetadata.class.isInstance(file) ? ((DriveMetadata) file).optFileMD5Sum() : file.getFileMD5Sum();
    }

    private static String calculateMD5(SyncSession session, File file) throws OXException {
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
        return find(checksums, file, true);
    }

    private static FileChecksum find(Collection<? extends FileChecksum> checksums, File file, boolean considerSequenceNumber) {
        FileChecksum matchingChecksum = null;
        for (FileChecksum checksum : checksums) {
            if (checksum.getFileID().toUniqueID().equals(file.getId())) {
                String folderID = new FolderID(
                    checksum.getFileID().getService(), checksum.getFileID().getAccountId(), checksum.getFileID().getFolderId()).toUniqueID();
                if (folderID.equals(file.getFolderId()) &&
                    (null == checksum.getVersion() ? null == file.getVersion() : checksum.getVersion().equals(file.getVersion()))) {
                    if (checksum.getSequenceNumber() == file.getSequenceNumber()) {
                        return checksum; // perfect match
                    }
                    if (false == considerSequenceNumber) {
                        matchingChecksum = checksum; // match with different sequence number
                    }
                }
            }
        }
        return matchingChecksum;
    }

    /**
     * Looks up the checksum matching the supplied folder ID and view.
     *
     * @param checksums The checksums to search in
     * @param folderID The folder ID to match
     * @param view The view to match
     * @return The checksum, or <code>null</code> if not found
     */
    private static DirectoryChecksum find(HashMap<Integer, DirectoryChecksum> checksums, FolderID folderID, int view) {
        return checksums.get(I(getChecksumHelperKey(folderID, view)));
    }

    /**
     * Prepares a HashMap to lookup DirectoryChecksum by Key.
     * The Key is constructed by using the FolderID and View
     *
     * @param checksums The checksums to search in
     * @return A map to get an easier lookup for DirectoryChecksum
     */
    private static HashMap<Integer, DirectoryChecksum> getChecksumHelperMap(Collection<? extends DirectoryChecksum> checksums) {
        HashMap<Integer, DirectoryChecksum> returnMap = new HashMap<Integer, DirectoryChecksum>();
        for (DirectoryChecksum checksum : checksums) {
            returnMap.put(I(getChecksumHelperKey(checksum.getFolderID(), checksum.getView())), checksum);
        }
        return returnMap;
    }

    /**
     * Prepares the Key for the DirectoryChecksum lookup
     * @param folderID of the Key
     * @param view of the Key
     * @return Hash to be used for a HashMap
     */
    private static int getChecksumHelperKey(FolderID folderID, int view) {
        final int prime = 31;
        int hashCode = folderID.hashCode();
        int result = 1;
        result = prime * result + hashCode;
        result = prime * result + view;
        return result;
    }



    private static final Comparator<File> FILENAME_COMPARATOR = new Comparator<File>() {

        @Override
        public int compare(File o1, File o2) {
            byte[] fileName1 = PathNormalizer.normalize(o1.getFileName()).getBytes(Charsets.UTF_8);
            byte[] fileName2 = PathNormalizer.normalize(o2.getFileName()).getBytes(Charsets.UTF_8);
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
