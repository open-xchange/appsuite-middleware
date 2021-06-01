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

package com.openexchange.message.timeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.message.timeline.actions.AbstractMessageTimelineAction;
import com.openexchange.message.timeline.actions.PopAction;
import com.openexchange.message.timeline.actions.PutAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MessageTimelineActionFactory} - The action factory for message timeline module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessageTimelineActionFactory implements AJAXActionServiceFactory {

    /**
     * The module name.
     */
    public static final String MODULE = "message.timeline";

    // ----------------------------------------------------------------------------------- //

    private final Map<String, AbstractMessageTimelineAction> actions;

    /**
     * Initializes a new {@link MessageTimelineActionFactory}.
     *
     * @param services The service look-up
     */
    public MessageTimelineActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AbstractMessageTimelineAction>(4, 0.9f, 1);
        {
            final PutAction putAction = new PutAction(services, actions);
            actions.put(putAction.getAction(), putAction);
        }
        {
            final PopAction popAction = new PopAction(services, actions);
            actions.put(popAction.getAction(), popAction);
        }
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    /**
     * Checks if this action factory supports given action identifier.
     *
     * @param action The action identifier
     * @return <code>true</code> if supported; else <code>false</code>
     */
    public boolean contains(final String action) {
        if (Strings.isEmpty(action)) {
            return false;
        }
        return actions.containsKey(action);
    }

}
