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

import java.util.Collections;
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.EditDirectoryAction;
import com.openexchange.drive.impl.actions.RemoveDirectoryAction;
import com.openexchange.drive.impl.actions.SyncDirectoryAction;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.drive.impl.sync.SimpleDirectoryVersion;


/**
 * {@link DirectoryOrderOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryOrderOptimizer extends DirectoryActionOptimizer {

    public DirectoryOrderOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> optimize(SyncSession session, IntermediateSyncResult<DirectoryVersion> result) {
        List<AbstractAction<DirectoryVersion>> actionsForClient = result.getActionsForClient();
        List<AbstractAction<DirectoryVersion>> actionsForServer = result.getActionsForServer();
        Collections.sort(actionsForClient);
        Collections.sort(actionsForServer);
        actionsForClient = propagateRenames(actionsForClient);
        actionsForServer = propagateRenames(actionsForServer);
        return new IntermediateSyncResult<DirectoryVersion>(actionsForServer, actionsForClient);
    }

    private static List<AbstractAction<DirectoryVersion>> propagateRenames(List<AbstractAction<DirectoryVersion>> actions) {
        /*
         * propagate previous rename operations
         */
        for (int i = 0; i < actions.size(); i++) {
            if (Action.EDIT.equals(actions.get(i).getAction()) && null != actions.get(i).getVersion() && null != actions.get(i).getNewVersion()) {
                String oldPath = actions.get(i).getVersion().getPath();
                String newPath = actions.get(i).getNewVersion().getPath();
                for (int j = i + 1; j < actions.size(); j++) {
                    if (Action.EDIT.equals(actions.get(j).getAction()) &&
                        actions.get(j).getVersion().getPath().startsWith(oldPath + DriveConstants.PATH_SEPARATOR)) {
                        String newOldPath = newPath + actions.get(j).getVersion().getPath().substring(oldPath.length());
                        DirectoryVersion modifiedOldVersion = new SimpleDirectoryVersion(newOldPath, actions.get(j).getVersion().getChecksum());
                        actions.set(j, new EditDirectoryAction(modifiedOldVersion, actions.get(j).getNewVersion(), actions.get(j).getComparison()));
                    } else if (Action.REMOVE.equals(actions.get(j).getAction()) &&
                        actions.get(j).getVersion().getPath().startsWith(oldPath + DriveConstants.PATH_SEPARATOR)) {
                        String newOldPath = newPath + actions.get(j).getVersion().getPath().substring(oldPath.length());
                        DirectoryVersion modifiedOldVersion = new SimpleDirectoryVersion(newOldPath, actions.get(j).getVersion().getChecksum());
                        actions.set(j, new RemoveDirectoryAction(modifiedOldVersion, actions.get(j).getComparison()));
                    } else if (Action.SYNC.equals(actions.get(j).getAction()) &&
                        actions.get(j).getVersion().getPath().startsWith(oldPath + DriveConstants.PATH_SEPARATOR)) {
                        String newOldPath = newPath + actions.get(j).getVersion().getPath().substring(oldPath.length());
                        DirectoryVersion modifiedOldVersion = new SimpleDirectoryVersion(newOldPath, actions.get(j).getVersion().getChecksum());
                        actions.set(j, new SyncDirectoryAction(modifiedOldVersion, actions.get(j).getComparison()));
                    }
                }
            }
        }
        return actions;
    }

}
