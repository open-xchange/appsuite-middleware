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
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SubscribeAction} - Maps the action to an subscribe action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "subscribe", description = "", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, responseDescription = "")
public final class SubscribeAction extends AbstractFolderAction {

    private static final String NAME_SUBSCRIBE = "subscribe".intern();

    private static final String NAME_TREE = "tree".intern();

    private static final class SubscribeParams {

        protected final String sourceTree;
        protected final String folderId;
        protected final boolean subscribe;

        protected SubscribeParams(final String sourceTree, final String folderId, final boolean subscribe) {
            super();
            this.sourceTree = sourceTree;
            this.folderId = folderId;
            this.subscribe = subscribe;
        }
    }

    public static final String ACTION = NAME_SUBSCRIBE;

    /**
     * Initializes a new {@link SubscribeAction}.
     */
    public SubscribeAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final String targetTreeId = request.getParameter(NAME_TREE);
            if (null == targetTreeId) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(NAME_TREE);
            }
            final String optParent = request.getParameter("parent");
            /*
             * Parse JSON object
             */
            final JSONArray jArray = (JSONArray) request.requireData();
            final int len = jArray.length();
            if (0 == len) {
                return new AJAXRequestResult();
            }
            /*
             * Iterate JSON array
             */
            final List<SubscribeParams> subscribeList = new ArrayList<SubscribeParams>(len);
            final List<SubscribeParams> unsubscribeList = new ArrayList<SubscribeParams>(len);
            final String defaultTreeIdentifier = getDefaultTreeIdentifier();
            for (int i = 0; i < len; i++) {
                final JSONObject jObject = jArray.getJSONObject(i);
                if (!jObject.hasAndNotNull(NAME_SUBSCRIBE) || jObject.getBoolean(NAME_SUBSCRIBE)) {
                    subscribeList.add(new SubscribeParams(
                        jObject.hasAndNotNull(NAME_TREE) ? jObject.getString(NAME_TREE) : defaultTreeIdentifier,
                        jObject.getString("id"),
                        true));
                } else {
                    unsubscribeList.add(new SubscribeParams(
                        jObject.hasAndNotNull(NAME_TREE) ? jObject.getString(NAME_TREE) : defaultTreeIdentifier,
                        jObject.getString("id"),
                        false));
                }
            }
            /*
             * Do subscribe/unsubscribe operations
             */
            final FolderService folderService = ServiceRegistry.getInstance().getService(FolderService.class, true);
            for (final SubscribeParams subscribeParams : unsubscribeList) {
                folderService.unsubscribeFolder(targetTreeId, subscribeParams.folderId, session);
            }
            for (final SubscribeParams subscribeParams : subscribeList) {
                folderService.subscribeFolder(subscribeParams.sourceTree, subscribeParams.folderId, targetTreeId, optParent, session);
            }
            /*
             * Return
             */
            return new AJAXRequestResult();
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
