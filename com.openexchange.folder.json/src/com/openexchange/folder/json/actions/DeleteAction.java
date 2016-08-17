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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction} - Maps the action to a DELETE action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "delete", description = "Delete folders", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "timestamp", description = "Timestamp of the last update of the deleted folders."),
    @Parameter(name = "tree", description = "(Preliminary) The identifier of the folder tree. If missing '0' (primary folder tree) is assumed."),
    @Parameter(name = "allowed_modules", description = "(Preliminary) An array of modules (either numbers or strings; e.g. \"tasks,calendar,contacts,mail\") supported by requesting client. If missing, all available modules are considered."),
    @Parameter(name = "hardDelete", type=Type.BOOLEAN, description = "Optional, defaults to \"false\". If set to \"true\", the folders are deleted permanently. Otherwise, and if the underlying storage supports a trash folder and the folders are not yet located below the trash folder, they are moved to the trash folder.")
}, requestBody = "An array with object IDs of the folders that shall be deleted.",
responseDescription = "An array with object IDs of folders that were NOT deleted. There may be a lot of different causes for a not deleted folder: A folder has been modified in the mean time, the user does not have the permission to delete it or those permissions have just been removed, the folder does not exist, etc.")
@OAuthAction(OAuthAction.CUSTOM)
public final class DeleteAction extends AbstractFolderAction {

    public static final String ACTION = AJAXServlet.ACTION_DELETE;

    /**
     * Initializes a new {@link DeleteAction}.
     */
    public DeleteAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(final AJAXRequestData request, final ServerSession session) throws OXException, JSONException {
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
        final Date timestamp;
        {
            final String timestampStr = request.getParameter("timestamp");
            if (null == timestampStr) {
                timestamp = null;
            } else {
                try {
                    timestamp = new Date(Long.parseLong(timestampStr));
                } catch (final NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("timestamp", timestampStr);
                }
            }
        }
        /*
         * Compose JSON array with id
         */
        final JSONArray jsonArray = (JSONArray) request.requireData();
        final int len = jsonArray.length();
        /*
         * Delete
         */
        final boolean failOnError = AJAXRequestDataTools.parseBoolParameter("failOnError", request, false);
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        FolderServiceDecorator decorator = new FolderServiceDecorator().put("hardDelete", request.getParameter("hardDelete"));
        final AJAXRequestResult result;
        if (failOnError) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteAction.class);
            Map<String, OXException> foldersWithError = new HashMap<String, OXException>(len);
            for (int i = 0; i < len; i++) {
                final String folderId = jsonArray.getString(i);
                try {
                    final FolderResponse<Void> response = folderService.deleteFolder(treeId, folderId, timestamp, session, decorator);
                    final Collection<OXException> warnings = response.getWarnings();
                    if (null != warnings && !warnings.isEmpty()) {
                        throw warnings.iterator().next();
                    }
                } catch (final OXException e) {
                    e.setCategory(Category.CATEGORY_ERROR);
                    log.error("Failed to delete folder {} in tree {}.", folderId, treeId, e);
                    foldersWithError.put(folderId, e);
                }
            }
            final int size = foldersWithError.size();
            if (size > 0) {
                if (1 == size) {
                    throw foldersWithError.values().iterator().next();
                }
                final StringBuilder sb = new StringBuilder(64);
                Iterator<String> iterator = foldersWithError.keySet().iterator();
                sb.append(getFolderNameSafe(folderService, iterator.next(), treeId, session));
                while (iterator.hasNext()) {
                    sb.append(", ").append(getFolderNameSafe(folderService, iterator.next(), treeId, session));
                }
                throw FolderExceptionErrorMessage.FOLDER_DELETION_FAILED.create(sb.toString());
            }
            result = new AJAXRequestResult(new JSONArray(0));
        } else {
            final JSONArray responseArray = new JSONArray();
            final List<OXException> warnings = new LinkedList<OXException>();
            for (int i = 0; i < len; i++) {
                final String folderId = jsonArray.getString(i);
                try {
                    folderService.deleteFolder(treeId, folderId, timestamp, session, decorator);
                } catch (final OXException e) {
                    final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteAction.class);
                    log.error("Failed to delete folder {} in tree {}.", folderId, treeId, e);
                    e.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(e);
                    responseArray.put(folderId);
                }
            }
            result = new AJAXRequestResult(responseArray).addWarnings(warnings);
        }
        /*
         * Return appropriate result
         */
        return result;
    }

    /**
     * Tries to get the name of a folder, not throwing an exception in case retrieval fails, but falling back to the folder identifier.
     *
     * @param folderService The folder service
     * @param folderId The ID of the folder to get the name for
     * @param treeId The folder tree
     * @param session the session
     * @return The folder name, or the passed folder ID as fallback
     */
    private static String getFolderNameSafe(FolderService folderService, String folderId, String treeId, ServerSession session) {
        try {
            UserizedFolder folder = folderService.getFolder(treeId, folderId, session, null);
            if (null != folder) {
                return folder.getLocalizedName(session.getUser().getLocale());
            }
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(DeleteAction.class).debug("Error getting folder name for {}", folderId, e);
        }
        return folderId;
    }

    @OAuthScopeCheck
    public boolean accessAllowed(final AJAXRequestData request, final ServerSession session, final OAuthAccess access) throws OXException {
        final JSONArray jsonArray = (JSONArray) request.requireData();
        final int len = jsonArray.length();
        String treeId = request.getParameter("tree");
        if (null == treeId) {
            treeId = getDefaultTreeIdentifier();
        }

        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        try {
            for (int i = 0; i < len; i++) {
                final String folderId = jsonArray.getString(i);
                UserizedFolder folder = folderService.getFolder(treeId, folderId, session, new FolderServiceDecorator());
                if (!mayWriteViaOAuthRequest(folder.getContentType(), access)) {
                    return false;
                }
            }

            return true;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
