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
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.Tools;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folder.json.writer.FolderWriter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ListAction} - Maps the action to a list action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "list", description = "Get subfolders", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "parent", description = "Object ID of a folder, which is the parent folder of the requested folders."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in Common folder data and Detailed folder data."),
    @Parameter(name = "all", description = "Set to 1 to list even not subscribed folders."),
    @Parameter(name = "tree", description = "The identifier of the folder tree. If missing '0' (primary folder tree) is assumed."),
    @Parameter(name = "allowed_modules", description = "An array of modules (either numbers or strings; e.g. \"tasks,calendar,contacts,mail\") supported by requesting client. If missing, all available modules are considered."),
    @Parameter(name = "errorOnDuplicateName", description = "An optional flag to enable or disable (default) check for duplicate folder names within returned folder response (since v6.20.1). If a duplicate folder name is detected, an appropriate error is returned as response.")
}, responseDescription = "Response with timestamp: An array with data for all folders, which have the folder with the requested object ID as parent. Each array element describes one folder and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
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

        // Ensure ID is contained
        {
            boolean found = false;
            final int idCol = FolderField.ID.getColumn();
            for (int i = 0; !found && i < columns.length; i++) {
                found = (idCol == columns[i]);
            }
            if (!found) {
                final int[] tmp = columns;
                columns = new int[tmp.length + 1];
                System.arraycopy(tmp, 0, columns, 1, tmp.length);
                columns[0] = idCol;
            }
        }

        final boolean all;
        {
            final String parameter = request.getParameter(AJAXServlet.PARAMETER_ALL);
            all = "1".equals(parameter) || Boolean.parseBoolean(parameter);
        }
        final String timeZoneId = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        final java.util.List<ContentType> allowedContentTypes = parseOptionalContentTypeArrayParameter("allowed_modules", request);
        boolean filterDuplicateNames = parseBoolean(request.getParameter("errorOnDuplicateName"), false);
        if (!filterDuplicateNames) {
            filterDuplicateNames = parseBoolean(request.getParameter("errOnDuplName"), false);
        }
        /*
         * Request subfolders from folder service
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        final FolderResponse<UserizedFolder[]> subfoldersResponse =
            folderService.getSubfolders(
                treeId,
                parentId,
                all,
                session,
                new FolderServiceDecorator().setTimeZone(Tools.getTimeZone(timeZoneId)).setAllowedContentTypes(allowedContentTypes).put("altNames", request.getParameter("altNames")).put(PARAM_IGNORE_TRANSLATION, request.getParameter(PARAM_IGNORE_TRANSLATION)));
        /*
         * Determine max. last-modified time stamp
         */
        long lastModified = 0;
        UserizedFolder[] subfolders = subfoldersResponse.getResponse();
        final int length = subfolders.length;
        if (length <= 0) {
            /*
             * Return appropriate result
             */
            final Date dNull = null;
            return new AJAXRequestResult(new JSONArray(0), dNull).addWarnings(subfoldersResponse.getWarnings());
        }
        /*
         * length > 0
         */
        final boolean ignoreTranslation = parseBoolean(request.getParameter(PARAM_IGNORE_TRANSLATION), false);
        // Align to client identifier
        if (filterDuplicateNames) {
            // Filter equally named folder
            final Map<String, UserizedFolder> name2folder = new HashMap<String, UserizedFolder>(length);
            final Map<String, UserizedFolder> id2folder = new HashMap<String, UserizedFolder>(length);
            for (int i = 0; i < length; i++) {
                final UserizedFolder userizedFolder = subfolders[i];
                Locale locale = userizedFolder.getLocale();
                if (null == locale) {
                    locale = FolderWriter.DEFAULT_LOCALE;
                }
                final String name = ignoreTranslation ? userizedFolder.getLocalizedName(locale) : userizedFolder.getName();
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
        }

        // Determine last-modified time stamp
        for (int i = length - 1; i >= 0; i--) {
            final Date modified = subfolders[i].getLastModifiedUTC();
            if (modified != null) {
                final long time = modified.getTime();
                lastModified = ((lastModified >= time) ? lastModified : time);
            }
        }
        /*
         * Write subfolders as JSON arrays to JSON array
         */
        final JSONArray jsonArray = FolderWriter.writeMultiple2Array(columns, subfolders, session, Constants.ADDITIONAL_FOLDER_FIELD_LIST, ignoreTranslation ? parametersFor(PARAM_IGNORE_TRANSLATION, Boolean.TRUE) : null);
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(jsonArray, 0 == lastModified ? null : new Date(lastModified)).addWarnings(subfoldersResponse.getWarnings());
    }

    private boolean isUsmEas(final String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        final String uc = Strings.toUpperCase(clientId);
        return uc.startsWith("USM-EAS") || uc.startsWith("USM-JSON");
    }

}
