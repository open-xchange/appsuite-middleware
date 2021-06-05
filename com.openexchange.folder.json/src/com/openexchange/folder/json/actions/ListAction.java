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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ListAction} - Maps the action to a list action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class ListAction extends AbstractFolderAction {

    public static final String ACTION = AJAXServlet.ACTION_LIST;

    /**
     * Initializes a new {@link ListAction}.
     */
    public ListAction() {
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
        final String parentId = request.getParameter("parent");
        if (null == parentId) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("parent");
        }

        int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);

        final boolean all;
        {
            final String parameter = request.getParameter(AJAXServlet.PARAMETER_ALL);
            all = "1".equals(parameter) || Boolean.parseBoolean(parameter);
        }
        boolean filterDuplicateNames = parseBoolean(request.getParameter("errorOnDuplicateName"), false);
        if (!filterDuplicateNames) {
            filterDuplicateNames = parseBoolean(request.getParameter("errOnDuplName"), false);
        }
        /*
         * Request subfolders from folder service
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        //@formatter:off
        final FolderResponse<UserizedFolder[]> subfoldersResponse =
            folderService.getSubfolders(
                treeId,
                parentId,
                all,
                session,
                getDecorator(request).put("forceRetry",request.getParameter("forceRetry")));
        /*
         * Determine max. last-modified time stamp
         */
        long lastModified = 0;
        UserizedFolder[] subfolders = subfoldersResponse.getResponse();
        int length = subfolders.length;
        if (length <= 0) {
            /*
             * Return appropriate result
             */
            final Date dNull = null;
            return new AJAXRequestResult(new JSONArray(0), dNull).addWarnings(subfoldersResponse.getWarnings());
        }

        // Used to filter out folders that must not be visible due to insufficient scope
        boolean checkOAuthScope = isOAuthRequest(request);
        OAuthAccess oauthAccess = getOAuthAccess(request);

        /*
         * length > 0
         */
        // Previously: final boolean ignoreTranslation = parseBoolean(request.getParameter(PARAM_IGNORE_TRANSLATION), false);
        // Align to client identifier
        if (filterDuplicateNames) {
            // Filter equally named folder
            final Map<String, UserizedFolder> name2folder = new HashMap<String, UserizedFolder>(length);
            final Map<String, UserizedFolder> id2folder = new HashMap<String, UserizedFolder>(length);
            for (int i = 0; i < length; i++) {
                final UserizedFolder userizedFolder = subfolders[i];
                if (checkOAuthScope && !mayReadViaOAuthRequest(userizedFolder.getContentType(), oauthAccess)) {
                    continue;
                }

                Locale locale = userizedFolder.getLocale();
                if (null == locale) {
                    locale = FolderWriter.DEFAULT_LOCALE;
                }
                // Previously: final String name = ignoreTranslation ? userizedFolder.getLocalizedName(locale) : userizedFolder.getName();
                final String name = userizedFolder.getLocalizedName(locale);
                final UserizedFolder prev = name2folder.get(name);
                if (null == prev) {
                    name2folder.put(name, userizedFolder);
                    id2folder.put(userizedFolder.getID(), userizedFolder);
                } else {
                    // Decide which folder to keep in list:
                    // First come, first serve; unless duplicate one is a default folder
                    if (userizedFolder.isDefault() && !prev.isDefault()) {
                        // Replace
                        id2folder.remove(prev.getID());
                        name2folder.put(name, userizedFolder);
                        id2folder.put(userizedFolder.getID(), userizedFolder);
                    }
                }
            }

            final List<UserizedFolder> ret = new ArrayList<UserizedFolder>(length);
            for (int i = 0; i < length; i++) {
                final String id = subfolders[i].getID();
                final UserizedFolder userizedFolder = id2folder.get(id);
                if (null != userizedFolder) {
                    ret.add(userizedFolder);
                }
            }
            subfolders = ret.toArray(new UserizedFolder[0]);
            length = subfolders.length;
        } else {
            final List<UserizedFolder> ret = new ArrayList<UserizedFolder>(length);
            for (int i = 0; i < length; i++) {
                final UserizedFolder userizedFolder = subfolders[i];
                if (checkOAuthScope && !mayReadViaOAuthRequest(userizedFolder.getContentType(), oauthAccess)) {
                    continue;
                }

                ret.add(userizedFolder);
            }
            subfolders = ret.toArray(new UserizedFolder[0]);
            length = subfolders.length;
        }

        // Determine last-modified time stamp
        for (int i = length - 1; i >= 0; i--) {
            Date modified = subfolders[i].getLastModifiedUTC();
            if (modified != null && lastModified < modified.getTime()) {
                lastModified = modified.getTime();
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
