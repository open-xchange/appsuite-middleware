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

package com.openexchange.chronos.itip.generators;

import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link ITipNotificationMailGeneratorFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipNotificationMailGeneratorFactory implements ITipMailGeneratorFactory {

    private final NotificationParticipantResolver resolver;

    private final ITipIntegrationUtility util;

    private final ServiceLookup services;

    public ITipNotificationMailGeneratorFactory(NotificationParticipantResolver resolver, ITipIntegrationUtility util, ServiceLookup services) {
        super();
        this.resolver = resolver;
        this.util = util;
        this.services = services;
    }

    @Override
    public ITipMailGenerator create(Event original, Event updated, CalendarSession session, int onBehalfOfId, CalendarUser principal) throws OXException {
        return create(original, updated, session, onBehalfOfId, principal, null);
    }

    @Override
    public ITipMailGenerator create(Event original, Event updated, CalendarSession session, int onBehalfOfId, CalendarUser principal, String comment) throws OXException {
        Context ctx = services.getService(ContextService.class).getContext(session.getContextId());
        User user = services.getService(UserService.class).getUser(session.getUserId(), ctx);
        User onBehalfOf = (onBehalfOfId <= 0) ? user : services.getService(UserService.class).getUser(onBehalfOfId, ctx);

        ITipNotificationMailGenerator generator = new ITipNotificationMailGenerator(services, resolver, util, original, updated, user, onBehalfOf, ctx, session, principal, comment);
        return generator;
    }

}
