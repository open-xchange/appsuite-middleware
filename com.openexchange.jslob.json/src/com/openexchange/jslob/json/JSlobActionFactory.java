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

package com.openexchange.jslob.json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.json.action.JSlobAction;
import com.openexchange.jslob.json.action.Method;
import com.openexchange.server.ServiceLookup;

/**
 * {@link JSlobActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class JSlobActionFactory implements AJAXActionServiceFactory {

    private final Map<String, JSlobAction> actions;

    /**
     * Initializes a new {@link JSlobActionFactory}.
     *
     * @param services The service look-up
     */
    public JSlobActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, JSlobAction>(10, 0.9f, 1);
        addJSlobAction(new com.openexchange.jslob.json.action.AllAction(services, actions));
        addJSlobAction(new com.openexchange.jslob.json.action.GetAction(services, actions));
        addJSlobAction(new com.openexchange.jslob.json.action.ListAction(services, actions));
        addJSlobAction(new com.openexchange.jslob.json.action.SetAction(services, actions));
        addJSlobAction(new com.openexchange.jslob.json.action.UpdateAction(services, actions));
    }

    private void addJSlobAction(final JSlobAction jslobAction) {
        final List<Method> restMethods = jslobAction.getRESTMethods();
        if (null != restMethods && !restMethods.isEmpty()) {
            for (final Method method : restMethods) {
                actions.put(method.toString(), jslobAction);
            }
        }
        actions.put(jslobAction.getAction(), jslobAction);
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}
