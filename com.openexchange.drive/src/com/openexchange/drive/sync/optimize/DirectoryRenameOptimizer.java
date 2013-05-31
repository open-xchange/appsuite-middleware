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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.actions.AcknowledgeDirectoryAction;
import com.openexchange.drive.actions.EditDirectoryAction;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.sync.SyncResult;


/**
 * {@link DirectoryRenameOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryRenameOptimizer extends DirectoryActionOptimizer {

    public DirectoryRenameOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    @Override
    public SyncResult<DirectoryVersion> optimize(DriveSession session, SyncResult<DirectoryVersion> result) {
        List<DriveAction<DirectoryVersion>> optimizedActionsForClient = new ArrayList<DriveAction<DirectoryVersion>>(result.getActionsForClient());
        List<DriveAction<DirectoryVersion>> optimizedActionsForServer = new ArrayList<DriveAction<DirectoryVersion>>(result.getActionsForServer());
        List<DriveAction<DirectoryVersion>> renameActionsForClient = new ArrayList<DriveAction<DirectoryVersion>>();
        List<DriveAction<DirectoryVersion>> renameActionsForServer = new ArrayList<DriveAction<DirectoryVersion>>();
        for (DriveAction<DirectoryVersion> clientAction : result.getActionsForClient()) {
            /*
             * Move of subfolder at client: check for client ACKNOWLEDGE / client SYNC / server REMOVE of identical version
             *
             * Client: {"action":"sync","newVersion":null,"version":{"path":"/ANDERS","checksum":"55bd0578618be81d9b4140212e0fae50"}}
             * Client: {"action":"acknowledge","newVersion":null,"version":{"path":"/wf","checksum":"55bd0578618be81d9b4140212e0fae50"}}
             * Server: {"action":"remove","newVersion":null,"version":{"path":"/wf","checksum":"55bd0578618be81d9b4140212e0fae50"}}
             */
            if (Action.SYNC == clientAction.getAction() && false == DriveConstants.ROOT_PATH.equals(clientAction.getVersion().getPath()) &&
                false == wasConflict(clientAction)) {
                DriveAction<DirectoryVersion> matchingServerAction = findMatchingRenameAction(
                    Action.REMOVE, clientAction.getVersion(), optimizedActionsForServer);
                if (null != matchingServerAction) {
                    DriveAction<DirectoryVersion> matchingClientAction = findMatchingRenameAction(
                        Action.ACKNOWLEDGE, clientAction.getVersion(), optimizedActionsForClient);
                    if (null != matchingClientAction) {
                        /*
                         * edit server directory instead
                         */
                        optimizedActionsForClient.remove(clientAction);
                        optimizedActionsForClient.remove(matchingClientAction);
                        optimizedActionsForServer.remove(matchingServerAction);
                        optimizedActionsForClient.add(new AcknowledgeDirectoryAction(matchingClientAction.getVersion(), clientAction.getVersion()));
                        EditDirectoryAction renameDirectoryAction = new EditDirectoryAction(matchingServerAction.getVersion(), clientAction.getVersion());
                        optimizedActionsForServer.add(renameDirectoryAction);
                        renameActionsForServer.add(renameDirectoryAction);
                        continue;
                    }
                }
            }
            /*
             * Move of subfolder at server: check for client REMOVE / client SYNC of identical version
             *
             * Client: {"action":"remove","newVersion":null,"version":{"path":"/WIEDER ANDERS","checksum":"55bd0578618be81d9b4140212e0fae50"}}
             * Client: {"action":"sync","newVersion":null,"version":{"path":"/jajaja","checksum":"55bd0578618be81d9b4140212e0fae50"}}
             */
            if (Action.SYNC == clientAction.getAction() && false == DriveConstants.ROOT_PATH.equals(clientAction.getVersion().getPath())) {
                DriveAction<DirectoryVersion> matchingClientAction = null;
                for (DriveAction<DirectoryVersion> directoryAction : optimizedActionsForClient) {
                    if (Action.REMOVE == directoryAction.getAction() && null == directoryAction.getNewVersion() &&
                        clientAction.getVersion().getChecksum().equals(directoryAction.getVersion().getChecksum())) {
                        matchingClientAction = directoryAction;
                        break;
                    }
                }
                if (null != matchingClientAction) {
                    /*
                     * edit client directory instead
                     */
                    optimizedActionsForClient.remove(clientAction);
                    optimizedActionsForClient.remove(matchingClientAction);
                    EditDirectoryAction renameDirectoryAction = new EditDirectoryAction(matchingClientAction.getVersion(), clientAction.getVersion());
                    optimizedActionsForClient.add(renameDirectoryAction);
                    renameActionsForClient.add(renameDirectoryAction);
                    continue;
                }
            }
        }
        /*
         * remove redundant rename actions
         */
        if (0 < renameActionsForServer.size()) {
            optimizedActionsForServer.removeAll(getRedundantRenames(renameActionsForServer));
        }
        if (0 < renameActionsForClient.size()) {
            optimizedActionsForClient.removeAll(getRedundantRenames(renameActionsForClient));
        }
        /*
         * return new sync results
         */
        return new SyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

    private DriveAction<DirectoryVersion> findMatchingRenameAction(Action action, DirectoryVersion version, List<DriveAction<DirectoryVersion>> driveActions) {
        DriveAction<DirectoryVersion> renameAction = null;
        int similarityScore = 0;
        for (DriveAction<DirectoryVersion> driveAction : driveActions) {
            if (action.equals(driveAction.getAction()) && matchesByChecksum(version, driveAction.getVersion())) {
                int similarity = calculateSimilarity(version.getPath(), driveAction.getVersion().getPath());
                if (null == renameAction || similarity > similarityScore) {
                    similarityScore = similarity;
                    renameAction = driveAction;
                }
            }
        }
        return renameAction;
    }

    private static int calculateSimilarity(String path1, String path2) {
        if (null == path1) {
            return null == path2 ? Integer.MAX_VALUE : 0;
        } else if (null == path2) {
            return null == path1 ? Integer.MAX_VALUE : 0;
        } else if (path1.equals(path2)) {
            return Integer.MAX_VALUE;
        }
        String[] splitted1 = path1.split("/");
        String[] splitted2 = path2.split("/");
        int score = 0;
        int minLength = Math.min(splitted1.length, splitted2.length);
        for (int i = 0; i < minLength; i++) {
            if (splitted1[i].equals(splitted2[i])) {
                score++;
            }
        }
        for (int i = 1; i <= minLength; i++) {
            if (splitted1[splitted1.length - i].equals(splitted2[splitted2.length - i])) {
                score++;
            }
        }
        return score;
    }

    private static List<DriveAction<DirectoryVersion>> getRedundantRenames(List<DriveAction<DirectoryVersion>> renameActions) {
        /*
         * sort by new directory path
         */
        Collections.sort(renameActions, new Comparator<DriveAction<DirectoryVersion>>() {

            @Override
            public int compare(DriveAction<DirectoryVersion> o1, DriveAction<DirectoryVersion> o2) {
                if (null != o1 && null != o2 && Action.EDIT.equals(o1.getAction()) && Action.EDIT.equals(o2.getAction()) &&
                    null != o1.getNewVersion() && null != o2.getNewVersion()) {
                    return o1.getNewVersion().getPath().compareTo(o2.getNewVersion().getPath());
                } else {
                    return 0;
                }
            }
        });
        /*
         * collect effective renames
         */
        List<DriveAction<DirectoryVersion>> effectiveRenames = new ArrayList<DriveAction<DirectoryVersion>>();
        for (DriveAction<DirectoryVersion> renameAction : renameActions) {
            String oldPath = renameAction.getVersion().getPath();
            String newPath = renameAction.getNewVersion().getPath();
            boolean redundant = false;
            for (DriveAction<DirectoryVersion> effectiveRename : effectiveRenames) {
                String renamedOldPath = effectiveRename.getVersion().getPath();
                String renamedNewPath = effectiveRename.getNewVersion().getPath();
                if (oldPath.startsWith(renamedOldPath) && newPath.startsWith(renamedNewPath)) {
                    String effectiveOldPath = oldPath.substring(renamedOldPath.length());
                    String effectiveNewPath = newPath.substring(renamedNewPath.length());
                    if (effectiveNewPath.equals(effectiveOldPath)) {
                        redundant = true;
                    }
                }
            }
            if (false == redundant) {
                effectiveRenames.add(renameAction);
            }
        }
        /*
         * create complement list
         */
        List<DriveAction<DirectoryVersion>> redundantRenames = new ArrayList<DriveAction<DirectoryVersion>>(renameActions);
        redundantRenames.removeAll(effectiveRenames);
        return redundantRenames;
    }

}
