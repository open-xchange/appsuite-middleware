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

package com.openexchange.folder.json.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.parser.NotificationData;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.Entities.PermissionType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NotifyAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@OAuthAction(OAuthAction.CUSTOM)
public final class NotifyAction extends AbstractFolderAction {

    /** The action identifier */
    public static final String ACTION = "notify";

    /**
     * Initializes a new {@link NotifyAction}.
     */
    public NotifyAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData request, ServerSession session) throws OXException {
        /*
         * parse parameters
         */
        String treeId;
        String folderId;
        NotificationData notificationData;
        List<Integer> entityIDs;
        try {
            treeId = request.getParameter("tree");
            if (null == treeId) {
                /*
                 * Fallback to default tree identifier
                 */
                treeId = getDefaultTreeIdentifier();
            }
            folderId = request.getParameter("id");
            if (null == folderId) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
            }
            JSONObject data = (JSONObject) request.requireData();
            if (data.hasAndNotNull("notification")) {
                notificationData = parseNotificationData(data.optJSONObject("notification"));
            } else {
                notificationData = new NotificationData();
                notificationData.setTransport(Transport.MAIL);
            }
            if (false == data.hasAndNotNull("entities")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("entities");
            }
            JSONArray jsonArray = data.getJSONArray("entities");
            entityIDs = new ArrayList<Integer>();
            for (int i = 0; i < jsonArray.length(); i++) {
                entityIDs.add(Integer.valueOf(jsonArray.getInt(i)));
            }
            if (0 >= entityIDs.size()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("entities");
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * obtain existing folder permissions from folder service
         */
        FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        UserizedFolder folder = folderService.getFolder(treeId, folderId, session, new FolderServiceDecorator());
        if (isOAuthRequest(request) && !mayWriteViaOAuthRequest(folder.getContentType(), getOAuthAccess(request))) {
            throw new OAuthInsufficientScopeException(OAuthContentTypes.writeScopeForContentType(folder.getContentType()));
        }
        /*
         * notify entities
         */
        Entities entities = filterEntities(entityIDs, folder.getPermissions());
        ShareTargetPath targetPath = new ShareTargetPath(folder.getContentType().getModule(), folder.getID(), null);
        ShareNotificationService notificationService = ServiceRegistry.getInstance().getService(ShareNotificationService.class);
        if (null == notificationService) {
            throw ServiceExceptionCode.absentService(ShareNotificationService.class);
        }
        List<OXException> warnings = notificationService.sendShareNotifications(
            notificationData.getTransport(), entities, notificationData.getMessage(), targetPath, session, request.getHostData());
        /*
         * return empty response in case of success, including any warnings that occurred during notification transport
         */
        AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), "json");
        result.addWarnings(warnings);
        return result;
    }

    @OAuthScopeCheck
    public boolean accessAllowed(final AJAXRequestData request, final ServerSession session, final OAuthAccess access) throws OXException {
        String treeId = request.getParameter("tree");
        if (null == treeId) {
            treeId = getDefaultTreeIdentifier();
        }

        final String id = request.getParameter("id");
        if (null == id) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }

        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        ContentType contentType = folderService.getFolder(treeId, id, session, new FolderServiceDecorator()).getContentType();
        return mayWriteViaOAuthRequest(contentType, access);
    }

    private static Entities filterEntities(List<Integer> entityIDs, Permission[] permissions) throws OXException {
        Entities entities = new Entities();
        for (Integer entityID : entityIDs) {
            Permission matchingPermission = null;
            if (null != permissions) {
                for (Permission permission : permissions) {
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
                entities.addGroup(matchingPermission.getEntity(), PermissionType.FOLDER, Permissions.createPermissionBits(matchingPermission));
            } else {
                entities.addUser(matchingPermission.getEntity(), PermissionType.FOLDER, Permissions.createPermissionBits(matchingPermission));
            }
        }
        return entities;
    }

}
