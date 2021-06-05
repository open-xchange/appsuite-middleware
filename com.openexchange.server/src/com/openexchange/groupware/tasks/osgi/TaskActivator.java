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

package com.openexchange.groupware.tasks.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.groupware.tasks.InsertData;
import com.openexchange.groupware.tasks.ModifyThroughDependant;
import com.openexchange.groupware.tasks.TaskQuotaProvider;
import com.openexchange.groupware.tasks.database.CreateTaskTables;
import com.openexchange.groupware.tasks.database.RemoveUselessExternalParticipantsV2;
import com.openexchange.groupware.tasks.database.TasksModifyCostColumnTask;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.osgi.UpdateTaskRegisterer;
import com.openexchange.i18n.I18nService;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.quota.QuotaProvider;

/**
 * {@link TaskActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskActivator extends AJAXModuleActivator {

    public TaskActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { RecurrenceService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CreateTableService.class, new CreateTaskTables());
        track(I18nService.class, new TranslatorCustomizer(context));
        track(DatabaseService.class, new UpdateTaskRegisterer(context) {

            @Override
            protected Collection<? extends UpdateTaskV2> createTasks(DatabaseService service) {
                return Arrays.asList(new TasksModifyCostColumnTask(), new RemoveUselessExternalParticipantsV2());
            }
        });

        final Dictionary<String, Integer> props = new Hashtable<String, Integer>(1, 1);
        props.put(TargetService.MODULE_PROPERTY, I(Types.TASK));
        registerService(TargetService.class, new ModifyThroughDependant(), props);
        DependentServiceRegisterer<QuotaProvider> quotaProviderRegisterer = new DependentServiceRegisterer<QuotaProvider>(
            context,
            QuotaProvider.class,
            TaskQuotaProvider.class,
            null,
            DatabaseService.class,
            ConfigViewFactory.class) {

            @Override
            protected void register() {
                super.register();
                InsertData.setQuotaProvider((TaskQuotaProvider) registeredService);
            }

            @Override
            protected void unregister(ServiceRegistration<?> unregister, Object service) {
                InsertData.setQuotaProvider(null);
                super.unregister(unregister, service);
            }
        };
        track(quotaProviderRegisterer.getFilter(), quotaProviderRegisterer);
        trackService(ContactCollectorService.class);
        openTrackers();
        Services.setServiceLookup(this);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            Services.setServiceLookup(null);
        } finally {
            super.stopBundle();
        }
    }
}
