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

import static com.openexchange.folder.json.FolderField.FOLDER_ID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.parser.ParsedFolder;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction} - Maps the action to an UPDATE action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(enqueueable = true)
@RestrictedAction(module = AbstractFolderAction.MODULE, type = RestrictedAction.Type.WRITE, hasCustomOAuthScopeCheck = true)
public final class UpdateAction extends AbstractFolderAction implements EnqueuableAJAXActionService {

    public static final String ACTION = AJAXServlet.ACTION_UPDATE;

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction() {
        super();
    }

    @Override
    public EnqueuableAJAXActionService.Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException {
        JSONObject data = (JSONObject) request.requireData();
        JSONObject jFolder = data.optJSONObject("folder");
        if (null == jFolder) {
            jFolder = data;
        }

        String newParent = jFolder.optString(FOLDER_ID.getName(), null);
        if (null == newParent) {
            return EnqueuableAJAXActionService.resultFor(false);
        }

        String id = request.getParameter("id");
        if (null == id) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }

        try {
            JSONObject jKeyDesc = new JSONObject(4);
            jKeyDesc.put("module", "folder");
            jKeyDesc.put("action", "update");
            jKeyDesc.put("id", id);
            jKeyDesc.put("parent", newParent);

            return EnqueuableAJAXActionService.resultFor(true, new JobKey(session.getUserId(), session.getContextId(), jKeyDesc.toString()), this);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
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
        final String id = request.getParameter("id");
        if (null == id) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }
        final Date timestamp;
        {
            final String timestampStr = request.getParameter("timestamp");
            if (null == timestampStr) {
                timestamp = null;
            } else {
                try {
                    timestamp = new Date(Long.parseLong(timestampStr));
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("timestamp", timestampStr);
                }
            }
        }
        final boolean cascadePermissions;
        {
            final String inherit = request.getParameter("cascadePermissions");
            if (inherit == null) {
                cascadePermissions = false;
            } else {
                cascadePermissions = Boolean.parseBoolean(inherit);
            }
        }
        /*
         * Parse request body
         */
        UpdateData updateData = parseRequestBody(treeId, id, request, session);

        /*-
         * Uncomment this to artificially insert a notification
         *
        {
            NotificationData notificationData = new NotificationData();
            notificationData.setMessage("Watch this!");
            notificationData.setTransport(Transport.MAIL);
            updateData.setNotificationData(notificationData);
        }
         *
         */

        ParsedFolder folder = updateData.getFolder();
        /*
         * Update
         */
        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request, false);
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        // @formatter:off
        FolderServiceDecorator decorator = getDecorator(request).put("cascadePermissions", Boolean.valueOf(cascadePermissions))
                                                                .put("permissions", request.getParameter("permissions"))
                                                                .put(PARAM_AUTORENAME, request.getParameter(PARAM_AUTORENAME))
                                                                .put("ignoreWarnings", Boolean.valueOf(ignoreWarnings))
                                                                .put(id, folderService);
        // @formatter:on

        boolean notify = updateData.notifyPermissionEntities() && folder.getPermissions() != null && folder.getPermissions().length > 0;
        UserizedFolder original = null;
        if (notify) {
            original = folderService.getFolder(treeId, id, session, decorator);
        }

        final FolderResponse<Void> response = folderService.updateFolder(folder, timestamp, session, decorator);
        List<OXException> warnings = new ArrayList<>(response.getWarnings());
        /*
         * Invoke folder.getID() to obtain possibly new folder identifier
         */
        final String newId = folder.getID();
        if (notify) {
            warnings.addAll(sendNotifications(updateData.getNotificationData(), original, folderService.getFolder(treeId, newId, session, decorator), session, request.getHostData()));
        }

        Date lastModified = null != newId ? folderService.getFolder(treeId, newId, session, null).getLastModifiedUTC() : null;
        AJAXRequestResult result = new AJAXRequestResult(newId, lastModified);

        result.addWarnings(warnings);

        if (null == newId && 0 < warnings.size() && false == ignoreWarnings) {
            result.setException(FolderExceptionErrorMessage.FOLDER_UPDATE_ABORTED.create(
                getFolderNameSafe(session, folder, id, treeId, folderService), id));
        }
        return result;
    }

    private static String getFolderNameSafe(Session session, Folder folder, String folderID, String treeID, FolderService folderService) {
        if (null != folder && Strings.isNotEmpty(folder.getName())) {
            return folder.getName();
        }
        String id = null != folderID ? folderID : null != folder ? folder.getID() : null;
        if (null != id && null != folderService) {
            String tree = null != treeID ? treeID : getDefaultTreeIdentifier();
            try {
                UserizedFolder userizedFolder = folderService.getFolder(tree, id, session, null);
                if (null != userizedFolder) {
                    return userizedFolder.getName();
                }
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(UpdateAction.class).debug("Error getting name for folder {}: {}", id, e.getMessage(), e);
            }
        }
        return "";
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

}
