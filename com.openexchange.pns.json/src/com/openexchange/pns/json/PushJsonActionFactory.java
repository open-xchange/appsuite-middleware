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

package com.openexchange.pns.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link PushJsonActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushJsonActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link PushJsonActionFactory}.
     *
     * @param services The service look-up to use
     */
    public PushJsonActionFactory(ServiceLookup services) {
        super();
        actions = initActions(services);
    }

    /**
     * Gets the module identifier
     *
     * @return The module identifier
     */
    public String getModule() {
        return "pns";
    }

    private Map<String, AJAXActionService> initActions(ServiceLookup services) {
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        {
            SubscribeAction subscribeAction = new SubscribeAction(services);
            tmp.put(subscribeAction.getAction(), subscribeAction);
        }
        {
            UnsubscribeAction unsubscribeAction = new UnsubscribeAction(services);
            tmp.put(unsubscribeAction.getAction(), unsubscribeAction);
        }
        return tmp.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        AJAXActionService retval = actions.get(action);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, getModule());
        }
        return retval;
    }

}
