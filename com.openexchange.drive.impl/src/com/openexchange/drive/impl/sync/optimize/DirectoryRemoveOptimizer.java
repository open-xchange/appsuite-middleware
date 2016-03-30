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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link DirectoryRemoveOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryRemoveOptimizer extends DirectoryActionOptimizer {

    public DirectoryRemoveOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> optimize(SyncSession session, IntermediateSyncResult<DirectoryVersion> result) {
        List<AbstractAction<DirectoryVersion>> clientActions = result.getActionsForClient();
        clientActions.removeAll(getRedundantRemoves(clientActions));
        List<AbstractAction<DirectoryVersion>> serverActions = result.getActionsForServer();
        serverActions.removeAll(getRedundantRemoves(serverActions));
        return new IntermediateSyncResult<DirectoryVersion>(serverActions, clientActions);
    }

    private static List<AbstractAction<DirectoryVersion>> getRedundantRemoves(List<AbstractAction<DirectoryVersion>> driveActions) {
        /*
         * get non-conflicting removes
         */
        List<AbstractAction<DirectoryVersion>> removeActions = getNonConflictingRemoves(driveActions);
        /*
         * sort & reverse order so that parent paths are before their children
         */
        Collections.sort(removeActions);
        Collections.reverse(removeActions);
        /*
         * find those removes where the parent directory is already removed
         */
        List<AbstractAction<DirectoryVersion>> redundantRemoves = new ArrayList<AbstractAction<DirectoryVersion>>();
        for (int i = 0; i < removeActions.size(); i++) {
            String prefix = removeActions.get(i).getVersion().getPath() + '/';
            for (int j = i + 1; j < removeActions.size(); j++) {
                if (removeActions.get(j).getVersion().getPath().startsWith(prefix)) {
                    redundantRemoves.add(removeActions.get(j));
                    List<AbstractAction<DirectoryVersion>> nestedRemoves;
                    Map<String, Object> parameters = removeActions.get(i).getParameters();
                    if (parameters.containsKey("nestedRemoves")) {
                        nestedRemoves = (List<AbstractAction<DirectoryVersion>>)removeActions.get(i).getParameters().get("nestedRemoves");
                    } else {
                        nestedRemoves = new ArrayList<AbstractAction<DirectoryVersion>>();
                        parameters.put("nestedRemoves", nestedRemoves);
                    }
                    nestedRemoves.add(removeActions.get(j));
                }
            }
        }
        /*
         * those are redundant
         */
        return redundantRemoves;
    }

    private static List<AbstractAction<DirectoryVersion>> getNonConflictingRemoves(List<AbstractAction<DirectoryVersion>> driveActions) {
        List<AbstractAction<DirectoryVersion>> removeActions = new ArrayList<AbstractAction<DirectoryVersion>>();
        for (AbstractAction<DirectoryVersion> driveAction : driveActions) {
            if (Action.REMOVE.equals(driveAction.getAction()) && (driveAction.wasCausedBy(Change.DELETED, Change.NONE) ||
                driveAction.wasCausedBy(Change.NONE, Change.DELETED) || driveAction.wasCausedBy(Change.DELETED, Change.DELETED))) {
                removeActions.add(driveAction);
            }
        }
        return removeActions;
    }

}
