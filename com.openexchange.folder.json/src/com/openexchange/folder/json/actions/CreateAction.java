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
import java.util.Collection;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
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
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CreateAction} - Maps the action to a NEW action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "new", description = "Create a folder", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder_id", description = "The parent folder of the newly created folder."),
    @Parameter(name = "tree", description = "(Preliminary) The identifier of the folder tree. If missing '0' (primary folder tree) is assumed."),
    @Parameter(name = "allowed_modules", description = "(Preliminary) An array of modules (either numbers or strings; e.g. \"tasks,calendar,contacts,mail\") supported by requesting client. If missing, all available modules are considered.")
}, requestBody = "Folder object as described in Common folder data and Detailed folder data. The field id should not be present. Provided that permission is granted to create a folder, its module is bound to the limitation, that the new folder's module must be equal to parent folder's module except that: Parent folder is one of the system folders private, public, or shared. Below these folders task, calendar, and contact modules are permitted. Parent folder's module is one of task, calendar, or contact. Below this kind of folders task, calendar, and contact modules are permitted.",
responseDescription = "Object ID of the newly created folder.")
@OAuthAction(OAuthAction.CUSTOM)
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
        FolderServiceDecorator decorator = new FolderServiceDecorator()
            .put("autorename", request.getParameter("autorename"));
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
