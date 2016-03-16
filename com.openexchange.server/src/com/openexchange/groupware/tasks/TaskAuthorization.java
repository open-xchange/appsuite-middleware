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

package com.openexchange.groupware.tasks;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.session.ServerSession;

/**
 * This class implements authorization checks for attachments.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskAuthorization implements AttachmentAuthorization {

    /**
     * Default constructor.
     */
    public TaskAuthorization() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkMayAttach(ServerSession session, int folderId, int taskId) throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        final FolderStorage foldStor = FolderStorage.getInstance();
        try {
            final Task task = storage.selectTask(session.getContext(), taskId, StorageType
                .ACTIVE);
            task.setParentFolderID(folderId);
            final FolderObject folder = Tools.getFolder(session.getContext(), folderId);
            Permission.checkWriteInFolder(session.getContext(), session.getUser(), session.getUserPermissionBits(), folder, task);
            // Check if task appears in folder.
            foldStor.selectFolderById(session.getContext(), taskId, folderId, StorageType
                .ACTIVE);
        } catch (final OXException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkMayDetach(ServerSession session, int folderId, int taskId) throws OXException {
        checkMayAttach(session, folderId, taskId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkMayReadAttachments(ServerSession session, int folderId, int taskId) throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        final FolderObject folder;
        final Task task;
        try {
            folder = Tools.getFolder(session.getContext(), folderId);
            task = storage.selectTask(session.getContext(), taskId, StorageType.ACTIVE);
        } catch (final OXException e) {
            throw e;
        }
        try {
            Permission.canReadInFolder(session.getContext(), session.getUser(), session.getUserPermissionBits(), folder, task);
        } catch (final OXException e) {
            throw e;
        }
    }
}
