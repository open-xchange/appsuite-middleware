/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.calendar.json;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import com.openexchange.documentation.annotations.Module;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;


/**
 * {@link AppointmentActionFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Module(name = "calendar", description = "Provides access to calendar information.")
@OAuthModule
public class AppointmentActionFactory implements AJAXActionServiceFactory {

    public static final String OAUTH_READ_SCOPE = "read_calendar";

    public static final String OAUTH_WRITE_SCOPE = "write_calendar";

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link AppointmentActionFactory}.
     *
     * @param services The service look-up
     */
    public AppointmentActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AJAXActionService>(15, 0.9f, 1);
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
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.unmodifiableCollection(actions.values());
    }

}
