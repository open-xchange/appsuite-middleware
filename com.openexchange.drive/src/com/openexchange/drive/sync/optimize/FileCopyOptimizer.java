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
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.Action;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.SyncResult;


/**
 * {@link FileCopyOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileCopyOptimizer implements ActionOptimizer<FileVersion> {

    private final VersionMapper<FileVersion> mapper;

    public FileCopyOptimizer(VersionMapper<FileVersion> mapper) {
        super();
        this.mapper = mapper;
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
                ServerFileVersion knownFile = (ServerFileVersion) findByChecksum(mapper.getServerVersions(), clientAction.getNewVersion().getChecksum());
                if (null != knownFile) {
                    /*
                     * no need to upload, just copy file on server and let client update it's metadata
                     */
                    String folderID = (String)clientAction.getParameters().get("folder");
                    optimizedActionsForClient.remove(clientAction);
                    optimizedActionsForServer.add(new DownloadFileAction(knownFile, clientAction.getNewVersion(), folderID, -1));
                    optimizedActionsForClient.add(new AcknowledgeFileAction(clientAction.getVersion(), clientAction.getNewVersion()));

                    if (null != clientAction.getVersion()) {
                        //TODO: replacement of server file in case of client update? DeleteAction?
                    }
                }
            }
        }
        /*
         * return new sync result
         */
        return new SyncResult<FileVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

    private static <T extends DriveVersion> T findByChecksum(Collection<T> versions, String checksum) {
        if (null != versions && 0 < versions.size()) {
            for (T file : versions) {
                if (checksum.equals(file.getChecksum())) {
                    return file;
                }
            }
        }
        return null;
    }
}
