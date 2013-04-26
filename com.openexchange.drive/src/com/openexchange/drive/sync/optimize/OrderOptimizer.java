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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.Action;
import com.openexchange.drive.actions.DriveAction;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.sync.SyncResult;


/**
 * {@link OrderOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class OrderOptimizer<T extends DriveVersion> implements ActionOptimizer<T> {

    @Override
    public SyncResult<T> optimize(DriveSession session, SyncResult<T> result) {
        List<DriveAction<T>> actionsForClient = result.getActionsForClient();
        List<DriveAction<T>> actionsForServer = result.getActionsForServer();
        Collections.sort(actionsForClient, ACTION_COMPARATOR);
        Collections.sort(actionsForServer, ACTION_COMPARATOR);
        return new SyncResult<T>(actionsForServer, actionsForClient);
    }

    protected static class ActionComparator<T extends DriveVersion> implements Comparator<DriveAction<T>> {

        @Override
        public int compare(DriveAction<T> action1, DriveAction<T> action2) {
            if (null == action1) {
                return null == action2 ? 0 : 1;
            }
            if (null == action2) {
                return null == action1 ? 0 : -1;
            }
            return action1.getAction().compareTo(action2.getAction());
        }
    }


    private static final Comparator<DriveAction<?>> ACTION_COMPARATOR = new Comparator<DriveAction<?>>() {

        @Override
        public int compare(DriveAction<?> action1, DriveAction<?> action2) {
            if (null == action1) {
                return null == action2 ? 0 : 1;
            }
            if (null == action2) {
                return null == action1 ? 0 : -1;
            }
            int result = action1.getAction().compareTo(action2.getAction());
            if (0 == result) {
                /*
                 * compare new directory path / new file name in EDIT actions
                 */
                if (Action.EDIT.equals(action1.getAction()) && Action.EDIT.equals(action2.getAction()) &&
                    null != action1.getNewVersion() && null != action2.getNewVersion()) {
                    if (DirectoryVersion.class.isInstance(action1.getNewVersion())) {
                        return -1 * ((DirectoryVersion)action1.getNewVersion()).getPath().compareTo(
                            ((DirectoryVersion)action2.getNewVersion()).getPath());
                    } else if (FileVersion.class.isInstance(action1.getNewVersion())) {
                        return -1 * ((FileVersion)action1.getNewVersion()).getName().compareTo(
                            ((FileVersion)action2.getNewVersion()).getName());
                    }
                }
            }
            return result;
        }
    };

}
