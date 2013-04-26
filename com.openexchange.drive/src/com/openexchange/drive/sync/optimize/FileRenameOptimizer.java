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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.Action;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.FileSynchronizer;
import com.openexchange.drive.sync.SyncResult;


/**
 * {@link FileRenameOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileRenameOptimizer extends FileActionOptimizer {

    private final Set<String> usedFilenames;

    public FileRenameOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
        usedFilenames = new HashSet<String>(mapper.getKeys());
    }

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
             *
             *
             * Actions for server:
  REMOVE [version=b.zip | 8238068f4d31f33f654c623b776f5f25 [29660/20361], newVersion=null, parameters={path=/}]
Actions for client:
  UPLOAD [version=a.zip | dc7a0740fdd1bddebf5bd30ed8499b90 [29660/20360], newVersion={"name":"a.zip","checksum":"8238068f4d31f33f654c623b776f5f25"}, parameters={path=/, offset=0}]
  ACKNOWLEDGE [version={"name":"b.zip","checksum":"8238068f4d31f33f654c623b776f5f25"}, newVersion=null, parameters={path=/}]

             *
             */
            if (Action.UPLOAD == clientAction.getAction()) {

                DriveAction<FileVersion> matchingServerAction = null;
                for (DriveAction<FileVersion> fileAction : optimizedActionsForServer) {
                    if (Action.REMOVE == fileAction.getAction() && matchesByChecksum(clientAction.getNewVersion(), fileAction.getVersion())) {
                        matchingServerAction = fileAction;
                        break;
                    }
                }
                if (null != matchingServerAction) {
                    DriveAction<FileVersion> matchingClientAction = null;
                    for (DriveAction<FileVersion> fileAction : optimizedActionsForClient) {
                        if (Action.ACKNOWLEDGE == fileAction.getAction() &&
                            matchesByNameAndChecksum(matchingServerAction.getVersion(), fileAction.getVersion())) {
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
                    EditFileAction editAction = new EditFileAction(matchingServerAction.getVersion(), clientAction.getNewVersion());
                    if (null != clientAction.getVersion()) {
                        // old version will be overwritten
                        editAction.getParameters().put("targetVersion", clientAction.getVersion());
                    }
                    optimizedActionsForServer.add(editAction);
                }
            }
            /*
             * special check for 'crossover' renames initiated by client:
             *
             * UPLOAD [version=hund.7z | dfd1e0e6daa0951ccaddef98f2c72b56 [29660/20331], newVersion={"name":"hund.7z","checksum":"af77e645c766a6461f5adb668549ec3f"}, parameters={path=/, offset=0}]
             * UPLOAD [version=katze.7z | af77e645c766a6461f5adb668549ec3f [29660/20332], newVersion={"name":"katze.7z","checksum":"dfd1e0e6daa0951ccaddef98f2c72b56"}, parameters={path=/, offset=0}]
             */
            if (Action.UPLOAD.equals(clientAction.getAction()) && matchesByName(clientAction.getVersion(), clientAction.getNewVersion())) {
                DriveAction<FileVersion> matchingAction = null;
                for (DriveAction<FileVersion> fileAction : optimizedActionsForClient) {
                    if (Action.UPLOAD == fileAction.getAction() &&
                        matchesByName(fileAction.getVersion(), fileAction.getNewVersion()) &&
                        matchesByChecksum(clientAction.getVersion(), fileAction.getNewVersion()) &&
                        matchesByChecksum(clientAction.getNewVersion(), fileAction.getVersion())) {
                        matchingAction = fileAction;
                        break;
                    }
                }
                if (null != matchingAction) {
                    /*
                     * lookup original versions on server
                     */
                    FileVersion clientActionServerVersion = findByNameAndChecksum(
                        clientAction.getVersion().getName(), clientAction.getVersion().getChecksum(), mapper.getServerVersions());
                    FileVersion matchingActionServerVersion = findByNameAndChecksum(
                        matchingAction.getVersion().getName(), matchingAction.getVersion().getChecksum(), mapper.getServerVersions());
                    if (null != clientActionServerVersion && null != matchingActionServerVersion) {
                        /*
                         * remove both client UPLOAD actions
                         */
                        optimizedActionsForClient.remove(clientAction);
                        optimizedActionsForClient.remove(matchingAction);
                        /*
                         * merge into corresponding rename actions for server
                         */
                        String tempName = FileSynchronizer.findAlternativeName(clientAction.getVersion().getName(), usedFilenames);
                        usedFilenames.add(tempName);
                        SimpleFileVersion tempVersion = new SimpleFileVersion(tempName, clientAction.getVersion().getChecksum());
                        optimizedActionsForServer.add(new EditFileAction(clientActionServerVersion, tempVersion, 1));
                        optimizedActionsForServer.add(new EditFileAction(matchingActionServerVersion, clientActionServerVersion, 2));
                        optimizedActionsForServer.add(new EditFileAction(tempVersion, matchingActionServerVersion, 3));
                        /*
                         * acknowledge client renames
                         */
                        optimizedActionsForClient.add(new AcknowledgeFileAction(
                            clientAction.getVersion(), clientAction.getNewVersion(), (String)clientAction.getParameters().get("path")));
                        optimizedActionsForClient.add(new AcknowledgeFileAction(
                            matchingAction.getVersion(), matchingAction.getNewVersion(), (String)matchingAction.getParameters().get("path")));
                    }
                }
            }
        }
        /*
         * optimize common rename operations and return results
         */
        return new SyncResult<FileVersion>(optimizeRenames(optimizedActionsForServer), optimizeRenames(optimizedActionsForClient));
    }

    private List<DriveAction<FileVersion>> optimizeRenames(List<DriveAction<FileVersion>> actions) {
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
                    if (Action.DOWNLOAD == fileAction.getAction() && matchesByChecksum(action.getVersion(), fileAction.getNewVersion())) {
                        matchingAction = fileAction;
                        break;
                    }
                }
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

    private static FileVersion findByNameAndChecksum(String name, String checksum, Collection<? extends FileVersion> fileVersions) {
        if (null != fileVersions && 0 < fileVersions.size()) {
            for (FileVersion fileVersion : fileVersions) {
                if (name.equals(fileVersion.getName()) && checksum.equals(fileVersion.getChecksum())) {
                    return fileVersion;
                }
            }
        }
        return null;
    }

}
