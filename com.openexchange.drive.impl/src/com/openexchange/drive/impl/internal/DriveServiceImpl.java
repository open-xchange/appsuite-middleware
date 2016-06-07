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

package com.openexchange.drive.impl.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryMetadata;
import com.openexchange.drive.DirectoryPattern;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveClientVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.DriveQuota;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveSettings;
import com.openexchange.drive.DriveUtility;
import com.openexchange.drive.FilePattern;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.AbstractFileAction;
import com.openexchange.drive.impl.actions.AcknowledgeFileAction;
import com.openexchange.drive.impl.actions.EditFileAction;
import com.openexchange.drive.impl.actions.ErrorDirectoryAction;
import com.openexchange.drive.impl.actions.ErrorFileAction;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.DirectoryVersionMapper;
import com.openexchange.drive.impl.comparison.FileVersionMapper;
import com.openexchange.drive.impl.comparison.FilteringDirectoryVersionMapper;
import com.openexchange.drive.impl.comparison.FilteringFileVersionMapper;
import com.openexchange.drive.impl.comparison.ServerDirectoryVersion;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.internal.tracking.SyncTracker;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.drive.impl.storage.DriveStorage;
import com.openexchange.drive.impl.storage.execute.DirectoryActionExecutor;
import com.openexchange.drive.impl.storage.execute.FileActionExecutor;
import com.openexchange.drive.impl.sync.DefaultSyncResult;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.drive.impl.sync.Synchronizer;
import com.openexchange.drive.impl.sync.optimize.OptimizingDirectorySynchronizer;
import com.openexchange.drive.impl.sync.optimize.OptimizingFileSynchronizer;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.Strings;

