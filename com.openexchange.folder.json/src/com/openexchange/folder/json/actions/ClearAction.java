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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ClearAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OAuthAction(OAuthAction.CUSTOM)
public final class ClearAction extends AbstractFolderAction {

    public static final String ACTION = AJAXServlet.ACTION_CLEAR;

    /**
     * Initializes a new {@link ClearAction}.
     */
    public ClearAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(final AJAXRequestData request, final com.openexchange.tools.session.ServerSession session) throws OXException, JSONException {
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
        /*
         * Compose JSON array with id
         */
        final JSONArray jsonArray = (JSONArray) request.requireData();
        final int len = jsonArray.length();
        /*
         * Delete
         */
        final List<OXException> warnings = new LinkedList<OXException>();
        final JSONArray responseArray = new JSONArray();
        final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
        for (int i = 0; i < len; i++) {
            final String folderId = jsonArray.getString(i);
            try {
                folderService.clearFolder(treeId, folderId, session);
            } catch (final OXException e) {
                final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClearAction.class);
                log.error("", e);
                responseArray.put(folderId);
                e.setCategory(com.openexchange.exception.Category.CATEGORY_WARNING);
                warnings.add(e);
            }
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(responseArray).addWarnings(warnings);
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
