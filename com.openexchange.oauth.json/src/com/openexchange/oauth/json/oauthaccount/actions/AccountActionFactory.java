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

package com.openexchange.oauth.json.oauthaccount.actions;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AccountActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AccountActionFactory implements AJAXActionServiceFactory {

    private static final AccountActionFactory SINGLETON = new AccountActionFactory();

    private final Map<String, AJAXActionService> actions;

    private AccountActionFactory() {
        super();
        actions = initActions();
    }

    public static final AccountActionFactory getInstance() {
        return SINGLETON;
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        final AJAXActionService retval = actions.get(action);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        return retval;
    }

    private Map<String, AJAXActionService> initActions() {
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        tmp.put("all", new AllAction());
        tmp.put("list", new AllAction());
        tmp.put("create", new CreateAction());
        tmp.put("get", new GetAction());
        tmp.put("update", new UpdateAction());
        tmp.put("delete", new DeleteAction());
        tmp.put("init", new InitAction());
        tmp.put("callback", new CallbackAction());
        tmp.put("reauthorize", new ReauthorizeAction());
        tmp.put("status", new StatusAction());
        return tmp.build();
    }

}
