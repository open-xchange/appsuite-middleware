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

package com.openexchange.folder.json.actions;

import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.parser.FolderParser;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CreateAction} - Maps the action to a NEW action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFolderAction.MODULE, type = RestrictedAction.Type.WRITE, hasCustomOAuthScopeCheck = true)
public final class CreateAction extends AbstractFolderAction {

    public static final String ACTION = AJAXServlet.ACTION_NEW;

    /**
     * Initializes a new {@link CreateAction}.
     */
    public CreateAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        String treeId = request.getParameter("tree");
        if (null == treeId) {
            /*
             * Fallback to default tree identifier
             */
            treeId = getDefaultTreeIdentifier();
        }
        String parentId = request.getParameter("folder_id");
        if (Strings.isEmpty(parentId)) {
            parentId = request.getParameter("folder");
            if (Strings.isEmpty(parentId)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("folder");
            }
        }
        /*
         * Parse request body
         */
        UpdateData updateData = parseRequestBody(treeId, null, request, session);
        final Folder folder = updateData.getFolder();
        folder.setParentID(parentId);
        folder.setTreeID(treeId);
        /*
         * Create
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        /*
         * Parse parameters
         */
        FolderServiceDecorator decorator = getDecorator(request).put(PARAM_AUTORENAME, request.getParameter(PARAM_AUTORENAME));
        final FolderResponse<String> newIdResponse = folderService.createFolder(folder, session, decorator);
        final String newId = newIdResponse.getResponse();
        Collection<OXException> warnings = new ArrayList<>(newIdResponse.getWarnings());
        if (updateData.notifyPermissionEntities()) {
            UserizedFolder createdFolder = folderService.getFolder(treeId, newId, session, decorator);
            warnings.addAll(sendNotifications(updateData.getNotificationData(), null, createdFolder, session, request.getHostData()));
        }
        return new AJAXRequestResult(newId, folderService.getFolder(treeId, newId, session, null).getLastModifiedUTC()).addWarnings(warnings);
    }

    @OAuthScopeCheck
    public boolean accessAllowed(final AJAXRequestData request, final ServerSession session, final OAuthAccess access) throws OXException {
        JSONObject folderObject = (JSONObject) request.requireData();
        Folder folder = new FolderParser(ServiceRegistry.getInstance().getService(ContentTypeDiscoveryService.class)).parseFolder(folderObject, getTimeZone(request, session));
        ContentType contentType = folder.getContentType();
        return mayWriteViaOAuthRequest(contentType, access);
    }

}
