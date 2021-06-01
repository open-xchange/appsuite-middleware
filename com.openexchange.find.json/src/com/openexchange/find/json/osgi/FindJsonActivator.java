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

package com.openexchange.find.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.find.SearchService;
import com.openexchange.find.json.FindActionFactory;
import com.openexchange.find.json.converters.AutocompleteResultJSONConverter;
import com.openexchange.find.json.converters.QueryResultJSONConverter;
import com.openexchange.i18n.I18nService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 *
 * {@link FindJsonActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FindJsonActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link FindJsonActivator}.
     */
    public FindJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SearchService.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        I18nTracker translator = new I18nTracker(context);
        track(I18nService.class, translator);

        final ResultConverterRegistry converterRegistry = new ResultConverterRegistry(context);
        track(ResultConverter.class, converterRegistry);

        openTrackers();

        registerService(ResultConverter.class, new AutocompleteResultJSONConverter(translator));
        registerService(ResultConverter.class, new QueryResultJSONConverter(translator, converterRegistry));
        registerModule(new FindActionFactory(this), "find");

        final String sCapability = "search";
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
        registerService(CapabilityChecker.class, new CapabilityChecker() {
            @Override
            public boolean isEnabled(String capability, Session ses) throws OXException {
                if (sCapability.equals(capability)) {
                    final ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous()) {
                        return false;
                    }

                    // TODO: Perform permission check here
                    return true;
                }

                return true;
            }
        }, properties);


        getService(CapabilityService.class).declareCapability(sCapability);
    }

}
