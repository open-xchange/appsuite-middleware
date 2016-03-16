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
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.AcknowledgeFileAction;
import com.openexchange.drive.impl.actions.DownloadFileAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link EmptyFileOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EmptyFileOptimizer extends FileActionOptimizer {

    public EmptyFileOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
        List<AbstractAction<FileVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForServer());
        /*
         * for client UPLOADs of new files, check for the empty checksum
         */
        for (AbstractAction<FileVersion> clientAction : result.getActionsForClient()) {
            if (Action.UPLOAD == clientAction.getAction() && clientAction.wasCausedBy(Change.NEW, Change.NONE) &&
                null == clientAction.getVersion() && null != clientAction.getNewVersion() &&
                DriveConstants.EMPTY_MD5.equals(clientAction.getNewVersion().getChecksum())) {
                /*
                 * no need to upload, just create file on server and let client update it's metadata
                 */
                String path = (String)clientAction.getParameters().get(DriveAction.PARAMETER_PATH);
                optimizedActionsForClient.remove(clientAction);
                DownloadFileAction serverDownload = new DownloadFileAction(session, null, clientAction.getNewVersion(), null, path, null);
                AcknowledgeFileAction clientAcknowledge = new AcknowledgeFileAction(session, null, clientAction.getNewVersion(), null, path, null);
                clientAcknowledge.setDependingAction(serverDownload);
                optimizedActionsForServer.add(serverDownload);
                optimizedActionsForClient.add(clientAcknowledge);
            }
        }
        /*
         * return new sync result
         */
        return new IntermediateSyncResult<FileVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

}
