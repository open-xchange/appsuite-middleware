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

        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasInfostore()) {
            return false;
        }

        return getBoolOption(InfostoreDataExportPropertyNames.PROP_ENABLED, true, infostoreModule);
    }

    @Override
    public Optional<Module> getModule(Session session) throws OXException {
        UserConfigurationService userConfigService = services.getServiceSafe(UserConfigurationService.class);
        if (!userConfigService.getUserConfiguration(session).hasInfostore()) {
            return Optional.empty();
        }

        Map<String, Object> properties = new LinkedHashMap<String, Object>(6);
        properties.put(InfostoreDataExportPropertyNames.PROP_ENABLED, Boolean.TRUE);
        properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, Boolean.FALSE);
        properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, Boolean.FALSE);
        properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_TRASH, Boolean.FALSE);
        properties.put(InfostoreDataExportPropertyNames.PROP_INCLUDE_ALL_VERSIONS, Boolean.FALSE);

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
