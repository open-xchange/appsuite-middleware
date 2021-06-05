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

package com.openexchange.gdpr.dataexport.provider.contacts;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import java.util.ArrayList;
import java.util.Collections;
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
 * {@link ContactsDataExportProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ContactsDataExportProvider extends AbstractDataExportProvider<ContactsDataExport> {

    private static final String ID_CONTACTS = "contacts";

    /**
     * Initializes a new {@link ContactsDataExportProvider}.
     *
     * @param services The service look-up
     */
    public ContactsDataExportProvider(ServiceLookup services) {
        super(services);
    }

    @Override
    public String getId() {
        return ID_CONTACTS;
    }

    @Override
    public boolean checkArguments(DataExportArguments args, Session session) throws OXException {
        Module contactsModule = null;
        List<Module> modules = args.getModules();
        for (Iterator<Module> it = modules.iterator(); contactsModule == null && it.hasNext(); ) {
            Module module = it.next();
            if (ID_CONTACTS.equals(module.getId())) {
                contactsModule = module;
            }
        }

        if (contactsModule == null) {
            return false;
        }

        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.contacts.enabled", true, session)) {
            return false;
        }

        boolean enabled = getBoolOption(ContactsDataExportPropertyNames.PROP_ENABLED, true, contactsModule);
        if (!enabled) {
            return false;
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasContact()) {
            // User may only see his own user contact
            List<Module> newModules = new ArrayList<Module>(modules.size());
            for (Module module : modules) {
                if (ID_CONTACTS.equals(module.getId())) {
                    // Store new module with adapted properties
                    newModules.add(Module.valueOf(ID_CONTACTS, Collections.singletonMap(ContactsDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE)));
                } else {
                    newModules.add(module);
                }
            }
            args.setModules(newModules);
        }

        return true;
    }

    @Override
    public Optional<Module> getModule(Session session) throws OXException {
        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.contacts.enabled", true, session)) {
            return Optional.empty();
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasContact()) {
            // User may only see his own user contact
            return Optional.of(Module.valueOf(ID_CONTACTS, Collections.singletonMap(ContactsDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE)));
        }

        boolean hasSharedFolders;
        {
            UserConfiguration userConfiguration = userConfigService.getUserConfiguration(session);
            hasSharedFolders = userConfiguration.hasFullSharedFolderAccess();
        }

        Map<String, Object> properties = new LinkedHashMap<String, Object>(6);
        properties.put(ContactsDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE);
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.contacts." + ContactsDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, true, session)) {
            properties.put(ContactsDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, Boolean.FALSE);
        }
        if (hasSharedFolders && getBoolProperty("com.openexchange.gdpr.dataexport.provider.contacts." + ContactsDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, true, session)) {
            properties.put(ContactsDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, Boolean.FALSE);
        }

        return Optional.of(Module.valueOf(ID_CONTACTS, properties));
    }

    @Override
    public String getPathPrefix(Locale locale) throws OXException {
        return StringHelper.valueOf(locale).getString(ContactsDataExportStrings.PREFIX_CONTACTS);
    }

    @Override
    protected ContactsDataExport createTask(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException {
        return new ContactsDataExport(sink, savepoint, task, locale, services);
    }

}
