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
 *    trademarks of the OX Software GmbH. group of companies.
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
