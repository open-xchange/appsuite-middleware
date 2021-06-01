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

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FolderActionFactory}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@SuppressWarnings("deprecation")
@OAuthModule
public final class FolderActionFactory implements AJAXActionServiceFactory {

    private static final FolderActionFactory SINGLETON = new FolderActionFactory();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static final FolderActionFactory getInstance() {
        return SINGLETON;
    }

    // -----------------------------------------------------------------------------------------------------

    private final Map<String, AJAXActionService> actions;

    private FolderActionFactory() {
        super();
        actions = initActions();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        final AJAXActionService retval = actions.get(action);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
        }
        return retval;
    }

    private Map<String, AJAXActionService> initActions() {
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        tmp.put(RootAction.ACTION, new RootAction());
        tmp.put(ListAction.ACTION, new ListAction());
        tmp.put(GetAction.ACTION, new GetAction());
        tmp.put(CreateAction.ACTION, new CreateAction());
        tmp.put(DeleteAction.ACTION, new DeleteAction());
        tmp.put(UpdateAction.ACTION, new UpdateAction());
        tmp.put(PathAction.ACTION, new PathAction());
        tmp.put(ClearAction.ACTION, new ClearAction());
        tmp.put(UpdatesAction.ACTION, new UpdatesAction());
        tmp.put(VisibleFoldersAction.ACTION, new VisibleFoldersAction());
        tmp.put(SubscribeAction.ACTION, new SubscribeAction());
        tmp.put(SharesAction.ACTION, new SharesAction());
        tmp.put(NotifyAction.ACTION, new NotifyAction());
        tmp.put(RestoreAction.ACTION, new RestoreAction());
        tmp.put(CheckLimitsAction.ACTION, new CheckLimitsAction());
        tmp.put(SearchAction.ACTION, new SearchAction());
        return tmp.build();
    }

}
