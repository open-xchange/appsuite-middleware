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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.parser.FolderParser;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction} - Maps the action to an UPDATE action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update a folder", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the updated folder."),
    @Parameter(name = "timestamp", description = "Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail."),
    @Parameter(name = "tree", description = "(Preliminary) The identifier of the folder tree. If missing '0' (primary folder tree) is assumed."),
    @Parameter(name = "allowed_modules", description = "(Preliminary) An array of modules (either numbers or strings; e.g. \"tasks,calendar,contacts,mail\") supported by requesting client. If missing, all available modules are considered.")
}, requestBody = "Folder object as described in Common folder data and Detailed folder data. Only modified fields are present.",
responseDescription = "Nothing, except the standard response object with empty data, the timestamp of the updated folder, and maybe errors.")
public final class UpdateAction extends AbstractFolderAction {

    public static final String ACTION = AJAXServlet.ACTION_UPDATE;

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction() {
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
                } catch (final NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("timestamp", timestampStr);
                }
            }
        }
        /*
         * Parse folder object
         */
        final JSONObject folderObject = (JSONObject) request.requireData();
        final Folder folder = new FolderParser(ServiceRegistry.getInstance().getService(ContentTypeDiscoveryService.class)).parseFolder(folderObject);
        folder.setID(id);
        try {
            final String fieldName = FolderField.SUBSCRIBED.getName();
            if (folderObject.hasAndNotNull(fieldName) && 0 == folderObject.getInt(fieldName)) {
                /*
                 * TODO: Remove this ugly hack to fix broken UI behavior which send "subscribed":0 for db folders
                 */
                try {
                    Integer.parseInt(id);
                    folder.setSubscribed(true);
                } catch (final NumberFormatException e) {
                    // Ignore
                }
            }
        } catch (final JSONException e) {
            // Ignore
        }
        folder.setTreeID(treeId);
        /*
         * Update
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        folderService.updateFolder(folder, timestamp, session, new FolderServiceDecorator().put("permissions", request.getParameter("permissions")).put("altNames", request.getParameter("altNames")).put(PARAM_IGNORE_TRANSLATION, request.getParameter(PARAM_IGNORE_TRANSLATION)).put("autorename", request.getParameter("autorename")));
        /*
         * Invoke folder.getID() to obtain possibly new folder identifier
         */
        final String newId = folder.getID();
        return new AJAXRequestResult(newId, folderService.getFolder(treeId, newId, session, null).getLastModifiedUTC());
    }

}
