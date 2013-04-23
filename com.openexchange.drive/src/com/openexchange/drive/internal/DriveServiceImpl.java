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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import jonelo.jacksum.algorithm.MD;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.RemoveFileAction;
import com.openexchange.drive.checksum.DirectoryFragment;
import com.openexchange.drive.comparison.DirectoryVersionMapper;
import com.openexchange.drive.comparison.FileVersionMapper;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.storage.filter.FileFilter;
import com.openexchange.drive.sync.SyncResult;
import com.openexchange.drive.sync.Synchronizer;
import com.openexchange.drive.sync.optimize.OptimizingDirectorySynchronizer;
import com.openexchange.drive.sync.optimize.OptimizingFileSynchronizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DriveServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveServiceImpl implements DriveService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveServiceImpl.class);

    /**
     * Initializes a new {@link DriveServiceImpl}.
     */
    public DriveServiceImpl() {
        super();
        LOG.debug("initialized.");
    }

    @Override
    public List<DriveAction<DirectoryVersion>> syncFolders(ServerSession session, String rootFolderID,
        List<DirectoryVersion> originalDirectories, List<DirectoryVersion> clientDirectories) throws OXException {
        long start = System.currentTimeMillis();
        DriveSession driveSession = createSession(session, rootFolderID);
        /*
         * map directories
         */
        List<ServerDirectoryVersion> serverDirectories = getServerDirectories(driveSession);
        DirectoryVersionMapper mapper = new DirectoryVersionMapper(originalDirectories, clientDirectories, serverDirectories);
        if (LOG.isDebugEnabled()) {
            StringAllocator allocator = new StringAllocator("Directory versions mapped to:\n");
            allocator.append(mapper).append('\n');
            LOG.debug(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<DirectoryVersion> synchronizer = new OptimizingDirectorySynchronizer(driveSession, mapper);
        SyncResult<DirectoryVersion> syncResult = synchronizer.sync();
        if (LOG.isDebugEnabled()) {
            LOG.debug(syncResult);
        }
        /*
         * execute actions on server
         */
        for (DriveAction<DirectoryVersion> action : syncResult.getActionsForServer()) {
            execute(driveSession, action);
        }
        /*
         * return actions for client
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug("syncFolders completed after " + (System.currentTimeMillis() - start) + "ms.");
        }
        return syncResult.getActionsForClient();
    }

    @Override
    public List<DriveAction<FileVersion>> syncFiles(ServerSession session, String rootFolderID, String path,
        List<FileVersion> originalFiles, List<FileVersion> clientFiles) throws OXException {
        long start = System.currentTimeMillis();
        DriveSession driveSession = createSession(session, rootFolderID);
        /*
         * map files
         */
        driveSession.getStorage().createFolder(path);
        List<ServerFileVersion> serverFiles = getServerFiles(driveSession, path);
        FileVersionMapper mapper = new FileVersionMapper(originalFiles, clientFiles, serverFiles);
        if (LOG.isDebugEnabled()) {
            StringAllocator allocator = new StringAllocator("File versions mapped to:\n");
            allocator.append(mapper).append('\n');
            LOG.debug(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<FileVersion> synchronizer = new OptimizingFileSynchronizer(driveSession, mapper, path);
        SyncResult<FileVersion> syncResult = synchronizer.sync();
        if (LOG.isDebugEnabled()) {
            LOG.debug(syncResult);
        }
        /*
         * execute actions on server
         */
        for (DriveAction<FileVersion> action : syncResult.getActionsForServer()) {
            execute(driveSession, path, action);
        }
        /*
         * return actions for client
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug("syncFiles completed after " + (System.currentTimeMillis() - start) + "ms.");
        }
        return syncResult.getActionsForClient();
    }

    @Override
    public IFileHolder download(ServerSession session, String rootFolderID, String path, FileVersion fileVersion,
        long offset, long length) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling download: " + fileVersion);
        }
        DownloadHelper downloadHelper = new DownloadHelper(driveSession);
        return downloadHelper.perform(path, fileVersion, offset, length);
    }

    @Override
    public List<DriveAction<FileVersion>> upload(ServerSession session, String rootFolderID, String path, InputStream uploadStream,
        FileVersion originalFile, FileVersion newFile, long offset, long totalLength) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling upload: " + newFile);
        }
        SyncResult<FileVersion> syncResult = new SyncResult<FileVersion>();
        FileVersion createdFile = new UploadHelper(driveSession).perform(path, originalFile, newFile, uploadStream, offset, totalLength);
        if (null != createdFile) {
            /*
             * store checksum
             */
            driveSession.getChecksumStore().addChecksum(((ServerFileVersion)createdFile).getFile(), newFile.getChecksum());
            /*
             * check if created file still equals uploaded one
             */
            if (newFile.getName().equals(createdFile.getName())) {
                syncResult.addActionForClient(new AcknowledgeFileAction(originalFile, createdFile, path));
            } else {
                syncResult.addActionForClient(new EditFileAction(newFile, createdFile));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(syncResult);
        }
        return syncResult.getActionsForClient();
    }

    private static void execute(DriveSession session, DriveAction<DirectoryVersion> action) throws OXException {
        switch (action.getAction()) {
        case EDIT:
            String folderID = session.getStorage().getFolderID(action.getVersion().getPath());
            String newFolderID = session.getStorage().moveFolder(action.getVersion().getPath(), action.getNewVersion().getPath());
            session.getChecksumStore().updateFolderIDs(folderID, newFolderID);
            break;
        case REMOVE:
            if (false == DriveConstants.EMPTY_MD5.equals(action.getVersion().getChecksum())) {
                // move all files to temp path
                List<ServerFileVersion> serverFileVersions = getServerFiles(session, action.getVersion().getPath());
                for (ServerFileVersion serverFileVersion : serverFileVersions) {
                    execute(session, action.getVersion().getPath(), new RemoveFileAction(serverFileVersion, action.getVersion().getPath()));
                }
            }
            // delete empty directory
            session.getStorage().deleteFolder(action.getVersion().getPath());
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static void execute(DriveSession session, String path, DriveAction<FileVersion> action) throws OXException {
        switch (action.getAction()) {
        case REMOVE:
            // invalidate checksums
            ServerFileVersion versionToRemove = (ServerFileVersion)action.getVersion();
            session.getChecksumStore().removeChecksums(versionToRemove.getFile());
            // move to trash
            File removedFile = session.getStorage().moveFile(
                versionToRemove.getFile(), action.getVersion().getChecksum(), DriveConstants.TEMP_PATH);
            if (null != removedFile) {
                if (action.getVersion().getChecksum().equals(removedFile.getFileName())) {
                    // moved successfully, remember checksum
                    session.getChecksumStore().addChecksum(removedFile, versionToRemove.getChecksum());
                } else {
                    // file already in trash, cleanup
                    session.getStorage().deleteFile(removedFile);
                }
            }

//            File removedFile = session.getStorage().deleteFile(file.getFile(), false);
            //TODO check possible edit-delete conflicts
            break;
        case DOWNLOAD:
            // copy file
            File sourceFile = ((ServerFileVersion)action.getParameters().get("sourceVersion")).getFile();
            File targetFile = null != action.getVersion() ? session.getStorage().findFileByNameAndChecksum(
                path, action.getVersion().getName(), action.getVersion().getChecksum()) : null;
            File copiedFile;
            if (null != targetFile) {
                session.getChecksumStore().removeChecksums(targetFile);
                copiedFile = session.getStorage().copyFile(sourceFile, targetFile);
            } else {
                copiedFile = session.getStorage().copyFile(sourceFile, action.getNewVersion().getName(), path);
            }
            session.getChecksumStore().addChecksum(copiedFile, action.getNewVersion().getChecksum());
            break;
        case EDIT:
            // edit file
            File originalFile = ((ServerFileVersion)action.getVersion()).getFile();
            session.getChecksumStore().removeChecksums(originalFile);
            File renamedFile = session.getStorage().renameFile(originalFile, action.getNewVersion().getName());
            session.getChecksumStore().addChecksum(renamedFile, action.getNewVersion().getChecksum());
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static List<ServerFileVersion> getServerFiles(DriveSession session, String path) throws OXException {
        List<File> files = session.getStorage().getFiles(path, new FileFilter() {
            @Override
            public boolean accept(File file) throws OXException {
                return null != file && false == Strings.isEmpty(file.getFileName());
            }
        });

        List<ServerFileVersion> serverFiles = new ArrayList<ServerFileVersion>();
        for (File file : files) {
            serverFiles.add(getServerFile(file, session));
        }
        return serverFiles;
    }

    private static ServerFileVersion getServerFile(File file, DriveSession session) throws OXException {
        return new ServerFileVersion(file, session.getChecksumStore().getChecksum(file));
    }

    private static List<ServerDirectoryVersion> getServerDirectories(DriveSession session) throws OXException {
        List<ServerDirectoryVersion> directories = new ArrayList<ServerDirectoryVersion>();
        Map<String, FileStorageFolder> folders = session.getStorage().getFolders();
        for (Map.Entry<String, FileStorageFolder> folder : folders.entrySet()) {
            if (session.getStorage().supportsFolderSequenceNumbers()) {
                /*
                 * try to use already known checksum if possible
                 */
                Entry<String, Long> knownChecksum = session.getChecksumStore().getFolder(folder.getValue().getId());
                if (null != knownChecksum) {
                    long lastSequenceNumber = knownChecksum.getValue().longValue();
                    if (false == session.getStorage().hasChangedSince(folder.getKey(), lastSequenceNumber)) {
                        LOG.debug("No changes detected since last calculated checksum for directory " + folder.getKey());
                        directories.add(new ServerDirectoryVersion(folder.getKey(), knownChecksum.getKey()));
                        continue;
                    } else {
                        LOG.debug("Changes detected since last calculated checksum for directory " + folder.getKey() + " ");
                        session.getChecksumStore().removeFolder(folder.getValue().getId());
                    }
                }
            }
            /*
             * calculate checksum
             */
            directories.add(getServerDirectory(folder.getKey(), session));
        }
        return directories;
    }

    private static Set<DirectoryFragment> getDirectoryFragments(String path, DriveSession session) throws OXException {
        List<File> files = session.getStorage().getFiles(path, new FileFilter() {

            @Override
            public boolean accept(File file) throws OXException {
                return null != file && false == Strings.isEmpty(file.getFileName());
            }
        });
        if (null == files || 0 == files.size()) {
            return Collections.emptySet();
        }
        TreeSet<DirectoryFragment> fragments = new TreeSet<DirectoryFragment>();
        String folderID = session.getStorage().getFolderID(path);
        Map<File, String> knownChecksums = session.getChecksumStore().getFilesInFolder(folderID);
        for (File file : files) {
            if (null != file.getFileName()) {
                String knownChecksum = null;
                for (Entry<File, String> entry : knownChecksums.entrySet()) {
                    if (entry.getKey().getId().equals(file.getId()) && entry.getKey().getSequenceNumber() == file.getSequenceNumber() &&
                        entry.getKey().getVersion().equals(file.getVersion())) {
                        knownChecksum = entry.getValue();
                        break;
                    }
                }
                if (null != knownChecksum) {
                    fragments.add(new DirectoryFragment(file, knownChecksum));
                } else {
                    fragments.add(new DirectoryFragment(file, session.getChecksumStore().getChecksum(file)));
                }
            }
        }
        return fragments;
    }

    private static ServerDirectoryVersion getServerDirectory(String path, DriveSession session) throws OXException {
        long sequenceNumber = session.getStorage().getFolder(path).getLastModifiedDate().getTime();
        Set<DirectoryFragment> fragments = getDirectoryFragments(path, session);
        ServerDirectoryVersion directoryVersion;
        if (null == fragments || 0 == fragments.size()) {
            directoryVersion = new ServerDirectoryVersion(path, DriveConstants.EMPTY_MD5);
        } else {
            MD md5 = session.newMD5();
            for (DirectoryFragment directoryFragment : fragments) {
                md5.update(directoryFragment.getEncodedFileName());
                md5.update(directoryFragment.getEncodedChecksum());
                if (sequenceNumber < directoryFragment.getFile().getSequenceNumber()) {
                    sequenceNumber = directoryFragment.getFile().getSequenceNumber();
                }
            }
            directoryVersion = new ServerDirectoryVersion(path, md5.getFormattedValue());
        }
        if (session.getStorage().supportsFolderSequenceNumbers()) {
            session.getChecksumStore().addFolder(session.getStorage().getFolderID(path), sequenceNumber, directoryVersion.getChecksum());
        }
        return directoryVersion;
    }

    private static DriveSession createSession(ServerSession session, String rootFolderID) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating new drive session for user " + session.getLoginName() + " (" + session.getUserId() +
                ") in context " + session.getContextId() + ", root folder ID is " + rootFolderID);
        }
        return new DriveSession(session, rootFolderID);
    }

}
