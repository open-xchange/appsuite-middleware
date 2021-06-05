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
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folder.json.writer.FolderWriter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VisibleFoldersAction} - Maps the action to a <code>allVisible</code> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class VisibleFoldersAction extends AbstractFolderAction {

    public static final String ACTION = "allVisible";

    /**
     * Initializes a new {@link VisibleFoldersAction}.
     */
    public VisibleFoldersAction() {
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
        final ContentType contentType = parseAndCheckContentTypeParameter(AJAXServlet.PARAMETER_CONTENT_TYPE, request);
        if (null == contentType) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_CONTENT_TYPE);
        }
        if (isOAuthRequest(request) && !mayReadViaOAuthRequest(contentType, getOAuthAccess(request))) {
            throw new OAuthInsufficientScopeException();
        }
        String rootFolderId = request.getParameter("root");
        if (Strings.isEmpty(rootFolderId)) {
            rootFolderId = null;
        }

        final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        final boolean all;
        {
            final String parameter = request.getParameter(AJAXServlet.PARAMETER_ALL);
            all = "1".equals(parameter) || Boolean.parseBoolean(parameter);
        }
        final String mailRootFolders = request.getParameter("mailRootFolders");
        /*
         * Get folder service
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        /*
         * Get all private folders
         */
        // @formatter:off
        final FolderResponse<UserizedFolder[]> privateResp =
            folderService.getVisibleFolders(
                rootFolderId,
                treeId,
                contentType,
                PrivateType.getInstance(),
                all,
                session,
                getDecorator(request).put("mailRootFolders", mailRootFolders));
        // @formatter:on
        /*
         * Get all shared folders
         */
        final FolderResponse<UserizedFolder[]> sharedResp =
            folderService.getVisibleFolders(
                rootFolderId,
                treeId,
                contentType,
                SharedType.getInstance(),
                all,
                session,
                getDecorator(request));
        /*
         * Get all public folders
         */
        final FolderResponse<UserizedFolder[]> publicResp =
            folderService.getVisibleFolders(
                rootFolderId,
                treeId,
                contentType,
                PublicType.getInstance(),
                all,
                session,
                getDecorator(request));
        /*
         * Determine max. last-modified time stamp
         */
        long lastModified = 0;
        final UserizedFolder[] privateFolders = privateResp.getResponse();
        if (null != privateFolders) {
            for (final UserizedFolder userizedFolder : privateFolders) {
                final Date modified = userizedFolder.getLastModifiedUTC();
                if (modified != null) {
                    final long time = modified.getTime();
                    lastModified = ((lastModified >= time) ? lastModified : time);
                }
            }
        }
        final UserizedFolder[] sharedFolders = sharedResp.getResponse();
        if (null != sharedFolders) {
            for (final UserizedFolder userizedFolder : sharedFolders) {
                final Date modified = userizedFolder.getLastModifiedUTC();
                if (modified != null) {
                    final long time = modified.getTime();
                    lastModified = ((lastModified >= time) ? lastModified : time);
                }
            }
        }
        final UserizedFolder[] publicFolders = publicResp.getResponse();
        if (null != publicFolders) {
            for (final UserizedFolder userizedFolder : publicFolders) {
                final Date modified = userizedFolder.getLastModifiedUTC();
                if (modified != null) {
                    final long time = modified.getTime();
                    lastModified = ((lastModified >= time) ? lastModified : time);
                }
            }
        }
        /*
         * Write subfolders as JSON arrays to JSON object
         */
        try {
            final JSONObject ret = new JSONObject(4);
            final AdditionalFolderFieldList additionalFolderFieldList = Constants.ADDITIONAL_FOLDER_FIELD_LIST;
            if (null != privateFolders && privateFolders.length > 0) {
                ret.put("private", FolderWriter.writeMultiple2Array(request, columns, privateFolders, additionalFolderFieldList));
            }
            if (null != publicFolders && publicFolders.length > 0) {
                ret.put("public", FolderWriter.writeMultiple2Array(request, columns, publicFolders, additionalFolderFieldList));
            }
            if (null != sharedFolders && sharedFolders.length > 0) {
                ret.put("shared", FolderWriter.writeMultiple2Array(request, columns, sharedFolders, additionalFolderFieldList));
            }
            /*
             * Gather possible warnings
             */
            final List<OXException> warnings = new ArrayList<OXException>(4);
            warnings.addAll(privateResp.getWarnings());
            warnings.addAll(publicResp.getWarnings());
            warnings.addAll(sharedResp.getWarnings());
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(ret, 0 == lastModified ? null : new Date(lastModified)).addWarnings(warnings);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
