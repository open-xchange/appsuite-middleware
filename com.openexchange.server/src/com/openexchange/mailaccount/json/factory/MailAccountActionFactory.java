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

package com.openexchange.mailaccount.json.factory;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Constants;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.actions.AllAction;
import com.openexchange.mailaccount.json.actions.DeleteAction;
import com.openexchange.mailaccount.json.actions.EnableAction;
import com.openexchange.mailaccount.json.actions.GetAction;
import com.openexchange.mailaccount.json.actions.GetTreeAction;
import com.openexchange.mailaccount.json.actions.ListAction;
import com.openexchange.mailaccount.json.actions.NewAction;
import com.openexchange.mailaccount.json.actions.ResolveFolderAction;
import com.openexchange.mailaccount.json.actions.StatusAction;
import com.openexchange.mailaccount.json.actions.UpdateAction;
import com.openexchange.mailaccount.json.actions.ValidateAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link MailAccountActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@OAuthModule
public class MailAccountActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link MailAccountActionFactory}.
     */
    public MailAccountActionFactory(ActiveProviderDetector activeProviderDetector) {
        super();
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        tmp.put(AllAction.ACTION, new AllAction(activeProviderDetector));
        tmp.put(ListAction.ACTION, new ListAction(activeProviderDetector));
        tmp.put(GetAction.ACTION, new GetAction(activeProviderDetector));
        tmp.put(ValidateAction.ACTION, new ValidateAction(activeProviderDetector));
        tmp.put(DeleteAction.ACTION, new DeleteAction(activeProviderDetector));
        tmp.put(UpdateAction.ACTION, new UpdateAction(activeProviderDetector));
        tmp.put(GetTreeAction.ACTION, new GetTreeAction(activeProviderDetector));
        tmp.put(NewAction.ACTION, new NewAction(activeProviderDetector));
        tmp.put(StatusAction.ACTION, new StatusAction(activeProviderDetector));
        tmp.put(EnableAction.ACTION, new EnableAction(activeProviderDetector));
        tmp.put(ResolveFolderAction.ACTION, new ResolveFolderAction(activeProviderDetector));
        actions = tmp.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        AJAXActionService actionService = actions.get(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, Constants.getModule());
        }
        return actionService;
    }

}
