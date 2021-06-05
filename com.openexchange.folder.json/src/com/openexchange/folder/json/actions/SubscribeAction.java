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
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
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
public final class SubscribeAction extends AbstractFolderAction {

    private static final String NAME_SUBSCRIBE = "subscribe".intern();

    private static final String NAME_TREE = "tree".intern();

    private static final class SubscribeParams {

        protected final String sourceTree;
        protected final String folderId;
        @SuppressWarnings("unused")
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
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
