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
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link FileDelayMetadataDownloadOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileDelayMetadataDownloadOptimizer extends FileActionOptimizer {

    public FileDelayMetadataDownloadOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        /*
         * filter out all DOWNLOAD actions for .drive-meta file in case there are outstanding client-side modifications of the directory
         * contents
         */
        AbstractAction<FileVersion> downloadMetadataAction = null;
        boolean pendingClientChanges = false;
        for (AbstractAction<FileVersion> action : result.getActionsForClient()) {
            if (Action.UPLOAD.equals(action.getAction())) {
                pendingClientChanges = true;
            } else if (Action.DOWNLOAD.equals(action.getAction()) &&
                DriveConstants.METADATA_FILENAME.equals(action.getNewVersion().getName())) {
                downloadMetadataAction = action;
            }
        }
        if (pendingClientChanges && null != downloadMetadataAction) {
            List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
            optimizedActionsForClient.remove(downloadMetadataAction);
            return new IntermediateSyncResult<FileVersion>(result.getActionsForServer(), optimizedActionsForClient);
        } else {
            return result;
        }
    }

}
