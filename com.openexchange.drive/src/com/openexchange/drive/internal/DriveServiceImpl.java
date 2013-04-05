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
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.comparison.DirectoryVersionMapper;
import com.openexchange.drive.comparison.FileVersionMapper;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.sim.checksum.SimChecksumStore;
import com.openexchange.drive.storage.filter.FileFilter;
import com.openexchange.drive.sync.SyncResult;
import com.openexchange.drive.sync.Synchronizer;
import com.openexchange.drive.sync.optimize.OptimizingDirectorySynchronizer;
import com.openexchange.drive.sync.optimize.OptimizingFileSynchronizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DriveServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveServiceImpl implements DriveService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveServiceImpl.class);

    private final ChecksumProvider checksumProvider;
    private final ChecksumStore checksumStore;

    /**
     * Initializes a new {@link DriveServiceImpl}.
     */
    public DriveServiceImpl() {
        super();
        LOG.debug("initialized.");
        this.checksumStore = new SimChecksumStore();
        this.checksumProvider = new ChecksumProvider(checksumStore);
    }

    private DriveSession createSession(ServerSession session, String rootFolderID) {
        return new DriveSession(session, rootFolderID, checksumStore, checksumProvider);
    }

    @Override
    public List<DriveAction<DirectoryVersion>> syncFolders(ServerSession session, String rootFolderID, List<DirectoryVersion> originalDirectories, List<DirectoryVersion> clientDirectories) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        List<ServerDirectoryVersion> serverDirectories = getServerDirectories(driveSession);
        DirectoryVersionMapper mapper = new DirectoryVersionMapper(originalDirectories, clientDirectories, serverDirectories);
        Synchronizer<DirectoryVersion> synchronizer = new OptimizingDirectorySynchronizer(driveSession, mapper);
        SyncResult<DirectoryVersion> syncResult = synchronizer.sync();
        /*
         * execute actions on server
         */
        for (DriveAction<DirectoryVersion> directoryAction : syncResult.getActionsForServer()) {
            switch (directoryAction.getAction()) {
            case EDIT:
                driveSession.getStorage().moveFolder(directoryAction.getVersion().getPath(), directoryAction.getNewVersion().getPath());
                break;
            case REMOVE:
                driveSession.getStorage().deleteFolder(directoryAction.getVersion().getPath());
                break;
            default:
                throw new IllegalStateException("Can't perform action " + directoryAction + " on server");
            }
        }
        /*
         * return actions for client
         */
        return syncResult.getActionsForClient();
    }

    @Override
    public List<DriveAction<FileVersion>> syncFiles(ServerSession session, String rootFolderID, String path, List<FileVersion> originalFiles, List<FileVersion> clientFiles) throws OXException {
        /*
         * determine actions
         */
        DriveSession driveSession = createSession(session, rootFolderID);
        driveSession.getStorage().createFolder(path);
        List<ServerFileVersion> serverFiles = getServerFiles(driveSession, path);
        FileVersionMapper mapper = new FileVersionMapper(originalFiles, clientFiles, serverFiles);
        Synchronizer<FileVersion> synchronizer = new OptimizingFileSynchronizer(driveSession, mapper, path);
        SyncResult<FileVersion> syncResult = synchronizer.sync();
        List<DriveAction<FileVersion>> actionsForServer = syncResult.getActionsForServer();
        List<DriveAction<FileVersion>> actionsForClient = syncResult.getActionsForClient();
        /*
         * execute actions on server
         */
        if (null != actionsForServer && 0 < actionsForServer.size()) {

            for (DriveAction<FileVersion> fileAction : actionsForServer) {
                switch (fileAction.getAction()) {
                case REMOVE:
                    // delete file
                    ServerFileVersion file = (ServerFileVersion)fileAction.getVersion();
                    driveSession.getStorage().deleteFile(file.getFile(), false);
                    //TODO check possible edit-delete conflicts
                    break;
                case DOWNLOAD:
                    // copy file
                    File sourceFile = ((ServerFileVersion)fileAction.getVersion()).getFile();
                    driveSession.getStorage().copyFile(sourceFile, fileAction.getNewVersion().getName(), path);
                    break;
                case EDIT:
                    // edit file
                    File originalFile = ((ServerFileVersion)fileAction.getVersion()).getFile();
                    driveSession.getStorage().renameFile(originalFile, fileAction.getNewVersion().getName());
                    break;
                default:
                    throw new IllegalStateException("Can't perform action " + fileAction + " on server");
                }
            }
        }
        /*
         * return actions for client
         */
        return actionsForClient;
    }

    @Override
    public IFileHolder download(ServerSession session, String rootFolderID, String path, FileVersion fileVersion, long offset, long length) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        DownloadHelper downloadHelper = new DownloadHelper(driveSession);
        return downloadHelper.perform(path, fileVersion, offset, length);
    }

    @Override
    public List<DriveAction<FileVersion>> upload(ServerSession session, String rootFolderID, String path, InputStream uploadStream, FileVersion originalFile, FileVersion newFile, long offset, long totalLength) throws OXException {
        DriveSession driveSession = createSession(session, rootFolderID);
        List<DriveAction<FileVersion>> actions = new ArrayList<DriveAction<FileVersion>>();

        FileVersion createdFile = new UploadHelper(driveSession).perform(path, originalFile, newFile, uploadStream, offset, totalLength);
        if (null != createdFile) {
            /*
             * check if created file still equals uploaded one
             */
            if (newFile.getName().equals(createdFile.getName())) {
                actions.add(new AcknowledgeFileAction(originalFile, createdFile));
            } else {
                actions.add(new EditFileAction(newFile, createdFile));
            }
        }

        return actions;
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
        return new ServerFileVersion(file, session.getMD5(file));
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
        String checksum = session.getChecksumProvider().getMD5(files, session.getStorage());
        return new ServerDirectoryVersion(path, checksum);
    }

}
