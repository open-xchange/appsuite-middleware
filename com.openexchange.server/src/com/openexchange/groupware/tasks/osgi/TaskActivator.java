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

package com.openexchange.groupware.tasks.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
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
import com.openexchange.groupware.tasks.database.RemoveUselessExternalParticipants;
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
        return new Class<?>[0];
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CreateTableService.class, new CreateTaskTables());
        track(I18nService.class, new TranslatorCustomizer(context));
        track(DatabaseService.class, new UpdateTaskRegisterer(context) {
            @Override
            protected Collection<? extends UpdateTaskV2> createTasks(DatabaseService service) {
                return Arrays.asList(new TasksModifyCostColumnTask(service), new RemoveUselessExternalParticipants(service)
                    // TODO enable this task with the upcoming major release after 7.8.0
                    // RemoveUselessExternalParticipantsV2(service)
                    );
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
    }
}
