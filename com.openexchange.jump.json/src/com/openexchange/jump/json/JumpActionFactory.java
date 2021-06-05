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

package com.openexchange.jump.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.jump.json.actions.AbstractJumpAction;
import com.openexchange.jump.json.actions.DummyEndpointHandlerAction;
import com.openexchange.jump.json.actions.IdentityTokenAction;
import com.openexchange.server.ServiceLookup;


/**
 * {@link JumpActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JumpActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AbstractJumpAction> actions;

    /**
     * Initializes a new {@link JumpActionFactory}.
     */
    public JumpActionFactory(final ServiceLookup lookup) {
        super();
        ImmutableMap.Builder<String, AbstractJumpAction> actions = ImmutableMap.builder();
        actions.put("identityToken", new IdentityTokenAction(lookup));
        actions.put("dummy", new DummyEndpointHandlerAction(lookup));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) {
        return actions.get(action);
    }

}
