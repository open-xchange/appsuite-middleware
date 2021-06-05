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

import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
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
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SharesAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RestrictedAction()
public class SharesAction extends AbstractFolderAction {

    /** The action identifier */
    public static final String ACTION = "shares";

    /**
     * Initializes a new {@link SharesAction}.
     */
    public SharesAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData request, ServerSession session) throws OXException {
        /*
         * parse & check required parameters
         */
        String treeId = request.getParameter("tree");
        if (null == treeId) {
            treeId = getDefaultTreeIdentifier();
        }
        ContentType contentType = parseAndCheckContentTypeParameter(AJAXServlet.PARAMETER_CONTENT_TYPE, request);
        if (null == contentType) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_CONTENT_TYPE);
        }
        if (isOAuthRequest(request) && false == mayReadViaOAuthRequest(contentType, getOAuthAccess(request))) {
            throw new OAuthInsufficientScopeException();
        }
        /*
         * prepare folder service decorator based on additional request parameters
         */
        FolderServiceDecorator decorator = getDecorator(request).put("mailRootFolders", request.getParameter("mailRootFolders"));

        /*
         * get shares from folder service
         */
        FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        FolderResponse<UserizedFolder[]> folderResponse = folderService.getUserSharedFolders(treeId, contentType, session, decorator);
        /*
         * construct & return appropriate ajax response
         */
        int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        UserizedFolder[] folderShares = folderResponse.getResponse();
        JSONArray jsonFolders = FolderWriter.writeMultiple2Array(request, columns, folderShares, Constants.ADDITIONAL_FOLDER_FIELD_LIST);
        return new AJAXRequestResult(jsonFolders, getLastModified(folderShares)).addWarnings(folderResponse.getWarnings());
    }

    /**
     * Extracts the latest last modification date from the supplied folders.
     *
     * @param folders The folders to get the latest modification date from
     * @return The latest modification date, or <code>null</code> if not available
     */
    protected static Date getLastModified(UserizedFolder[] folders) {
        Date lastModified = null;
        if (null != folders && 0 < folders.length) {
            for (UserizedFolder folder : folders) {
                Date folderLastModifed = folder.getLastModifiedUTC();
                if (null != folderLastModifed) {
                    if (null == lastModified || lastModified.before(folderLastModifed)) {
                        lastModified = folderLastModifed;
                    }
                }
            }
        }
        return lastModified;
    }

}
