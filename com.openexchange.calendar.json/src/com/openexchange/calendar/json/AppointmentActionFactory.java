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

package com.openexchange.calendar.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.calendar.json.actions.AllAction;
import com.openexchange.calendar.json.actions.ChangeExceptionsAction;
import com.openexchange.calendar.json.actions.ConfirmAction;
import com.openexchange.calendar.json.actions.CopyAction;
import com.openexchange.calendar.json.actions.DeleteAction;
import com.openexchange.calendar.json.actions.FreeBusyAction;
import com.openexchange.calendar.json.actions.GetAction;
import com.openexchange.calendar.json.actions.HasAction;
import com.openexchange.calendar.json.actions.ListAction;
import com.openexchange.calendar.json.actions.NewAction;
import com.openexchange.calendar.json.actions.NewAppointmentsSearchAction;
import com.openexchange.calendar.json.actions.ResolveUIDAction;
import com.openexchange.calendar.json.actions.SearchAction;
import com.openexchange.calendar.json.actions.UpdateAction;
import com.openexchange.calendar.json.actions.UpdatesAction;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;


/**
 * {@link AppointmentActionFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthModule
public class AppointmentActionFactory implements AJAXActionServiceFactory {

    public static final String MODULE = "calendar";

    @Deprecated
    public static final String OAUTH_READ_SCOPE = "read_calendar";

    @Deprecated
    public static final String OAUTH_WRITE_SCOPE = "write_calendar";

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link AppointmentActionFactory}.
     *
     * @param services The service look-up
     */
    public AppointmentActionFactory(final ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AJAXActionService> actions = ImmutableMap.builder();
        actions.put("new", new NewAction(services));
        actions.put("update", new UpdateAction(services));
        actions.put("updates", new UpdatesAction(services));
        actions.put("confirm", new ConfirmAction(services));
        actions.put("delete", new DeleteAction(services));
        actions.put("all", new AllAction(services));
        actions.put("list", new ListAction(services));
        actions.put("get", new GetAction(services));
        actions.put("search", new SearchAction(services));
        actions.put("newappointments", new NewAppointmentsSearchAction(services));
        actions.put("has", new HasAction(services));
        actions.put("freebusy", new FreeBusyAction(services));
        actions.put("copy", new CopyAction(services));
        actions.put("resolveuid", new ResolveUIDAction(services));
        actions.put("getChangeExceptions", new ChangeExceptionsAction(services));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

}
