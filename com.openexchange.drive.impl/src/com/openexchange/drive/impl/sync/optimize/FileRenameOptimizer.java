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

package com.openexchange.drive.impl.sync.optimize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.AcknowledgeFileAction;
import com.openexchange.drive.impl.actions.EditFileAction;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.drive.impl.sync.RenameTools;
import com.openexchange.drive.impl.sync.SimpleFileVersion;


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
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
        List<AbstractAction<FileVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForServer());
        for (AbstractAction<FileVersion> clientAction : result.getActionsForClient()) {
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

                AbstractAction<FileVersion> matchingServerAction = null;
                for (AbstractAction<FileVersion> fileAction : optimizedActionsForServer) {
                    if (Action.REMOVE == fileAction.getAction() && matchesByChecksum(clientAction.getNewVersion(), fileAction.getVersion())) {
                        matchingServerAction = fileAction;
                        break;
                    }
                }
                if (null != matchingServerAction) {
                    AbstractAction<FileVersion> matchingClientAction = null;
                    for (AbstractAction<FileVersion> fileAction : optimizedActionsForClient) {
                        if (Action.ACKNOWLEDGE == fileAction.getAction() &&
                            matchesByNameAndChecksum(matchingServerAction.getVersion(), fileAction.getVersion())) {
                            matchingClientAction = fileAction;
                            break;
                        }
                    }
                    if (null != matchingClientAction) {
                        String path = (String)clientAction.getParameters().get(DriveAction.PARAMETER_PATH);
                        /*
                         * edit server file instead
                         */
                        optimizedActionsForClient.remove(clientAction);
                        optimizedActionsForClient.remove(matchingClientAction);
                        optimizedActionsForServer.remove(matchingServerAction);
                        EditFileAction serverEdit = new EditFileAction(matchingServerAction.getVersion(), clientAction.getNewVersion(), null, path);
                        if (null != clientAction.getVersion()) {
                            // old version will be overwritten
                            serverEdit.getParameters().put("targetVersion", clientAction.getVersion());
                        }
                        AcknowledgeFileAction clientAcknowledge = new AcknowledgeFileAction(
                            session, matchingClientAction.getVersion(), clientAction.getNewVersion(), null, path, null);
                        clientAcknowledge.setDependingAction(serverEdit);
                        optimizedActionsForClient.add(clientAcknowledge);
                        optimizedActionsForServer.add(serverEdit);
                    }
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
                        String path = (String)clientAction.getParameters().get(DriveAction.PARAMETER_PATH);
                        /*
                         * remove both client UPLOAD actions
                         */
                        optimizedActionsForClient.remove(clientAction);
                        optimizedActionsForClient.remove(matchingAction);
                        /*
                         * merge into corresponding rename actions for server
                         */
                        FileVersion renamedVersion = getRenamedVersion(session, clientAction.getVersion());
                        optimizedActionsForServer.add(new EditFileAction(clientActionServerVersion, renamedVersion, null, path, 1));
                        EditFileAction serverEdit1 = new EditFileAction(matchingActionServerVersion, clientActionServerVersion, null, path, 2);
                        optimizedActionsForServer.add(serverEdit1);
                        EditFileAction serverEdit2 = new EditFileAction(renamedVersion, matchingActionServerVersion, null, path, 3);
                        optimizedActionsForServer.add(serverEdit2);
                        /*
                         * acknowledge client renames
                         */
                        AcknowledgeFileAction clientAcknowledge1 = new AcknowledgeFileAction(
                            session, clientAction.getVersion(), clientAction.getNewVersion(), null, path, null);
                        clientAcknowledge1.setDependingAction(serverEdit1);
                        AcknowledgeFileAction clientAcknowledge2 = new AcknowledgeFileAction(
                            session, matchingAction.getVersion(), matchingAction.getNewVersion(), null, path, null);
                        clientAcknowledge2.setDependingAction(serverEdit2);
                        optimizedActionsForClient.add(clientAcknowledge1);
                        optimizedActionsForClient.add(clientAcknowledge2);
                    }
                }
            }
        }
        /*
         * optimize common rename operations and return results
         */
        return new IntermediateSyncResult<FileVersion>(optimizeRenames(optimizedActionsForServer), optimizeRenames(optimizedActionsForClient));
    }

    private List<AbstractAction<FileVersion>> optimizeRenames(List<AbstractAction<FileVersion>> actions) {
        /*
         * analyze actions
         */
        List<AbstractAction<FileVersion>> optimizedList = new ArrayList<AbstractAction<FileVersion>>(actions);
        for (AbstractAction<FileVersion> action : actions) {
            /*
             * check for DELETE + DOWNLOAD of identical file
             */
            if (Action.REMOVE.equals(action.getAction())) {
                AbstractAction<FileVersion> matchingAction = null;
                for (AbstractAction<FileVersion> fileAction : optimizedList) {
                    if (Action.DOWNLOAD == fileAction.getAction() && false == isDriveMeta(fileAction.getNewVersion()) &&
                        matchesByChecksum(action.getVersion(), fileAction.getNewVersion())) {
                        matchingAction = fileAction;
                        break;
                    }
                }
                if (null != matchingAction) {
                    String path = (String)matchingAction.getParameters().get(DriveAction.PARAMETER_PATH);
                    /*
                     * remove DELETE + DOWNLOAD action
                     */
                    optimizedList.remove(action);
                    optimizedList.remove(matchingAction);
                    /*
                     * merge into corresponding edit action
                     */
                    optimizedList.add(new EditFileAction(action.getVersion(), matchingAction.getNewVersion(), null, path));
                }
            }
        }
        return optimizedList;
    }

    private FileVersion getRenamedVersion(SyncSession session, FileVersion conflictingVersion) {
        String alternativeName = RenameTools.findAlternativeName(conflictingVersion.getName(), usedFilenames, session.getDeviceName());
        if (null != usedFilenames) {
            usedFilenames.add(alternativeName);
        }
        return new SimpleFileVersion(alternativeName, conflictingVersion.getChecksum());
    }

    private static FileVersion findByNameAndChecksum(String name, String checksum, Collection<? extends FileVersion> fileVersions) {
        if (null != fileVersions && 0 < fileVersions.size()) {
            for (FileVersion fileVersion : fileVersions) {
                if (name.equals(fileVersion.getName()) && checksum.equals(fileVersion.getChecksum()) && false == isDriveMeta(fileVersion)) {
                    return fileVersion;
                }
            }
        }
        return null;
    }

}
