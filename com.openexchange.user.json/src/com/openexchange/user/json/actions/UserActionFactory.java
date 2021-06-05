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

package com.openexchange.user.json.actions;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UserActionFactory} - Factory for user component.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserActionFactory implements AJAXActionServiceFactory {

    /** The map to store actions. */
    private final Map<String, AJAXActionService> actions;

    /** The service look-up */
    private final ServiceLookup services;

    /**
     * Initializes a new {@link UserActionFactory}.
     */
    public UserActionFactory(ServiceLookup services) {
        super();
        this.services = services;
        actions = initActions();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        if (null == action) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        final AJAXActionService retval = actions.get(action);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        return retval;
    }

    /**
     * Initializes the unmodifiable map to stored actions.
     *
     * @return The unmodifiable map with actions stored
     */
    private Map<String, AJAXActionService> initActions() {
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        tmp.put(GetAction.ACTION, new GetAction(services));
        tmp.put(ListAction.ACTION, new ListAction(services));
        tmp.put(AllAction.ACTION, new AllAction(services));
        tmp.put(SearchAction.ACTION, new SearchAction(services));
        tmp.put(UpdateAction.ACTION, new UpdateAction(services));
        tmp.put(GetAttributeAction.ACTION, new GetAttributeAction(services));
        tmp.put(SetAttributeAction.ACTION, new SetAttributeAction(services));
        return tmp.build();
    }

}
