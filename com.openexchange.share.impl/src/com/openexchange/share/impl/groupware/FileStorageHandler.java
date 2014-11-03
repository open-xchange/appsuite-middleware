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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl.groupware;

import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedAdministrativeFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.infostore.PermissionHelper;
import com.openexchange.groupware.container.EffectiveObjectPermissions;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetProxy;


/**
 * {@link FileStorageHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileStorageHandler implements ModuleHandler {

    private final ServiceLookup services;

    public FileStorageHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<TargetProxy> loadTargets(List<ShareTarget> targets, HandlerParameters parameters) throws OXException {
        List<TargetProxy> files = new ArrayList<TargetProxy>(targets.size());
        if (parameters.isAdministrative()) {
            IDBasedAdministrativeFileAccess fileAccess = getAdministrativeFileAccess(parameters.getContext());
            for (ShareTarget target : targets) {
                FileID fileID = new FileID(target.getItem());
                if (fileID.getFolderId() == null) {
                    fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                }
                File file = fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
                files.add(new FileTargetProxy(file));
            }
        } else {
            IDBasedFileAccess fileAccess = getFileAccess(parameters.getSession());
            for (ShareTarget target : targets) {
                FileID fileID = new FileID(target.getItem());
                if (fileID.getFolderId() == null) {
                    fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
                }
                File file = fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
                files.add(new FileTargetProxy(file));
            }
        }

        return files;
    }

    @Override
    public TargetProxy loadTarget(ShareTarget target, Session session) throws OXException {
        IDBasedFileAccess fileAccess = getFileAccess(session);
        FileID fileID = new FileID(target.getItem());
        if (fileID.getFolderId() == null) {
            fileID.setFolderId(new FolderID(target.getFolder()).getFolderId());
        }

        return new FileTargetProxy(fileAccess.getFileMetadata(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION));
    }

    @Override
    public boolean canShare(boolean canShareInFolder, TargetProxy proxy, HandlerParameters parameters) {
        File file = ((FileTargetProxy) proxy).getFile();
        List<ObjectPermission> objectPermissions = PermissionHelper.getObjectPermissions(file.getObjectPermissions());
        if (objectPermissions == null || objectPermissions.isEmpty()) {
            return canShareInFolder;
        }

        ObjectPermission objectPermission = EffectiveObjectPermissions.find(parameters.getUser(), objectPermissions);
        if (objectPermission == null) {
            return canShareInFolder;
        }

        return objectPermission.canWrite();
    }

    @Override
    public void updateObjects(List<TargetProxy> modified, HandlerParameters parameters) throws OXException {
        if (parameters.isAdministrative()) {
            IDBasedAdministrativeFileAccess fileAccess = getAdministrativeFileAccess(parameters.getContext());
            for (TargetProxy proxy : modified) {
                File file = ((FileTargetProxy) proxy).getFile();
                fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
            }
        } else {
            IDBasedFileAccess fileAccess = getFileAccess(parameters.getSession());
            try {
                fileAccess.startTransaction();
                for (TargetProxy proxy : modified) {
                    File file = ((FileTargetProxy) proxy).getFile();
                    fileAccess.saveFileMetadata(file, file.getLastModified().getTime(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }

                fileAccess.commit();
            } catch (OXException e) {
                fileAccess.rollback();
                throw e;
            } finally {
                fileAccess.finish();
            }
        }
    }

    private IDBasedAdministrativeFileAccess getAdministrativeFileAccess(Context context) throws OXException {
        IDBasedFileAccessFactory factory = requireService(IDBasedFileAccessFactory.class, services);
        return factory.createAccess(context.getContextId());
    }

    private IDBasedFileAccess getFileAccess(Session session) throws OXException {
        IDBasedFileAccessFactory factory = requireService(IDBasedFileAccessFactory.class, services);
        return factory.createAccess(session);
    }

}
