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

package com.openexchange.gdpr.dataexport.provider.infostore;

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
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link InfostoreDataExportProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class InfostoreDataExportProvider extends AbstractDataExportProvider<InfostoreDataExport> {

    private static final String ID_INFOSTORE = "infostore";

    /**
     * Initializes a new {@link InfostoreDataExportProvider}.
     *
     * @param services The service look-up
     */
    public InfostoreDataExportProvider(ServiceLookup services) {
        super(services);
    }

    @Override
    public String getId() {
        return ID_INFOSTORE;
    }

    @Override
    public boolean checkArguments(DataExportArguments args, Session session) throws OXException {
        Module infostoreModule = null;
        List<Module> modules = args.getModules();
        for (Iterator<Module> it = modules.iterator(); infostoreModule == null && it.hasNext(); ) {
            Module module = it.next();
            if (ID_INFOSTORE.equals(module.getId())) {
                infostoreModule = module;
            }
        }

        if (infostoreModule == null) {
            return false;
        }

        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.infostore.enabled", true, session)) {
            return false;
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasInfostore()) {
            return false;
        }

        return getBoolOption(InfostoreDataExportPropertyNames.PROP_ENABLED, true, infostoreModule);
    }

    @Override
    public Optional<Module> getModule(Session session) throws OXException {
        if (!getBoolProperty("com.openexchange.gdpr.dataexport.provider.infostore.enabled", true, session)) {
            return Optional.empty();
        }

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasInfostore()) {
            return Optional.empty();
        }

        Map<String, Object> properties = new LinkedHashMap<String, Object>(6);
        properties.put(InfostoreDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE);
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.calendar." + InfostoreDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, true, session)) {
            properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, Boolean.FALSE);
        }
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.calendar." + InfostoreDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, true, session)) {
            properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, Boolean.FALSE);
        }
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.calendar." + InfostoreDataExportPropertyNames.PROP_INCLUDE_TRASH, true, session)) {
            properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_TRASH, Boolean.FALSE);
        }
        if (getBoolProperty("com.openexchange.gdpr.dataexport.provider.calendar." + InfostoreDataExportPropertyNames.PROP_INCLUDE_ALL_VERSIONS, true, session)) {
            properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_ALL_VERSIONS, Boolean.FALSE);
        }

        return Optional.of(Module.valueOf(ID_INFOSTORE, properties));
    }

    @Override
    public String getPathPrefix(Locale locale) throws OXException {
        return StringHelper.valueOf(locale).getString(InfostoreDataExportStrings.PREFIX_DRIVE);
    }

    @Override
    protected InfostoreDataExport createTask(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException {
        return new InfostoreDataExport(sink, savepoint, task, locale, services);
    }

}