/**
 * {@link DriveServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveServiceImpl implements DriveService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveServiceImpl.class);

    /**
     * Initializes a new {@link DriveServiceImpl}.
     */
    public DriveServiceImpl() {
        super();
        LOG.debug("initialized.");
    }

    @Override
    public SyncResult<DirectoryVersion> syncFolders(DriveSession session, List<DirectoryVersion> originalVersions,
        List<DirectoryVersion> clientVersions) throws OXException {
        /*
         * check (hard) version restrictions
         */
        if (session.getApiVersion() < DriveConfig.getInstance().getMinApiVersion()) {
            OXException error = DriveExceptionCodes.CLIENT_OUTDATED.create();
            LOG.debug("Client synchronization aborted for {}", session, error);
            List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
            actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, true));
            return new DefaultSyncResult<DirectoryVersion>(actionsForClient, error.getLogMessage());
        }
        DriveClientVersion clientVersion = session.getClientVersion();
        if (null != clientVersion) {
            DriveClientVersion hardVersionLimit = DriveConfig.getInstance().getHardMinimumVersion(session.getClientType(), session.getServerSession());
            if (0 > clientVersion.compareTo(hardVersionLimit)) {
                OXException error = DriveExceptionCodes.CLIENT_VERSION_OUTDATED.create(clientVersion, hardVersionLimit);
                LOG.debug("Client synchronization aborted for {}", session, error);
                List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
                actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, true));
                return new DefaultSyncResult<DirectoryVersion>(actionsForClient, error.getLogMessage());
            }
        }
        if (false == DriveUtils.isSynchronizable(session.getRootFolderID())) {
            OXException error = DriveExceptionCodes.NOT_SYNCHRONIZABLE_DIRECTORY.create(session.getRootFolderID());
            LOG.debug("Client synchronization aborted for {}", session, error);
            List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
            actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, true));
            return new DefaultSyncResult<DirectoryVersion>(actionsForClient, error.getLogMessage());
        }
        int maxDirectories = DriveConfig.getInstance().getMaxDirectories();
        if (-1 != maxDirectories && null != clientVersions && maxDirectories < clientVersions.size()) {
            OXException error = DriveExceptionCodes.TOO_MANY_DIRECTORIES.create(maxDirectories);
            LOG.debug("Client synchronization aborted for {}", session, error);
            List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
            actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, true));
            return new DefaultSyncResult<DirectoryVersion>(actionsForClient, error.getLogMessage());
        }
        /*
         * sync folders
         */
        long start = System.currentTimeMillis();
        DriveVersionValidator.validateDirectoryVersions(originalVersions);
        DriveVersionValidator.validateDirectoryVersions(clientVersions);
        List<ServerDirectoryVersion> serverVersions;
        int retryCount = 0;
        while (true) {
            /*
             * get server directories
             */
            final SyncSession driveSession = new SyncSession(session);
            ;
            try {
                serverVersions = driveSession.getServerDirectories(maxDirectories);
            } catch (OXException e) {
                if ("DRV-0035".equals(e.getErrorCode())) {
                    /*
                     * Maximum number of synchronizable directories exceeded - stop synchronization
                     */
                    LOG.debug("Client synchronization aborted for {}", session, e);
                    List<AbstractAction<DirectoryVersion>> actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
                    actionsForClient.add(new ErrorDirectoryAction(null, null, null, e, false, true));
                    return new DefaultSyncResult<DirectoryVersion>(actionsForClient, e.getLogMessage());
                }
                throw e;
            }
            /*
             * sync
             */
            IntermediateSyncResult<DirectoryVersion> syncResult = syncDirectories(driveSession, originalVersions, clientVersions, serverVersions);
            /*
             * track & check sync result for cycles
             */
            if (0 == retryCount) {
                syncResult = new SyncTracker(driveSession).trackAndCheck(syncResult);
            }
            List<AbstractAction<DirectoryVersion>> actionsForClient = null;
            try {
                /*
                 * execute actions on server
                 */
                DirectoryActionExecutor executor = new DirectoryActionExecutor(driveSession, true, retryCount < DriveConstants.MAX_RETRIES);
                actionsForClient = executor.execute(syncResult);
            } catch (OXException e) {
                if (0 == retryCount || (tryAgain(e) && retryCount < DriveConstants.MAX_RETRIES)) {
                    retryCount++;
                    int delay = DriveConstants.RETRY_BASEDELAY * retryCount;
                    driveSession.trace("Got exception during execution of server actions (" + e.getMessage() + "), trying again in " +
                        delay + "ms" + (1 == retryCount ? "..." : " (" + retryCount + '/' + DriveConstants.MAX_RETRIES + ")..."));
                    delay(delay);
                    continue;
                }
                driveSession.trace("Got exception during execution of server actions (" + e.getMessage() + ")");
                LOG.warn("Got exception during execution of server actions\nPrevious sync result:\n{}", syncResult, e);
                throw e;
            }
            /*
             * start background cleanup tasks after empty sync results if applicable
             */
            if (syncResult.isEmpty()) {
                List<DirectoryChecksum> directoryChecksums = new ArrayList<DirectoryChecksum>(serverVersions.size());
                for (ServerDirectoryVersion serverVersion : serverVersions) {
                    directoryChecksums.add(serverVersion.getDirectoryChecksum());
                }
                int touched = driveSession.getChecksumStore().touchDirectoryChecksums(directoryChecksums);
                driveSession.trace("Successfully touched " + touched + " stored directory checksums.");
                TempCleaner.cleanUpIfNeeded(driveSession);
            }
            /*
             * check (soft) version restrictions
             */
            if (null != clientVersion) {
                DriveClientVersion softVersionLimit = DriveConfig.getInstance().getSoftMinimumVersion(session.getClientType(), session.getServerSession());
                if (0 > clientVersion.compareTo(softVersionLimit)) {
                    OXException error = DriveExceptionCodes.CLIENT_VERSION_UPDATE_AVAILABLE.create(clientVersion, softVersionLimit);
                    LOG.trace("Client upgrade available for {}", session, error);
                    if (null == actionsForClient) {
                        actionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(1);
                    }
                    actionsForClient.add(new ErrorDirectoryAction(null, null, null, error, false, false));
                }
            }
            /*
             * return actions for client
             */
            if (driveSession.isTraceEnabled()) {
                driveSession.trace("syncFolders with " + syncResult.length() + " resulting action(s) completed after "
                    + (System.currentTimeMillis() - start) + "ms.");
            }
            return new DefaultSyncResult<DirectoryVersion>(actionsForClient, driveSession.getDiagnosticsLog());
        }
    }

    @Override
    public SyncResult<FileVersion> syncFiles(DriveSession session, final String path, List<FileVersion> originalVersions, List<FileVersion> clientVersions) throws OXException {
        long start = System.currentTimeMillis();
        DriveVersionValidator.validateFileVersions(originalVersions);
        DriveVersionValidator.validateFileVersions(clientVersions);
        int maxFilesPerDirectory = DriveConfig.getInstance().getMaxFilesPerDirectory();
        if (-1 != maxFilesPerDirectory && null != clientVersions && maxFilesPerDirectory < clientVersions.size()) {
            OXException error = DriveExceptionCodes.TOO_MANY_FILES.create(maxFilesPerDirectory, path);
            LOG.debug("Client synchronization aborted for {}", session, error);
            List<AbstractAction<FileVersion>> actionsForClient = new ArrayList<AbstractAction<FileVersion>>(1);
            actionsForClient.add(new ErrorFileAction(null, null, null, path, error, false, true));
            return new DefaultSyncResult<FileVersion>(actionsForClient, error.getLogMessage());
        }
        int retryCount = 0;
        while (true) {
            /*
             * get server files
             */
            final SyncSession driveSession = new SyncSession(session);
            driveSession.getStorage().createFolder(path);
            List<ServerFileVersion> serverVersions;
            try {
                serverVersions = driveSession.getServerFiles(path, maxFilesPerDirectory);
            } catch (OXException e) {
                if ("DRV-0036".equals(e.getErrorCode())) {
                    /*
                     * Maximum number of synchronizable files exceeded - stop synchronization
                     */
                    LOG.debug("Client synchronization aborted for {}", session, e);
                    List<AbstractAction<FileVersion>> actionsForClient = new ArrayList<AbstractAction<FileVersion>>(1);
                    actionsForClient.add(new ErrorFileAction(null, null, null, path, e, false, true));
                    return new DefaultSyncResult<FileVersion>(actionsForClient, e.getLogMessage());
                }
                throw e;
            }
            /*
             * sync
             */
            IntermediateSyncResult<FileVersion> syncResult = syncFiles(driveSession, path, originalVersions, clientVersions, serverVersions);
            /*
             * track sync result
             */
            if (0 == retryCount) {
                syncResult = new SyncTracker(driveSession).track(syncResult, path);
            }
            List<AbstractAction<FileVersion>> actionsForClient = null;
            try {
                /*
                 * execute actions on server
                 */
                FileActionExecutor executor = new FileActionExecutor(driveSession, true, retryCount < DriveConstants.MAX_RETRIES, path);
                actionsForClient = executor.execute(syncResult);
            } catch (OXException e) {
                if (0 == retryCount || (tryAgain(e) && retryCount < DriveConstants.MAX_RETRIES)) {
                    retryCount++;
                    int delay = DriveConstants.RETRY_BASEDELAY * retryCount;
                    driveSession.trace("Got exception during execution of server actions (" + e.getMessage() + "), trying again in " +
                        delay + "ms" + (1 == retryCount ? "..." : " (" + retryCount + '/' + DriveConstants.MAX_RETRIES + ")..."));
                    delay(delay);
                    continue;
                }
                throw e;
            }
            /*
             * return actions for client
             */
            if (driveSession.isTraceEnabled()) {
                driveSession.trace("syncFiles with " + syncResult.length() + " resulting action(s) completed after "
                    + (System.currentTimeMillis() - start) + "ms.");
            }
            return new DefaultSyncResult<FileVersion>(actionsForClient, driveSession.getDiagnosticsLog());
        }
    }

    @Override
    public IFileHolder download(DriveSession session, String path, FileVersion fileVersion, long offset, long length) throws OXException {
        DriveVersionValidator.validateFileVersion(fileVersion);
        SyncSession syncSession = new SyncSession(session);
        LOG.debug("Handling download: file version: {}, offset: {}, length: {}", fileVersion, offset, length);
        /*
         * track sync result to represent the download as performed by client
         */
        AbstractAction<FileVersion> action = new AbstractFileAction(null, fileVersion, null) {

            @Override
            public Action getAction() {
                return Action.DOWNLOAD;
            }
        };
        action.getParameters().put(DriveAction.PARAMETER_OFFSET, Long.valueOf(offset));
        action.getParameters().put(DriveAction.PARAMETER_LENGTH, Long.valueOf(length));
        new SyncTracker(syncSession).track(new IntermediateSyncResult<FileVersion>(
            Collections.<AbstractAction<FileVersion>> emptyList(), Collections.<AbstractAction<FileVersion>> singletonList(action)), path);
        /*
         * return file holder for download
         */
        return new DownloadHelper(syncSession).perform(path, fileVersion, offset, length);
    }

    @Override
    public SyncResult<FileVersion> upload(DriveSession session, String path, InputStream uploadStream, FileVersion originalVersion,
        FileVersion newVersion, String contentType, long offset, long totalLength, Date created, Date modified) throws OXException {
        DriveVersionValidator.validateFileVersion(newVersion);
        if (null != originalVersion) {
            DriveVersionValidator.validateFileVersion(originalVersion);
        }
        SyncSession syncSession = new SyncSession(session);
        if (syncSession.isTraceEnabled()) {
            syncSession.trace("Handling upload: original version: " + originalVersion + ", new version: " + newVersion +
                ", offset: " + offset + ", total length: " + totalLength +
                ", created: " + (null != created ? DriveConstants.LOG_DATE_FORMAT.get().format(created) : "") +
                ", modified: " + (null != modified ? DriveConstants.LOG_DATE_FORMAT.get().format(modified) : ""));
        }
        IntermediateSyncResult<FileVersion> syncResult = new IntermediateSyncResult<FileVersion>();
        File createdFile = null;
        try {
            createdFile = new UploadHelper(syncSession).perform(path, originalVersion, newVersion, uploadStream, contentType, offset, totalLength, created, modified);
        } catch (OXException e) {
            if ("FLS-0022".equals(e.getErrorCode())) {
                // The connected client closed the connection unexpectedly
                LOG.debug("Client connection lost during upload ({})\nSession: {}, path: {}, original version: {}, new version: {}, offset: {}, total length: {}",
                    e.getMessage(), syncSession, path, originalVersion, newVersion, offset, totalLength, e);
                throw DriveExceptionCodes.CLIENT_CONNECTION_LOST.create(e);
            }
            LOG.warn("Got exception during upload ({})\nSession: {}, path: {}, original version: {}, new version: {}, offset: {}, total length: {}",
                e.getMessage(), syncSession, path, originalVersion, newVersion, offset, totalLength, e);
            if (DriveUtils.indicatesQuotaExceeded(e)) {
                syncResult.addActionsForClient(DriveUtils.handleQuotaExceeded(syncSession, e, path, originalVersion, newVersion));
            } else if (DriveUtils.indicatesFailedSave(e)) {
                syncResult.addActionForClient(new ErrorFileAction(null, newVersion, null, path, e, true));
            } else {
                throw e;
            }
        }
        if (null != createdFile) {
            /*
             * store checksum, invalidate parent directory checksum
             */
            FileChecksum fileChecksum = syncSession.getChecksumStore().insertFileChecksum(new FileChecksum(
                DriveUtils.getFileID(createdFile), createdFile.getVersion(), createdFile.getSequenceNumber(), newVersion.getChecksum()));
            syncSession.getChecksumStore().removeDirectoryChecksum(new FolderID(createdFile.getFolderId()));
            /*
             * check if created file still equals uploaded one
             */
            ServerFileVersion createdVersion = new ServerFileVersion(createdFile, fileChecksum);
            if (newVersion.getName().equals(createdFile.getFileName())) {
                syncResult.addActionForClient(new AcknowledgeFileAction(syncSession, originalVersion, createdVersion, null, path));
            } else {
                syncResult.addActionForClient(new EditFileAction(newVersion, createdVersion, null, path));
            }
        }
        if (syncSession.isTraceEnabled()) {
            syncSession.trace(syncResult);
        }
        /*
         * track & return sync result
         */
        syncResult = new SyncTracker(syncSession).track(syncResult, path);
        return new DefaultSyncResult<FileVersion>(syncResult.getActionsForClient(), syncSession.getDiagnosticsLog());
    }

    @Override
    public DriveQuota getQuota(DriveSession session) throws OXException {
        return getSettings(session).getQuota();
    }

    @Override
    public DriveSettings getSettings(DriveSession session) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        LOG.debug("Handling get-settings for '{}'", session);
        /*
         * collect settings
         */
        DriveSettings settings = new DriveSettings();
        Quota[] quota = syncSession.getStorage().getQuota();
        LOG.debug("Got quota for root folder '{}': {}", session.getRootFolderID(), quota);
        settings.setQuota(new DriveQuotaImpl(quota, syncSession.getLinkGenerator().getQuotaLink()));
        settings.setHelpLink(syncSession.getLinkGenerator().getHelpLink());
        settings.setServerVersion(com.openexchange.version.Version.getInstance().getVersionString());
        settings.setMinApiVersion(String.valueOf(DriveConfig.getInstance().getMinApiVersion()));
        settings.setSupportedApiVersion(String.valueOf(DriveConstants.SUPPORTED_API_VERSION));
        /*
         * add any localized folder names (up to a certain depth after which no localized names are expected anymore)
         */
        Map<String, String> localizedFolders = new HashMap<String, String>();
        int maxDirectories = DriveConfig.getInstance().getMaxDirectories();
        Map<String, FileStorageFolder> folders = syncSession.getStorage().getFolders(maxDirectories, 2);
        for (Map.Entry<String, FileStorageFolder> entry : folders.entrySet()) {
            String localizedName = entry.getValue().getLocalizedName(session.getLocale());
            if (Strings.isNotEmpty(localizedName) && false == localizedName.equals(entry.getValue().getName())) {
                localizedFolders.put(entry.getKey(), localizedName);
            }
        }
        settings.setLocalizedFolders(localizedFolders);
        /*
         * evaluate relevant capabilities
         */
        Set<String> capabilities = new HashSet<String>();
        CapabilitySet capabilitySet = DriveServiceLookup.getService(CapabilityService.class).getCapabilities(session.getServerSession());
        capabilities.add("invite_users_and_groups");
        if (capabilitySet.contains("invite_guests")) {
            capabilities.add("invite_guests");
        }
        if (capabilitySet.contains("share_links")) {
            capabilities.add("share_links");
        }
        /*
         * indicate ability to listen for changes in multiple root folders via long polling (bug #45919)
         */
        capabilities.add("multiple_folder_long_polling");
        settings.setCapabilities(capabilities);
        return settings;
    }

    @Override
    public List<DriveFileMetadata> getFileMetadata(DriveSession session, String path, List<FileVersion> fileVersions, List<DriveFileField> fields) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        if (null == fileVersions) {
            return DriveMetadataFactory.getFileMetadata(syncSession, syncSession.getServerFiles(path), fields);
        } else if (1 == fileVersions.size()) {
            ServerFileVersion serverFile = ServerFileVersion.valueOf(fileVersions.get(0), path, syncSession);
            return Collections.singletonList(DriveMetadataFactory.getFileMetadata(syncSession, serverFile, fields));
        } else {
            List<DriveFileMetadata> metadata = new ArrayList<DriveFileMetadata>(fileVersions.size());
            List<ServerFileVersion> serverFiles = syncSession.getServerFiles(path);
            for (FileVersion requestedVersion : fileVersions) {
                ServerFileVersion matchingVersion = null;
                for (ServerFileVersion serverFileVersion : serverFiles) {
                    if (Change.NONE.equals(Change.get(serverFileVersion, requestedVersion))) {
                        matchingVersion = serverFileVersion;
                        break;
                    }
                }
                if (null == matchingVersion) {
                    throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(requestedVersion.getName(), requestedVersion.getChecksum(), path);
                }
                metadata.add(DriveMetadataFactory.getFileMetadata(syncSession, matchingVersion, fields));
            }
            return metadata;
        }
    }

    @Override
    public DirectoryMetadata getDirectoryMetadata(DriveSession session, String path) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        String folderID = syncSession.getStorage().getFolderID(path);
        List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(syncSession, Arrays.asList(new String[] { folderID }));
        if (null == checksums || 0 == checksums.size()) {
            throw DriveExceptionCodes.PATH_NOT_FOUND.create(path);
        }
        return new DefaultDirectoryMetadata(syncSession, new ServerDirectoryVersion(path, checksums.get(0)));
    }

    private static IntermediateSyncResult<DirectoryVersion> syncDirectories(SyncSession session, List<? extends DirectoryVersion> originalVersions,
        List<? extends DirectoryVersion> clientVersions, List<? extends DirectoryVersion> serverVersions) throws OXException {
        /*
         * map directories
         */
        List<DirectoryPattern> directoryExclusions = session.getDriveSession().getDirectoryExclusions();
        DirectoryVersionMapper mapper;
        if (null == directoryExclusions || 0 == directoryExclusions.size()) {
            mapper = new DirectoryVersionMapper(originalVersions, clientVersions, serverVersions);
        } else {
            mapper = new FilteringDirectoryVersionMapper(directoryExclusions, originalVersions, clientVersions, serverVersions);
        }
        if (session.isTraceEnabled()) {
            StringBuilder allocator = new StringBuilder("Directory versions mapped to:\n");
            allocator.append(mapper).append('\n');
            session.trace(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<DirectoryVersion> synchronizer = new OptimizingDirectorySynchronizer(session, mapper);
        IntermediateSyncResult<DirectoryVersion> syncResult = synchronizer.sync();
        if (session.isTraceEnabled()) {
            session.trace(syncResult);
        }
        return syncResult;
    }

    private static IntermediateSyncResult<FileVersion> syncFiles(SyncSession session, String path, List<? extends FileVersion> originalVersions,
        List<? extends FileVersion> clientVersions, List<? extends FileVersion> serverVersions) throws OXException {
        /*
         * map files
         */
        List<FilePattern> fileExclusions = session.getDriveSession().getFileExclusions();
        FileVersionMapper mapper;
        if (null == fileExclusions || 0 == fileExclusions.size()) {
            mapper = new FileVersionMapper(originalVersions, clientVersions, serverVersions);
        } else {
            mapper = new FilteringFileVersionMapper(path, fileExclusions, originalVersions, clientVersions, serverVersions);
        }
        if (session.isTraceEnabled()) {
            StringBuilder allocator = new StringBuilder("File versions in directory " + path + " mapped to:\n");
            allocator.append(mapper).append('\n');
            session.trace(allocator);
        }
        /*
         * determine sync actions
         */
        Synchronizer<FileVersion> synchronizer = new OptimizingFileSynchronizer(session, mapper, path);
        IntermediateSyncResult<FileVersion> syncResult = synchronizer.sync();
        if (session.isTraceEnabled()) {
            session.trace(syncResult);
        }
        return syncResult;
    }

    private static boolean tryAgain(OXException e) {
        if (null == e) {
            return false;
        }
        return Category.CATEGORY_TRY_AGAIN.equals(e.getCategory()) ||
            Category.CATEGORY_CONFLICT.equals(e.getCategory()) ||
            "FLD-0008".equals(e.getErrorCode()) || // 'Folder 123 does not exist in context 1'
            "DRV-0007".equals(e.getErrorCode()) // The file "123.txt" with checksum "8fc1a2f5e9a2dbd1d5f4f9e330bd1563" was not found at "/"
        ;
    }

    private static void delay(long millis) throws OXException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OXException(e);
        }
    }

    @Override
    public String getJumpRedirectUrl(DriveSession session, String path, String fileName, String method) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        DriveStorage storage = syncSession.getStorage();
        String folderId = null;
        String fileId = null;
        String mimeType = null;
        if (null == fileName) {
            folderId = storage.getFolderID(path);
        } else {
            File file = storage.getFileByName(path, fileName);
            if (null == file) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create(fileName, path);
            }
            folderId = file.getFolderId();
            fileId = file.getId();
            mimeType = DriveUtils.determineMimeType(file);
        }
        return new JumpLinkGenerator(syncSession).getJumpLink(folderId, fileId, method, mimeType);
    }

    @Override
    public DriveUtility getUtility() {
        return DriveUtilityImpl.getInstance();
    }

}
