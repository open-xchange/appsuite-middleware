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
import java.util.List;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.Action;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.SyncResult;


/**
 * {@link FileRenameOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileRenameOptimizer implements ActionOptimizer<FileVersion> {

    @Override
    public SyncResult<FileVersion> optimize(DriveSession session, SyncResult<FileVersion> result) {
        List<DriveAction<FileVersion>> optimizedActionsForClient = new ArrayList<DriveAction<FileVersion>>(result.getActionsForClient());
        List<DriveAction<FileVersion>> optimizedActionsForServer = new ArrayList<DriveAction<FileVersion>>(result.getActionsForServer());
        for (DriveAction<FileVersion> clientAction : result.getActionsForClient()) {
            /*
             * check for client UPLOAD / server REMOVE / client ACKNOWLEDGE of identical file
             *
             * Client: UPLOAD[file=null, newFile={"name":"b.msi","checksum":"152b95f026e5dbe6c258064d49b25e38"}, parameters={folder=56, offset=0}]
             * Server: REMOVE[file=ServerFileVersion [name=a.msi, checksum=152b95f026e5dbe6c258064d49b25e38], newFile=null, parameters={}]
             * Client: ACKNOWLEDGE[file={"name":"a.msi","checksum":"152b95f026e5dbe6c258064d49b25e38"}, newFile=null, parameters={}]
             */
            if (Action.UPLOAD == clientAction.getAction()) {

                DriveAction<FileVersion> matchingServerAction = null;
                for (DriveAction<FileVersion> fileAction : optimizedActionsForServer) {
                    if (Action.REMOVE == fileAction.getAction() && clientAction.getNewVersion().getChecksum().equals(fileAction.getVersion().getChecksum())) {
                        matchingServerAction = fileAction;
                        break;
                    }
                }
                if (null != matchingServerAction) {

                    DriveAction<FileVersion> matchingClientAction = null;
                    for (DriveAction<FileVersion> fileAction : optimizedActionsForClient) {
                        if (Action.ACKNOWLEDGE == fileAction.getAction() && matchingServerAction.getVersion().getChecksum().equals(fileAction.getVersion().getChecksum()) &&
                            matchingServerAction.getVersion().getName().equals(fileAction.getVersion().getName())) {
                            matchingClientAction = fileAction;
                            break;
                        }
                    }
                    /*
                     * edit server file instead
                     */
                    optimizedActionsForClient.remove(clientAction);
                    optimizedActionsForClient.remove(matchingClientAction);
                    optimizedActionsForClient.add(new AcknowledgeFileAction(matchingClientAction.getVersion(), clientAction.getNewVersion(),
                        (String)clientAction.getParameters().get("path")));
                    optimizedActionsForServer.remove(matchingServerAction);
                    optimizedActionsForServer.add(new EditFileAction(matchingServerAction.getVersion(), clientAction.getNewVersion()));
                }
            }
        }
        /*
         * optimize common rename operations and return results
         */
        return new SyncResult<FileVersion>(optimizeRenames(optimizedActionsForServer), optimizeRenames(optimizedActionsForClient));
    }

    private static List<DriveAction<FileVersion>> optimizeRenames(List<DriveAction<FileVersion>> actions) {
        /*
         * analyze actions
         */
        List<DriveAction<FileVersion>> optimizedList = new ArrayList<DriveAction<FileVersion>>(actions);
        for (DriveAction<FileVersion> action : actions) {
            /*
             * check for DELETE + STORE of identical file
             */
            if (Action.REMOVE.equals(action.getAction())) {
                DriveAction<FileVersion> matchingAction = null;
                for (DriveAction<FileVersion> fileAction : optimizedList) {
                    if (Action.DOWNLOAD == fileAction.getAction() && action.getVersion().getChecksum().equals(fileAction.getNewVersion().getChecksum())) {
                        matchingAction = fileAction;
                        break;
                    }
                }
//                FileAction matchingAction = find(optimizedList, action.getFile().getChecksum(), Action.STORE);
                if (null != matchingAction) {
                    /*
                     * remove DELETE + STORE action
                     */
                    optimizedList.remove(action);
                    optimizedList.remove(matchingAction);
                    /*
                     * merge into corresponding edit action
                     */
                    optimizedList.add(new EditFileAction(action.getVersion(), matchingAction.getNewVersion()));
                }
            }
        }
        return optimizedList;
    }

}
