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

package com.openexchange.groupware.reminder.osgi;

import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.groupware.reminder.ReminderService;
import com.openexchange.groupware.reminder.ReminderServiceImpl;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.groupware.reminder.json.ReminderActionFactory;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

/**
 * {@link ReminderActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ReminderActivator extends AJAXModuleActivator {

    public ReminderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {CalendarService.class, RecurrenceService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        rememberTracker(new ServiceTracker<TargetService, TargetService>(context, TargetService.class.getName(), new TargetRegistryCustomizer(context)));
        openTrackers();
        ReminderService reminderService = new ReminderServiceImpl();
        registerService(ReminderService.class, reminderService);
        registerModule(new ReminderActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "reminder");
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ReminderActionFactory.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {
            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.TASKS.getCapabilityName()) || capabilities.contains(Permission.CALENDAR.getCapabilityName());
            }
        });
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ReminderActionFactory.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {
            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.TASKS.getCapabilityName()) || capabilities.contains(Permission.CALENDAR.getCapabilityName());
            }
        });
    }

}
