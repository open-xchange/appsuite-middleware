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

package com.openexchange.groupware.datahandler.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.ResultConverterRegistry;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.datahandler.ICalChronosDataHandler;
import com.openexchange.groupware.datahandler.ICalInsertDataHandler;
import com.openexchange.groupware.datahandler.ICalJSONDataHandler;
import com.openexchange.mail.conversion.VCardAttachMailDataHandler;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link DataHandlerActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class DataHandlerActivator extends HousekeepingActivator {

    /**
     * Constant for string: "identifier"
     */
    private static final String STR_IDENTIFIER = "identifier";

    private static final Class<?>[] NEEDED_CLASSES = { ICalService.class, ICalParser.class, IDBasedCalendarAccessFactory.class, CalendarService.class, ResultConverterRegistry.class };

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        /*
         * Register data handlers
         */
        registerService(DataHandler.class, new ICalInsertDataHandler(this), generateDictionary("com.openexchange.ical"));
        registerService(DataHandler.class, new ICalChronosDataHandler(this), generateDictionary("com.openexchange.chronos.ical"));
        registerService(DataHandler.class, new ICalJSONDataHandler(this), generateDictionary("com.openexchange.ical.json"));
        registerService(DataHandler.class, new VCardAttachMailDataHandler(), generateDictionary("com.openexchange.mail.vcard"));
    }

    private Dictionary<String, Object> generateDictionary(String key) {
        final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
        props.put(STR_IDENTIFIER, key);
        return props;
    }

}
