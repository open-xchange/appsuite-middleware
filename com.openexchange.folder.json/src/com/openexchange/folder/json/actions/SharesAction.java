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

import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.Tools;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folder.json.writer.FolderWriter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SharesAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "shares", description = "Get folders of the user shared to other entities", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in Common folder data and Detailed folder data."),
    @Parameter(name = "all", description = "Set to 1 to list even not subscribed folders."),
    @Parameter(name = "tree", description = "The identifier of the folder tree. If missing '0' (primary folder tree) is assumed."),
    @Parameter(name = "content_type", description = "The desired content type (either numbers or strings; e.g. \"tasks\", \"calendar\", \"contacts\", \"infostore\")"),
}, responseDescription = "Response with timestamp: An array with data for all folders that are considered as shared by the user. Each array element describes one folder and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(OAuthAction.GRANT_ALL)
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
        FolderServiceDecorator decorator = new FolderServiceDecorator()
            .setTimeZone(Tools.getTimeZone(request.getParameter(AJAXServlet.PARAMETER_TIMEZONE)))
            .setAllowedContentTypes(collectAllowedContentTypes(request))
            .put("mailRootFolders", request.getParameter("mailRootFolders"))
            .put("altNames", request.getParameter("altNames"))
            .put("suppressUnifiedMail", isSuppressUnifiedMail(request, session))
        ;
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
