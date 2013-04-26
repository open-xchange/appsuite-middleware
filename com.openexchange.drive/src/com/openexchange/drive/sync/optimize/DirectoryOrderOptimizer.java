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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.actions.Action;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.actions.EditDirectoryAction;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.SyncResult;


/**
 * {@link DirectoryOrderOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryOrderOptimizer extends OrderOptimizer<DirectoryVersion> {

    @Override
    public SyncResult<DirectoryVersion> optimize(DriveSession session, SyncResult<DirectoryVersion> result) {
        List<DriveAction<DirectoryVersion>> actionsForClient = result.getActionsForClient();
        List<DriveAction<DirectoryVersion>> actionsForServer = result.getActionsForServer();
        actionsForClient = layoutRemoves(actionsForClient);
        actionsForServer = layoutRemoves(actionsForServer);
        actionsForClient = propagateRenames(actionsForClient);
        actionsForServer = propagateRenames(actionsForServer);
        return new SyncResult<DirectoryVersion>(actionsForServer, actionsForClient);
    }

    private static List<DriveAction<DirectoryVersion>> layoutRemoves(List<DriveAction<DirectoryVersion>> actions) {
        /*
         * for removes, ensure that the inner directories are processed before their parents
         */
        return sortByPath(actions, true, Action.REMOVE);
    }

    private static List<DriveAction<DirectoryVersion>> propagateRenames(List<DriveAction<DirectoryVersion>> actions) {
        /*
         * ensure hierarchical tree order
         */
        actions = sortByPath(actions, false, Action.EDIT);
        /*
         * propagate previous rename operations
         */
        List<DriveAction<DirectoryVersion>> modifiedActions = new ArrayList<DriveAction<DirectoryVersion>>();
        Map<String, String> renamedPaths = new HashMap<String, String>();
        for (DriveAction<DirectoryVersion> action : actions) {
            if (Action.EDIT.equals(action.getAction())) {
                for (Entry<String, String> renamedPath : renamedPaths.entrySet()) {
                    if (action.getVersion().getPath().startsWith(renamedPath.getKey())) {
                        String newOldPath = renamedPath.getValue() + action.getVersion().getPath().substring(renamedPath.getKey().length());
                        DirectoryVersion modifiedOldVersion = new ServerDirectoryVersion(
                            newOldPath, ((ServerDirectoryVersion)action.getVersion()).getDirectoryChecksum());
                        action = new EditDirectoryAction(modifiedOldVersion, action.getNewVersion());
                        break;
                    }
                }
                renamedPaths.put(action.getVersion().getPath(), action.getNewVersion().getPath());
            }
            modifiedActions.add(action);
        }
        return modifiedActions;
    }

    private static List<DriveAction<DirectoryVersion>> sortByPath(List<DriveAction<DirectoryVersion>> directoryActions, final boolean reverse, final Action action) {
        Collections.sort(directoryActions, new ActionComparator<DirectoryVersion>() {

            @Override
            public int compare(DriveAction<DirectoryVersion> action1, DriveAction<DirectoryVersion> action2) {
                int result = super.compare(action1, action2);
                if (0 == result && null != action && action.equals(action1.getAction()) && action.equals(action2.getAction()) &&
                    null != action1.getVersion() && null != action2.getVersion()) {
                    result = action1.getVersion().getPath().compareTo(action2.getVersion().getPath());
                }
                return reverse ? -1 * result : result;
            }
        });
        return directoryActions;
    }

}
