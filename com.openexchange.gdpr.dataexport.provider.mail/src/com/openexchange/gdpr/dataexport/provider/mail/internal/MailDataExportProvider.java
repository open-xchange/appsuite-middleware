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
        Optional<Map<String, Object>> optionalProperties = mailModule.getProperties();
        if (optionalProperties.isPresent()) {
            properties = new LinkedHashMap<String, Object>(optionalProperties.get());
        } else {
            properties = new LinkedHashMap<String, Object>(extendedProperties.size() + 2);
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
        properties.put(MailDataExportPropertyNames.PROP_INCLUDE_TRASH_FOLDER, Boolean.FALSE);
        if (hasPublicFolders) {
            properties.put(MailDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, Boolean.FALSE);
        }
        if (hasSharedFolders) {
            properties.put(MailDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, Boolean.FALSE);
        }
        if (hasSubscription) {
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
