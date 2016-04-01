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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
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
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VisibleFoldersAction} - Maps the action to a <code>allVisible</code> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "allVisible", description = "Get all visible folder of a certain module (since v6.18.2) ", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "tree", description = "The identifier of the folder tree. If missing '0' (primary folder tree) is assumed."),
    @Parameter(name = "content_type", description = "The desired content type (either numbers or strings; e.g. \"tasks\", \"calendar\", \"contacts\", \"mail\", \"infostore\")"),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in Common folder data and Detailed folder data.")
}, requestBody = "None",
responseDescription = "Response with timestamp: A JSON object containing three fields: \"private\", \"public\", and \"shared\". Each field is a JSON array with data for all folders. Each folder is itself described by an array.")
@OAuthAction(OAuthAction.GRANT_ALL)
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

        final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        final boolean all;
        {
            final String parameter = request.getParameter(AJAXServlet.PARAMETER_ALL);
            all = "1".equals(parameter) || Boolean.parseBoolean(parameter);
        }
        final String timeZoneId = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        final String mailRootFolders = request.getParameter("mailRootFolders");
        final java.util.List<ContentType> allowedContentTypes = collectAllowedContentTypes(request);
        /*
         * Get folder service
         */
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        /*
         * Get all private folders
         */
        final TimeZone timeZone = Tools.getTimeZone(timeZoneId);
        final String sAltNames = "altNames";
        final String altNames = request.getParameter(sAltNames);
        final String sSuppressUnifiedMail = "suppressUnifiedMail";
        final Boolean suppressUnifiedMail = isSuppressUnifiedMail(request, session);
        final FolderResponse<UserizedFolder[]> privateResp =
            folderService.getVisibleFolders(
                treeId,
                contentType,
                PrivateType.getInstance(),
                all,
                session,
                new FolderServiceDecorator().setTimeZone(timeZone).setAllowedContentTypes(allowedContentTypes).put(
                    "mailRootFolders", mailRootFolders).put(sAltNames, altNames).put(sSuppressUnifiedMail, suppressUnifiedMail));
        /*
         * Get all shared folders
         */
        final FolderResponse<UserizedFolder[]> sharedResp =
            folderService.getVisibleFolders(
                treeId,
                contentType,
                SharedType.getInstance(),
                all,
                session,
                new FolderServiceDecorator().setTimeZone(timeZone).setAllowedContentTypes(allowedContentTypes).put(sAltNames, altNames).put(sSuppressUnifiedMail, suppressUnifiedMail));
        /*
         * Get all public folders
         */
        final FolderResponse<UserizedFolder[]> publicResp =
            folderService.getVisibleFolders(
                treeId,
                contentType,
                PublicType.getInstance(),
                all,
                session,
                new FolderServiceDecorator().setTimeZone(timeZone).setAllowedContentTypes(allowedContentTypes).put(sAltNames, altNames).put(sSuppressUnifiedMail, suppressUnifiedMail));
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
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
