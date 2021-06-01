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
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PathAction} - Maps the action to a PATH action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class PathAction extends AbstractFolderAction {

    public static final String ACTION = AJAXServlet.ACTION_PATH;

    /**
     * Initializes a new {@link PathAction}.
     */
    public PathAction() {
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
        final String folderId = request.getParameter("id");
        if (null == folderId) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }
        final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        /*
         * Request subfolders from folder service
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        final FolderResponse<UserizedFolder[]> subfoldersResponse =
            folderService.getPath(
                treeId,
                folderId,
                session,
                getDecorator(request));
        /*
         * Determine last-modified time stamp
         */
        long lastModified = 0;
        final UserizedFolder[] subfolders = subfoldersResponse.getResponse();
        for (final UserizedFolder userizedFolder : subfolders) {
            final Date modified = userizedFolder.getLastModifiedUTC();
            if (modified != null) {
                final long time = modified.getTime();
                lastModified = ((lastModified >= time) ? lastModified : time);
            }
        }
        /*
         * Write subfolders as JSON arrays to JSON array
         */
        final JSONArray jsonArray = FolderWriter.writeMultiple2Array(request, columns, subfolders, Constants.ADDITIONAL_FOLDER_FIELD_LIST);
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(jsonArray, 0 == lastModified ? null : new Date(lastModified)).addWarnings(subfoldersResponse.getWarnings());
    }

}
