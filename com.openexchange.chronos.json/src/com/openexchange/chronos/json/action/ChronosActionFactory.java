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

package com.openexchange.chronos.json.action;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ChronosActionFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@SuppressWarnings("deprecation")
@OAuthModule
public class ChronosActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    public ChronosActionFactory(ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AJAXActionService> actions = ImmutableMap.builderWithExpectedSize(14);
        actions.put("get", new GetAction(services));
        actions.put("all", new AllAction(services));
        actions.put("list", new ListAction(services));
        actions.put("new", new NewAction(services));
        actions.put("update", new UpdateAction(services));
        actions.put("delete", new DeleteAction(services));
        actions.put("updateAttendee", new UpdateAttendeeAction(services));
        actions.put("updates", new UpdatesAction(services));
        actions.put("move", new MoveAction(services));
        actions.put("getAttachment", new GetAttachment(services));
        actions.put("zipAttachments", new ZipAttachments(services));
        actions.put("freeBusy", new FreeBusyAction(services));
        actions.put("needsAction", new NeedsActionAction(services));
        actions.put("resolve", new ResolveAction(services));
        actions.put("changeOrganizer", new ChangeOrganizerAction(services));
        actions.put("advancedSearch", new AdvancedSearchAction(services));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }
}
