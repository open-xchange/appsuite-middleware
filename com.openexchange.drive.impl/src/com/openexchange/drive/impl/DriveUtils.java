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

package com.openexchange.drive.impl;

import static com.openexchange.drive.impl.DriveConstants.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.DirectoryPattern;
import com.openexchange.drive.DriveClientType;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.FilePattern;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.DownloadFileAction;
import com.openexchange.drive.impl.actions.EditFileAction;
import com.openexchange.drive.impl.actions.ErrorFileAction;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.drive.impl.sync.RenameTools;
import com.openexchange.drive.impl.sync.SimpleFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link DriveUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveUtils {

    /**
     * Constructs the identifier for the supplied file, setting all relevant properties in the {@link FileID} object, especially the
     * folder ID field is populated with the (non-unique) identifier of the parent folder if not already done by the {@link FileID}
     * constructor.
     *
     * @param file The file to generate the ID for
     * @return The file ID
     * @throws IllegalArgumentException if the supplied file lacks the required properties
     */
    public static FileID getFileID(File file) {
        if (null == file.getId() || null == file.getFolderId()) {
            throw new IllegalArgumentException("File- and folder IDs  are required");
        }
        FileID fileID = new FileID(file.getId());
        FolderID folderID = new FolderID(file.getFolderId());
        if (null == fileID.getFolderId()) {
            fileID.setFolderId(folderID.getFolderId());
        }
        return fileID;
    }

    /**
     * Gets a value indicating whether the supplied path is invalid, i.e. it contains illegal characters or is not supported for
     * other reasons.
     *
     * @param path The path to check
     * @return <code>true</code> if the path is considered invalid, <code>false</code>, otherwise
     * @throws OXException
     */
    public static boolean isInvalidPath(String path) throws OXException {
        if (Strings.isEmpty(path)) {
            return true; // no empty paths
        }
        if (false == DriveConstants.PATH_VALIDATION_PATTERN.matcher(path).matches()) {
            return true; // no invalid paths
        }
        for (String pathSegment : split(path)) {
            if (DriveConstants.MAX_PATH_SEGMENT_LENGTH < pathSegment.length()) {
                return true; // no too long paths
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied path is ignored, i.e. it is excluded from synchronization by definition.
     *
     * @param session The sync session
     * @param path The path to check
     * @return <code>true</code> if the path is considered to be ignored, <code>false</code>, otherwise
     * @throws OXException
     */
    public static boolean isIgnoredPath(SyncSession session, String path) throws OXException {
        if (session.getTemp().supported()) {
            String tempPath = session.getTemp().getPath(false);
            if (null != tempPath && tempPath.equals(path)) {
                return true; // no temp path
            }
        }
        if (DriveConfig.getInstance().getExcludedDirectoriesPattern().matcher(path).matches()) {
            return true; // no (server-side) excluded paths
        }
        List<DirectoryPattern> directoryExclusions = session.getDriveSession().getDirectoryExclusions();
        if (null != directoryExclusions && 0 < directoryExclusions.size()) {
            for (DirectoryPattern pattern : directoryExclusions) {
                if (pattern.matches(path)) {
                    return true; // no (client-side) excluded paths
                }
            }
        }
        if (session.getStorage().hasTrashFolder()) {
            FileStorageFolder trashFolder = session.getStorage().getTrashFolder();
            String trashPath = session.getStorage().getPath(trashFolder.getId());
            if (null != trashPath && trashPath.equals(path)) {
                return true; // no trash path
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied filename is ignored, i.e. it is excluded from synchronization by definition. Only
     * static / global exclusions are considered in this check.
     *
     * @param fileName The filename to check
     * @return <code>true</code> if the filename is considered to be ignored, <code>false</code>, otherwise
     */
    public static boolean isIgnoredFileName(String fileName) {
        if (fileName.endsWith(DriveConstants.FILEPART_EXTENSION)) {
            return true; // no temporary upload files
        }
        if (DriveConfig.getInstance().getExcludedFilenamesPattern().matcher(fileName).matches()) {
            return true; // no (server-side) excluded files
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied filename is ignored, i.e. it is excluded from synchronization by definition. Static /
     * global exclusions are considered, as well as client-defined filters based on path and filename.
     *
     * @param session The drive session
     * @param path The directory path, relative to the root directory
     * @param fileName The filename to check
     * @return <code>true</code> if the filename is considered to be ignored, <code>false</code>, otherwise
     * @throws OXException
     */
    public static boolean isIgnoredFileName(DriveSession session, String path, String fileName) throws OXException {
        if (isIgnoredFileName(fileName)) {
            return true;
        }
        List<FilePattern> fileExclusions = session.getFileExclusions();
        if (null != fileExclusions && 0 < fileExclusions.size()) {
            for (FilePattern pattern : fileExclusions) {
                if (pattern.matches(path, fileName)) {
                    return true; // no (client-side) excluded files
                }
            }
        }
        return false;
    }

    /**
     * Handles a 'quota-exceeded' situation that happened when a new file version was saved by generating an appropriate sequence of
     * actions to inform the client and instruct him to put the affected file into quarantine.
     *
     * @param session The sync session
     * @param quotaException The quota exception that occurred
     * @param path The path where the new file version was tried to be saved
     * @param originalVersion The original version if it was an update to an existing file, or <code>null</code>, otherwise
     * @param newVersion The new file version that was tried to be saved
     * @return A sequence of file actions the client should execute in order to handle the 'quota-exceeded' situation
     * @throws OXException
     */
    public static List<AbstractAction<FileVersion>> handleQuotaExceeded(SyncSession session, OXException quotaException, String path,
        FileVersion originalVersion, FileVersion newVersion) throws OXException {
        List<AbstractAction<FileVersion>> actionsForClient = new ArrayList<AbstractAction<FileVersion>>();
        /*
         * quota reached
         */
        OXException quotaReachedException = DriveExceptionCodes.QUOTA_REACHED.create(quotaException, (Object[])null);
        if (null != originalVersion) {
            /*
             * upload should have replaced an existing file, let client first rename it's file and mark as error with quarantine flag...
             */
            String alternativeName = RenameTools.findRandomAlternativeName(originalVersion.getName());
            FileVersion renamedVersion = new SimpleFileVersion(alternativeName, originalVersion.getChecksum());
            actionsForClient.add(new EditFileAction(newVersion, renamedVersion, null, path, false));
            actionsForClient.add(new ErrorFileAction(newVersion, renamedVersion, null, path, quotaReachedException, true));
            /*
             * ... then download the server version afterwards
             */
            ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(originalVersion, path, session);
            actionsForClient.add(new DownloadFileAction(session, null, serverFileVersion, null, path));
        } else {
            /*
             * upload of new file, mark as error with quarantine flag
             */
            actionsForClient.add(new ErrorFileAction(null, newVersion, null, path, quotaReachedException, true));
        }
        return actionsForClient;
    }

    /**
     * Gets a value indicating whether the supplied exception indicates a 'quota-exceeded' exception or not.
     *
     * @param e The exception to check
     * @return <code>true</code> if the exception indicates a 'quota-exceeded' exception, <code>false</code>, otherwise
     */
    public static boolean indicatesQuotaExceeded(OXException e) {
        return "FLS-0024".equals(e.getErrorCode()) || FileStorageExceptionCodes.QUOTA_REACHED.equals(e) ||
            DriveExceptionCodes.QUOTA_REACHED.equals(e) || "SMARTDRIVEFILE_STORAGE-0008".equals(e.getErrorCode()) ||
            QuotaExceptionCodes.QUOTA_EXCEEDED.equals(e) || QuotaExceptionCodes.QUOTA_EXCEEDED_FILES.equals(e)
        ;
    }

    /**
     * Gets a value indicating whether the supplied exception indicates an unrecoverable failed save operation, e.g. due to an invalid
     * path name not supported by the underlying storage backend.
     *
     * @param e The exception to check
     * @return <code>true</code> if the exception indicates a failed saved exception, <code>false</code>, otherwise
     */
    public static boolean indicatesFailedSave(OXException e) {
        return "IFO-0100".equals(e.getErrorCode()) || "IFO-2103".equals(e.getErrorCode()) ||
            "FLD-0092".equals(e.getErrorCode()) || "FLD-0064".equals(e.getErrorCode()) || "FLD-1014".equals(e.getErrorCode());
    }

    /**
     * Gets a value indicating whether the supplied exception indicates an unrecoverable failed remove operation, e.g. due to insufficient
     * permissions for a subfolder.
     *
     * @param e The exception to check
     * @return <code>true</code> if the exception indicates a failed remove exception, <code>false</code>, otherwise
     */
    public static boolean indicatesFailedRemove(OXException e) {
        return "FLD-0029".equals(e.getErrorCode()) || "FLD-0074".equals(e.getErrorCode());
    }

    /**
     * Gets a set of the normalized names of all supplied folders.
     *
     * @param folders The subfolders to get the names for
     * @return The normalied folder names
     */
    public static Set<String> getNormalizedFolderNames(Collection<FileStorageFolder> folders) {
        if (null == folders || 0 == folders.size()) {
            return Collections.emptySet();
        }
        Set<String> folderNames = new HashSet<String>(folders.size());
        for (FileStorageFolder folder : folders) {
            folderNames.add(PathNormalizer.normalize(folder.getName()));
        }
        return folderNames;
    }

    /**
     * Gets a set of the normalized names of all supplied files.
     *
     * @param file The files to get the names for
     * @param lowercase <code>true</code> to make them lowercase, <code>false</code>, otherwise
     * @return The normalized file names
     */
    public static Set<String> getNormalizedFileNames(Collection<File> files, boolean lowercase) {
        if (null == files || 0 == files.size()) {
            return Collections.emptySet();
        }
        Set<String> fileNames = new HashSet<String>(files.size());
        for (File file : files) {
            String normalizedName = PathNormalizer.normalize(file.getFileName());
            if (lowercase) {
                normalizedName = normalizedName.toLowerCase();
            }
            fileNames.add(normalizedName);
        }
        return fileNames;
    }

    /**
     * Gets a value indicating whether the session belongs to a known drive synchronization client or not.
     *
     * @param session The session to check
     * @return <code>true</code> if the session belongs to a known drive client, <code>false</code>, otherwise
     */
    public static boolean isDriveSession(Session session) {
        return null != session && false == DriveClientType.UNKNOWN.equals(DriveClientType.parse(session.getClient()));
    }

    /**
     * Tries to determine the MIME type for a file by looking at the extension of the filename and the file's MIME type property.
     *
     * @param file The file to get the MIME type for
     * @return The MIME type, or <code>null</code> if not available
     */
    public static String determineMimeType(File file) {
        String mimeType = null;
        if (false == Strings.isEmpty(file.getFileName())) {
            mimeType = MimeType2ExtMap.getContentType(file.getFileName(), null);
        }
        if (null == mimeType) {
            mimeType = file.getFileMIMEType();
        }
        return mimeType;
    }

    /**
     * Splits the supplied path string into segments based on {@link DriveConstants#PATH_SEPARATOR} character.
     *
     * @param path The path to split
     * @return A linked list holding the path segments from left to right, or an empty list if the path equals the root path
     */
    public static LinkedList<String> split(String path) throws OXException {
        if (null == path || false == path.startsWith(ROOT_PATH)) {
            throw DriveExceptionCodes.INVALID_PATH.create(path);
        }
        LinkedList<String> names = new LinkedList<String>();
        for (String name : path.split(String.valueOf(PATH_SEPARATOR))) {
            if (Strings.isEmpty(name)) {
                continue;
            }
            names.addLast(name);
        }
        return names;
    }

    /**
     * Combines a directory path with another file- or directory path, i.e. appends the latter one to the first path.
     *
     * @param path1 The path to combine
     * @param path2 The path to append
     * @return The combined path
     */
    public static String combine(String path1, String path2) {
        if (Strings.isEmpty(path1)) {
            return path2;
        } else if (Strings.isEmpty(path2)) {
            return path1;
        } else if (path1.endsWith("/")) {
            return path2.startsWith("/") ? path1 + path2.substring(1) : path1 + path2;
        } else {
            return path2.startsWith("/") ? path1 + path2 : path1 + '/' + path2;
        }
    }

    /**
     * Filters out the those folders that support all of the specified file storage capabilities.
     *
     * @param session The sync session
     * @param folderIDs The folder identifiers that should be supported
     * @param capabilities The capabilities to check for each folder
     * @return A list of folder identifiers whose storage supports all capabilities, or an empty list if there are none
     */
    public static List<String> filterByCapabilities(SyncSession session, List<String> folderIDs, FileStorageCapability...capabilities) throws OXException {
        Set<String> knowinglySupported = new HashSet<String>();
        Set<String> knowinglyUnsupported = new HashSet<String>();
        List<String> filteredFolderIDs = new ArrayList<String>(folderIDs);
        Iterator<String> iterator = filteredFolderIDs.iterator();
        while (iterator.hasNext()) {
            FolderID folderID = new FolderID(iterator.next());
            String key = folderID.getService() + ':' + folderID.getAccountId();
            if (knowinglySupported.contains(key)) {
                // keep
            } else if (knowinglyUnsupported.contains(key)) {
                // skip
                iterator.remove();
            } else if (session.getStorage().supports(folderID, capabilities)) {
                // keep & remember
                knowinglySupported.add(key);
            } else {
                // skip & remember
                iterator.remove();
                knowinglyUnsupported.add(key);
            }
        }
        return filteredFolderIDs;
    }

    /**
     * Converts a list of folder ID strings to a list of {@link FolderID}s.
     *
     * @param folderIDs The folder identifiers to convert
     * @return The converted folder IDs
     */
    public static List<FolderID> getFolderIDs(List<String> folderIDs) {
        List<FolderID> fids = new ArrayList<FolderID>(folderIDs.size());
        for (String folderID : folderIDs) {
            fids.add(new FolderID(folderID));
        }
        return fids;
    }

    /**
     * Gets a value indicating whether a specific folder is synchronizable or not.
     *
     * @param folderID The folder identifier to check
     * @return <code>true</code> if the folder is synchronizable, <code>false</code>, otherwise
     */
    public static boolean isSynchronizable(String folderID) {
        if (null != folderID) {
            /*
             * check for numerical folder identifier, only allowing specific system folders if smaller than 20
             * constants from com.openexchange.groupware.container.FolderObject
             * MIN_FOLDER_ID = 20
             * SYSTEM_USER_INFOSTORE_FOLDER_ID = 10
             * SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID = 15
             */
            try {
                int numericalID = Integer.parseInt(folderID);
                if (numericalID < 20 && numericalID != 10 && numericalID != 15) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // no numerical folder identifier
            }
            /*
             * check for blacklisted folders / disabled services
             */
            if (DriveConfig.getInstance().isExcludedFolder(folderID)) {
                return false;
            }
            if (false == DriveConfig.getInstance().isEnabledService(new FolderID(folderID).getService())) {
                return false;
            }
            /*
             * allow, otherwise
             */
            return true;
        }
        return false;
    }

    /**
     * Calculates a hash code based on the (client-side) file- and directory exclusion filters and whether metadata synchronization is
     * enabled or not, which may be used as a specific client "view" for the calculated directory checksums.
     *
     * @param session The drive session to get the view for
     * @return The hash code for the exclusion filters, or <code>0</code> if no filters are defined, and no metadata synchronization is off
     */
    public static int calculateView(DriveSession session) {
        return calculateView(session.getDirectoryExclusions(), session.getFileExclusions(), session.useDriveMeta());
    }

    /**
     * Calculates a hash code based on the (client-side) file- and directory exclusion filters and whether metadata synchronization is
     * enabled or not, which may be used as a specific client "view" for the calculated directory checksums.
     *
     * @param directoryExclusions The directory exclusions to consider
     * @param fileExclusions The file exclusions to consider
     * @param useDriveMeta <code>true</code> to consider metadata synchronization, <code>false</code>, otherwise
     * @return The hash code for the exclusion filters, or <code>0</code> if no filters are defined, and no metadata synchronization is off
     */
    public static int calculateView(List<DirectoryPattern> directoryExclusions, List<FilePattern> fileExclusions, boolean useDriveMeta) {
        final int prime = 31;
        int result = useDriveMeta ? -1 : 1;
        if (null != directoryExclusions && 0 < directoryExclusions.size()) {
            for (DirectoryPattern directoryPattern : directoryExclusions) {
                result = prime * result + directoryPattern.hashCode();
            }
        }
        if (null != fileExclusions && 0 < fileExclusions.size()) {
            for (FilePattern filePattern : fileExclusions) {
                result = prime * result + filePattern.hashCode();
            }
        }
        return 1 == result ? 0 : result;
    }

    private DriveUtils() {
        super();
    }

}
