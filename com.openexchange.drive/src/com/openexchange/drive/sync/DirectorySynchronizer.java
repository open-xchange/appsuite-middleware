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
    protected void processServerChange(SyncResult<DirectoryVersion> result, Change serverChange, DirectoryVersion originalVersion, DirectoryVersion clientVersion, DirectoryVersion serverVersion) throws OXException {
        switch (serverChange) {
        case DELETED:
            /*
             * deleted on server, delete directory on client, too
             */
            result.addActionForClient(new RemoveDirectoryAction(clientVersion));
            break;
        case MODIFIED:
        case NEW:
            /*
             * new/modified on server, let client synchronize the folder
             */
            result.addActionForClient(new SyncDirectoryAction(serverVersion));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processClientChange(SyncResult<DirectoryVersion> result, Change clientChange, DirectoryVersion originalVersion, DirectoryVersion clientVersion, DirectoryVersion serverVersion) throws OXException {
        switch (clientChange) {
        case DELETED:
            /*
             * deleted on client, delete on server, too, let client remove it's metadata
             */
            result.addActionForServer(new RemoveDirectoryAction(serverVersion));
            result.addActionForClient(new AcknowledgeDirectoryAction(originalVersion, null));
            break;
        case NEW:
        case MODIFIED:
            /*
             * new/modified on client, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(clientVersion));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processConflictingChange(SyncResult<DirectoryVersion> result, Change clientChange, Change serverChange, DirectoryVersion originalVersion, DirectoryVersion clientVersion, DirectoryVersion serverVersion) throws OXException {
        if (Change.DELETED == serverChange && Change.DELETED == clientChange) {
            /*
             * both deleted, just let client remove it's metadata
             */
            result.addActionForClient(new AcknowledgeDirectoryAction(originalVersion, null));
        } else if ((Change.NEW == clientChange || Change.MODIFIED == clientChange) &&
            (Change.NEW == serverChange || Change.MODIFIED == serverChange)) {
            /*
             * name clash for new/modified directories, check directory content equivalence
             */
            if (Change.NONE.equals(Change.get(clientVersion, serverVersion))) {
                /*
                 * same directory version, let client update it's metadata
                 */
                result.addActionForClient(new AcknowledgeDirectoryAction(originalVersion, clientVersion));
            } else {
                /*
                 * different contents, let client synchronize the directory
                 */
                result.addActionForClient(new SyncDirectoryAction(clientVersion));
            }
        } else if (Change.DELETED == clientChange && (Change.MODIFIED == serverChange || Change.NEW == serverChange)) {
            /*
             * delete-edit conflict, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(serverVersion));
        } else if ((Change.NEW == clientChange || Change.MODIFIED == clientChange) && Change.DELETED == serverChange) {
            /*
             * edit-delete conflict, create on server, let client synchronize the directory
             */
            result.addActionForClient(new SyncDirectoryAction(clientVersion));
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + serverChange + ", Client: " + clientChange);
        }
    }

}
