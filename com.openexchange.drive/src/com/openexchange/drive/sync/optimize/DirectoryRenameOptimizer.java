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
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.AcknowledgeDirectoryAction;
import com.openexchange.drive.actions.EditDirectoryAction;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.IntermediateSyncResult;


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
    public IntermediateSyncResult<DirectoryVersion> optimize(DriveSession session, IntermediateSyncResult<DirectoryVersion> result) {
        List<AbstractAction<DirectoryVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(result.getActionsForClient());
        List<AbstractAction<DirectoryVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<DirectoryVersion>>(result.getActionsForServer());
        List<AbstractAction<DirectoryVersion>> renameActionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>();
        List<AbstractAction<DirectoryVersion>> renameActionsForServer = new ArrayList<AbstractAction<DirectoryVersion>>();
        /*
         * Move of subfolder at client: check for client ACKNOWLEDGE / client SYNC / server REMOVE of identical version
         */
        for (AbstractAction<DirectoryVersion> serverAction : result.getActionsForServer()) {
            /*
             * check each server REMOVE caused by non-conflicting client-deletion
             * REMOVE [version=/vorher | d41d8cd98f00b204e9800998ecf8427e [59408], newVersion=null, parameters={}]
             */
            if (Action.REMOVE.equals(serverAction.getAction()) && serverAction.wasCausedBy(Change.DELETED, Change.NONE)) {
                /*
                 * find matching client ACKNOWLEDGE for the same version
                 * ACKNOWLEDGE [version={"path":"/vorher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, newVersion=null, parameters={}]
                 */
                for (AbstractAction<DirectoryVersion> clientAction : result.getActionsForClient()) {
                    if (Action.ACKNOWLEDGE.equals(clientAction.getAction()) && clientAction.wasCausedBy(Change.DELETED, Change.NONE) &&
                        matchesByPathAndChecksum(clientAction.getVersion(), serverAction.getVersion())) {
                        /*
                         * find best matching client SYNC caused by a new matching directory at client
                         * SYNC [version={"path":"/nachher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, newVersion=null, parameters={}]
                         */
                        AbstractAction<DirectoryVersion> clientSync = findBestMatchingAction(
                            optimizedActionsForClient, Action.SYNC, clientAction.getVersion(), Change.NEW, Change.NONE);
                        if (null != clientSync) {
                            /*
                             * edit server directory instead, acknowledged client move/rename
                             * EDIT [version=/vorher | d41d8cd98f00b204e9800998ecf8427e [59408], newVersion={"path":"/nachher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, parameters={}]
                             * ACKNOWLEDGE [version={"path":"/vorher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, newVersion={"path":"/nachher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, parameters={}]
                             */
                            optimizedActionsForClient.remove(clientSync);
                            optimizedActionsForClient.remove(clientAction);
                            optimizedActionsForServer.remove(serverAction);
                            optimizedActionsForClient.add(new AcknowledgeDirectoryAction(clientAction.getVersion(), clientSync.getVersion(), null));
                            EditDirectoryAction renameDirectoryAction = new EditDirectoryAction(serverAction.getVersion(), clientSync.getVersion(), null);
                            optimizedActionsForServer.add(renameDirectoryAction);
                            renameActionsForServer.add(renameDirectoryAction);
                            /*
                             * restore any nested removes that are no longer valid after rename
                             */
                            if (serverAction.getParameters().containsKey("nestedRemoves")) {
                                restoreNestedRemoves((List<AbstractAction<DirectoryVersion>>)serverAction.getParameters().get("nestedRemoves"),
                                    optimizedActionsForServer);
                            }
                            continue;
                        }
                    }
                }
            }
        }
        /*
         * Move of subfolder at server: check for client REMOVE / client SYNC of identical version
         */
        for (AbstractAction<DirectoryVersion> clientAction : result.getActionsForClient()) {
            /*
             * check each client REMOVE caused by a non-conflicting server remove
             * REMOVE [version=/vorher | d41d8cd98f00b204e9800998ecf8427e [59408], newVersion=null, parameters={}]
             */
            if (Action.REMOVE == clientAction.getAction() && clientAction.wasCausedBy(Change.NONE, Change.DELETED)) {
                /*
                 * check each client SYNC caused by a non-conflicting server creation
                 * SYNC [version={"path":"/nachher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, newVersion=null, parameters={}]
                 */
                AbstractAction<DirectoryVersion> clientSync = findBestMatchingAction(
                    optimizedActionsForClient, Action.SYNC, clientAction.getVersion(), Change.NONE, Change.NEW);
                if (null != clientSync) {
                    /*
                     * edit client directory instead
                     * EDIT [version=/vorher | d41d8cd98f00b204e9800998ecf8427e [59408], newVersion={"path":"/nachher","checksum":"d41d8cd98f00b204e9800998ecf8427e"}, parameters={}]
                     */
                    optimizedActionsForClient.remove(clientAction);
                    optimizedActionsForClient.remove(clientSync);
                    EditDirectoryAction renameDirectoryAction = new EditDirectoryAction(clientAction.getVersion(), clientSync.getVersion(), null);
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
        return new IntermediateSyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient);

    }

    private static int restoreNestedRemoves(List<AbstractAction<DirectoryVersion>> nestedRemoves, List<AbstractAction<DirectoryVersion>> originalActions) {
        int restored = 0;
        if (null != nestedRemoves && 0 < nestedRemoves.size()) {
            for (AbstractAction<DirectoryVersion> nestedRemove : nestedRemoves) {
                if (originalActions.add(nestedRemove)) {
                    restored++;
                }
            }
        }
        return restored;
    }

    private static AbstractAction<DirectoryVersion> findBestMatchingAction(List<AbstractAction<DirectoryVersion>> driveActions,
        Action action, DirectoryVersion version, Change clientChange, Change serverChange) {
        AbstractAction<DirectoryVersion> renameAction = null;
        int similarityScore = 0;
        for (AbstractAction<DirectoryVersion> driveAction : driveActions) {
            if (action.equals(driveAction.getAction()) && matchesByChecksum(version, driveAction.getVersion()) &&
                driveAction.wasCausedBy(clientChange, serverChange)) {
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

    private static List<AbstractAction<DirectoryVersion>> getRedundantRenames(List<AbstractAction<DirectoryVersion>> renameActions) {
        /*
         * sort by new directory path
         */
        Collections.sort(renameActions, new Comparator<AbstractAction<DirectoryVersion>>() {

            @Override
            public int compare(AbstractAction<DirectoryVersion> o1, AbstractAction<DirectoryVersion> o2) {
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
        List<AbstractAction<DirectoryVersion>> effectiveRenames = new ArrayList<AbstractAction<DirectoryVersion>>();
        for (AbstractAction<DirectoryVersion> renameAction : renameActions) {
            String oldPath = renameAction.getVersion().getPath();
            String newPath = renameAction.getNewVersion().getPath();
            boolean redundant = false;
            for (AbstractAction<DirectoryVersion> effectiveRename : effectiveRenames) {
                String renamedOldPath = effectiveRename.getVersion().getPath();
                String renamedNewPath = effectiveRename.getNewVersion().getPath();
                if (oldPath.startsWith(renamedOldPath + '/') && newPath.startsWith(renamedNewPath + '/')) {
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
        List<AbstractAction<DirectoryVersion>> redundantRenames = new ArrayList<AbstractAction<DirectoryVersion>>(renameActions);
        redundantRenames.removeAll(effectiveRenames);
        return redundantRenames;
    }

}
