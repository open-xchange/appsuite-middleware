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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jonelo.jacksum.algorithm.MD;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditFileAction;
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
import com.openexchange.java.Charsets;
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
        return syncResult.getActionsForClient();
    }

    @Override
    public List<DriveAction<FileVersion>> syncFiles(ServerSession session, String rootFolderID, String path,
        List<FileVersion> originalFiles, List<FileVersion> clientFiles) throws OXException {
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
        return syncResult.getActionsForClient();
    }

    @Override
    public IFileHolder download(ServerSession session, String rootFolderID, String path, FileVersion fileVersion,
        long offset, long length) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        DownloadHelper downloadHelper = new DownloadHelper(driveSession);
        return downloadHelper.perform(path, fileVersion, offset, length);
    }

    @Override
    public List<DriveAction<FileVersion>> upload(ServerSession session, String rootFolderID, String path, InputStream uploadStream,
        FileVersion originalFile, FileVersion newFile, long offset, long totalLength) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        List<DriveAction<FileVersion>> actions = new ArrayList<DriveAction<FileVersion>>();

        FileVersion createdFile = new UploadHelper(driveSession).perform(path, originalFile, newFile, uploadStream, offset, totalLength);
        if (null != createdFile) {
            /*
             * check if created file still equals uploaded one
             */
            if (newFile.getName().equals(createdFile.getName())) {
                actions.add(new AcknowledgeFileAction(originalFile, createdFile, path));
            } else {
                actions.add(new EditFileAction(newFile, createdFile));
            }
        }

        return actions;
    }

    private static void execute(DriveSession session, DriveAction<DirectoryVersion> action) throws OXException {
        switch (action.getAction()) {
        case EDIT:
            session.getStorage().moveFolder(action.getVersion().getPath(), action.getNewVersion().getPath());
            break;
        case REMOVE:
            session.getStorage().deleteFolder(action.getVersion().getPath());
            break;
        default:
            throw new IllegalStateException("Can't perform action " + action + " on server");
        }
    }

    private static void execute(DriveSession session, String path, DriveAction<FileVersion> action) throws OXException {
        switch (action.getAction()) {
        case REMOVE:
            // delete file
            ServerFileVersion file = (ServerFileVersion)action.getVersion();
            File removedFile = session.getStorage().deleteFile(file.getFile(), false);
            session.getChecksumStore().removeChecksums(file.getFile());
            session.getChecksumStore().addChecksum(removedFile, file.getChecksum());
            //TODO check possible edit-delete conflicts
            break;
        case DOWNLOAD:
            // copy file
            File sourceFile = ((ServerFileVersion)action.getVersion()).getFile();
            File targetFile = null != action.getNewVersion() ? session.getStorage().findFileByNameAndChecksum(
                path, action.getNewVersion().getName(), action.getNewVersion().getChecksum()) : null;
            File copiedFile;
            if (null != targetFile) {
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
            directories.add(getServerDirectory(folder.getKey(), session));
        }
        return directories;
    }

    private static ServerDirectoryVersion getServerDirectory(String path, DriveSession session) throws OXException {
        List<File> files = session.getStorage().getFiles(path, new FileFilter() {

            @Override
            public boolean accept(File file) throws OXException {
                return null != file && false == Strings.isEmpty(file.getFileName());
            }
        });
        if (null == files || 0 == files.size()) {
            return new ServerDirectoryVersion(path, DriveConstants.EMPTY_MD5);
        }
        Collections.sort(files, new Comparator<File>() {

            @Override
            public int compare(File file1, File file2) {
                return file1.getFileName().compareTo(file2.getFileName());
            }
        });
        String folderID = session.getStorage().getFolderID(path);
        Map<File, String> knownChecksums = session.getChecksumStore().getFilesInFolder(folderID);
        try {
            MD md5 = new MD("MD5");
            for (File file : files) {
                if (null != file.getFileName()) {
                    String knownChecksum = null;
                    md5.update(file.getFileName().getBytes(Charsets.UTF_8));
                    for (Entry<File, String> entry : knownChecksums.entrySet()) {
                        if (entry.getKey().getId().equals(file.getId()) && entry.getKey().getSequenceNumber() == file.getSequenceNumber()) {
                            knownChecksum = entry.getValue();
                            break;
                        }
                    }
                    if (null != knownChecksum) {
                        md5.update(knownChecksum.getBytes(Charsets.UTF_8));
                    } else {
                        md5.update(session.getChecksumStore().getChecksum(file).getBytes(Charsets.UTF_8));
                    }
                }
            }
            return new ServerDirectoryVersion(path, md5.getFormattedValue());
        } catch (NoSuchAlgorithmException e) {
            throw new OXException(e);
        }
    }

    private static DriveSession createSession(ServerSession session, String rootFolderID) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating new drive session for user " + session.getLoginName() + " (" + session.getUserId() +
                ") in context " + session.getContextId() + ", root folder ID is " + rootFolderID);
        }
        return new DriveSession(session, rootFolderID);
    }

}
