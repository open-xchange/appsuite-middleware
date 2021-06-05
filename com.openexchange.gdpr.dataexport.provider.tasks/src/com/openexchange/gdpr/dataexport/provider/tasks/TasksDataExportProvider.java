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

package com.openexchange.gdpr.dataexport.provider.tasks;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportArguments;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.gdpr.dataexport.provider.general.AbstractDataExportProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link TasksDataExportProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TasksDataExportProvider extends AbstractDataExportProvider<TaskDataExport> {

    private static final String ID_TASKS = "tasks";

    /**
     * Initializes a new {@link TasksDataExportProvider}.
     *
     * @param services The service look-up
     */
    public TasksDataExportProvider(ServiceLookup services) {
        super(services);
    }

    @Override
    public String getId() {
        return ID_TASKS;
    }

    @Override
    public boolean checkArguments(DataExportArguments args, Session session) throws OXException {
        Module tasksModule = null;
        List<Module> modules = args.getModules();
        for (Iterator<Module> it = modules.iterator(); tasksModule == null && it.hasNext(); ) {
            Module module = it.next();
            if (ID_TASKS.equals(module.getId())) {
                tasksModule = module;
            }
        }

        if (tasksModule == null) {
            return false;
        }

        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.tasks.enabled", true, session)) {
            return false;
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasTask()) {
            return false;
        }

        return getBoolOption(TasksDataExportPropertyNames.PROP_ENABLED, true, tasksModule);
    }

    @Override
    public Optional<Module> getModule(Session session) throws OXException {
        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.tasks.enabled", true, session)) {
            return Optional.empty();
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasTask()) {
            return Optional.empty();
        }

        boolean hasSharedFolders;
        {
            UserConfiguration userConfiguration = userConfigService.getUserConfiguration(session);
            hasSharedFolders = userConfiguration.hasFullSharedFolderAccess();
        }

        Map<String, Object> properties = new LinkedHashMap<String, Object>(6);
        properties.put(TasksDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE);
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.tasks." + TasksDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, true, session)) {
            properties.put(TasksDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, Boolean.FALSE);
        }
        if (hasSharedFolders && getBoolProperty("com.openexchange.gdpr.dataexport.provider.tasks." + TasksDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, true, session)) {
            properties.put(TasksDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, Boolean.FALSE);
        }

        return Optional.of(Module.valueOf(ID_TASKS, properties));
    }

    @Override
    public String getPathPrefix(Locale locale) throws OXException {
        return StringHelper.valueOf(locale).getString(TasksDataExportStrings.PREFIX_TASKS);
    }

    @Override
    protected TaskDataExport createTask(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException {
        return new TaskDataExport(sink, savepoint, task, locale, services);
    }

}
