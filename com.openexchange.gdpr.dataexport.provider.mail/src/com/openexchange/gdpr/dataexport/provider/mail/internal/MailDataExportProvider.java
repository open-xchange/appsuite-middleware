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

package com.openexchange.gdpr.dataexport.provider.mail.internal;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import java.util.ArrayList;
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
import com.openexchange.gdpr.dataexport.provider.mail.MailDataExportPropertyNames;
import com.openexchange.gdpr.dataexport.provider.mail.generator.SessionGenerator;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.service.MailService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link MailDataExportProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MailDataExportProvider extends AbstractDataExportProvider<MailDataExport> {

    private static final String ID_MAIL = "mail";

    private final SessionGeneratorRegistry generatorRegistry;

    /**
     * Initializes a new {@link MailDataExportProvider}.
     *
     * @param generatorRegistry The generator registry
     * @param services The service look-up
     */
    public MailDataExportProvider(SessionGeneratorRegistry generatorRegistry, ServiceLookup services) {
        super(services);
        this.generatorRegistry = generatorRegistry;
    }

    @Override
    public String getId() {
        return ID_MAIL;
    }

    @Override
    public boolean checkArguments(DataExportArguments args, Session session) throws OXException {
        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasWebMail()) {
            return false;
        }

        // Acquire requested modules
        List<Module> modules = args.getModules();

        // Check if "mail" module is present
        Module mailModule = null;
        for (Iterator<Module> it = modules.iterator(); mailModule == null && it.hasNext(); ) {
            Module module = it.next();
            if (ID_MAIL.equals(module.getId())) {
                mailModule = module;
            }
        }

        if (mailModule == null) {
            return false;
        }

        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.mail.enabled", true, session)) {
            return false;
        }

        // Check if enabled
        boolean enabled = getBoolOption(MailDataExportPropertyNames.PROP_ENABLED, true, mailModule);
        if (!enabled) {
            return false;
        }

        // Determine appropriate session generator and generate its extended properties
        SessionGenerator generator = generatorRegistry.getGenerator(session);
        Map<String, Object> extendedProperties = generator.craftExtendedProperties(session);

        // Store extended properties (if any) to access mail system later on
        Map<String, Object> properties;
        {
            Optional<Map<String, Object>> optionalProperties = mailModule.getProperties();
            if (optionalProperties.isPresent()) {
                properties = new LinkedHashMap<String, Object>(optionalProperties.get());
            } else {
                properties = new LinkedHashMap<String, Object>(extendedProperties.size() + 2);
            }
        }
        properties.put(MailDataExportPropertyNames.PROP_SESSION_GENERATOR, generator.getId());
        if (!extendedProperties.isEmpty()) {
            properties.putAll(extendedProperties);
        }

        // Craft new module listing
        List<Module> newModules = new ArrayList<Module>(modules.size());
        for (Module module : modules) {
            if (ID_MAIL.equals(module.getId())) {
                // Store new module with adapted properties
                newModules.add(Module.valueOf(ID_MAIL, properties));
            } else {
                newModules.add(module);
            }
        }
        args.setModules(newModules);
        return true;
    }

    @Override
    public Optional<Module> getModule(Session session) throws OXException {
        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.mail.enabled", true, session)) {
            return Optional.empty();
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasWebMail()) {
            return Optional.empty();
        }

        MailService mailService = services.getServiceSafe(MailService.class);

        boolean hasSharedFolders;
        boolean hasPublicFolders;
        boolean hasSubscription;
        {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = mailService.getMailAccess(session, 0);
                mailAccess.connect();

                MailCapabilities capabilities = mailAccess.getMailConfig().getCapabilities();
                hasSharedFolders = capabilities.hasSharedFolders();
                hasPublicFolders = capabilities.hasPublicFolders();
                hasSubscription = capabilities.hasSubscription();

            } finally {
                MailAccess.closeInstance(mailAccess);
            }
        }

        Map<String, Object> properties = new LinkedHashMap<String, Object>(6);
        properties.put(MailDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE);
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.mail." + MailDataExportPropertyNames.PROP_INCLUDE_TRASH_FOLDER, true, session)) {
            properties.put(MailDataExportPropertyNames.PROP_INCLUDE_TRASH_FOLDER, Boolean.FALSE);
        }
        if (hasPublicFolders && getBoolProperty("com.openexchange.gdpr.dataexport.provider.mail." + MailDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, true, session)) {
            properties.put(MailDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, Boolean.FALSE);
        }
        if (hasSharedFolders && getBoolProperty("com.openexchange.gdpr.dataexport.provider.mail." + MailDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, true, session)) {
            properties.put(MailDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, Boolean.FALSE);
        }
        if (hasSubscription && getBoolProperty("com.openexchange.gdpr.dataexport.provider.mail." + MailDataExportPropertyNames.PROP_INCLUDE_UNSUBSCRIBED, true, session)) {
            properties.put(MailDataExportPropertyNames.PROP_INCLUDE_UNSUBSCRIBED, Boolean.FALSE);
        }

        return Optional.of(Module.valueOf(ID_MAIL, properties));
    }

    @Override
    public String getPathPrefix(Locale locale) throws OXException {
        return StringHelper.valueOf(locale).getString(MailDataExportStrings.PREFIX_MAILS);
    }

    @Override
    protected MailDataExport createTask(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException {
        return new MailDataExport(sink, savepoint, task, locale, generatorRegistry, services);
    }

}
