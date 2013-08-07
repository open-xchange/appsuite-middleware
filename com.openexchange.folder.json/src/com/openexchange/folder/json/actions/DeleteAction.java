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
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.FolderService;
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
    @Parameter(name = "allowed_modules", description = "(Preliminary) An array of modules (either numbers or strings; e.g. \"tasks,calendar,contacts,mail\") supported by requesting client. If missing, all available modules are considered.")
}, requestBody = "An array with object IDs of the folders that shall be deleted.",
responseDescription = "An array with object IDs of folders that were NOT deleted. There may be a lot of different causes for a not deleted folder: A folder has been modified in the mean time, the user does not have the permission to delete it or those permissions have just been removed, the folder does not exist, etc.")
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
        final JSONArray jsonArray = (JSONArray) request.getData();
        final int len = jsonArray.length();
        /*
         * Delete
         */
        final JSONArray responseArray = new JSONArray();
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        final List<OXException> warnings = new LinkedList<OXException>();
        for (int i = 0; i < len; i++) {
            final String folderId = jsonArray.getString(i);
            try {
                folderService.deleteFolder(treeId, folderId, timestamp, session);
            } catch (final OXException e) {
                final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DeleteAction.class));
                log.error(e.getMessage(), e);
                e.setCategory(Category.CATEGORY_WARNING);
                warnings.add(e);
                responseArray.put(folderId);
            }
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(responseArray).addWarnings(warnings);
    }

}
