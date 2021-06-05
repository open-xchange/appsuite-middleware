/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
