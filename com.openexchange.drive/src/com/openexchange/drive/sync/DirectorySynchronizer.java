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

package com.openexchange.drive.sync;

import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.actions.AcknowledgeDirectoryAction;
import com.openexchange.drive.actions.RemoveDirectoryAction;
import com.openexchange.drive.actions.SyncDirectoryAction;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.ThreeWayComparison;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.exception.OXException;


/**
 * {@link DirectorySynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectorySynchronizer extends Synchronizer<DirectoryVersion>{

    public DirectorySynchronizer(DriveSession session, VersionMapper<DirectoryVersion> mapper) throws OXException {
        super(session, mapper);
    }

    @Override
    protected void processServerChange(SyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        switch (comparison.getServerChange()) {
        case DELETED:
            /*
             * deleted on server, delete directory on client, too
             */
            result.addActionForClient(new RemoveDirectoryAction(comparison.getClientVersion(), comparison));
            break;
        case MODIFIED:
        case NEW:
            /*
             * new/modified on server, let client synchronize the folder
             */
            result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processClientChange(SyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        switch (comparison.getClientChange()) {
        case DELETED:
            /*
             * deleted on client, delete on server, too, let client remove it's metadata
             */
            result.addActionForServer(new RemoveDirectoryAction(comparison.getServerVersion(), comparison));
            result.addActionForClient(new AcknowledgeDirectoryAction(comparison.getOriginalVersion(), null, comparison));
            break;
        case NEW:
        case MODIFIED:
            /*
             * new/modified on client, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processConflictingChange(SyncResult<DirectoryVersion> result, ThreeWayComparison<DirectoryVersion> comparison) throws OXException {
        if (Change.DELETED == comparison.getServerChange() && Change.DELETED == comparison.getClientChange()) {
            /*
             * both deleted, just let client remove it's metadata
             */
            result.addActionForClient(new AcknowledgeDirectoryAction(comparison.getOriginalVersion(), null, comparison));
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) &&
            (Change.NEW == comparison.getServerChange() || Change.MODIFIED == comparison.getServerChange())) {
            /*
             * name clash for new/modified directories, check directory content equivalence
             */
            if (Change.NONE.equals(Change.get(comparison.getClientVersion(), comparison.getServerVersion()))) {
                /*
                 * same directory version, let client update it's metadata
                 */
                result.addActionForClient(new AcknowledgeDirectoryAction(comparison.getOriginalVersion(), comparison.getClientVersion(), comparison));
            } else {
                /*
                 * different contents, let client synchronize the directory
                 */
                result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
            }
        } else if (Change.DELETED == comparison.getClientChange() && (Change.MODIFIED == comparison.getServerChange() || Change.NEW == comparison.getServerChange())) {
            /*
             * delete-edit conflict, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(comparison.getServerVersion(), comparison));
        } else if ((Change.NEW == comparison.getClientChange() || Change.MODIFIED == comparison.getClientChange()) && Change.DELETED == comparison.getServerChange()) {
            /*
             * edit-delete conflict, create on server, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(comparison.getClientVersion(), comparison));
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + comparison.getServerChange() + ", Client: " + comparison.getClientChange());
        }
    }

}
