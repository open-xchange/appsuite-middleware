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

package com.openexchange.file.storage.json.actions.files;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.Entities.PermissionType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * {@link NotifyAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NotifyAction extends AbstractWriteAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        /*
         * get file to re-send notifications for
         */
        request.require(Param.ID);
        IDBasedFileAccess fileAccess = request.getFileAccess();
        File metadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());
        /*
         * get notification parameters
         */
        Entities entities = filterEntities(request.getEntities(), metadata.getObjectPermissions());
        Transport transport = request.getNotificationTransport();
        if (null == transport) {
            transport = Transport.MAIL;
        }
        String message = request.getNotifiactionMessage();
        /*
         * send notification(s)
         */
        ShareNotificationService notificationService = Services.getShareNotificationService();
        if (null == notificationService) {
            throw ServiceExceptionCode.absentService(ShareNotificationService.class);
        }
        ShareTargetPath targetPath = new ShareTargetPath(8, metadata.getFolderId(), metadata.getId());
        List<OXException> warnings = notificationService.sendShareNotifications(
            transport, entities, message, targetPath, request.getSession(), request.getRequestData().getHostData());
        /*
         * return empty response in case of success, including any warnings that occurred during notification transport
         */
        AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), "json");
        result.addWarnings(warnings);
        return result;
    }

    private static Entities filterEntities(List<Integer> entityIDs, List<FileStorageObjectPermission> permissions) throws OXException {
        Entities entities = new Entities();
        for (Integer entityID : entityIDs) {
            FileStorageObjectPermission matchingPermission = null;
            if (null != permissions) {
                for (FileStorageObjectPermission permission : permissions) {
                    if (permission.getEntity() == entityID.intValue()) {
                        matchingPermission = permission;
                        break;
                    }
                }
            }
            if (null == matchingPermission) {
                throw OXException.notFound(entityID.toString());
            }
            if (matchingPermission.isGroup()) {
                entities.addGroup(matchingPermission.getEntity(), PermissionType.OBJECT, matchingPermission.getPermissions());
            } else {
                entities.addUser(matchingPermission.getEntity(), PermissionType.OBJECT, matchingPermission.getPermissions());
            }
        }
        return entities;
    }

}
