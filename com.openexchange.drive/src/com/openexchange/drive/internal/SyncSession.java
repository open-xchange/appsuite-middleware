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

import static com.openexchange.drive.storage.DriveConstants.TEMP_PATH;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jonelo.jacksum.algorithm.MD;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.checksum.DirectoryChecksum;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.storage.DriveStorage;
import com.openexchange.drive.storage.StorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.java.StringAllocator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SyncSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncSession {

    private final DriveSession session;
    private final Tracer tracer;
    private ChecksumStore checksumStore;
    private DriveStorage storage;
    private DirectLinkGenerator linkGenerator;
    private Boolean hasTempFolder;

    /**
     * Initializes a new {@link SyncSession}.
     *
     * @param session The underlying drive session
     */
    public SyncSession(DriveSession session) {
        super();
        this.session = session;
        this.tracer = new Tracer(session.isDiagnostics());
        if (isTraceEnabled()) {
            trace("Creating new sync session for user " + session.getServerSession().getLoginName() + " (" +
                session.getServerSession().getUserId() + ") in context " + session.getServerSession().getContextId() +
                ", root folder ID is " + session.getRootFolderID());
        }
    }

    /**
     * Gets the underlying server session
     *
     * @return The server session
     */
    public ServerSession getServerSession() {
        return session.getServerSession();
    }

    /**
     * Gets the drive storage
     *
     * @return The drive storage
     */
    public DriveStorage getStorage() {
        if (null == storage) {
            storage = new DriveStorage(this);
        }
        return storage;
    }

    public String getRootFolderID() {
        return session.getRootFolderID();
    }

    /**
     * Gets the checksumStore
     *
     * @return The checksumStore
     */
    public ChecksumStore getChecksumStore() throws OXException {
        if (null == checksumStore) {
            checksumStore = new RdbChecksumStore(getServerSession().getContextId());
        }
        return checksumStore;
    }

    /**
     * Creates a new MD5 instance.
     *
     * @return A new MD5 instance.
     * @throws OXException
     */
    public MD newMD5() throws OXException {
        try {
            return new MD("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the device name as supplied by the client.
     *
     * @return The device name, or <code>null</code> if not set
     */
    public String getDeviceName() {
        return session.getDeviceName();
    }

    public DirectLinkGenerator getLinkGenerator() {
        if (null == this.linkGenerator) {
            linkGenerator = new DirectLinkGenerator(this);
        }
        return linkGenerator;
    }

    /**
     * Appends a new line for the supplied message into the trace log.
     *
     * @param message The message to trace
     */
    public void trace(Object message) {
        tracer.trace(message);
    }

    public String getDiagnosticsLog() {
        return tracer.getTraceLog();
    }

    /**
     * Gets a value indicating whether tracing is enabled either in the named logger instance or the drive-internal diagnostics log
     * generator.
     *
     * @return <code>true</code> if tracing is enabled, <code>false</code>, otherwise
     */
    public boolean isTraceEnabled() {
        return tracer.isTraceEnabled();
    }

    /**
     * Gets a value indicating whether the TEMP folder is available for uploads or not. If possible and not yet exists, the folder is
     * created dynamically.
     *
     * @return <code>true</code> if the folder is available, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean hasTempFolder() throws OXException {
        if (null == hasTempFolder) {
            /*
             * check configuration first
             */
            ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
            if (null != configService && false ==
                Boolean.valueOf(configService.getBoolProperty("com.openexchange.drive.useTempFolder", false))) {
                trace("Temporary folder for upload is disabled by configuration.");
                hasTempFolder = Boolean.FALSE;
            } else {
                /*
                 * check temp folder and permissions
                 */
                FileStorageFolder tempFolder = getStorage().optFolder(TEMP_PATH, false);
                if (null == tempFolder) {
                    try {
                        tempFolder = getStorage().optFolder(TEMP_PATH, true);
                    } catch (OXException e) {
                        trace("Error creating temporary folder for uploads: " + e.getMessage());
                    }
                }
                if (null == tempFolder) {
                    trace("No temporary folder available for uploads.");
                    hasTempFolder = Boolean.FALSE;
                } else if (null != tempFolder.getOwnPermission() &&
                    FileStoragePermission.CREATE_OBJECTS_IN_FOLDER <= tempFolder.getOwnPermission().getFolderPermission()) {
                    trace("Using folder '" + tempFolder + "' for temporary uploads.");
                    hasTempFolder = Boolean.TRUE;
                } else {
                    trace("Temporary folder for uploads found, but not enough permissions for current user.");
                    hasTempFolder = Boolean.FALSE;
                }
            }
        }
        return hasTempFolder.booleanValue();
    }

    public List<ServerFileVersion> getServerFiles(String path) throws OXException {
        String folderID = getStorage().getFolderID(path);
        List<File> files = getStorage().getFilesInFolder(folderID);
        List<FileChecksum> checksums = ChecksumProvider.getChecksums(this, folderID, files);
        List<ServerFileVersion> serverFiles = new ArrayList<ServerFileVersion>(files.size());
        for (int i = 0; i < files.size(); i++) {
            serverFiles.add(new ServerFileVersion(files.get(i), checksums.get(i)));
        }
        return serverFiles;
    }

    public List<ServerDirectoryVersion> getServerDirectories() throws OXException {
        final SyncSession syncSession = this;
        return getStorage().wrapInTransaction(new StorageOperation<List<ServerDirectoryVersion>>() {

            @Override
            public List<ServerDirectoryVersion> call() throws OXException {
                StringAllocator stringAllocator = isTraceEnabled() ? new StringAllocator("Server directories:\n") : null;
                Map<String, FileStorageFolder> folders = getStorage().getFolders();
                List<String> folderIDs = new ArrayList<String>(folders.size());
                for (Map.Entry<String, FileStorageFolder> entry : folders.entrySet()) {
                    if (false == DriveConstants.PATH_VALIDATION_PATTERN.matcher(entry.getKey()).matches()) {
                        trace("Skipping invalid server directory: " + entry.getKey());
                    } else {
                        folderIDs.add(entry.getValue().getId());
                    }
                }
                List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(syncSession, folderIDs);
                List<ServerDirectoryVersion> serverDirectories = new ArrayList<ServerDirectoryVersion>(folderIDs.size());
                for (int i = 0; i < folderIDs.size(); i++) {
                    ServerDirectoryVersion directoryVersion = new ServerDirectoryVersion(
                        getStorage().getPath(folderIDs.get(i)), checksums.get(i));
                    serverDirectories.add(directoryVersion);
                    if (isTraceEnabled()) {
                        stringAllocator.append(" [").append(directoryVersion.getDirectoryChecksum().getFolderID()).append("] ")
                            .append(directoryVersion.getPath()).append(" | ").append(directoryVersion.getChecksum())
                            .append(" (").append(directoryVersion.getDirectoryChecksum().getSequenceNumber()).append(")\n");
                    }
                }
                if (isTraceEnabled()) {
                    trace(stringAllocator);
                }
                return serverDirectories;
            }
        });
    }

    @Override
    public String toString() {
        return session.getServerSession().getLoginName() + " [" + session.getServerSession().getContextId() + ':' +
            session.getServerSession().getUserId() + "] # " + session.getRootFolderID();
    }

}
