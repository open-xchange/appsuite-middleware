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

package com.openexchange.drive.sync.optimize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.Action;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.SyncResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;


/**
 * {@link FileCopyOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileCopyOptimizer extends FileActionOptimizer {

    public FileCopyOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public SyncResult<FileVersion> optimize(DriveSession session, SyncResult<FileVersion> result) {
        List<DriveAction<FileVersion>> optimizedActionsForClient = new ArrayList<DriveAction<FileVersion>>(result.getActionsForClient());
        List<DriveAction<FileVersion>> optimizedActionsForServer = new ArrayList<DriveAction<FileVersion>>(result.getActionsForServer());
        for (DriveAction<FileVersion> clientAction : result.getActionsForClient()) {
            /*
             * for client UPLOADs, check if file already known on server
             */
            if (Action.UPLOAD == clientAction.getAction()) {
                ServerFileVersion knownFile = findByChecksum(session, clientAction.getNewVersion().getChecksum());
                if (null != knownFile) {
                    /*
                     * no need to upload, just copy file on server and let client update it's metadata
                     */
                    String path = (String)clientAction.getParameters().get("path");
                    optimizedActionsForClient.remove(clientAction);
                    DownloadFileAction copyAction = new DownloadFileAction(
                        clientAction.getVersion(), clientAction.getNewVersion(), path, -1);
                    copyAction.getParameters().put("sourceVersion", knownFile);
                    optimizedActionsForServer.add(copyAction);
                    optimizedActionsForClient.add(new AcknowledgeFileAction(clientAction.getVersion(), clientAction.getNewVersion(),
                        (String)clientAction.getParameters().get("path")));
                }
            }
        }
        /*
         * return new sync result
         */
        return new SyncResult<FileVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

    private ServerFileVersion findByChecksum(DriveSession session, String checksum) {
        /*
         * check server file versions known by mapper
         */
        Collection<? extends FileVersion> versions = mapper.getServerVersions();
        if (null != versions && 0 < versions.size()) {
            for (FileVersion version : versions) {
                if (checksum.equals(version.getChecksum()) && ServerFileVersion.class.isInstance(version)) {
                    return (ServerFileVersion) version;
                }
            }
        }
        /*
         * check files known by checksum store
         */
        try {
            List<FileChecksum> fileChecksums = session.getChecksumStore().getMatchingFileChecksums(checksum);
            for (FileChecksum fileChecksum : fileChecksums) {
                File storageFile = null;
                try {
                    String path = session.getStorage().getPath(fileChecksum.getFolderID());
                    storageFile = session.getStorage().getFile(path, fileChecksum.getFileID(), fileChecksum.getVersion());
                } catch (OXException e) {
                    LOG.debug("Error accessing file referenced by checksum store", e);
                }
                if (null == storageFile || storageFile.getSequenceNumber() != fileChecksum.getSequenceNumber()) {
                    LOG.debug("Invalidating stored file checksum: " + fileChecksum);
                    session.getChecksumStore().removeFileChecksum(fileChecksum);
                } else {
                    LOG.debug("Found matching file in storage for stored checksum: " + storageFile);
                    return new ServerFileVersion(storageFile, fileChecksum);
                }
            }
        } catch (OXException e) {
            LOG.warn("unexpected error during file lookup by checksum", e);
        }
        /*
         * not found
         */
        return null;
    }

}
